package org.example.amz;

import org.example.protocol.AmazonUps;
import org.hibernate.sql.ast.tree.expression.Over;

import java.util.ArrayList;
import java.util.Queue;

public class AmazonResender implements Runnable{
    private volatile Queue<ArrayList<Object>> amz_send_list;
    // Recv Queue
    private volatile Queue<Long> amz_recv_list;
    // Resend Queue
    private volatile Queue<ArrayList<Object>> amz_resend_list;

    public AmazonResender(){}

    public AmazonResender(Queue<ArrayList<Object>> amz_send_list,
                          Queue<Long> amz_recv_list, Queue<ArrayList<Object>> amz_resend_list){
        this.amz_send_list = amz_send_list;
        this.amz_recv_list = amz_recv_list;
        this.amz_resend_list = amz_resend_list;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(this.amz_resend_list == null){
                return;
            }else{
                if(this.amz_resend_list.isEmpty() == false){
                    ArrayList<Object> msg = this.amz_resend_list.poll();

                    if(msg.get(0) != (Integer)3){
                        long seq_num = get_seq_from_obj(msg);
                        handleResend(msg, seq_num);
                    }

                }
            }
        }
    }

    private Long get_seq_from_obj(ArrayList<Object> msg){
        Integer type = (Integer) msg.get(0);
        long seq_num;
        if(type == 1){
            seq_num = ((AmazonUps.UTruckArrived) msg.get(1)).getSeqnum();
        }
        else if(type == 2){
            seq_num = ((AmazonUps.UDelivered) msg.get(1)).getSeqnum();
        }
        else{
            seq_num = -1L;
        }
        return seq_num;
    }

    private void handleResend(ArrayList<Object> msg, long seq_num){
        if(this.amz_recv_list.contains(seq_num) == true){
            boolean flag = true;
            while(flag){
                flag = this.amz_recv_list.remove(seq_num);
            }
        }else{
            if(this.amz_send_list.contains(msg) == false){
                this.amz_send_list.add(msg);
                System.out.println("send queue length: " + amz_send_list.size()+"\n");
            }
            if(this.amz_resend_list.contains(msg) == false){
                this.amz_resend_list.add(msg);
                System.out.println("Add to amz resend queue: " + msg +"\n");
                System.out.println("resend queue length: " + amz_resend_list.size()+"\n");
            }
        }
    }
}
