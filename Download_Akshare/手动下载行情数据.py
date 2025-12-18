from datetime import date
import 期货数据.商品期货_大连商品交易所_excel as dlqh
import 期权数据.商品期权_大连商品交易所_excel as dlqq

if __name__ == '__main__':
    # today
    current_date = date.today()
    trade_date = current_date.strftime("%Y%m%d")
    print(trade_date)

    # 期货大连行情
    print("期货大连行情")
    dlqh.core(trade_date, "/Users/admin/Downloads/日行情_1765959471459.csv")
    # 期权大连行情
    print("期权大连行情")
    dlqq.core(trade_date, "/Users/admin/Downloads/日行情_1765959607110.csv")
