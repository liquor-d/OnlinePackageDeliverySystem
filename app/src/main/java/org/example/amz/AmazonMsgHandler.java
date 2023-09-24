package org.example.amz;

import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.io.InputStream;
import java.io.OutputStream;

import org.example.amzutil.SeqGenerator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import org.example.IO.WorldConnect;
import org.example.IO.AmazonConnect;
import org.example.protocol.AmazonUps;
import org.example.protocol.WorldUps;
import org.example.model.DbManager;
import static org.example.IO.MessageHelper.recvResponse;
//import static org.example.IO.MessageHelper.sendRequset;

public class AmazonMsgHandler implements Runnable{
    private Socket amazon_socket;
    private WorldConnect world_connect;
    private AmazonConnect amazon_connect;
    private InputStream in;
    private OutputStream out;
    private HashSet<Long> handled_list;
    private volatile Queue<Long> amz_recv_list; // recvQueue form server
//    private volatile Queue<ArrayList<Object>> send_queue; // sendQueue to server: Object and type

    public AmazonMsgHandler(Socket amazon_socket, AmazonConnect amazon_connect,
                            WorldConnect world_connect, Queue<Long> amazon_recv_queue){
        this.amazon_socket = amazon_socket;
        this.amazon_connect = amazon_connect;
        this.world_connect = world_connect;
        this.amz_recv_list = amazon_recv_queue;
        this.handled_list = new HashSet<>();

        try{
            this.in = this.amazon_socket.getInputStream();
            this.out = this.amazon_socket.getOutputStream();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        while(true){
            AmazonUps.AUCommands.Builder amazon_request = AmazonUps.AUCommands.newBuilder();
            try{
                recvResponse(amazon_request, in);
            }catch (Exception e){
                e.printStackTrace();
            }

            List<AmazonUps.AAcknowledge> a_ack_list = amazon_request.getAcknowledgeList();
            if(!a_ack_list.isEmpty()){
                for (AmazonUps.AAcknowledge a_ack : a_ack_list) {
                    System.out.println("Receive ack is:" + a_ack);
                    this.amz_recv_list.add(a_ack.getAck());
                }
            }

            List<Long> u_ack_list = new ArrayList<>();

            List<AmazonUps.ARequestShipment> a_req_ship_list = amazon_request.getRequestPackageIdList();
            List<AmazonUps.AReadyForShipment> a_ready_ship_list = amazon_request.getPackagesLoadedList();
            try {
                handleReqShipAddAck(u_ack_list, a_req_ship_list);
                handleReadyShipAddAck(u_ack_list, a_ready_ship_list);
            } catch (SQLException e){
                e.printStackTrace();
            }

//            AmazonUps.UACommands.Builder u_commands_builder = AmazonUps.UACommands.newBuilder();
            for (long ack : u_ack_list){
                AmazonUps.UAcknowledge.Builder u_ack_builder = AmazonUps.UAcknowledge.newBuilder();
                u_ack_builder.setAck(ack);
//                u_commands_builder.addAcknowledge(u_ack_builder.build());
                amazon_connect.addToAmzSend(u_ack_builder.build(), 3);
            }

//            if(!u_ack_list.isEmpty()){
////                try{
////                    sendRequset(u_commands_builder.build(), this.out);
////                } catch (Exception e){
////                    e.printStackTrace();
////                }
//                amazon_connect.addToAmzSend(u_commands_builder.build(), 3);
//            }
        }
    }

    private void handleReqShipAddAck(List<Long> u_ack_list,
                                         List<AmazonUps.ARequestShipment> a_req_ship_list) throws SQLException{
        DbManager db = new DbManager();
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        for (AmazonUps.ARequestShipment a_req_ship : a_req_ship_list){
            if(this.handled_list.contains(a_req_ship.getSeqnum())) {
                continue;
            }
            //find a truck
            int truck_id = db.findTruck(session);
            //set truck warehouseID
            db.updateTruckWhid(truck_id, a_req_ship.getWhnum(), session);

            if(truck_id == -1){
                System.out.println("Error: Can not find a truck");
                return;
            }

            //add package to DB
            int dest_x = a_req_ship.getX();
            int dest_y = a_req_ship.getY();
            String detail = "";
            for (AmazonUps.Product product : a_req_ship.getProductsList()){
                detail += product.getDescription() + ", count :"
                        + product.getCount() + "\n";
            }
            db.addPackage(dest_x, dest_y, truck_id, a_req_ship.getPackageId(),
                    Long.parseLong(a_req_ship.getUpsUserId()), detail, session);

            //go pickup command
            WorldUps.UGoPickup.Builder u_pickup_builder = WorldUps.UGoPickup.newBuilder();
            u_pickup_builder.setTruckid(truck_id)
                    .setWhid(a_req_ship.getWhnum())
                    .setSeqnum(SeqGenerator.getInstance().get_cur_id());
            world_connect.addToSend(u_pickup_builder.build(), 1);

            //add ack
            u_ack_list.add(a_req_ship.getSeqnum());

            this.handled_list.add(a_req_ship.getSeqnum());
        }
        session.close();
    }

    private void handleReadyShipAddAck(List<Long> u_ack_list,
                                       List<AmazonUps.AReadyForShipment> a_ready_ship_list) throws SQLException{
        DbManager db = new DbManager();
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        for (AmazonUps.AReadyForShipment a_ready_ship : a_ready_ship_list){
            if(this.handled_list.contains(a_ready_ship.getSeqnum())) {
                continue;
            }
            //update truck status & package status
            db.updateTruckStatus(a_ready_ship.getTruckId(), "DELIVERING", session);
            List<Long> package_id_list = a_ready_ship.getPackageIdList();
            for(long package_id : package_id_list){
                db.updatePackageStatus(package_id, "delivering", session);
            }

            //go deliver command
            WorldUps.UGoDeliver.Builder u_godeliver_builder = WorldUps.UGoDeliver.newBuilder();
            u_godeliver_builder.setTruckid(a_ready_ship.getTruckId())
                    .setSeqnum(SeqGenerator.getInstance().get_cur_id());

            for (long package_id : package_id_list){
                //search for package in the database
                WorldUps.UDeliveryLocation u_del_loc= db.getDelLoc(package_id, session);

                u_godeliver_builder.addPackages(u_del_loc);
            }

            world_connect.addToSend(u_godeliver_builder.build(), 2);

            //add ack
            u_ack_list.add(a_ready_ship.getSeqnum());
            this.handled_list.add(a_ready_ship.getSeqnum());
        }
        session.close();
    }
}
