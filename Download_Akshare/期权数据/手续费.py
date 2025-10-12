import pandas as pd
import akshare as ak
from sqlalchemy import create_engine
import time

if __name__ == '__main__':
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # query data
    option_comm_symbol_df = ak.option_comm_symbol()
    # 遍历每行元组，第一个元素（索引0）是第一个字段
    for row in option_comm_symbol_df.itertuples(index=False):  # index=False 不包含索引
        first_field = row[0]  # 元组的第一个元素
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            option_comm_info_df = ak.option_comm_info(symbol=f"{first_field}")
            # write to mysql
            res = option_comm_info_df.to_sql('option_comm_info', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")