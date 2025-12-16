import warnings
import pandas as pd

# 抑制 pandas 的 SettingWithCopyWarning 警告
warnings.filterwarnings('ignore', category=pd.errors.SettingWithCopyWarning)

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
    product = ["原油期权", "铜期权", "铝期权", "锌期权", "铅期权", "螺纹钢期权", "镍期权", "锡期权", "氧化铝期权", "黄金期权", "白银期权", "丁二烯橡胶期权", "天胶期权", "石油沥青期权", "铸造铝合金期权", "燃料油期权", "胶版印刷纸期权", "纸浆期权"]
    # 遍历
    for row in product:
        first_field = row
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            option_hist_shfe_df = ak.option_hist_shfe(symbol=f"{first_field}", trade_date=f"{trade_date}")
            option_hist_shfe_df['交易日'] = trade_date
            # write to mysql
            res = option_hist_shfe_df.to_sql('option_hist_shfe', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")