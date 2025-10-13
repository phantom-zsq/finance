import akshare as ak
import pandas as pd
from sqlalchemy import create_engine

if __name__ == '__main__':
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # today
    futures_fees_info_df = ak.futures_fees_info()
    res = futures_fees_info_df.to_sql('futures_fees_info', engine, index=False, if_exists='append', chunksize=10000)