package org.example.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "package")
public class UpsPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packageID")
    private int packageID;

    @Column(name = "amzPackageID")
    private long amzPackageID;

    @Column(name = "userID")
    private long userID;

    @Column(name = "truckID")
    private int truckID;

    @Column(name = "status")
    private String status;

    @Column(name = "updateTime")
    private String updateTime;

    @Column(name = "detail") // the details of the package (e.g., items inside it)
    private String detail;

//    @Column(name = "address")
//    private String address;
    @Column(name = "X")
    private int x;
    @Column(name = "Y")
    private int y;

    public UpsPackage(){}

    public UpsPackage(int X, int Y, int truckID, long amzPackageID, long user_id, String detail) {
        this.x = X;
        this.y = Y;
        this.truckID = truckID;
        this.amzPackageID = amzPackageID;
        this.userID = user_id;
        this.detail = detail;

        this.status = "packed";
        setTimeNow();
    }

    // set the latest update time to current time
    public void setTimeNow() {
        SimpleDateFormat time_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp current_time = new Timestamp(System.currentTimeMillis());
        String time_str = time_format.format(current_time);

        this.updateTime = time_str;
    }

//    public Package(int userID, String status, String updateTime, String detail, String address) {
//        this.userID = userID;
//        this.status = status;
//        this.updateTime=updateTime;
//        this.detail=detail;
//        this.address=address;
//        setTimeNow();
//    }


    public int getPackageID() {
        return packageID;
    }


    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public int getTruckID() {
        return truckID;
    }

    public void setTruckID(int userID) {
        this.truckID = truckID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setYD(int y) {
        this.y = y;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
}

