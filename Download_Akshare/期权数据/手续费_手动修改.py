#!/usr/bin/env python
# -*- coding:utf-8 -*-
"""
Date: 2024/7/5 15:00
Desc: 九期网-商品期权手续费
https://www.9qihuo.com/qiquanshouxufei
"""

from functools import lru_cache
from io import StringIO

import pandas as pd
import requests
from bs4 import BeautifulSoup

from sqlalchemy import create_engine
import time

def core(trade_date: str, cookie: str) -> None:
    # set option of pandas
    pd.set_option('display.max_rows', None)  # 显示所有行
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', 1000)  # 调整宽度避免换行
    # create mysql engine
    engine = create_engine('mysql+pymysql://root:12345678@localhost:3306/akshare')
    # query data
    option_comm_symbol_df = option_comm_symbol(cookie)
    # 遍历每行元组，第一个元素（索引0）是第一个字段
    for row in option_comm_symbol_df.itertuples(index=False):  # index=False 不包含索引
        first_field = row[0]  # 元组的第一个元素
        try:
            # 打印基本信息
            print(first_field)
            # 可能出错的代码
            option_comm_info_df = option_comm_info(cookie, symbol=f"{first_field}")
            option_comm_info_df['交易日'] = trade_date
            # 间隔 2 秒
            time.sleep(2)
            # write to mysql
            res = option_comm_info_df.to_sql('option_comm_info', engine, index=False, if_exists='append', chunksize=10000)
        except Exception as e:  # 捕获所有继承自Exception的异常
            print(f"发生错误: {str(e)}")

@lru_cache()
def option_comm_symbol(cookie: str) -> pd.DataFrame:
    import urllib3

    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    url = "https://www.9qihuo.com/qiquanshouxufei"
    # 仅添加这部分：模拟浏览器的请求头，解决403问题
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "cookie": cookie,
        "Referer": "https://www.9qihuo.com/qiquanshouxufei"
    }
    # 请求时添加headers参数
    r = requests.get(url, headers=headers, verify=False)
    soup = BeautifulSoup(r.text, features="lxml")
    name = [
        item.string.strip()
        for item in soup.find(name="div", attrs={"id": "inst_list"}).find_all(name="a")
    ]
    code = [
        item["href"].split("?")[1].split("=")[1]
        for item in soup.find(name="div", attrs={"id": "inst_list"}).find_all(name="a")
    ]
    temp_df = pd.DataFrame([name, code]).T
    temp_df.columns = ["品种名称", "品种代码"]
    return temp_df


def option_comm_info(cookie: str, symbol: str = "工业硅期权") -> pd.DataFrame:
    """
    九期网-商品期权手续费
    https://www.9qihuo.com/qiquanshouxufei
    :param symbol: choice of {"所有", "上海期货交易所", "大连商品交易所", "郑州商品交易所", "上海国际能源交易中心", "广州期货交易所"}
    :type symbol: str
    :return: 期权手续费
    :rtype: pandas.DataFrame
    """
    import urllib3

    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    symbol_df = option_comm_symbol(cookie)
    #symbol_str = symbol_df[symbol_df["品种名称"].str.contains(symbol)][
    #    "品种代码"
    #].values[0]
    # 修改了"橡胶期权"和"合成橡胶期权"只会匹配到"合成橡胶期权"的bug
    symbol_str = symbol_df[symbol_df["品种名称"] == symbol]["品种代码"].values[0]
    params = {"heyue": symbol_str}
    url = "https://www.9qihuo.com/qiquanshouxufei"
    # 仅添加这部分：模拟浏览器的请求头，解决403问题
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "cookie": cookie,
        "Referer": "https://www.9qihuo.com/qiquanshouxufei"
    }
    # 请求时添加headers参数
    r = requests.get(url, headers=headers, params=params, verify=False)
    temp_df = pd.read_html(StringIO(r.text))[0]
    market_symbol = temp_df.iloc[0, 0]
    columns = temp_df.iloc[2, :]
    temp_df = temp_df.iloc[3:, :]
    temp_df.columns = columns
    temp_df["交易所"] = market_symbol
    temp_df.reset_index(drop=True, inplace=True)
    temp_df.index.name = None
    temp_df.columns.name = None
    temp_df["现价"] = pd.to_numeric(temp_df["现价"], errors="coerce")
    temp_df["成交量"] = pd.to_numeric(temp_df["成交量"], errors="coerce")
    temp_df["每跳毛利/元"] = pd.to_numeric(temp_df["每跳毛利/元"], errors="coerce")
    temp_df["每跳净利/元"] = pd.to_numeric(temp_df["每跳净利/元"], errors="coerce")
    soup = BeautifulSoup(r.text, features="lxml")
    raw_date_text = soup.find(name="a", attrs={"id": "dlink"}).previous
    comm_update_time = raw_date_text.split("，")[0].strip("（手续费更新时间：")
    price_update_time = (
        raw_date_text.split("，")[1].strip("价格更新时间：").strip("。）")
    )
    temp_df["手续费更新时间"] = comm_update_time
    temp_df["价格更新时间"] = price_update_time
    return temp_df


if __name__ == "__main__":
    cookie = "023ad3a0c69409e954e4f67f16b8f63d=1b1e9546d4b30850aee19b6b77868bca; PHPSESSID=44nnmth42ed0odibl1pme4c49o"
    option_comm_symbol_df = option_comm_symbol(cookie)
    print(option_comm_symbol_df)

    option_comm_info_df = option_comm_info(cookie, symbol="工业硅期权")
    print(option_comm_info_df)
