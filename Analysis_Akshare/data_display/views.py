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
            cursor.execute("select t1.品种名称, "
                           "    t1.`看涨合约-看涨期权合约` as 合约名称, "
                           "    t1.`看涨合约-买价` as 权利金, "
                           "    t1.行权价, "
                           "    t2.settle as 当前价, "
                           "    REPLACE(t3.开仓,'元','') as 手续费, "
                           "    t4.做多保证金率 as 保证金率, "
                           "    t4.合约乘数, "
                           "    t5.涨跌停比例, "
                           "    a.到期日分组, "
                           "    b.剩余天数 "
                           "from "
                           "    (select * from option_commodity_contract_table_sina where 交易日='20251031') t1 "
                           "inner join "
                           "    basic_information a "
                           "    on REGEXP_REPLACE(a.品种代码, '-[oO]', '') = REGEXP_REPLACE(t1.合约名称, '[0-9]', '') "
                           "inner join "
                           "    (select f1.到期日分组,f1.合约日期,f1.到期日,count(*) as 剩余天数 from expiration_date f1 inner join calendar f2 on f2.交易日 between DATE_FORMAT(CURDATE(), '%Y%m%d') and f1.到期日 group by f1.到期日分组,f1.合约日期,f1.到期日) b "
                           "    on a.到期日分组 = b.到期日分组 and REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = b.合约日期 "
                           "inner join "
                           "    (select settle,symbol from get_futures_daily where date='20251030') t2 "
                           "    on t1.合约名称 = t2.symbol "
                           "left join "
                           "    (select regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1') as 期权品种,min(开仓) as 开仓 from option_comm_info where 交易日='20251031' group by regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1')) t3 "
                           "    on REGEXP_REPLACE(t1.合约名称, '[0-9]', '') = t3.期权品种 "
                           "left join "
                           "    (select 交易所,合约代码,品种名称,做多保证金率,合约乘数 from futures_fees_info where 交易日='20251031') t4 "
                           "    on t1.合约名称 = t4.合约代码 or CONCAT(SUBSTRING(t1.合约名称, 1, LENGTH(t1.合约名称) - 4), SUBSTRING(t1.合约名称, -3)) = t4.合约代码 "
                           "left join "
                           "    (   select a.合约名称, "
                           "        case when b.特殊合约参数调整 REGEXP CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%，)?涨跌幅度为([0-9.]+)%') "
                           "            then REGEXP_SUBSTR(REGEXP_SUBSTR(REGEXP_SUBSTR(b.特殊合约参数调整, CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%，)?涨跌幅度为([0-9.]+)%')),'涨跌幅度为([0-9.]+)%'),'[0-9.]+') "
                           "            else b.涨跌停板幅度 end as 涨跌停比例 "
                           "        from (select distinct 合约名称 from option_commodity_contract_table_sina where 交易日='20251031') a "
                           "        inner join (select * from futures_rule where 交易日='20251031') b "
                           "        on REGEXP_REPLACE(a.合约名称, '[0-9]', '') = b.代码 "
                           "    ) t5 "
                           "    on t1.合约名称 = t5.合约名称 "
                           "where t1.`看涨合约-买价` is not null and REPLACE(t3.开仓,'元','') is not null and t4.做多保证金率 is not null and t5.涨跌停比例 is not null "
                           "order by b.剩余天数 "
                           "limit 800000000 ")
            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 添加序号和当前时间
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            for index, item in enumerate(raw_data, start=1):
                item["序号"] = index
                item["当前时间"] = current_time
                item["涨跌停比例"] = float(item["涨跌停比例"])
                item["当前价"] = float(item["当前价"])
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
                item["性价比分值"] = int(max(float(item["权利金"]) * float(item["合约乘数"]) - float(item["手续费"]), 0) * 5000 / float(item["期权保证金"]))
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

                if item["八一分位"] >= item["剩余天数"]:
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

