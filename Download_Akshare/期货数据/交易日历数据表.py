import pandas as pd
import akshare as ak
from sqlalchemy import create_engine
import time
from datetime import date

if __name__ == '__main__':
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # today
    current_date = date.today()
    trade_date = current_date.strftime("%Y%m%d")
    print(trade_date)
    # query data
    try:
        # 可能出错的代码
        futures_rule_df = ak.futures_rule(date=f"{trade_date}")
        futures_rule_df['交易日'] = trade_date
        # 间隔 2 秒
        time.sleep(2)
        # write to mysql
        res = futures_rule_df.to_sql('futures_rule', engine, index=False, if_exists='append', chunksize=10000)
    except Exception as e:  # 捕获所有继承自Exception的异常
        print(f"发生错误: {str(e)}")