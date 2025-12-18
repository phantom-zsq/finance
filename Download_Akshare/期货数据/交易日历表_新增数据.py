import pandas as pd
from sqlalchemy import create_engine

if __name__ == '__main__':
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')

    # 定义二维表的列（字段）
    columns = ["交易日"]

    # 生成10行数据（10条记录）
    data = [
        ["20260105"],
        ["20260106"],
        ["20260107"],
        ["20260108"],
        ["20260109"],
        ["20260112"],
        ["20260113"],
        ["20260114"],
        ["20260115"],
        ["20260116"],
        ["20260119"],
        ["20260120"],
        ["20260121"],
        ["20260122"],
        ["20260123"],
        ["20260126"],
        ["20260127"],
        ["20260128"],
        ["20260129"],
        ["20260130"]
    ]

    # 创建纯粹的二维表DataFrame（仅包含行列结构，无额外业务字段）
    df = pd.DataFrame(data, columns=columns)
    res = df.to_sql('calendar', engine, index=False, if_exists='append', chunksize=10000)