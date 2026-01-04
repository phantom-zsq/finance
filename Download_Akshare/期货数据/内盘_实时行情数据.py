import pandas as pd
import akshare as ak
from sqlalchemy import create_engine
from datetime import datetime

def future_real_time() -> None:
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')

    # today
    current_time = datetime.now().strftime("%Y%m%d%H%M%S")
    print(f"函数real_time被精准调用，当前时间：{current_time}")

    try:
        futures_symbol_mark_df = ak.futures_symbol_mark()
        for item in futures_symbol_mark_df['symbol']:
            print(item)
            futures_zh_realtime_df = ak.futures_zh_realtime(symbol=item)
            futures_zh_realtime_df['交易日'] = current_time
            # write to mysql
            res = futures_zh_realtime_df.to_sql('futures_zh_realtime', engine, index=False, if_exists='append', chunksize=10000)
    except Exception as e:  # 捕获所有继承自Exception的异常
        print(f"发生错误: {str(e)}")
