package org.example.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

import org.example.protocol.AmazonUps;

import static org.example.IO.MessageHelper.sendRequset;
import static org.example.IO.MessageHelper.recvResponse;

public class MockAmazonReceiveHandler implements Runnable {
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public MockAmazonReceiveHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = this.socket.getInputStream();
        this.out = this.socket.getOutputStream();
    }

    @Override
    public void run() {
        while(true){
            // Build a new RecvResponse to recv
            System.out.println("Starting Amazon receive handler");
            AmazonUps.UACommands.Builder u_request = AmazonUps.UACommands.newBuilder();
            try{
                recvResponse(u_request, this.in);
            } catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("Recv form UPS: " + u_request);


            // Response ACK
            ArrayList<Long> responseACKList = new ArrayList<>();
            handleMsgAndSendACKs(u_request, responseACKList);


            // Send back all ACKs to UPS
            AmazonUps.AUCommands.Builder u_commands = AmazonUps.AUCommands.newBuilder();
            for (long a_ack : responseACKList){
                AmazonUps.AAcknowledge.Builder a_ack_builder = AmazonUps.AAcknowledge.newBuilder();
                a_ack_builder.setAck(a_ack);
                u_commands.addAcknowledge(a_ack_builder);
            }

            // If we have ACKs to send
            if(!responseACKList.isEmpty()){
                try{
                    sendRequset(u_commands.build(), this.out);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("Response ACK " + u_commands.build());
            }
        }
    }

    private void handleMsgAndSendACKs(AmazonUps.UACommands.Builder u_request, ArrayList<Long> responseACKList) {
        List<AmazonUps.UTruckArrived> truckArrivedNotificationList = u_request.getTruckArrivalList();
        List<AmazonUps.UDelivered> DeliveredNotificationList = u_request.getPackageDeliverList();

        // Handle UShippingResponse
        for (AmazonUps.UTruckArrived u_truck_arrived : truckArrivedNotificationList) {
            responseACKList.add(u_truck_arrived.getSeqnum());
        }

        for (AmazonUps.UDelivered u_delivered : DeliveredNotificationList) {
            responseACKList.add(u_delivered.getSeqnum());
        }

    }


}
