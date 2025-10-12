import spot_price_qh_changed as ak

if __name__ == '__main__':
    spot_price_qh_df = ak.spot_price_qh(symbol='螺纹钢')
    print(spot_price_qh_df)