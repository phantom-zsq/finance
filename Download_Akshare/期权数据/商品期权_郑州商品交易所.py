import pandas as pd
import akshare as ak
from sqlalchemy import create_engine

def core(trade_date: str) -> None:
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # 品种
    product = ["白糖期权", "棉花期权", "甲醇期权", "PTA期权", "动力煤期权", "菜籽粕期权", "菜籽油期权", "花生期权", "对二甲苯期权", "烧碱期权", "纯碱期权", "短纤期权", "锰硅期权", "硅铁期权", "尿素期权", "苹果期权", "红枣期权", "玻璃期权", "瓶片期权", "丙烯期权"]
    # 遍历
    for row in product:
        first_field = row
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            option_hist_czce_df = ak.option_hist_czce(symbol=f"{first_field}", trade_date=f"{trade_date}")
            option_hist_czce_df['交易日'] = trade_date
            # write to mysql
            res = option_hist_czce_df.to_sql('option_hist_czce', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")