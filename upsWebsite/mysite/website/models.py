from django.db import models

# Create Truck
class Truck(models.Model):
    truckid = models.AutoField(primary_key=True)
    x = models.IntegerField()
    y = models.IntegerField()
    wh_id = models.IntegerField(default=0)
    # status = models.CharField(max_length=255)
    available = models.BooleanField(default=True)
    status_choice = [
        ('IDLE', 'Idle'),
        ('TRAVELING', 'Traveling'),
        ('ARRIVEWH', 'ArriveWarehouse'),
        ('LOADING', 'Loading'),
        ('DELIVERING', 'Delivering'),
    ]
    status = models.CharField(max_length=100, default="IDLE", choices=status_choice)

    class Meta:
        db_table = 'truck'


# Create UpsPackage
class UpsPackage(models.Model):
    packageid = models.AutoField(primary_key=True)
    amzpackageid = models.BigIntegerField(default=0)
    truckid = models.IntegerField(default=0)
    userid = models.IntegerField(default=0)
    updatetime = models.CharField(max_length=100)
    detail = models.CharField(max_length=1000,default="")
    x = models.IntegerField()
    y = models.IntegerField()

    status_choice = [('packed','packed'), ('loading','loading'), ('loaded','loaded'), ('delivering','delivering'), ('delivered','delivered')]
    status = models.CharField(max_length=100, default='packed', choices=status_choice)

    class Meta:
        db_table = 'package'
