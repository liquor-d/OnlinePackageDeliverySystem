package org.example.WorldUpsCommunication;

import org.example.IO.AmazonConnect;
import org.example.amzutil.SeqGenerator;
import org.example.IO.WorldConnect;
import org.example.model.DbManager;
import org.example.protocol.AmazonUps;
import org.example.protocol.WorldUps;
import org.example.model.Truck;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import static org.example.IO.MessageHelper.recvResponse;
import static org.example.IO.MessageHelper.sendRequset;

public class WorldReceiver implements Runnable{

    private WorldConnect worldconnect;
    private Socket socket;
    private volatile Queue<Long> recv_list;

    private InputStream in;
    private OutputStream out;

    private ArrayList<Long> ack_list = new ArrayList<>();

    private HashSet<Long> seqnum_list;//already processed

    private AmazonConnect amazonconnect;

    public WorldReceiver(WorldConnect worldConnect, Socket worldSocket, Queue<Long> recvList,AmazonConnect amazonconnect) throws IOException {
        this.worldconnect=worldConnect;
        this.socket=worldSocket;
        this.recv_list=recvList;
        this.out = this.socket.getOutputStream();
        this.in = this.socket.getInputStream();
        this.amazonconnect=amazonconnect;

        this.seqnum_list = new HashSet<>();
        // amazon connect?
    }

