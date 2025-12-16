import warnings
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
    trade_date = "20251215"
    print(trade_date)
    # 品种
    product = ["工业硅", "碳酸锂", "多晶硅", "铂", "钯"]
    # 遍历
    for row in product:
        first_field = row
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            option_hist_gfex_df = ak.option_hist_gfex(symbol=f"{first_field}", trade_date=f"{trade_date}")
            option_hist_gfex_df['交易日'] = trade_date
            df_filtered = option_hist_gfex_df[~option_hist_gfex_df["商品名称"].str.contains('小计', na=False)]
            # write to mysql
            res = df_filtered.to_sql('option_hist_gfex', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")