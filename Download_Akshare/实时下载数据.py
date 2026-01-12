import time
import logging
from datetime import date, datetime, time as dt_time
import pandas as pd
import sys
from sqlalchemy import create_engine
from contextlib import contextmanager

# ===================== 全局配置常量（核心修改）=====================
# 执行间隔：20分钟（单位：分钟），作为全局变量统一管理
EXECUTE_INTERVAL_MINUTES = 20  # 只需修改这里即可调整执行间隔
# =================================================================

# 配置日志：输出到控制台+文件，便于排查问题
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),  # 控制台输出
        logging.FileHandler("option_future_real_time.log", encoding="utf-8")  # 文件输出
    ]
)
logger = logging.getLogger(__name__)

# 本地模块导入（保持你的路径不变）
try:
    from 期权数据.期权实时行情_东方财富 import option_real_time
    from 期货数据.内盘_实时行情数据 import future_real_time
except ImportError as e:
    logger.error(f"导入本地模块失败：{e}，请检查模块路径是否正确")
    sys.exit(1)

def real_time():
    """需要被每隔{EXECUTE_INTERVAL_MINUTES}分钟整点调用的目标函数"""
    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    logger.info(f"函数real_time被精准调用（间隔{EXECUTE_INTERVAL_MINUTES}分钟），当前时间：{current_time}")
    try:
        # 捕获行情接口执行异常，避免程序崩溃
        option_real_time()
        future_real_time()
        logger.info("期权、期货实时行情接口调用成功")
    except Exception as e:
        logger.error(f"行情接口调用失败：{e}", exc_info=True)  # 打印异常堆栈

def is_within_futures_hours(current_dt: datetime) -> bool:
    """
    判断当前时间是否在国内期货交易时间范围内
    交易时段（北京时间）：
    - 日盘上午：9:00 - 11:30
    - 日盘下午：13:30 - 15:00
    - 夜盘：21:00 - 23:00（部分品种，此处按通用时段）
    :param current_dt: 当前datetime对象
    :return: 符合交易时间返回True，否则返回False
    """
    current_time = current_dt.time()

    # 精准定义交易时段
    morning_start, morning_end = dt_time(9, 0), dt_time(11, 35)
    afternoon_start, afternoon_end = dt_time(13, 30), dt_time(15, 5)
    evening_start, evening_end = dt_time(21, 0), dt_time(23, 5)

    is_morning = morning_start <= current_time <= morning_end
    is_afternoon = afternoon_start <= current_time <= afternoon_end
    is_evening = evening_start <= current_time <= evening_end

    return is_morning or is_afternoon or is_evening