    @Override
    public void run() {
        while(true) {
            // recv response
            WorldUps.UResponses.Builder uresponses=WorldUps.UResponses.newBuilder();
            try {
                System.out.println("try to recv from world:");
                recvResponse(uresponses,this.in);
                System.out.println("received response from world: " + uresponses.build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // add acks(uResponses) to recv_list
            List<Long> acks= uresponses.getAcksList();
            if(acks.isEmpty()==false){
                System.out.println(uresponses+" - add acks(uResponses) to recv_list");
                recv_list.addAll(acks);
            }
            System.out.println("uresponses:\n"+uresponses);

            // handle UPS response details:
            List<WorldUps.UFinished> completions_list = uresponses.getCompletionsList();
            List<WorldUps.UDeliveryMade> delivered_list = uresponses.getDeliveredList();
            List<WorldUps.UTruck> truck_status_list = uresponses.getTruckstatusList();
            List<WorldUps.UErr> error_list = uresponses.getErrorList();
            // completions UFinished
            recvCompletions(completions_list);
            // delivered UDeliveryMade
            recvDelivered(delivered_list);
            // truck status UTruck
            recvTruckStatus(truck_status_list);
            // error
            recvError(error_list);

            // return ack
            try {
                System.out.println("enter return Acks");
                returnAcks();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void recvError(List<WorldUps.UErr> errorList) {
        for (WorldUps.UErr uerr : errorList) {
            System.out.println(uerr.getErr());
            if(this.seqnum_list.contains(uerr.getSeqnum())){
                continue;
            }
            System.out.println(uerr.getErr());

            this.ack_list.add(uerr.getSeqnum());
            this.seqnum_list.add(uerr.getSeqnum());
        }
    }

    private void recvTruckStatus(List<WorldUps.UTruck> truckStatusList) {
        if(truckStatusList == null || truckStatusList.size() == 0){
            System.out.println("Do not have any TruckStatus");
            return;
        }
        for (WorldUps.UTruck utruck : truckStatusList) {
            // If that seqNum has been handled before, continue
            if(this.seqnum_list.contains(utruck.getSeqnum())) {
                continue;
            }

            SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            String sql_update = "UPDATE Truck SET x= :locx, y= :locy, status= :s WHERE truckID = :id";
            Query query = session.createQuery(sql_update);
            query.setParameter("locx", utruck.getX());
            query.setParameter("locy", utruck.getY());
            query.setParameter("s", utruck.getStatus());
            query.setParameter("id", utruck.getTruckid());
            int result = query.executeUpdate();

            this.ack_list.add(utruck.getSeqnum());
            this.seqnum_list.add(utruck.getSeqnum());
            session.flush();
            tx.commit();
            session.close();
        }
    }

    private String getCurrentTime(){
        SimpleDateFormat time_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp current_time = new Timestamp(System.currentTimeMillis());
        String time_str = time_format.format(current_time);
        return time_str;
    }

    private void recvDelivered(List<WorldUps.UDeliveryMade> deliveredList) {
        if(deliveredList == null || deliveredList.size() == 0){
            System.out.println("Do not have any Delivered");
            return;
        }
        for (WorldUps.UDeliveryMade udeliverymade : deliveredList) {

            if(this.seqnum_list.contains(udeliverymade.getSeqnum())){
                continue;
            }
            // Update package status and UpdateTime
            SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            //UpsPackage??
            String sql_update = "UPDATE UpsPackage SET status = :s, updateTime = :t WHERE amzPackageID = :id";
            Query query = session.createQuery(sql_update);
            query.setParameter("s", "delivered");// \' ???
            query.setParameter("t", getCurrentTime());
            query.setParameter("id", udeliverymade.getPackageid());
            int result = query.executeUpdate();


            session.flush();
            tx.commit();
            session.close();

            // Send UDelivered to Amazon
//            message UDelivered {
//                required int64 package_id = 1; // Package ID of the delivered package
//                required int64 seqnum = 2; // ADD
//            }
            AmazonUps.UDelivered.Builder udelivered = AmazonUps.UDelivered.newBuilder();
            udelivered.setPackageId(udeliverymade.getPackageid());
            udelivered.setSeqnum(SeqGenerator.getInstance().get_cur_id());
            amazonconnect.addToAmzSend(udelivered.build(),2);

            this.ack_list.add(udeliverymade.getSeqnum());
            this.seqnum_list.add(udeliverymade.getSeqnum());
        }

    }

    private void recvCompletions(List<WorldUps.UFinished> completionsList) {
        if(completionsList == null || completionsList.size() == 0){
            System.out.println("Do not have any Completions");
            return;
        }
        System.out.println("recv UFinished from world, length is: " +completionsList.size() + completionsList);
//  completion tells you the current location of the truck:
//  (a) a truck reaches the warehouse you sent it to (with a pickup command) and is ready to load a package
//          change truck's status to arrive warehouse , set x y
//  (b) a truck has finished all of its deliveries (that you sent it to make with a deliveries command).
//          change truck's status to idle

        for (WorldUps.UFinished ufinished : completionsList) {
            System.out.println("Here is a UFinished "+ufinished);
            if(this.seqnum_list.contains(ufinished.getSeqnum())){
                continue;
            }
            // (a)
            SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            String sql_update = "UPDATE Truck SET x= :locx, y= :locy, status= :s WHERE truckID = :id";
            Query query = session.createQuery(sql_update);
            query.setParameter("locx", ufinished.getX());
            query.setParameter("locy", ufinished.getY());
            query.setParameter("s", ufinished.getStatus());
            query.setParameter("id", ufinished.getTruckid());
            int result = query.executeUpdate();
            System.out.println("Query: "+query.toString());

            if(ufinished.getStatus().equals("ARRIVE WAREHOUSE")){
                System.out.println("ARRIVE WAREHOUSE");
//                message UTruckArrived {
//                    required int32 truck_id = 1; // Truck ID assigned by UPS
//                    required int32 wh_id = 2; // Warehouse ID assigned by Amazon
//                    required int64 seqnum = 3; //ADD
//                }

                AmazonUps.UTruckArrived.Builder utruckarrived = AmazonUps.UTruckArrived.newBuilder();
                utruckarrived.setTruckId(ufinished.getTruckid());
                utruckarrived.setSeqnum(SeqGenerator.getInstance().get_cur_id());
                int wh_id=getWhid(ufinished.getTruckid());
                utruckarrived.setWhId(wh_id);

//                utruckarrived.setPackageId(dm.findPackage(ufinished.getTruckid(), session));
                String sql_order2="SELECT amzPackageID FROM UpsPackage WHERE truckID = :t";
                Query query_p = session.createQuery(sql_order2);
                query_p.setParameter("t", ufinished.getTruckid());
                List<Long> query_order0 = query_p.list();
                long pid=0;
                if (!query_order0.isEmpty()) {
                    pid = query_order0.get(0);
                }
                utruckarrived.setPackageId(pid);


//                dm.updateTruckStatus(ufinished.getTruckid(), "ARRIVEWH", session);
                String sql_updatetruck = "UPDATE Truck SET status= :s WHERE truckID = :id";
                Query queryt = session.createQuery(sql_updatetruck);
                queryt.setParameter("s", "ARRIVEWH");
                queryt.setParameter("id", ufinished.getTruckid());
                int result1 = queryt.executeUpdate();

//                dm.updatePackageStatus(dm.findPackage(ufinished.getTruckid(), session), "loading", session);
                String sql_updatepackage = "UPDATE UpsPackage SET status = :s WHERE truckID = :id";
                Query queryp = session.createQuery(sql_updatepackage);
                queryp.setParameter("s", "loading");
                queryp.setParameter("id", ufinished.getTruckid());
                int result2 = queryp.executeUpdate();


                amazonconnect.addToAmzSend(utruckarrived.build(),1);

            }
            if(ufinished.getStatus().equals("IDLE")){
                String sql_update2 = "UPDATE Truck SET available = :bo WHERE truckID = :id";
                Query query2 = session.createQuery(sql_update2);
                query2.setParameter("bo", false);
                query2.setParameter("id", ufinished.getTruckid());
                int result2 = query2.executeUpdate();
            }

            session.flush();
            tx.commit();
            this.ack_list.add(ufinished.getSeqnum());
            this.seqnum_list.add(ufinished.getSeqnum());
            System.out.println(this.seqnum_list);
            session.close();
        }
    }

    private void returnAcks() throws IOException {
        System.out.println("return Acks");
        WorldUps.UCommands.Builder ucommands = WorldUps.UCommands.newBuilder();
        ucommands.addAllAcks(ack_list);
        if(!ack_list.isEmpty()){
            sendRequset(ucommands.build(), this.out);
        }
    }

    private int getWhid(int truck_id){

        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        String sql_order="SELECT wh_id FROM Truck WHERE truckID = :t";
        Query query1 = session.createQuery(sql_order);
        query1.setParameter("t", truck_id);
        List<Integer> query_order = query1.list();
        int whid=0;

        if (!query_order.isEmpty()) {
            whid = query_order.get(0);
        }

        session.flush();
        tx.commit();
        session.close();

        System.out.println("wh_id: "+whid);
        return whid;
    }
}
