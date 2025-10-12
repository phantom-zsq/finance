import requests
from urllib3.exceptions import InsecureRequestWarning, SSLError

try:
    response = requests.get('https://www.99qh.com', verify=False)
    response.raise_for_status()
except SSLError as e:
    print(f"遇到SSL错误: {e}")
except InsecureRequestWarning:
    print("存在不安全的请求警告，可能是证书验证问题。")
except requests.HTTPError as http_err:
    print(f"发生HTTP错误: {http_err}")
else:
    print("请求成功，证书验证通过。")
    # 查看证书信息（如果请求成功）
    cert = response.raw.getpeercert()
    if cert:
        print("证书信息:")
        for key, value in cert.items():
            print(f" {key}: {value}")