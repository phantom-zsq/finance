from django.shortcuts import render
from django.db import connection  # 导入数据库连接


def data_list(request):
    # 使用原始SQL查询，只查询需要的字段
    with connection.cursor() as cursor:
        # 明确指定查询的字段：品种和涨跌停板幅度
        cursor.execute("SELECT 品种, 涨跌停板幅度 FROM futures_rule limit 10")
        # 获取查询结果
        columns = [col[0] for col in cursor.description]  # 获取字段名
        # 将结果转换为字典列表，方便模板使用
        records = [
            dict(zip(columns, row))
            for row in cursor.fetchall()
        ]

    return render(request, "data_display/data_list.html", {"records": records})
