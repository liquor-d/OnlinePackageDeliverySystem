# Create your views here.
import math

from django.db.models import Q
from .forms import SearchPackageForm, SearchPostalFeeForm, SearchTruckForm, UpdatePackageForm, UserUpdateForm
from .models import UpsPackage, Truck
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm
from django.contrib import messages

def register(request):
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            form.save()
            username = form.cleaned_data.get('username')
            messages.success(request, f'Account created for {username}!')
            return redirect('login')
    else:
        form = UserCreationForm()
    return render(request, 'userregister.html', {'form': form})


def home(request):
    return render(request, 'upshome.html')


def packageSearch(request): # done
    data = []
    form = SearchPackageForm(request.POST)
    if request.method == 'POST':
        if form.is_valid():
            package_ID = form.cleaned_data.get('packageid')
            data = UpsPackage.objects.filter(Q(packageid=package_ID))
            print(data)
            if len(data) == 0:
                messages.warning(request, f'Package not found!')

    return render(request, 'upspackageSearch.html', {'form': form, "data": data})


def calculatePostalFee(request): # done
    data = []
    timeEst = []
    form = SearchPostalFeeForm(request.POST)
    if request.method == 'POST':
        if form.is_valid():
            x = form.cleaned_data.get('destX')
            y = form.cleaned_data.get('destY')

            initPrice = 2
            distance = int(math.sqrt(x*x + y*y))
            data = distance * initPrice

            if data < 20:
                timeEst = 1
            else:
                timeEst = 2

    return render(request, 'upspackagePrice.html', {'form': form, "data": data, "timeEst": timeEst})


def SearchTruck(request):
    data = []
    form = SearchTruckForm(request.POST)
    if request.method == 'POST':
        if form.is_valid():
            truck_ID = form.cleaned_data.get('truckid')
            data = Truck.objects.filter(Q(truckid=truck_ID))

    return render(request, 'upstruckSearch.html', {'form': form, "data": data})



# user views:

@login_required
def profile(request):
    packages = UpsPackage.objects.filter(Q(userid=request.user.id))

    context = {
        'packages': packages
    }

    return render(request, 'userprofile.html', context)


@login_required
def updatePackage(request):
    data = []
    form = UpdatePackageForm(request.POST)
    if request.method == 'POST':
        if form.is_valid():
            package_ID = form.cleaned_data.get('packageid')
            x = form.cleaned_data.get('x')
            y = form.cleaned_data.get('y')
            data = UpsPackage.objects.filter(Q(packageid=package_ID) & Q(userid=request.user.id))
            if len(data) == 1:
                if data[0].status != 'delivered' and data[0].status != 'delivering':
                    package = data[0]
                    package.x = x
                    package.y = y
                    package.save()
                    messages.success(request, f'Update Address of Package Successfully!')
                else:
                    messages.warning(request, f'Update Address of Package Failed! you cannot change others package!')

    return render(request, 'userpackageUpdate.html', {'form': form, "data": data})