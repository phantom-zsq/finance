from django.urls import path
from . import views

urlpatterns = [
    path("monitor", views.monitor, name="monitor"),  # 监控页
    path("data_list", views.data_list, name="data_list"),  # 离线数据列表页
    path("data_list_84", views.data_list_84, name="data_list_84"),  # 离线数据列表页
    path("real_time", views.real_time, name="real_time"),  # 实时数据列表页
    path("real_time_84", views.real_time_84, name="real_time_84"),  # 实时数据列表页
    path("real_time_last_day", views.real_time_last_day, name="real_time_last_day"),  # 实时数据列表页
]
