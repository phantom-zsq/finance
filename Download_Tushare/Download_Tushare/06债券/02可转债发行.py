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

#获取可转债发行数据，自定义字段
df = pro.cb_issue(fields='ts_code,ann_date,res_ann_date,plan_issue_size,issue_size,issue_price,issue_type,issue_cost,onl_code,onl_name,onl_date,onl_size,onl_pch_vol,onl_pch_num,onl_pch_excess,onl_winning_rate,shd_ration_code,shd_ration_name,shd_ration_date,shd_ration_record_date,shd_ration_pay_date,shd_ration_price,shd_ration_ratio,shd_ration_size,shd_ration_vol,shd_ration_num,shd_ration_excess,offl_size,offl_deposit,offl_pch_vol,offl_pch_num,offl_pch_excess,offl_winning_rate,lead_underwriter,lead_underwriter_vol')

# -------------------common end-------------------
# load to mysql
res = df.to_sql('cb_issue', engine, index=False, if_exists='append', chunksize=10000)
print(res)
