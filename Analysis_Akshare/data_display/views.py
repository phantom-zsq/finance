from django.shortcuts import render
from django.db import connection
from datetime import datetime
import math
import pytz
import sqlparse
from datetime import date, timedelta
# 补充导入decimal（防止Decimal类型转换报错，直接嵌入方法内）
import decimal

def data_list(request):
    original_records = []
    records = []

    try:
        with connection.cursor() as cursor:
            # 日期
            current_date = date.today()
            current_date = current_date.strftime("%Y%m%d")
            TIME_THRESHOLD = datetime.strptime("16:00", "%H:%M").time()

            # 直接执行SELECT语句并读取为DataFrame
            sql = "SELECT min(交易日) as pre_workday,max(交易日) as current_workday FROM (SELECT * FROM calendar where 交易日 <= %s order by 交易日 desc limit 2) t;"
            cursor.execute(sql, (current_date,))
            pre_workday = None  # 初始化变量: 最小交易日
            current_workday = None  # 初始化变量: 最大交易日
            trade_date = None  # 初始化变量: 交易日
            result = cursor.fetchone()  # 该SQL仅返回1行结果，用fetchone()
            if result:  # 防止查询结果为空导致报错
                pre_workday = result[0]  # 最小交易日
                current_workday = result[1]  # 最大交易日

            current_time = datetime.now(pytz.timezone("Asia/Shanghai")).time()
            if current_date == current_workday and current_time <= TIME_THRESHOLD:
                trade_date = pre_workday
            else:
                trade_date = current_workday

            cursor.execute("select "
                           "    SUBSTR(a.交易所,1,2) as 交易所, "
                           "    REGEXP_REPLACE(a.品种名称, '期权', '') as 品种名称, "
                           "    t1.`看涨合约-看涨期权合约` as 合约名称, "
                           "    t1.`看涨合约-买价` as 权利金, "
                           "    t1.行权价, "
                           "    t2.settle as 当前价, "
                           "    REPLACE(t3.开仓,'元','') as 手续费, "
                           "    REPLACE(t3.平今,'元','') as 平今, "
                           "    t4.做多保证金率 as 保证金率, "
                           "    t4.合约乘数, "
                           "    t5.涨跌停比例, "
                           "    a.到期日分组, "
                           "    b.剩余天数 "
                           "from ( "
                           "    select * from (select a.商品名称 as 品种名称,a.合约名称 as `看涨合约-看涨期权合约`,a.收盘价 as `看涨合约-买价`,REGEXP_REPLACE(a.合约名称, '-[CP].*', '') as 合约名称,REGEXP_REPLACE(a.合约名称, '.*[CP]-', '') as 行权价,row_number() over(partition by a.商品名称 order by REGEXP_REPLACE(a.合约名称, '-[CP].*', '') asc,CONVERT(REGEXP_REPLACE(a.合约名称, '.*[CP]-', ''), FLOAT) desc) as rk from option_hist_gfex a left join futures_fees_info b on REGEXP_REPLACE(a.合约名称, '-[CP].*', '') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约名称 regexp '.*-C-.*' and a.收盘价 > b.最小跳动 / 2) t where t.rk=1 "
                           "    union "
                           "    select * from (select a.合约代码 as 品种名称,a.合约代码 as `看涨合约-看涨期权合约`,a.今收盘 as `看涨合约-买价`,REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称,REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1') as 行权价,row_number() over(partition by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1') order by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') asc,CONVERT(REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1'), FLOAT) desc) as rk from option_hist_czce a left join futures_fees_info b on REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约代码 regexp '.*[0-9]C[0-9].*' and a.今收盘 > b.最小跳动 / 2) t where t.rk=1 "
                           "    union "
                           "    select * from (select a.合约代码 as 品种名称,a.合约代码 as `看涨合约-看涨期权合约`,a.收盘价 as `看涨合约-买价`,REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称,REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1') as 行权价,row_number() over(partition by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1') order by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') asc,CONVERT(REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1'), FLOAT) desc) as rk from option_hist_shfe a left join futures_fees_info b on REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约代码 regexp '.*[0-9]C[0-9].*' and a.收盘价 > b.最小跳动 / 2) t where t.rk=1 "
                           "    union "
                           "    select * from (select a.品种名称,a.合约 as `看涨合约-看涨期权合约`,a.收盘价 as `看涨合约-买价`,REGEXP_REPLACE(a.合约, '-[CP].*', '') as 合约名称,REGEXP_REPLACE(a.合约, '.*[CP]-', '') as 行权价,row_number() over(partition by a.品种名称 order by REGEXP_REPLACE(a.合约, '-[CP].*', '') asc,CONVERT(REGEXP_REPLACE(a.合约, '.*[CP]-', ''), FLOAT) desc) as rk from option_hist_dce a left join futures_fees_info b on REGEXP_REPLACE(a.合约, '-[CP].*', '') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约 regexp '.*-C-.*' and a.收盘价 > b.最小跳动 / 2) t where t.rk=1 "
                           ") t1 "
                           "inner join "
                           "    basic_information a "
                           "    on REGEXP_REPLACE(a.品种代码, '-[oO]', '') = REGEXP_REPLACE(t1.合约名称, '[0-9]', '') "
                           "inner join "
                           "    (select f1.到期日分组,f1.合约日期,f1.到期日,count(*) as 剩余天数 from expiration_date f1 inner join calendar f2 on f2.交易日 > %s and f2.交易日 <= f1.到期日 group by f1.到期日分组,f1.合约日期,f1.到期日) b "
                           "    on a.到期日分组 = b.到期日分组 and (REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = b.合约日期 or REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = SUBSTRING(b.合约日期, -3)) "
                           "inner join "
                           "    (select distinct settle,symbol from get_futures_daily where date=%s) t2 "
                           "    on t1.合约名称 = t2.symbol "
                           "left join "
                           "    (select regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1') as 期权品种,min(开仓) as 开仓,min(平今) as 平今 from option_comm_info where 交易日=%s group by regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1')) t3 "
                           "    on REGEXP_REPLACE(t1.合约名称, '[0-9]', '') = t3.期权品种 "
                           "left join "
                           "    (select 交易所,合约代码,品种名称,做多保证金率,合约乘数 from futures_fees_info where 交易日=%s) t4 "
                           "    on t1.合约名称 = t4.合约代码 "
                           "left join "
                           "    (   select a.合约名称, "
                           "        case when b.特殊合约参数调整 REGEXP CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%%，)?涨跌幅度为([0-9.]+)%%') "
                           "            then REGEXP_SUBSTR(REGEXP_SUBSTR(REGEXP_SUBSTR(b.特殊合约参数调整, CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%%，)?涨跌幅度为([0-9.]+)%%')),'涨跌幅度为([0-9.]+)%%'),'[0-9.]+') "
                           "            else b.涨跌停板幅度 end as 涨跌停比例 "
                           "        from ( "
                           "            select distinct REGEXP_REPLACE(合约名称, '-[CP].*', '') as 合约名称 from option_hist_gfex where 交易日=%s and 合约名称 regexp '.*-C-.*' "
                           "            union "
                           "            select distinct REGEXP_REPLACE(合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称 from option_hist_czce where 交易日=%s and 合约代码 regexp '.*[0-9]C[0-9].*' "
                           "            union "
                           "            select distinct REGEXP_REPLACE(合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称 from option_hist_shfe where 交易日=%s and 合约代码 regexp '.*[0-9]C[0-9].*' "
                           "            union "
                           "            select distinct REGEXP_REPLACE(合约, '-[CP].*', '') as 合约名称 from option_hist_dce where 交易日=%s and 合约 regexp '.*-C-.*' "
                           "        ) a "
                           "        inner join (select * from futures_rule where 交易日=%s) b "
                           "        on REGEXP_REPLACE(a.合约名称, '[0-9]', '') = b.代码 "
                           "    ) t5 "
                           "    on t1.合约名称 = t5.合约名称 "
                           "where a.品种名称 not like '%%动力煤%%'  "
                           " ",(trade_date,current_workday,trade_date,current_workday,trade_date,current_workday,trade_date,current_workday,trade_date,trade_date,current_workday,current_workday,trade_date,trade_date,trade_date,trade_date,current_workday))

            # print sql
            for index, query in enumerate(connection.queries, start=1):
                raw_sql = query['sql']
                sql_time = query['time']
                # 调用专业格式化函数
                formatted_sql = professional_format_sql(raw_sql)
                print(f"【第 {index} 条 SQL】")
                print(f"SQL 语句：\n{formatted_sql}")
                print(f"执行耗时：{sql_time} 秒\n")

            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 添加序号和当前时间
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            for index, item in enumerate(raw_data, start=1):
                item["序号"] = index
                item["当前时间"] = current_time
                item["涨跌停比例"] = float(item["涨跌停比例"])
                item["当前价"] = float(item["当前价"])
                item["行权价"] = float(item["行权价"])
                item["八八分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01),1)
                item["八四分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 2),1)
                item["八三分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 * 3 / 8),1)
                item["八二分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 4),1)
                item["八一分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 8),1)
                # 深度分值
                item["深度分值"] = min(int(item["八八分位"] * 100 / item["剩余天数"]), 100)
                # 性价比分值
                item["期货保证金"] = item["当前价"] * float(item["保证金率"]) * float(item["合约乘数"])
                item["期权结算价"] = 1
                item["期权保证金"] = item["期权结算价"] * float(item["合约乘数"]) + max(item["期货保证金"] - max(0.5 * (item["行权价"] - item["当前价"]) * float(item["合约乘数"]), 0), 0.5 * item["期货保证金"])
                item["性价比分值"] = int(max(float(item["权利金"]) * float(item["合约乘数"]) - float(item["手续费"]), 0) * 10000 / item["剩余天数"] / float(item["期权保证金"]))
                # 多空
                item["多空"] = '多'
                # 技术分值
                item["技术分值"] = 1
                # 现货价
                item["现货价"] = 54345
                # 期货价
                item["期货价"] = item["当前价"]
                # 基差分值
                item["基差分值"] = 1
                # 总分
                item["总分"] = int(item["深度分值"] * item["性价比分值"] * item["技术分值"] * item["基差分值"] / 100)

                if item["八二分位"] >= item["剩余天数"]:
                    original_records.append(item)
            # 核心：按"总分"排序（数值排序）
            # 注意：确保数据库中该字段是数值类型（如DECIMAL/FLOAT）
            records = sorted(
                original_records,
                key=lambda x: x["总分"],
                reverse=True  # False=升序，True=降序
            )
    except Exception as e:
        print(f"数据库查询错误: {str(e)}")

    return render(request, "data_display/data_list.html", {"records": records})

def professional_format_sql(raw_sql, indent_width=2):
    """
    利用 sqlparse 进行专业 SQL 格式化
    :param raw_sql: 原始 SQL 字符串（来自 connection.queries['sql']）
    :param indent_width: 缩进空格数，默认 2
    :return: 格式化后的美观 SQL
    """
    if not isinstance(raw_sql, str):
        return raw_sql

    # 核心：调用 sqlparse.format 进行格式化
    formatted_sql = sqlparse.format(
        raw_sql,
        reindent=True,  # 自动重新缩进
        indent_width=indent_width,  # 缩进宽度
        keyword_case='upper',  # SQL 关键字转为大写（可选：'lower' 转为小写，None 保留原样）
        strip_comments=False,  # 保留注释（如需去除设为 True）
        reindent_aligned=True  # 对齐相关子句，排版更美观
    )
    return formatted_sql.strip()

# 定义执行原生SQL的工具函数（内部嵌套，不额外定义类/外部方法）
def query_option_data(sql_template, target_date):
    """内部工具函数：执行SQL查询指定日期的期权数据"""
    data_list = []
    with connection.cursor() as cursor:
        # 执行SQL，传入日期参数（防止SQL注入）
        cursor.execute(sql_template, target_date)
        # 获取查询结果的字段名（用于组装字典）
        col_names = [desc[0] for desc in cursor.description]
        # 遍历结果集，组装为与原模拟数据格式一致的字典列表
        for row in cursor.fetchall():
            data_dict = dict(zip(col_names, row))
            # 转换Decimal类型为float（避免前端模板渲染问题，保持原数据格式）
            for key, value in data_dict.items():
                if isinstance(value, (decimal.Decimal,)):
                    data_dict[key] = float(value)
            data_list.append(data_dict)
    return data_list

def monitor(request):
    """
    无模型、直接通过原生SQL从MySQL读取数据：实现期权监控页面逻辑，数据来源MySQL
    """
    # ===================== 步骤1：获取今日/昨日日期 =====================
    with connection.cursor() as cursor:
        # 日期
        current_date = date.today()
        current_date = current_date.strftime("%Y%m%d")

        # 直接执行SELECT语句并读取为DataFrame
        sql = "SELECT min(交易日) as pre_workday,max(交易日) as current_workday FROM (SELECT * FROM calendar where 交易日 <= %s order by 交易日 desc limit 2) t;"
        cursor.execute(sql, (current_date,))
        pre_workday = None  # 初始化变量: 最小交易日
        current_workday = None  # 初始化变量: 最大交易日
        result = cursor.fetchone()  # 该SQL仅返回1行结果，用fetchone()
        if result:  # 防止查询结果为空导致报错
            pre_workday = result[0]  # 最小交易日
            current_workday = result[1]  # 最大交易日

    # ===================== 步骤2：原生SQL查询今日/昨日数据（无需模型类） =====================
    # 定义查询SQL（适配之前创建的option_data表，若表名/字段名不一致请对应修改）
    sql_template = ("select "
                   "    SUBSTR(a.交易所,1,2) as 交易所, "
                   "    REGEXP_REPLACE(a.品种名称, '期权', '') as 品种名称, "
                   "    t1.`看涨合约-看涨期权合约` as 合约名称, "
                   "    t1.`看涨合约-买价` as 权利金, "
                   "    t1.行权价, "
                   "    t2.settle as 当前价, "
                   "    REPLACE(t3.开仓,'元','') as 手续费, "
                   "    REPLACE(t3.平今,'元','') as 平今, "
                   "    t4.做多保证金率 as 保证金率, "
                   "    t4.合约乘数, "
                   "    t5.涨跌停比例, "
                   "    a.到期日分组, "
                   "    b.剩余天数 "
                   "from ( "
                   "    select * from (select a.商品名称 as 品种名称,a.合约名称 as `看涨合约-看涨期权合约`,a.收盘价 as `看涨合约-买价`,REGEXP_REPLACE(a.合约名称, '-[CP].*', '') as 合约名称,REGEXP_REPLACE(a.合约名称, '.*[CP]-', '') as 行权价,row_number() over(partition by a.商品名称 order by REGEXP_REPLACE(a.合约名称, '-[CP].*', '') asc,CONVERT(REGEXP_REPLACE(a.合约名称, '.*[CP]-', ''), FLOAT) desc) as rk from option_hist_gfex a left join futures_fees_info b on REGEXP_REPLACE(a.合约名称, '-[CP].*', '') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约名称 regexp '.*-C-.*' and a.收盘价 > b.最小跳动 / 2) t where t.rk=1 "
                   "    union "
                   "    select * from (select a.合约代码 as 品种名称,a.合约代码 as `看涨合约-看涨期权合约`,a.今收盘 as `看涨合约-买价`,REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称,REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1') as 行权价,row_number() over(partition by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1') order by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') asc,CONVERT(REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1'), FLOAT) desc) as rk from option_hist_czce a left join futures_fees_info b on REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约代码 regexp '.*[0-9]C[0-9].*' and a.今收盘 > b.最小跳动 / 2) t where t.rk=1 "
                   "    union "
                   "    select * from (select a.合约代码 as 品种名称,a.合约代码 as `看涨合约-看涨期权合约`,a.收盘价 as `看涨合约-买价`,REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称,REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1') as 行权价,row_number() over(partition by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1') order by REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') asc,CONVERT(REGEXP_REPLACE(a.合约代码, '.*[0-9][CP]([0-9]+)', '$1'), FLOAT) desc) as rk from option_hist_shfe a left join futures_fees_info b on REGEXP_REPLACE(a.合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约代码 regexp '.*[0-9]C[0-9].*' and a.收盘价 > b.最小跳动 / 2) t where t.rk=1 "
                   "    union "
                   "    select * from (select a.品种名称,a.合约 as `看涨合约-看涨期权合约`,a.收盘价 as `看涨合约-买价`,REGEXP_REPLACE(a.合约, '-[CP].*', '') as 合约名称,REGEXP_REPLACE(a.合约, '.*[CP]-', '') as 行权价,row_number() over(partition by a.品种名称 order by REGEXP_REPLACE(a.合约, '-[CP].*', '') asc,CONVERT(REGEXP_REPLACE(a.合约, '.*[CP]-', ''), FLOAT) desc) as rk from option_hist_dce a left join futures_fees_info b on REGEXP_REPLACE(a.合约, '-[CP].*', '') = b.合约代码 where a.交易日=%s and b.交易日=%s and a.合约 regexp '.*-C-.*' and a.收盘价 > b.最小跳动 / 2) t where t.rk=1 "
                   ") t1 "
                   "inner join "
                   "    basic_information a "
                   "    on REGEXP_REPLACE(a.品种代码, '-[oO]', '') = REGEXP_REPLACE(t1.合约名称, '[0-9]', '') "
                   "inner join "
                   "    (select f1.到期日分组,f1.合约日期,f1.到期日,count(*) as 剩余天数 from expiration_date f1 inner join calendar f2 on f2.交易日 > %s and f2.交易日 <= f1.到期日 group by f1.到期日分组,f1.合约日期,f1.到期日) b "
                   "    on a.到期日分组 = b.到期日分组 and (REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = b.合约日期 or REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = SUBSTRING(b.合约日期, -3)) "
                   "inner join "
                   "    (select distinct settle,symbol from get_futures_daily where date=%s) t2 "
                   "    on t1.合约名称 = t2.symbol "
                   "left join "
                   "    (select regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1') as 期权品种,min(开仓) as 开仓,min(平今) as 平今 from option_comm_info where 交易日=%s group by regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1')) t3 "
                   "    on REGEXP_REPLACE(t1.合约名称, '[0-9]', '') = t3.期权品种 "
                   "left join "
                   "    (select 交易所,合约代码,品种名称,做多保证金率,合约乘数 from futures_fees_info where 交易日=%s) t4 "
                   "    on t1.合约名称 = t4.合约代码 "
                   "left join "
                   "    (   select a.合约名称, "
                   "        case when b.特殊合约参数调整 REGEXP CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%%，)?涨跌幅度为([0-9.]+)%%') "
                   "            then REGEXP_SUBSTR(REGEXP_SUBSTR(REGEXP_SUBSTR(b.特殊合约参数调整, CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%%，)?涨跌幅度为([0-9.]+)%%')),'涨跌幅度为([0-9.]+)%%'),'[0-9.]+') "
                   "            else b.涨跌停板幅度 end as 涨跌停比例 "
                   "        from ( "
                   "            select distinct REGEXP_REPLACE(合约名称, '-[CP].*', '') as 合约名称 from option_hist_gfex where 交易日=%s and 合约名称 regexp '.*-C-.*' "
                   "            union "
                   "            select distinct REGEXP_REPLACE(合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称 from option_hist_czce where 交易日=%s and 合约代码 regexp '.*[0-9]C[0-9].*' "
                   "            union "
                   "            select distinct REGEXP_REPLACE(合约代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') as 合约名称 from option_hist_shfe where 交易日=%s and 合约代码 regexp '.*[0-9]C[0-9].*' "
                   "            union "
                   "            select distinct REGEXP_REPLACE(合约, '-[CP].*', '') as 合约名称 from option_hist_dce where 交易日=%s and 合约 regexp '.*-C-.*' "
                   "        ) a "
                   "        inner join (select * from futures_rule where 交易日=%s) b "
                   "        on REGEXP_REPLACE(a.合约名称, '[0-9]', '') = b.代码 "
                   "    ) t5 "
                   "    on t1.合约名称 = t5.合约名称 "
                   "where a.品种名称 not like '%%动力煤%%' "
                   "order by a.品种名称 ")

    # 查询今日、昨日数据（调用内部工具函数）
    today_data_list = query_option_data(sql_template, [pre_workday,current_workday,pre_workday,current_workday,pre_workday,current_workday,pre_workday,current_workday,pre_workday,pre_workday,current_workday,current_workday,pre_workday,pre_workday,pre_workday,pre_workday,current_workday])
    yesterday_data_list = query_option_data(sql_template, [pre_workday,current_workday,pre_workday,current_workday,pre_workday,current_workday,pre_workday,current_workday,pre_workday,pre_workday,pre_workday,pre_workday,pre_workday,pre_workday,pre_workday,pre_workday,pre_workday])

    # 转为字典：key=合约名称，方便匹配对比
    today_data_dict = {data["品种名称"]: data for data in today_data_list}
    yesterday_data_dict = {data["品种名称"]: data for data in yesterday_data_list}

    # ===================== 步骤2：记录数统计 =====================
    today_total_count = len(today_data_dict)
    yesterday_total_count = len(yesterday_data_dict)
    count_change = today_total_count - yesterday_total_count

    # 设定变动值颜色
    if count_change > 0:
        count_change_color = "#28a745"
    elif count_change < 0:
        count_change_color = "#dc3545"
    else:
        count_change_color = "#333333"

    # ===================== 步骤3：初始化三大预警列表 =====================
    price_change_warnings = []
    margin_warnings = []
    fee_warnings = []

    # ===================== 步骤4：对比数据，筛选差异 =====================
    for contract_name, today_data in today_data_dict.items():
        yesterday_data = yesterday_data_dict.get(contract_name)
        if not yesterday_data:
            continue

        # 基础公共数据
        base_contrast = {
            "交易所": today_data["交易所"],
            "品种名称": today_data["品种名称"],
            "合约名称": today_data["合约名称"],
        }

        # ---------- 4.1 涨跌幅差异 ----------
        today_price_change = today_data["涨跌停比例"]
        yesterday_price_change = yesterday_data["涨跌停比例"]
        price_change_diff = float(today_price_change) - float(yesterday_price_change)

        if abs(price_change_diff) > 0.0001:
            price_contrast = {
                **base_contrast,
                "今日涨跌幅": today_price_change,
                "昨日涨跌幅": yesterday_price_change,
                "涨跌幅差异": price_change_diff,
            }
            price_change_warnings.append(price_contrast)

        # ---------- 4.2 保证金差异 ----------
        today_margin_rate = today_data["保证金率"]
        yesterday_margin_rate = yesterday_data["保证金率"]
        margin_rate_diff = today_margin_rate - yesterday_margin_rate

        if abs(margin_rate_diff) > 0.0001:
            margin_contrast = {
                **base_contrast,
                "今日保证金率": today_margin_rate,
                "昨日保证金率": yesterday_margin_rate,
                "保证金率差异": margin_rate_diff,
            }
            margin_warnings.append(margin_contrast)

        # ---------- 4.3 手续费差异 ----------
        today_fee = today_data["手续费"]
        yesterday_fee = yesterday_data["手续费"]
        fee_diff = float(today_fee) - float(yesterday_fee)

        if abs(fee_diff) > 0.0001:
            fee_contrast = {
                **base_contrast,
                "今日手续费": today_fee,
                "昨日手续费": yesterday_fee,
                "手续费差异": fee_diff,
            }
            fee_warnings.append(fee_contrast)

    # ===================== 步骤5：总预警条数 =====================
    total_warning_count = len(price_change_warnings) + len(margin_warnings) + len(fee_warnings)

    # ===================== 步骤6：渲染模板 =====================
    template_context = {
        "today_total_count": today_total_count,
        "yesterday_total_count": yesterday_total_count,
        "count_change": count_change,
        "count_change_color": count_change_color,
        "total_warning_count": total_warning_count,
        "price_change_warnings": price_change_warnings,
        "margin_warnings": margin_warnings,
        "fee_warnings": fee_warnings,
    }

    return render(request, "data_display/monitor.html", template_context)

def real_time(request):
    original_records = []
    records = []

    try:
        with connection.cursor() as cursor:
            # 日期
            current_date = date.today()
            current_date = current_date.strftime("%Y%m%d")

            # 直接执行SELECT语句并读取为DataFrame
            sql = "SELECT min(交易日) as pre_workday,max(交易日) as current_workday FROM (SELECT * FROM calendar where 交易日 <= %s order by 交易日 desc limit 2) t;"
            cursor.execute(sql, (current_date,))
            pre_workday = None  # 初始化变量: 最小交易日
            current_workday = None  # 初始化变量: 最大交易日
            trade_date = None  # 初始化变量: 交易日
            result = cursor.fetchone()  # 该SQL仅返回1行结果，用fetchone()
            if result:  # 防止查询结果为空导致报错
                pre_workday = result[0]  # 最小交易日
                current_workday = result[1]  # 最大交易日
            trade_date = current_workday

            cursor.execute("select "
                           "    SUBSTR(a.交易所,1,2) as 交易所, "
                           "    REGEXP_REPLACE(a.品种名称, '期权', '') as 品种名称, "
                           "    t1.`看涨合约-看涨期权合约` as 合约名称, "
                           "    t1.`看涨合约-买价` as 权利金, "
                           "    t1.行权价, "
                           "    t2.settle as 当前价, "
                           "    REPLACE(t3.开仓,'元','') as 手续费, "
                           "    REPLACE(t3.平今,'元','') as 平今, "
                           "    t4.做多保证金率 as 保证金率, "
                           "    t4.合约乘数, "
                           "    t5.涨跌停比例, "
                           "    a.到期日分组, "
                           "    b.剩余天数 "
                           "from ( "
                           "    select * from ( "
                           "        select "
                           "            regexp_replace(a.名称,'[0-9]+.*$','') as 品种名称, "
                           "            a.代码 as `看涨合约-看涨期权合约`, "
                           "            a.最新价 as `看涨合约-买价`, "
                           "            case when a.代码 regexp '.*-C-.*' then REGEXP_REPLACE(a.代码, '-[CP].*', '') else REGEXP_REPLACE(a.代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') end AS 合约名称, "
                           "            case when a.代码 regexp '.*-C-.*' then REGEXP_REPLACE(a.代码, '.*[CP]-', '') else REGEXP_REPLACE(a.代码, '.*[0-9][CP]([0-9]+)', '$1') end AS 行权价, "
                           "            case when a.代码 regexp '.*-C-.*' then row_number() over(PARTITION BY regexp_replace(a.名称,'[0-9]+.*$','') ORDER BY REGEXP_REPLACE(a.代码, '-[CP].*', '') ASC,CONVERT(REGEXP_REPLACE(a.代码, '.*[CP]-', ''), FLOAT) DESC) "
                           "                else row_number() over(PARTITION BY REGEXP_REPLACE(a.代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1') ORDER BY REGEXP_REPLACE(a.代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') ASC,CONVERT(REGEXP_REPLACE(a.代码, '.*[0-9][CP]([0-9]+)', '$1'), FLOAT) DESC) end AS rk "
                           "        from (select * from option_current_em where 交易日 = (select max(交易日) from option_current_em)) a "
                           "        left join futures_fees_info b "
                           "            on REGEXP_REPLACE(a.代码, '-[CP].*', '') = b.合约代码 or REGEXP_REPLACE(a.代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') = b.合约代码 "
                           "        where b.交易日=%s "
                           "            and (a.代码 regexp '.*-C-.*' or a.代码 regexp '.*[0-9]C[0-9].*') "
                           "            and a.最新价 > b.最小跳动 / 2 "
                           "    ) t where rk=1 "
                           ") t1 "
                           "inner join "
                           "    basic_information a "
                           "    on REGEXP_REPLACE(a.品种代码, '-[oO]', '') = REGEXP_REPLACE(t1.合约名称, '[0-9]', '') "
                           "inner join "
                           "    (select f1.到期日分组,f1.合约日期,f1.到期日,count(*) as 剩余天数 from expiration_date f1 inner join calendar f2 on f2.交易日 > %s and f2.交易日 <= f1.到期日 group by f1.到期日分组,f1.合约日期,f1.到期日) b "
                           "    on a.到期日分组 = b.到期日分组 and (REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = b.合约日期 or REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = SUBSTRING(b.合约日期, -3)) "
                           "inner join "
                           "    (select trade as settle,symbol from futures_zh_realtime where 交易日 = (select max(交易日) from futures_zh_realtime)) t2 "
                           "    on t1.合约名称 = t2.symbol or t1.合约名称 = REGEXP_REPLACE(t2.symbol, '([a-zA-Z])[0-9]', '$1') "
                           "left join "
                           "    (select regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1') as 期权品种,min(开仓) as 开仓,min(平今) as 平今 from option_comm_info where 交易日=%s group by regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1')) t3 "
                           "    on REGEXP_REPLACE(t1.合约名称, '[0-9]', '') = t3.期权品种 "
                           "left join "
                           "    (select 交易所,合约代码,品种名称,做多保证金率,合约乘数 from futures_fees_info where 交易日=%s) t4 "
                           "    on t1.合约名称 = t4.合约代码 "
                           "left join "
                           "    (   select a.合约名称, "
                           "        case when b.特殊合约参数调整 REGEXP CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%%，)?涨跌幅度为([0-9.]+)%%') "
                           "            then REGEXP_SUBSTR(REGEXP_SUBSTR(REGEXP_SUBSTR(b.特殊合约参数调整, CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%%，)?涨跌幅度为([0-9.]+)%%')),'涨跌幅度为([0-9.]+)%%'),'[0-9.]+') "
                           "            else b.涨跌停板幅度 end as 涨跌停比例 "
                           "        from ( "
                           "            select distinct case when 代码 regexp '.*-C-.*' then REGEXP_REPLACE(代码, '-[CP].*', '') else REGEXP_REPLACE(代码, '([a-zA-Z]+)([0-9]+)[CP][0-9].*', '$1$2') end AS 合约名称 from option_current_em where 交易日 = (select max(交易日) from option_current_em) "
                           "        ) a "
                           "        inner join (select * from futures_rule where 交易日=%s) b "
                           "        on REGEXP_REPLACE(a.合约名称, '[0-9]', '') = b.代码 "
                           "    ) t5 "
                           "    on t1.合约名称 = t5.合约名称 "
                           "where a.品种名称 not like '%%动力煤%%' "
                           " ",(trade_date,trade_date,current_workday,current_workday,current_workday))

            # print sql
            for index, query in enumerate(connection.queries, start=1):
                raw_sql = query['sql']
                sql_time = query['time']
                # 调用专业格式化函数
                formatted_sql = professional_format_sql(raw_sql)
                print(f"【第 {index} 条 SQL】")
                print(f"SQL 语句：\n{formatted_sql}")
                print(f"执行耗时：{sql_time} 秒\n")

            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 添加序号和当前时间
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            for index, item in enumerate(raw_data, start=1):
                item["序号"] = index
                item["当前时间"] = current_time
                item["涨跌停比例"] = float(item["涨跌停比例"])
                item["当前价"] = float(item["当前价"])
                item["行权价"] = float(item["行权价"])
                item["八八分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01),1)
                item["八四分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 2),1)
                item["八三分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 * 3 / 8),1)
                item["八二分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 4),1)
                item["八一分位"] = 0 if item["行权价"] < item["当前价"] else round(math.log(item["行权价"] / item["当前价"]) / math.log(1 + item["涨跌停比例"] * 0.01 / 8),1)
                # 深度分值
                item["深度分值"] = min(int(item["八八分位"] * 100 / item["剩余天数"]), 100)
                # 性价比分值
                item["期货保证金"] = item["当前价"] * float(item["保证金率"]) * float(item["合约乘数"])
                item["期权结算价"] = 1
                item["期权保证金"] = item["期权结算价"] * float(item["合约乘数"]) + max(item["期货保证金"] - max(0.5 * (item["行权价"] - item["当前价"]) * float(item["合约乘数"]), 0), 0.5 * item["期货保证金"])
                item["性价比分值"] = int(max(float(item["权利金"]) * float(item["合约乘数"]) - float(item["手续费"]), 0) * 10000 / item["剩余天数"] / float(item["期权保证金"]))
                # 多空
                item["多空"] = '多'
                # 技术分值
                item["技术分值"] = 1
                # 现货价
                item["现货价"] = 54345
                # 期货价
                item["期货价"] = item["当前价"]
                # 基差分值
                item["基差分值"] = 1
                # 总分
                item["总分"] = int(item["深度分值"] * item["性价比分值"] * item["技术分值"] * item["基差分值"] / 100)

                if item["八二分位"] >= item["剩余天数"]:
                    original_records.append(item)
            # 核心：按"总分"排序（数值排序）
            # 注意：确保数据库中该字段是数值类型（如DECIMAL/FLOAT）
            records = sorted(
                original_records,
                key=lambda x: x["总分"],
                reverse=True  # False=升序，True=降序
            )
    except Exception as e:
        print(f"数据库查询错误: {str(e)}")

    return render(request, "data_display/real_time.html", {"records": records})
