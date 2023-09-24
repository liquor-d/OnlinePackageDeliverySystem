package org.example.WorldUpsCommunication;

import org.example.protocol.WorldUps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;

public class WorldResender implements Runnable{



    // Send Queue
    private volatile Queue<ArrayList<Object>> send_list;
    // Recv Queue
    private volatile Queue<Long> recv_list;
    // Resend Queue
    private volatile Queue<ArrayList<Object>> resend_list;


    public WorldResender(Queue<ArrayList<Object>> send_list, Queue<Long> recv_list, Queue<ArrayList<Object>> resend_list) {
        this.send_list = send_list;
        this.recv_list = recv_list;
        this.resend_list = resend_list;
        }


    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000); // Sleep 5s
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(!this.resend_list.isEmpty()){
                ArrayList<Object> msg =  this.resend_list.poll();

                long seqNum=-1L; // default:
                int msg_type = (int) msg.get(0);
                if(msg_type==1){seqNum = ((WorldUps.UGoPickup) msg.get(1)).getSeqnum();}
                if(msg_type==2){seqNum = ((WorldUps.UGoDeliver) msg.get(1)).getSeqnum();}
                if(msg_type==5){seqNum = ((WorldUps.UQuery) msg.get(1)).getSeqnum();}
//                if(msg_type!=1 && msg_type!=2 && msg_type!=5){seqNum = -1L;}

                if(seqNum !=-1L){
                    if(!this.recv_list.contains(seqNum)){
                        if(!this.send_list.contains(msg)){
                            this.send_list.add(msg);
                        }
                        if(!this.resend_list.contains(msg)){
                            this.resend_list.add(msg);
                        }
                    }
                    else {
                        this.recv_list.removeAll(Collections.singleton(seqNum));
                    }
                }
            }
        }
    }
}
