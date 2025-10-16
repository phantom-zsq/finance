from django.shortcuts import render
from django.db import connection
from datetime import datetime


def data_list(request):
    original_records = []
    records = []

    try:
        with connection.cursor() as cursor:
            # 查询原始数据
            cursor.execute("SELECT 品种, 涨跌停板幅度 FROM futures_rule LIMIT 8")
            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 添加序号和当前时间
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            for index, item in enumerate(raw_data, start=1):
                item["序号"] = index
                item["当前时间"] = current_time
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
