# coding=utf-8

import tushare as ts
import pandas as pd
import time
from sqlalchemy import create_engine

# create mysql engine
engine = create_engine('mysql+pymysql://root:123456@localhost:3306/tushare')
# set Download_Tushare token
ts.set_token('594b808743cb001d120038c81f3ec360ed7f6c81b45834d04a84a130')
# get Download_Tushare api
pro = ts.pro_api()

# -------------------common start-------------------

basic_df = pd.read_sql_query('select ts_code from manual_cb_issue order by ts_code desc', engine)
for index, row in basic_df.iterrows():
    print(index, row[0])
    time.sleep(15)
    try:
        # 获取可转债转股价变动
        df = pro.cb_price_chg(ts_code=row[0])
    except Exception:
        print('exception: ' + row[0])
        continue
    res = df.to_sql('cb_price_chg', engine, index=False, if_exists='append', chunksize=10000)
    print(res)
