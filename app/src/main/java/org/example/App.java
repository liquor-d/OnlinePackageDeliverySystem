package org.example;

import org.example.IO.AmazonConnect;
import org.example.IO.WorldConnect;
import org.example.model.Database;
import org.example.protocol.WorldUps;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;

import static org.example.IO.MessageHelper.sendRequset;


public class App {
    private WorldConnect world_connect;
    private AmazonConnect amazon_connect;
    public static void main(String[] args) throws IOException {
        GetIPAddress();
        App app = new App();
        app.init();
    }


    private void init() throws IOException {
        // connect to the db, create table
        Database db = new Database();
        db.init();

        world_connect = new WorldConnect(1, true); //run in the 1st world
        world_connect.initConnection();
        WorldUps.UCommands.Builder worldSpeed =  WorldUps.UCommands.newBuilder().setSimspeed(500);
        sendRequset(worldSpeed.build(), world_connect.getWorld_socket().getOutputStream());

        String amazon_host = "vcm-30918.vm.duke.edu";
//        String amazon_host = "rocco.colab.duke.edu";
//        String amazon_host = "152.3.43.51";
        amazon_connect = new AmazonConnect(amazon_host, 12316,
                world_connect.getWorldid(), world_connect);
        amazon_connect.init_socket();
        amazon_connect.connect_amazon();

        world_connect.setAmazonconnect(amazon_connect);////
//        amazon_connect = new AmazonConnect(12316,1, new WorldConnect());

        amazon_connect.startProcess();
        world_connect.startProcess();

        while (true){}
    }


    public static void GetIPAddress(){
            try {
                InetAddress ipAddress = InetAddress.getLocalHost();
                System.out.println("Current IP Address: " + ipAddress.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();

        }
    }
}