def calculate_sleep_to_next_interval(current_dt: datetime) -> int:
    """
    精准计算距离下一个{EXECUTE_INTERVAL_MINUTES}分钟整点需要睡眠的秒数
    （适配全局变量EXECUTE_INTERVAL_MINUTES，支持任意整分钟间隔）
    :param current_dt: 当前datetime对象
    :return: 需睡眠的秒数（非负整数）
    """
    current_minute = current_dt.minute
    current_second = current_dt.second

    # 基于全局间隔变量，计算下一个目标分钟数（如20分钟间隔：0/20/40分）
    next_interval_minute = ((current_minute // EXECUTE_INTERVAL_MINUTES) + 1) * EXECUTE_INTERVAL_MINUTES % 60

    # 计算时间差（秒）：避免跨整点负数问题
    delta_minute = next_interval_minute - current_minute
    if delta_minute < 0:
        delta_minute += 60  # 跨整点（如50分→0分，20分钟间隔：delta_minute=10）

    sleep_seconds = delta_minute * 60 - current_second
    # 确保非负
    sleep_seconds = max(sleep_seconds, 0)

    logger.debug(f"当前时间：{current_dt.strftime('%H:%M:%S')}，需睡眠{sleep_seconds}秒到下一个{EXECUTE_INTERVAL_MINUTES}分钟整点")
    return sleep_seconds

@contextmanager
def get_db_engine(db_config: str):
    """
    数据库引擎上下文管理器：自动处理连接创建/关闭，避免连接泄露
    :param db_config: 数据库连接字符串
    """
    engine = None
    try:
        engine = create_engine(db_config)
        yield engine
    except Exception as e:
        logger.error(f"创建数据库引擎失败：{e}")
        sys.exit(1)
    finally:
        if engine:
            engine.dispose()  # 关闭引擎，释放连接

def is_trading_day(engine, check_date: date) -> bool:
    """
    封装交易日判断逻辑，使用参数化查询避免SQL注入
    :param engine: 数据库连接引擎
    :param check_date: 需要判断的日期
    :return: 是交易日返回True，否则返回False
    """
    trade_date = check_date.strftime("%Y%m%d")
    sql = "SELECT 1 FROM calendar where 交易日=%s LIMIT 1;"  # 只查1条，提升效率

    try:
        df = pd.read_sql(sql, engine, params=(trade_date,))
        is_trade_day = not df.empty
        logger.info(f"日期{check_date}是否为交易日：{is_trade_day}")
        return is_trade_day
    except Exception as e:
        logger.error(f"查询交易日失败：{e}", exc_info=True)
        sys.exit(1)

def main():
    """主循环：精准对齐{EXECUTE_INTERVAL_MINUTES}分钟整点，遵循期货交易时间+交易日判断"""
    logger.info(f"程序启动，开始监控期货交易时间（{EXECUTE_INTERVAL_MINUTES}分钟间隔）并精准调用期权实时行情...")
    logger.info(f"当前系统时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info("=" * 60)

    # 配置项（集中管理）
    DB_CONFIG = 'mysql+pymysql://root:12345678@localhost:3306/akshare'
    NON_TRADING_WAIT = 60  # 非交易时间每次等待1分钟

    # 初始化数据库引擎
    with get_db_engine(DB_CONFIG) as engine:
        current_date = date.today()
        # 先判断当日是否为交易日
        if not is_trading_day(engine, current_date):
            logger.info(f"今日{current_date}非交易日，程序终止。")
            sys.exit(0)

        # 主循环
        while True:
            now = datetime.now()
            current_date_now = now.date()

            # 跨天判断：重新检查交易日
            if current_date_now != current_date:
                current_date = current_date_now
                if not is_trading_day(engine, current_date):
                    logger.info(f"次日{current_date}非交易日，程序终止。")
                    sys.exit(0)

            # 判断是否在交易时间内
            if is_within_futures_hours(now):
                # 计算到下一个{EXECUTE_INTERVAL_MINUTES}分钟整点的睡眠时长（核心适配）
                sleep_seconds = calculate_sleep_to_next_interval(now)
                if sleep_seconds > 0:
                    time.sleep(sleep_seconds)

                # 睡眠后再次验证是否仍在交易时间内（避免跨时段）
                after_sleep_now = datetime.now()
                if is_within_futures_hours(after_sleep_now):
                    real_time()
                else:
                    logger.info(f"睡眠后时间{after_sleep_now.strftime('%H:%M:%S')}已超出交易时间，跳过本次调用")
            else:
                # 非交易时间：短暂休眠，减少CPU占用
                logger.debug(f"当前时间{now.strftime('%H:%M:%S')}非期货交易时间，休眠{NON_TRADING_WAIT}秒")
                time.sleep(NON_TRADING_WAIT)

if __name__ == "__main__":
    # 打印全局配置，便于确认运行参数
    logger.info(f"当前配置：执行间隔 = {EXECUTE_INTERVAL_MINUTES}分钟")
    try:
        real_time()
        time.sleep(5 * 60)
        main()
    except KeyboardInterrupt:
        logger.info("用户手动终止程序，程序退出")
        sys.exit(0)
    except Exception as e:
        logger.error(f"程序运行异常终止：{e}", exc_info=True)
        sys.exit(1)