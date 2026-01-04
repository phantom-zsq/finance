import time
from datetime import date
import pandas as pd
import sys  # 导入sys模块，用于退出程序
from sqlalchemy import create_engine
from datetime import datetime, time as dt_time
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
    afternoon_start, afternoon_end = dt_time(13, 30), dt_time(15, 0)  # 下午13:30-15:00
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

def main():
    """主循环：精准对齐10分钟整点，遵循期货交易时间"""
    interval_10min = 10 * 60  # 10分钟对应的秒数（600秒）
    print("程序启动，开始监控期货交易时间并精准调用期权实时行情...")
    print(f"当前系统时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    """只有交易日才执行"""
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # today
    current_date = date.today()
    trade_date = current_date.strftime("%Y%m%d")
    # 直接执行SELECT语句并读取为DataFrame
    sql = f"SELECT * FROM calendar where 交易日='{trade_date}';"
    df = pd.read_sql(sql, engine)
    if df.empty:
        print("今日非交易日，程序终止。")
        sys.exit(0)  # 正常终止程序，返回0状态码

    while True:
        now = datetime.now()

        # 第一步：判断当前是否在期货交易时间内
        if is_within_futures_hours(now):
            # 第二步：计算距离下一个10分钟整点的睡眠时长
            sleep_seconds = calculate_sleep_to_next_10min(now)

            # 第三步：睡眠至下一个10分钟整点
            if sleep_seconds > 0:
                time.sleep(sleep_seconds)

            # 第四步：到达整点后，判断是否仍在交易时间内（避免跨时间段漏判）
            after_sleep_now = datetime.now()
            if is_within_futures_hours(after_sleep_now):
                real_time()

                # 第五步：后续保持严格10分钟间隔，无需重复计算整点
                time.sleep(interval_10min)
            else:
                # 睡眠后已超出交易时间，直接进入非交易时间等待
                continue
        else:
            # 非交易时间：短暂睡眠（1分钟）后重新判断，避免CPU空转
            non_trading_wait = 60  # 可调整为5分钟（300秒）减少判断频次
            time.sleep(non_trading_wait)

if __name__ == "__main__":
    main()