import pandas as pd
import akshare as ak
from datetime import date
from sqlalchemy import create_engine

def core() -> None:
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

    try:
        # 可能出错的代码
        option_current_em_df = ak.option_current_em()
        option_current_em_df['交易日'] = trade_date
        # write to mysql
        res = option_current_em_df.to_sql('option_current_em', engine, index=False, if_exists='append', chunksize=10000)
    except Exception as e:  # 捕获所有继承自Exception的异常
        print(f"发生错误: {str(e)}")
