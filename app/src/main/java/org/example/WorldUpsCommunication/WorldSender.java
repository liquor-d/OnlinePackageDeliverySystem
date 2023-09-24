package org.example.WorldUpsCommunication;

import org.example.protocol.WorldUps;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

import static org.example.IO.MessageHelper.sendRequset;

public class WorldSender implements Runnable{

    private Socket socket;
    private volatile Queue<ArrayList<Object>> send_list;
    // Resend Queue
    private volatile Queue<ArrayList<Object>> resend_list;

    private OutputStream output_stream;
    public WorldSender(Socket s, Queue<ArrayList<Object>> send_list, Queue<ArrayList<Object>> resend_list) throws IOException {
        this.socket = s;
        this.send_list = send_list;
        this.resend_list = resend_list;
        this.output_stream = this.socket.getOutputStream();
    }

    private void sendMsg() throws IOException {
        // get the first msg from send_list
        ArrayList<Object> msg_info =  this.send_list.poll();
        Integer msg_type = (Integer) msg_info.get(0);
        Object msg = msg_info.get(1);

        // build the msg
        WorldUps.UCommands.Builder ucommand = WorldUps.UCommands.newBuilder();
        if(msg_type==1){ucommand.addPickups((WorldUps.UGoPickup) msg);}
        if(msg_type==2){ucommand.addDeliveries((WorldUps.UGoDeliver) msg);}
        if(msg_type==3){ucommand.setSimspeed((Integer) msg);}
        if(msg_type==4){ucommand.setDisconnect((Boolean) msg);}
        if(msg_type==5){ucommand.addQueries((WorldUps.UQuery) msg);}

        // send msg
        OutputStream out = this.output_stream;


//        long startTime = System.currentTimeMillis();
//        long maxRuntime = 10; // maximum runtime in milliseconds
        sendRequset(ucommand.build(), out); // time out???
//        if (System.currentTimeMillis() - startTime > maxRuntime) {
//            System.out.println("Function runtime exceeded " + maxRuntime + " milliseconds");
//        }
//
        System.out.println("Ups send Request to World: " + ucommand.build()+"\n");

       // add to resend list
        if(this.resend_list.contains(msg_info)==false && msg_type != 3 && msg_type != 4 ) {
                this.resend_list.add(msg_info);
        }
    }

    @Override
    public void run() {
        while(true){
//            System.out.println("Enter WorldSender:\n");
            // if there is msg waiting to be sent
            if(send_list.isEmpty()==false){
                try {
                    System.out.println("Enter SendMsg:\n");
                    sendMsg();
                    System.out.println("Already send\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
