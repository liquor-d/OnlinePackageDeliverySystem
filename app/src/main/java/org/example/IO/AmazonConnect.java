package org.example.IO;

import org.example.WorldUpsCommunication.WorldReceiver;
import org.example.WorldUpsCommunication.WorldResender;
import org.example.WorldUpsCommunication.WorldSender;
import org.example.amz.AmazonMsgHandler;
import org.example.amz.AmazonSender;
import org.example.amz.AmazonResender;
import org.example.protocol.AmazonUps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.example.IO.MessageHelper.sendRequset;
import static org.example.IO.MessageHelper.recvResponse;

public class AmazonConnect {
    private long world_id;
    private int amazon_port;
    private String amazon_host;
    private Socket amazon_socket;
//    private ServerSocket server_socket;
    private WorldConnect world_connect;
    private InputStream in;
    private OutputStream out;

    private volatile Queue<ArrayList<Object>> amz_send_list;
    private volatile Queue<Long> amz_recv_list;
    private volatile Queue<ArrayList<Object>> amz_resend_list;

    public AmazonConnect(String host, int port, long world_id, WorldConnect world_connect) throws IOException {
        this.amazon_host = host;
        this.amazon_port = port;
        this.world_id = world_id;
//        this.server_socket = new ServerSocket(port);

        this.world_connect = world_connect;

        this.amz_send_list = new ConcurrentLinkedQueue<>();
        this.amz_recv_list = new ConcurrentLinkedQueue<>();
        this.amz_resend_list = new ConcurrentLinkedQueue<>();
    }

    public void init_socket() throws IOException {
        this.amazon_socket = new Socket(this.amazon_host, this.amazon_port);
        System.out.println("UPS connect to Amazon, socket port:" + amazon_socket.getPort()+"\n");

        this.out = amazon_socket.getOutputStream();
        this.in = amazon_socket.getInputStream();
    }

    public String connect_amazon() throws IOException {
        AmazonUps.UAConnect.Builder ua_connect_builder = AmazonUps.UAConnect.newBuilder();
        ua_connect_builder.setWorldid((int)this.world_id);
        AmazonUps.UAConnect ua_connect = ua_connect_builder.build();
        sendRequset(ua_connect, out);
        System.out.println("Requesting connect to Amazon");

        AmazonUps.UAConnected.Builder ua_connected_builder = AmazonUps.UAConnected.newBuilder();
        recvResponse(ua_connected_builder, in);
        System.out.println("Receiving connection result from Amazon: " + ua_connected_builder.getResult());

        return ua_connected_builder.getResult();
    }

    public synchronized void addToAmzSend(Object object, int msg_type){
        ArrayList<Object> objectArr = new ArrayList<>();
        objectArr.add(msg_type);
        objectArr.add(object);
        this.amz_send_list.add(objectArr);
    }

    public Socket getAmzSocket(){return this.amazon_socket;};

    public void startProcess() throws IOException {
        AmazonSender amz_send=new AmazonSender(this.amazon_socket,this.amz_send_list,this.amz_resend_list);
        AmazonMsgHandler amz_mh=new AmazonMsgHandler(this.amazon_socket, this,
                this.world_connect,this.amz_recv_list);// this.AmazonConnect??
        AmazonResender amz_resend=new AmazonResender(amz_send_list, amz_recv_list, amz_resend_list);

        new Thread(amz_send).start();
        new Thread(amz_mh).start();
        new Thread(amz_resend).start();
    }

}
