from datetime import date
import 期货数据.历史行情数据 as qhhq
import 期权数据.商品期权_上海期货交易所 as shqq
import 期权数据.商品期权_广州期货交易所 as gzqq
import 期权数据.商品期权_郑州商品交易所 as zzqq

if __name__ == '__main__':
    # today
    current_date = date.today()
    trade_date = current_date.strftime("%Y%m%d")
    print(trade_date)

    # 期货行情
    print("期货历史行情数据")
    qhhq.core(trade_date)
    # 上海期权行情
    print("上海期权行情")
    shqq.core(trade_date)
    # 广州期权行情
    print("广州期权行情")
    gzqq.core(trade_date)
    # 郑州期权行情
    print("郑州期权行情")
    zzqq.core(trade_date)
