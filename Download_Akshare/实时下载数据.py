import time
from datetime import date, datetime, time as dt_time
import pandas as pd
import sys  # 导入sys模块，用于退出程序
from sqlalchemy import create_engine

# 注意：保持你的本地模块导入路径不变
from 期权数据.期权实时行情_东方财富 import option_real_time
from 期货数据.内盘_实时行情数据 import future_real_time

def real_time():
    """需要被每隔10分钟整点调用的目标函数"""
    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"函数real_time被精准调用，当前时间：{current_time}")
    option_real_time()
    future_real_time()

def is_within_futures_hours(current_dt: datetime) -> bool:
    """
    判断当前时间是否在国内期货交易时间范围内
    :param current_dt: 当前datetime对象
    :return: 符合交易时间返回True，否则返回False
    """
    current_time = current_dt.time()  # 提取当前时间（时分秒），忽略日期

    # 定义三个国内期货交易时间段（北京时间）
    morning_start, morning_end = dt_time(9, 0), dt_time(11, 30)  # 早9:00-11:30
    afternoon_start, afternoon_end = dt_time(13, 30), dt_time(15, 15)  # 下午13:30-15:00
    evening_start, evening_end = dt_time(21, 0), dt_time(23, 0)  # 晚21:00-23:00

    # 判断当前时间是否在任一有效时间段内
    return (morning_start <= current_time <= morning_end) or \
        (afternoon_start <= current_time <= afternoon_end) or \
        (evening_start <= current_time <= evening_end)

def calculate_sleep_to_next_10min(current_dt: datetime) -> int:
    """
    精准计算距离下一个10分钟整点（0分、10分、20分...）需要睡眠的秒数
    :param current_dt: 当前datetime对象
    :return: 需睡眠的秒数（非负整数）
    """
    current_minute = current_dt.minute
    current_second = current_dt.second
    current_microsecond = current_dt.microsecond  # 提升精度，忽略微秒级误差

    # 计算当前分钟距离最近的10分钟整点的差值（向上取整）
    minutes_to_next_10min = (10 - (current_minute % 10)) % 10

    # 转换为秒数，扣除当前已过的秒数和微秒数
    seconds_to_next_10min = minutes_to_next_10min * 60 - current_second
    # 忽略微秒级误差（若需极致精准可保留，此处简化）
    seconds_to_next_10min -= round(current_microsecond / 1000000)

    # 确保返回值非负（避免微秒级计算导致的负数）
    return max(seconds_to_next_10min, 0)

def is_trading_day(engine, check_date: date) -> bool:
    """
    封装交易日判断逻辑，使用参数化查询避免SQL注入，支持任意日期查询
    :param engine: 数据库连接引擎
    :param check_date: 需要判断的日期
    :return: 是交易日返回True，否则返回False
    """
    trade_date = check_date.strftime("%Y%m%d")
    # 关键修改：MySQL/pymysql适配的占位符是%s，而非?
    sql = "SELECT * FROM calendar where 交易日=%s;"
    try:
        # params保持元组形式传递，与%s占位符对应
        df = pd.read_sql(sql, engine, params=(trade_date,))
        return not df.empty
    except Exception as e:
        print(f"查询交易日失败：{str(e)}")
        sys.exit(1)

def main():
    """主循环：精准对齐10分钟整点，遵循期货交易时间+交易日判断"""
    print("程序启动，开始监控期货交易时间并精准调用期权实时行情...")
    print(f"当前系统时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # 配置项（集中管理，便于后续修改）
    DB_CONFIG = 'mysql+pymysql://root:12345678@localhost:3306/akshare'
    NON_TRADING_WAIT = 60  # 非交易时间每次等待1分钟
    engine = create_engine(DB_CONFIG)

    # 程序启动时先判断当日是否为交易日
    current_date = date.today()
    if not is_trading_day(engine, current_date):
        print("今日非交易日，程序终止。")
        sys.exit(0)  # 正常终止程序，返回0状态码

    while True:
        now = datetime.now()
        current_date_now = now.date()

        # 跨天判断：若当前日期与启动日期不一致，重新判断交易日
        if current_date_now != current_date:
            current_date = current_date_now
            if not is_trading_day(engine, current_date):
                print(f"次日{current_date}非交易日，程序终止。")
                sys.exit(0)

        # 第一步：判断当前是否在期货交易时间内
        if is_within_futures_hours(now):
            # 第二步：计算距离下一个10分钟整点的睡眠时长（每次都校准，无累积偏差）
            sleep_seconds = calculate_sleep_to_next_10min(now)

            # 第三步：睡眠至下一个10分钟整点
            if sleep_seconds > 0:
                time.sleep(sleep_seconds)

            # 第四步：到达整点后，再次判断是否仍在交易时间内（避免跨时间段漏判）
            after_sleep_now = datetime.now()
            if is_within_futures_hours(after_sleep_now):
                # 执行目标函数，无后续固定睡眠（避免叠加）
                real_time()
            else:
                # 睡眠后已超出交易时间，进入下一轮循环
                continue
        else:
            # 非交易时间：短暂睡眠后重新判断，避免CPU空转
            time.sleep(NON_TRADING_WAIT)

if __name__ == "__main__":
    main()