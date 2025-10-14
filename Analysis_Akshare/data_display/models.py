from django.db import models


class FuturesRule(models.Model):
    # 这里仅作为模型标识，实际查询用原始SQL
    class Meta:
        db_table = "futures_rule"
        managed = False  # 完全不让Django管理表结构