def real_time(request):
    original_records = []
    records = []

    try:
        with connection.cursor() as cursor:
            # 查询原始数据
            cursor.execute("select t1.品种名称, "
                           "    t1.`看涨合约-看涨期权合约` as 合约名称, "
                           "    t1.`看涨合约-买价` as 权利金, "
                           "    t1.行权价, "
                           "    t2.settle as 当前价, "
                           "    REPLACE(t3.开仓,'元','') as 手续费, "
                           "    t4.做多保证金率 as 保证金率, "
                           "    t4.合约乘数, "
                           "    t5.涨跌停比例, "
                           "    a.到期日分组, "
                           "    b.剩余天数 "
                           "from "
                           "    (select * from option_commodity_contract_table_sina where 交易日='20251031') t1 "
                           "inner join "
                           "    basic_information a "
                           "    on REGEXP_REPLACE(a.品种代码, '-[oO]', '') = REGEXP_REPLACE(t1.合约名称, '[0-9]', '') "
                           "inner join "
                           "    (select f1.到期日分组,f1.合约日期,f1.到期日,count(*) as 剩余天数 from expiration_date f1 inner join calendar f2 on f2.交易日 between DATE_FORMAT(CURDATE(), '%Y%m%d') and f1.到期日 group by f1.到期日分组,f1.合约日期,f1.到期日) b "
                           "    on a.到期日分组 = b.到期日分组 and REGEXP_REPLACE(t1.合约名称, '[a-zA-Z]', '') = b.合约日期 "
                           "inner join "
                           "    (select settle,symbol from get_futures_daily where date='20251030') t2 "
                           "    on t1.合约名称 = t2.symbol "
                           "left join "
                           "    (select regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1') as 期权品种,min(开仓) as 开仓 from option_comm_info where 交易日='20251031' group by regexp_replace(期权品种,'^.*\\\\(([^\\\\d]*).*$','$1')) t3 "
                           "    on REGEXP_REPLACE(t1.合约名称, '[0-9]', '') = t3.期权品种 "
                           "left join "
                           "    (select 交易所,合约代码,品种名称,做多保证金率,合约乘数 from futures_fees_info where 交易日='20251031') t4 "
                           "    on t1.合约名称 = t4.合约代码 or CONCAT(SUBSTRING(t1.合约名称, 1, LENGTH(t1.合约名称) - 4), SUBSTRING(t1.合约名称, -3)) = t4.合约代码 "
                           "left join "
                           "    (   select a.合约名称, "
                           "        case when b.特殊合约参数调整 REGEXP CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%，)?涨跌幅度为([0-9.]+)%') "
                           "            then REGEXP_SUBSTR(REGEXP_SUBSTR(REGEXP_SUBSTR(b.特殊合约参数调整, CONCAT('(', a.合约名称, '|', CONCAT(SUBSTRING(a.合约名称, 1, LENGTH(a.合约名称) - 4), SUBSTRING(a.合约名称, -3)), ')', '合约(交易保证金比例为[0-9.]+%，)?涨跌幅度为([0-9.]+)%')),'涨跌幅度为([0-9.]+)%'),'[0-9.]+') "
                           "            else b.涨跌停板幅度 end as 涨跌停比例 "
                           "        from (select distinct 合约名称 from option_commodity_contract_table_sina where 交易日='20251031') a "
                           "        inner join (select * from futures_rule where 交易日='20251031') b "
                           "        on REGEXP_REPLACE(a.合约名称, '[0-9]', '') = b.代码 "
                           "    ) t5 "
                           "    on t1.合约名称 = t5.合约名称 "
                           "where t1.`看涨合约-买价` is not null and REPLACE(t3.开仓,'元','') is not null and t4.做多保证金率 is not null and t5.涨跌停比例 is not null "
                           "order by b.剩余天数 "
                           "limit 800000000 ")
            columns = [col[0] for col in cursor.description]
            raw_data = [dict(zip(columns, row)) for row in cursor.fetchall()]

            # 添加序号和当前时间
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            for index, item in enumerate(raw_data, start=1):
                item["序号"] = index
                item["当前时间"] = current_time
                item["涨跌停比例"] = float(item["涨跌停比例"])
                item["当前价"] = float(item["当前价"])
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
                item["性价比分值"] = int(max(float(item["权利金"]) * float(item["合约乘数"]) - float(item["手续费"]), 0) * 5000 / float(item["期权保证金"]))
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

                if item["八一分位"] >= item["剩余天数"]:
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
