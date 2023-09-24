from django.contrib import admin
from django.contrib.auth import views as auth_views
from django.urls import path, include

from website import views as ups_views


urlpatterns = [
    path('admin/', admin.site.urls),
    path('login/', auth_views.LoginView.as_view(template_name='userlogin.html'), name='login'),
    path('logout/', auth_views.LogoutView.as_view(template_name='userlogout.html'), name='logout'),
    path('register/', ups_views.register, name='register'),

    path('profile/', ups_views.profile, name='profile'),
    path('', include('website.urls')),
    path('updatePackage/', ups_views.updatePackage, name='package-update'),
    path('calculatePostalFee/', ups_views.calculatePostalFee, name='search-postal-fee'),
    path('searchTruck/', ups_views.SearchTruck, name='search-truck')
]