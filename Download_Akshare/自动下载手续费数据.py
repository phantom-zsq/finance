from datetime import date
import 期货数据.交易日历数据表 as zdt
import 期货数据.期货交易费用参照表 as bzz
import 期权数据.手续费 as sxf

if __name__ == '__main__':
    # today
    current_date = date.today()
    trade_date = current_date.strftime("%Y%m%d")
    print(trade_date)

    # 期货涨跌停
    print("期货涨跌停")
    zdt.core(trade_date)
    # 期货保证金
    print("期货保证金")
    bzz.core(trade_date)
    # 期权手续费
    print("期权手续费")
    sxf.core(trade_date)
