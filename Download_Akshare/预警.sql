-- 涨跌幅预警 --
select * from
(select 交易所,品种,代码,涨跌停板幅度 from futures_rule where 交易日='20251013') a
inner join
(select 交易所,品种,代码,涨跌停板幅度 from futures_rule where 交易日='20251010') b
on a.交易所 = b.交易所 and a.品种 = b.品种 and a.代码 = b.代码
where a.涨跌停板幅度 != b.涨跌停板幅度;

-- 保证金预警 --
select * from
(select 交易所,合约代码,做多保证金率 from futures_fees_info where DATE_FORMAT(STR_TO_DATE(更新时间, '%Y-%m-%d %H:%i:%s'), '%Y%m%d')='20251014') a
inner join
(select 交易所,合约代码,做多保证金率 from futures_fees_info where DATE_FORMAT(STR_TO_DATE(更新时间, '%Y-%m-%d %H:%i:%s'), '%Y%m%d')='20251013') b
on a.交易所 = b.交易所 and a.合约代码 = b.合约代码
where a.做多保证金率 != b.做多保证金率;