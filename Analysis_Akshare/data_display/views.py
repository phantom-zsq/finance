from django.shortcuts import render
from django.db import connection
from datetime import datetime
import math

def data_list(request):
    original_records = []
    records = []

    try:
        with connection.cursor() as cursor:
            # 查询原始数据
            cursor.execute("SELECT 品种名称,`看涨合约-看涨期权合约` as 合约名称,行权价 FROM option_commodity_contract_table_sina where 交易日='20251021' LIMIT 8")
            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 添加序号和当前时间
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            for index, item in enumerate(raw_data, start=1):
                item["序号"] = index
                item["当前时间"] = current_time
                # 变量
                item["当前价"] = 51500
                item["涨跌停比例"] = 9
                # 已确定
                item["八八分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01),1)
                item["八四分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 2),1)
                item["八三分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 * 3 / 8),1)
                item["八二分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 4),1)
                original_records.append(item)

            # 核心：按"涨跌停板幅度"排序（数值排序）
            # 注意：确保数据库中该字段是数值类型（如DECIMAL/FLOAT）
            records = sorted(
                original_records,
                key=lambda x: x["序号"],  # 转换为浮点数用于比较
                reverse=False  # False=升序，True=降序
            )
    except Exception as e:
        print(f"数据库查询错误: {str(e)}")

    return render(request, "data_display/data_list.html", {"records": records})
