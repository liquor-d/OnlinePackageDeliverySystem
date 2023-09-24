package org.example.amz;

import org.example.protocol.AmazonUps;
import org.example.protocol.WorldUps;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

import java.io.OutputStream;
import java.io.IOException;

import static org.example.IO.MessageHelper.sendRequset;

public class AmazonSender implements Runnable {
    private Socket amz_socket;
    private OutputStream out;

    // Send Queue
    private volatile Queue<ArrayList<Object>> amz_send_list;
    // Resend Queue
    private volatile Queue<ArrayList<Object>> amz_resend_list;

    public AmazonSender(Socket amz_socket, Queue<ArrayList<Object>> amz_send_list,
                        Queue<ArrayList<Object>> amz_resend_list){
        this.amz_socket = amz_socket;
        this.amz_send_list = amz_send_list;
        this.amz_resend_list = amz_resend_list;
        try {
            this.out = this.amz_socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        while(true){
            // if there is msg waiting to be sent
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(amz_send_list== null){
//                System.out.println("Waiting for message to send");
                return;
            }else{
                if(amz_send_list.isEmpty()==false){
                    try {
                        sendMsg();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void sendMsg() throws IOException {
        // get the first msg from send_list
        ArrayList<Object> msg_info =  this.amz_send_list.poll();
        Integer msg_type = (Integer) msg_info.get(0);
        Object msg = msg_info.get(1);

        // build the msg
        AmazonUps.UACommands.Builder ua_commands = AmazonUps.UACommands.newBuilder();
        if(msg_type==1){ua_commands.addTruckArrival((AmazonUps.UTruckArrived) msg);}
        if(msg_type==2){ua_commands.addPackageDeliver((AmazonUps.UDelivered) msg);}
        if(msg_type==3){ua_commands.addAcknowledge((AmazonUps.UAcknowledge) msg);}

        // send msg
        OutputStream out = this.out;

//        long startTime = System.currentTimeMillis();
//        long maxRuntime = 10; // maximum runtime in milliseconds
        sendRequset(ua_commands.build(), out); // time out???
//        if (System.currentTimeMillis() - startTime > maxRuntime) {
//            System.out.println("Function runtime exceeded " + maxRuntime + " milliseconds");
//        }
//
        System.out.println("Ups send Request to Amz: " + ua_commands.build()+"\n");

        // add to resend list
        if(this.amz_resend_list.contains(msg_info)==false) {
            this.amz_resend_list.add(msg_info);
            System.out.println("add this message to resend_list: " + ua_commands.build()+"\n");
            System.out.println("resend queue length: " + amz_resend_list.size()+"\n");
        }
    }

}
