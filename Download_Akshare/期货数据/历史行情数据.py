import pandas as pd
import akshare as ak
from sqlalchemy import create_engine
import time

def core(trade_date: str) -> None:
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # 品种
    #product = ["CFFEX", "INE", "CZCE", "DCE", "SHFE", "GFEX"]
    product = ["CFFEX", "INE", "CZCE", "SHFE", "GFEX"]
    # 遍历
    for row in product:
        first_field = row
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            get_futures_daily_df = ak.get_futures_daily(start_date=f"{trade_date}", end_date=f"{trade_date}", market=f"{first_field}")
            if 'index' in get_futures_daily_df.columns:
                get_futures_daily_df = get_futures_daily_df.drop(columns='index')  # 只有存在时才删除
            # 间隔 2 秒
            time.sleep(2)
            # write to mysql
            res = get_futures_daily_df.to_sql('get_futures_daily', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")