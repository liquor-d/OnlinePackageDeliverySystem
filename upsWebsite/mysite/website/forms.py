from django.contrib.auth.models import User
from .models import UpsPackage
from django import forms

class UserUpdateForm(forms.ModelForm):
    email = forms.EmailField()

    class Meta:
        model = User
        fields = ['username', 'email']


# User Update Specific Package with ID
class UpdatePackageForm(forms.ModelForm):
    packageid = forms.IntegerField(min_value=0)
    x = forms.IntegerField(min_value=0)
    y = forms.IntegerField(min_value=0)
    class Meta:
        model = UpsPackage
        fields = [ 'x', 'y']

class SearchPackageForm(forms.Form):
    packageid = forms.IntegerField(min_value=0)

class SearchPostalFeeForm(forms.Form):
    destX = forms.IntegerField(min_value=0)
    destY = forms.IntegerField(min_value=0)

class SearchTruckForm(forms.Form):
    truckid = forms.IntegerField(min_value=0)
