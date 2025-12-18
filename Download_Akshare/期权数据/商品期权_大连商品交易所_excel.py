import pandas as pd
from sqlalchemy import create_engine

def core(trade_date: str, CSV_FILE_PATH: str) -> None:
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')

    try:
        # 表名
        option_hist_dce_df = pd.read_csv(
            CSV_FILE_PATH,
            encoding="utf-8",
            skiprows=1,          # 跳过第一行无效数据
            header=0,            # 用CSV第二行作为列名
            dtype=str,           # 统一字符串读取，避免类型错误
            na_filter=False      # 空值不处理
        )

        for col in option_hist_dce_df.columns:
            option_hist_dce_df[col] = option_hist_dce_df[col].replace(r"^-$", 0, regex=True)

        comma_cols = ['开盘价',
                      '最高价',
                      '最低价',
                      '收盘价',
                      '前结算价',
                      '结算价',
                      '涨跌',
                      '涨跌1',
                      'Delta',
                      '隐含波动率(%)',
                      '成交量',
                      '持仓量',
                      '持仓量变化',
                      '成交额',
                      '行权量'
                      ]
        for col in comma_cols:
            option_hist_dce_df[col] = (
                option_hist_dce_df[col]
                .astype(str)
                .str.replace(',', '')
                .pipe(pd.to_numeric, errors='coerce')
            )

        option_hist_dce_df = option_hist_dce_df[~option_hist_dce_df["品种名称"].str.contains(r"小计|总计", regex=True, na=False)]

        option_hist_dce_df['交易日'] = trade_date
        # write to mysql
        res = option_hist_dce_df.to_sql('option_hist_dce', engine, index=False, if_exists='append', chunksize=10000)
    except Exception as e:  # 捕获所有继承自Exception的异常
        print(f"发生错误: {str(e)}")