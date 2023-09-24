package org.example.IO;

import org.example.WorldUpsCommunication.WorldReceiver;
import org.example.WorldUpsCommunication.WorldResender;
import org.example.WorldUpsCommunication.WorldSender;
import org.example.model.UpsPackage;
import org.example.model.Truck;
import org.example.protocol.WorldUps;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.example.IO.MessageHelper.sendRequset;
import static org.example.IO.MessageHelper.recvResponse;

public class WorldConnect {
    private String world_ip;
    private int world_port;
    private Socket world_socket;
    private ArrayList<WorldUps.UInitTruck> truck_list;
    private InputStream in;
    private OutputStream out;
    private boolean create_new_world=true;///
    long worldid;

    private volatile Queue<ArrayList<Object>> send_list;
    private volatile Queue<Long> recv_list;
    private Queue<ArrayList<Object>> resend_list;

    public void setAmazonconnect(AmazonConnect amazonconnect) {
        this.amazonconnect = amazonconnect;
    }

    private AmazonConnect amazonconnect;

    public Queue<ArrayList<Object>> getSend_list() {
        return send_list;
    }

    public Queue<Long> getRecv_list() {
        return recv_list;
    }
    public Queue<ArrayList<Object>> getResend_list() {
        return resend_list;
    }


    public Socket getWorld_socket() {
        return world_socket;
    }

    public WorldConnect(long world_id, boolean create_new_world) throws IOException {
//        this.world_ip ="192.168.10.2"; // ???
//        this.world_ip ="vcm-31132.vm.duke.edu";
//        this.world_ip = "rocco.colab.duke.edu";
        this.world_ip ="vcm-30633.vm.duke.edu";
//        this.world_ip ="127.0.0.1";
        this.world_port =12345;
        this.truck_list = new ArrayList<>();
        this.worldid=world_id;///
        this.create_new_world = create_new_world;

        // socket
        this.world_socket = new Socket(world_ip, world_port);
        System.out.println("UPS connect to world, socket port:" + world_socket.getPort()+"\n");

        // ups - world
        this.out = world_socket.getOutputStream();
        this.in = world_socket.getInputStream();

        // msg to be sent or resend & received msg in queue
        this.send_list= new ConcurrentLinkedQueue<>();
        this.recv_list= new ConcurrentLinkedQueue<>();
        this.resend_list= new ConcurrentLinkedQueue<>();

        // amazon communicator?
//        this.amazonconnect=amazonconnect;
    }

    public void initConnection() throws IOException {
        // init truck into db
        getTrucks(100);///////

        // build connect msg
//        uconnect.addAllTrucks(trucks);
        WorldUps.UConnect.Builder uconnect = WorldUps.UConnect.newBuilder();
        uconnect.setIsAmazon(false);
        uconnect.addAllTrucks(truck_list);
        if(!create_new_world){
            uconnect.setWorldid(worldid);
        }

        WorldUps.UConnect uconnect_msg=uconnect.build();
        System.out.println("UConnect from UPS to World:" + uconnect_msg);

        // send connect request to world ,ups->world
        sendRequset(uconnect_msg,this.out);

        // recv connect response from world ,world->ups
        WorldUps.UConnected.Builder uconnected_msg = WorldUps.UConnected.newBuilder();
        recvResponse(uconnected_msg, this.in);

        if (uconnected_msg.getResult().equals("connected!")) {
            worldid = (int) uconnected_msg.getWorldid();
            WorldUps.UConnected uconnected=uconnected_msg.build();
            System.out.println("Connection: "+ uconnected.getResult() + "\nworldid: "+uconnected.getWorldid());
        }else{
            System.out.println("Do not connect to world\n");
        }

    }


    private void getTrucks(int sum) {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        //this.truck_list
        for(int i=0;i<sum;i++){
            Truck truck=new Truck(0,0,"IDLE",true, 0);
            session.save(truck);

            int truck_id=truck.getTruckID();
            WorldUps.UInitTruck.Builder tbuilder = WorldUps.UInitTruck.newBuilder();
            tbuilder.setId(truck_id).setX(0).setY(0);
            this.truck_list.add(tbuilder.build());
        }
        session.flush();
        tx.commit();
        session.close();
    }

    public void startProcess() throws IOException {
        WorldReceiver wrecv=new WorldReceiver(this,this.world_socket,this.recv_list,this.amazonconnect);// this.AmazonConnect??
        WorldSender wsend=new WorldSender(this.world_socket,this.send_list,this.resend_list);
        WorldResender wresend=new WorldResender(this.send_list,this.recv_list,this.resend_list);
        new Thread(wrecv).start();
        new Thread(wsend).start();
        new Thread(wresend).start();
    }

    // add msg to Ups-World send_list
    public synchronized void addToSend(Object object, int msg_type){
        System.out.println("add new msg to send list");///
        ArrayList<Object> objectArr = new ArrayList<>();
        objectArr.add(msg_type);
        objectArr.add(object);
        this.send_list.add(objectArr);
        System.out.println(objectArr.toString());///
    }

    public long getWorldid(){return this.worldid;}

}
