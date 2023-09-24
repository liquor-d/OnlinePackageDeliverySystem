package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "truck")
public class Truck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "truckID")
    private int truckID;

    @Column(name = "x")
    private int x;
    @Column(name = "y")
    private int y;

    @Column(name = "wh_id")
    private int wh_id;

    @Column(name = "status")
    private String status;

    @Column(name = "available")
    private boolean available;
    public Truck() {
    }

    public Truck(int x, int y, String status,boolean a ,int wh_id) {
        this.x = x;
        this.y = y;
        this.status=status;
        this.available=a;
        this.wh_id=wh_id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getTruckID() {
        return truckID;
    }


    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWh_id() {
        return wh_id;
    }

    public void setWh_id(int wh_id) {
        this.wh_id = wh_id;
    }
}
