-- 个数统计 --
select '涨跌停_今日',count(*) from futures_rule where 交易日='20251014'
union
select '涨跌停_昨日',count(*) from futures_rule where 交易日='20251013'
union
select '保证金_今日',count(*) from futures_fees_info where 交易日='20251014'
union
select '保证金_昨日',count(*) from futures_fees_info where 交易日='20251013'
union
select '手续费_今日',count(*) from option_comm_info where 交易日='20251014'
union
select '手续费_昨日',count(*) from option_comm_info where 交易日='20251013';

-- 涨跌幅预警 --
select * from
(select 交易所,品种,代码,涨跌停板幅度 from futures_rule where 交易日='20251014') a
inner join
(select 交易所,品种,代码,涨跌停板幅度 from futures_rule where 交易日='20251013') b
on a.交易所 = b.交易所 and a.品种 = b.品种 and a.代码 = b.代码
where a.涨跌停板幅度 != b.涨跌停板幅度;

-- 保证金预警 --
select * from
(select 交易所,合约代码,做多保证金率 from futures_fees_info where 交易日='20251014') a
inner join
(select 交易所,合约代码,做多保证金率 from futures_fees_info where 交易日='20251013') b
on a.交易所 = b.交易所 and a.合约代码 = b.合约代码
where a.做多保证金率 != b.做多保证金率;

-- 手续费预警 --
select * from
(select 期权品种,`手续费(开+平)` from option_comm_info where 交易日='20251014') a
inner join
(select 期权品种,`手续费(开+平)` from option_comm_info where 交易日='20251013') b
on a.期权品种 = b.期权品种
where a.`手续费(开+平)` != b.`手续费(开+平)`;