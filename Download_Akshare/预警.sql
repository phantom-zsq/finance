-- 涨跌幅预警 --
select * from
(select 交易所,品种,代码,涨跌停板幅度 from futures_rule where 交易日='20251013') a
inner join
(select 交易所,品种,代码,涨跌停板幅度 from futures_rule where 交易日='20251010') b
on a.交易所 = b.交易所 and a.品种 = b.品种 and a.代码 = b.代码
where a.涨跌停板幅度 != b.涨跌停板幅度;

