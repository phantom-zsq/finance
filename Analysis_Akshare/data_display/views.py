from django.shortcuts import render
from django.db import connection
from datetime import datetime  # 用于获取当前时间


def data_list(request):
    """
    从数据库读取品种和涨跌停板幅度，添加序号和当前时间后返回给前端
    """
    records = []

    try:
        with connection.cursor() as cursor:
            # 从数据库查询原始数据
            cursor.execute("SELECT 品种, 涨跌停板幅度 FROM futures_rule LIMIT 8")
            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 获取当前时间（格式化为字符串，如：2023-10-16 15:30:45）
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            # 遍历原始数据，添加序号和当前时间字段
            for index, item in enumerate(raw_data, start=1):  # start=1 使序号从1开始
                # 新增两个字段
                item["序号"] = index
                item["当前时间"] = current_time
                records.append(item)

    except Exception as e:
        print(f"数据库查询错误: {str(e)}")

    return render(request, "data_display/data_list.html", {"records": records})
