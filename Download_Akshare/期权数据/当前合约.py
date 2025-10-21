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
    # options
    options = ["碳酸锂期权", "工业硅期权", "黄金期权", "白银期权", "白糖期权", "苯乙烯期权", "菜籽粕期权", "菜籽油期权", "丁二烯橡胶期权", "动力煤期权", "豆粕期权", "豆油期权", "二甲苯期权", "沪铝期权", "沪铜期权", "花生期权", "黄大豆1号期权", "黄大豆2号期权", "甲醇期权", "螺纹钢期权", "棉花期权", "烧碱期权", "铁矿石期权", "橡胶期权", "液化石油气期权", "乙二醇期权", "玉米期权", "PTA期权"]
    # query data
    for option in options:
        print(option)
        option_commodity_contract_sina_df = ak.option_commodity_contract_sina(symbol=f"{option}")
        # 遍历每行元组，第一个元素（索引0）是第一个字段
        for row in option_commodity_contract_sina_df.itertuples(index=False):  # index=False 不包含索引
            first_field = row[1]  # 元组的第二个元素
            try:
                # 打印基本信息
                print(first_field)
                # 可能出错的代码
                option_commodity_contract_table_sina_df = ak.option_commodity_contract_table_sina(symbol=f"{option}", contract=f"{first_field}")
                option_commodity_contract_table_sina_df['品种名称'] = option
                option_commodity_contract_table_sina_df['合约名称'] = first_field
                option_commodity_contract_table_sina_df['交易日'] = trade_date
                # 间隔 2 秒
                time.sleep(2)
                # write to mysql
                res = option_commodity_contract_table_sina_df.to_sql('option_commodity_contract_table_sina', engine, index=False, if_exists='append', chunksize=10000)
            except Exception as e:  # 捕获所有继承自Exception的异常
                print(f"发生错误: {str(e)}")