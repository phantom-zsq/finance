import pandas as pd
import spot_price_qh_changed as ak
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
    spot_price_table_qh_df = ak.spot_price_table_qh()
    # 遍历每行元组，第二个元素（索引1）是第二个字段
    for row in spot_price_table_qh_df.itertuples(index=False):  # index=False 不包含索引
        second_field = row[1]  # 元组的第二个元素
        try:
            # 打印基本信息
            print(second_field)
            # 可能出错的代码
            spot_price_qh_df = ak.spot_price_qh(symbol=f"{second_field}")
            # 间隔 2 秒
            time.sleep(2)
            # write to mysql
            res = spot_price_qh_df.to_sql('spot_price_qh', engine, index=False, if_exists='replace', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")