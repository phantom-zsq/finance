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
    trade_date = "20251212"
    print(trade_date)
    # 品种
    product = ["玉米期权", "豆粕期权", "铁矿石期权", "液化石油气期权", "聚乙烯期权", "聚氯乙烯期权", "聚丙烯期权", "棕榈油期权", "黄大豆1号期权", "黄大豆2号期权", "豆油期权", "乙二醇期权", "苯乙烯期权", "鸡蛋期权", "玉米淀粉期权", "生猪期权", "原木期权", "纯苯期权"]
    # 遍历
    for row in product:
        first_field = row
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            option_hist_dce_df = ak.option_hist_dce(symbol=f"{first_field}", trade_date=f"{trade_date}")
            option_hist_dce_df['交易日'] = trade_date
            print(option_hist_dce_df)
            # write to mysql
            res = option_hist_dce_df.to_sql('option_hist_dce', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")