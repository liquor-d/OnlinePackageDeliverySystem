
import org.example.App;
import org.example.IO.AmazonConnect;
import org.example.IO.WorldConnect;
import org.example.model.Database;
import org.example.protocol.WorldUps;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;

import static org.example.IO.MessageHelper.sendRequset;
import static org.junit.jupiter.api.Assertions.*;

class WorldRecvHandlerTest {

        @Test
        void sendMsg() throws InterruptedException, IOException {
            // Init World and Connect to World

            Database db = new Database();
            db.init();

            WorldConnect world_connect;
            AmazonConnect amazonconnect;
            world_connect = new WorldConnect(1, false);
            world_connect.initConnection();

            WorldUps.UCommands.Builder worldSpeed =  WorldUps.UCommands.newBuilder().setSimspeed(9999);
            sendRequset(worldSpeed.build(), world_connect.getWorld_socket().getOutputStream());

            WorldUps.UGoPickup.Builder uGoPickup = WorldUps.UGoPickup.newBuilder();
            uGoPickup.setTruckid(2);
            uGoPickup.setWhid(1);
            uGoPickup.setSeqnum(3);
//
//            WorldUps.UGoPickup.Builder uGoPickup2 = WorldUps.UGoPickup.newBuilder();
//            uGoPickup2.setTruckid(2);
//            uGoPickup2.setWhid(1);
//            uGoPickup2.setSeqnum(4);

            world_connect.addToSend(uGoPickup.build(), 1);
//            world_connect.addToSend(uGoPickup2.build(), 1);

            world_connect.startProcess();

            while (true){
                Queue<ArrayList<Object>> send_list =  world_connect.getSend_list();
                Queue<ArrayList<Object>>  resend_list =  world_connect.getResend_list();
                Queue<Long>  recv_list =  world_connect.getRecv_list();

                System.out.println("----------------\nSEND list:");
                for(Object item : send_list){
                    System.out.println(item.toString());
                }

                System.out.println("\nRECV list：");
                for(Object item : recv_list){
                    System.out.println(item.toString());
                }

                System.out.println("\nRESEND list:");
                for(Object item : resend_list){
                    System.out.println(item.toString());
                }
                System.out.println("----------------");
                Thread.sleep(3000);
            }
    }
}