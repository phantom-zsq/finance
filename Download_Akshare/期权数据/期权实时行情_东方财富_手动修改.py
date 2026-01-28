from sqlalchemy import create_engine
from datetime import datetime
import requests

import math
import random
import time
from typing import List, Dict

import pandas as pd

from akshare.utils.tqdm import get_tqdm


from request import request_with_retry

def option_real_time() -> None:
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
        # 可能出错的代码
        option_current_em_df = option_current_em()
        option_current_em_df['交易日'] = '20260122000331'
        # write to mysql
        res = option_current_em_df.to_sql('option_current_em', engine, index=False, if_exists='append', chunksize=10000)
    except Exception as e:  # 捕获所有继承自Exception的异常
        print(f"发生错误: {str(e)}")

def option_current_em() -> pd.DataFrame:
    """
    东方财富网-行情中心-期权市场
    https://quote.eastmoney.com/center/qqsc.html
    :return: 期权价格
    :rtype: pandas.DataFrame
    """
    url = "https://23.push2.eastmoney.com/api/qt/clist/get"
    params = {
        "pn": "1",
        "pz": "100",
        "po": "1",
        "np": "1",
        "ut": "bd1d9ddb04089700cf9c27f6f7426281",
        "fltt": "2",
        "invt": "2",
        "fid": "f3",
        "fs": "m:10,m:12,m:140,m:141,m:151,m:163,m:226",
        "fields": "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,"
        "f23,f24,f25,f22,f28,f11,f62,f128,f136,f115,f152,f133,f108,f163,f161,f162",
    }
    temp_df = fetch_paginated_data(url=url, base_params=params)
    temp_df.columns = [
        "序号",
        "_",
        "最新价",
        "涨跌幅",
        "涨跌额",
        "成交量",
        "成交额",
        "_",
        "_",
        "_",
        "_",
        "_",
        "代码",
        "市场标识",
        "名称",
        "_",
        "_",
        "今开",
        "_",
        "_",
        "_",
        "_",
        "_",
        "_",
        "_",
        "昨结",
        "_",
        "持仓量",
        "_",
        "_",
        "_",
        "_",
        "_",
        "_",
        "_",
        "行权价",
        "剩余日",
        "日增",
    ]
    temp_df = temp_df[
        [
            "序号",
            "代码",
            "名称",
            "最新价",
            "涨跌额",
            "涨跌幅",
            "成交量",
            "成交额",
            "持仓量",
            "行权价",
            "剩余日",
            "日增",
            "昨结",
            "今开",
            "市场标识",
        ]
    ]
    temp_df["最新价"] = pd.to_numeric(temp_df["最新价"], errors="coerce")
    temp_df["涨跌额"] = pd.to_numeric(temp_df["涨跌额"], errors="coerce")
    temp_df["涨跌幅"] = pd.to_numeric(temp_df["涨跌幅"], errors="coerce")
    temp_df["成交量"] = pd.to_numeric(temp_df["成交量"], errors="coerce")
    temp_df["成交额"] = pd.to_numeric(temp_df["成交额"], errors="coerce")
    temp_df["持仓量"] = pd.to_numeric(temp_df["持仓量"], errors="coerce")
    temp_df["行权价"] = pd.to_numeric(temp_df["行权价"], errors="coerce")
    temp_df["剩余日"] = pd.to_numeric(temp_df["剩余日"], errors="coerce")
    temp_df["日增"] = pd.to_numeric(temp_df["日增"], errors="coerce")
    temp_df["昨结"] = pd.to_numeric(temp_df["昨结"], errors="coerce")
    temp_df["今开"] = pd.to_numeric(temp_df["今开"], errors="coerce")
    option_current_cffex_em_df = option_current_cffex_em()
    big_df = pd.concat(objs=[temp_df, option_current_cffex_em_df], ignore_index=True)
    big_df["序号"] = range(1, len(big_df) + 1)
    return big_df

