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

    try:
        # CSV文件路径
        CSV_FILE_PATH = "/Users/admin/Downloads/日行情_1765864835732.csv"
        # 表名
        option_hist_dce_df = pd.read_csv(
            CSV_FILE_PATH,
            encoding="utf-8",
            skiprows=1,          # 跳过第一行无效数据
            header=0,            # 用CSV第二行作为列名
            dtype=str,           # 统一字符串读取，避免类型错误
            na_filter=False      # 空值不处理
        )

        option_hist_dce_df = option_hist_dce_df[~option_hist_dce_df["品种名称"].str.contains(r"小计|总计|月均价", regex=True, na=False)]

        for col in option_hist_dce_df.columns:
            option_hist_dce_df[col] = option_hist_dce_df[col].replace(r"^-$", 0, regex=True)

        DCE_MAP = {
            "大豆": "A",
            "豆一": "A",
            "豆二": "B",
            "豆粕": "M",
            "豆油": "Y",
            "棕榈油": "P",
            "玉米": "C",
            "玉米淀粉": "CS",
            "鸡蛋": "JD",
            "纤维板": "FB",
            "胶合板": "BB",
            "聚乙烯": "L",
            "聚氯乙烯": "V",
            "聚丙烯": "PP",
            "焦炭": "J",
            "焦煤": "JM",
            "铁矿石": "I",
            "乙二醇": "EG",
            "粳米": "RR",
            "苯乙烯": "EB",
            "液化石油气": "PG",
            "生猪": "LH",
            "原木": "LG",
            "纯苯": "BZ"
        }
        option_hist_dce_df["variety"] = option_hist_dce_df["品种名称"].map(lambda x: DCE_MAP[x])
        option_hist_dce_df["symbol"] = option_hist_dce_df["合约"]
        del option_hist_dce_df["品种名称"]
        del option_hist_dce_df["合约"]
        option_hist_dce_df.columns = [
            "open",
            "high",
            "low",
            "close",
            "pre_settle",
            "settle",
            "_",
            "_",
            "volume",
            "open_interest",
            "_",
            "turnover",
            "variety",
            "symbol",
        ]

        option_hist_dce_df["date"] = trade_date
        option_hist_dce_df = option_hist_dce_df[
            [
                "symbol",
                "date",
                "open",
                "high",
                "low",
                "close",
                "volume",
                "open_interest",
                "turnover",
                "settle",
                "pre_settle",
                "variety",
            ]
        ]

        comma_cols = ['open',
                      'high',
                      'low',
                      'close',
                      'volume',
                      'open_interest',
                      'turnover',
                      'settle',
                      'pre_settle'
                      ]

        for col in comma_cols:
            option_hist_dce_df[col] = (
                option_hist_dce_df[col]
                .astype(str)
                .str.replace(',', '')
                .pipe(pd.to_numeric, errors='coerce')
            )

        option_hist_dce_df = option_hist_dce_df.astype(
            {
                "open": "float",
                "high": "float",
                "low": "float",
                "close": "float",
                "volume": "float",
                "open_interest": "float",
                "turnover": "float",
                "settle": "float",
                "pre_settle": "float",
            }
        )

        # write to mysql
        res = option_hist_dce_df.to_sql('get_futures_daily', engine, index=False, if_exists='append', chunksize=10000)
    except Exception as e:  # 捕获所有继承自Exception的异常
        print(f"发生错误: {str(e)}")