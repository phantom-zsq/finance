from django.urls import path
from . import views

urlpatterns = [
    path("", views.real_time, name="real_time"),  # 实时数据列表页
    path("data_list", views.data_list, name="data_list"),  # 离线数据列表页
]