def option_current_cffex_em() -> pd.DataFrame:
    url = "https://futsseapi.eastmoney.com/list/option/221"
    params = {
        "orderBy": "zdf",
        "sort": "desc",
        "pageSize": "20000",
        "pageIndex": "0",
        "token": "58b2fa8f54638b60b87d69b31969089c",
        "field": "dm,sc,name,p,zsjd,zde,zdf,f152,vol,cje,ccl,xqj,syr,rz,zjsj,o",
        "blockName": "callback",
        "_:": "1706689899924",
    }
    r = requests.get(url, params=params)
    data_json = r.json()
    temp_df = pd.DataFrame(data_json["list"])
    temp_df.reset_index(inplace=True)
    temp_df["index"] = temp_df["index"] + 1
    temp_df.rename(
        columns={
            "index": "序号",
            "rz": "日增",
            "dm": "代码",
            "zsjd": "-",
            "ccl": "持仓量",
            "syr": "剩余日",
            "o": "今开",
            "p": "最新价",
            "sc": "市场标识",
            "xqj": "行权价",
            "vol": "成交量",
            "name": "名称",
            "zde": "涨跌额",
            "zdf": "涨跌幅",
            "zjsj": "昨结",
            "cje": "成交额",
        },
        inplace=True,
    )
    temp_df = temp_df[
        [
            "序号",
            "代码",
            "名称",
            "最新价",
            "涨跌额",
            "涨跌幅",
            "成交量",
            "成交额",
            "持仓量",
            "行权价",
            "剩余日",
            "日增",
            "昨结",
            "今开",
            "市场标识",
        ]
    ]
    temp_df["最新价"] = pd.to_numeric(temp_df["最新价"], errors="coerce")
    temp_df["涨跌额"] = pd.to_numeric(temp_df["涨跌额"], errors="coerce")
    temp_df["涨跌幅"] = pd.to_numeric(temp_df["涨跌幅"], errors="coerce")
    temp_df["成交量"] = pd.to_numeric(temp_df["成交量"], errors="coerce")
    temp_df["成交额"] = pd.to_numeric(temp_df["成交额"], errors="coerce")
    temp_df["持仓量"] = pd.to_numeric(temp_df["持仓量"], errors="coerce")
    temp_df["行权价"] = pd.to_numeric(temp_df["行权价"], errors="coerce")
    temp_df["剩余日"] = pd.to_numeric(temp_df["剩余日"], errors="coerce")
    temp_df["日增"] = pd.to_numeric(temp_df["日增"], errors="coerce")
    temp_df["昨结"] = pd.to_numeric(temp_df["昨结"], errors="coerce")
    temp_df["今开"] = pd.to_numeric(temp_df["今开"], errors="coerce")
    return temp_df

def fetch_paginated_data(url: str, base_params: Dict, timeout: int = 15):
    """
    东方财富-分页获取数据并合并结果
    https://quote.eastmoney.com/f1.html?newcode=0.000001
    :param url: 股票代码
    :type url: str
    :param base_params: 基础请求参数
    :type base_params: dict
    :param timeout: 请求超时时间
    :type timeout: str
    :return: 合并后的数据
    :rtype: pandas.DataFrame
    """
    # 复制参数以避免修改原始参数
    params = base_params.copy()
    # 获取第一页数据，用于确定分页信息
    r = request_with_retry(url, params=params, timeout=timeout, max_retries=2, base_delay=60)
    data_json = r.json()
    # 计算分页信息
    per_page_num = len(data_json["data"]["diff"])
    total_page = math.ceil(data_json["data"]["total"] / per_page_num)
    # 存储所有页面数据
    temp_list = []
    # 添加第一页数据
    #temp_list.append(pd.DataFrame(data_json["data"]["diff"]))
    # 获取进度条
    tqdm = get_tqdm()
    # 获取剩余页面数据
    for page in tqdm(range(70, total_page + 1), leave=False):
        params.update({"pn": page})
        # 添加随机延迟，避免请求过于频繁
        time.sleep(random.uniform(1.5, 2.5))
        if page % 50 == 0:
            time.sleep(120)

        try:
            r = request_with_retry(url, params=params, timeout=timeout, max_retries=2, base_delay=60)
            data_json = r.json()
            inner_temp_df = pd.DataFrame(data_json["data"]["diff"])
            temp_list.append(inner_temp_df)

        except (requests.RequestException, ValueError) as e:
            print(e)
            print(page)
            break

    # 合并所有数据
    temp_df = pd.concat(temp_list, ignore_index=True)
    temp_df["f3"] = pd.to_numeric(temp_df["f3"], errors="coerce")
    temp_df.sort_values(by=["f3"], ascending=False, inplace=True, ignore_index=True)
    temp_df.reset_index(inplace=True)
    temp_df["index"] = temp_df["index"].astype(int) + 1
    return temp_df

if __name__ == "__main__":
    option_real_time()