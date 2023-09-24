//package org.example.mock;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//import java.util.Scanner;
//
//import org.example.IO.WorldConnect;
//import org.example.protocol.AmazonUps;
//import org.example.protocol.WorldAmazon;
//import org.example.amzutil.SeqGenerator;
//import org.example.protocol.WorldUps;
//
//import static org.example.IO.MessageHelper.sendRequset;
//import static org.example.IO.MessageHelper.recvResponse;
//
//public class MockAmazon {
//    private String HOST;
//    private Integer PORT;
//    private InputStream in;
//    private OutputStream out;
//    private InputStream world_in;
//    private OutputStream world_out;
//    private Socket socket;
//    private Socket world_socket;
//
//    public InputStream getIn() {
//        return in;
//    }
//    public OutputStream getOut() {
//        return out;
//    }
//    public Socket getSocket() {
//        return socket;
//    }
//    public MockAmazon(int portNum, String host) throws IOException {
//        this.PORT = portNum;
//        this.HOST = host;
//        this.socket = new Socket(this.HOST, this.PORT);
//        this.in = this.socket.getInputStream();
//        this.out = this.socket.getOutputStream();
//    }
//
//    public void connect_to_world()throws IOException{
//        this.world_socket = new Socket("127.0.0.1", 23456);
//        System.out.println("Amazon connect to world, socket port:" + world_socket.getPort()+"\n");
//
//        this.world_out = world_socket.getOutputStream();
//        this.world_in = world_socket.getInputStream();
//
//        WorldAmazon.AConnect.Builder a_connect_builder = WorldAmazon.AConnect.newBuilder();
//        a_connect_builder.setWorldid(1)
//                .setIsAmazon(true);
//
//        addWareHouse(1, 3, 5, a_connect_builder);
//        addWareHouse(2, 7, 8, a_connect_builder);
//
//        WorldAmazon.AConnect a_connect = a_connect_builder.build();
//        System.out.println("UConnect from Amz to World:\n" + a_connect);
//        sendRequset(a_connect,this.world_out);
//
//        WorldAmazon.AConnected.Builder u_connected = WorldAmazon.AConnected.newBuilder();
//        recvResponse(u_connected, this.world_in);
//        System.out.println("AConnected from world to Amz:" + u_connected.getResult());
//        if (u_connected.getResult().equals("connected!")) {
//            WorldAmazon.AConnected uconnected=u_connected.build();
//            System.out.println("Connection: "+ uconnected.getResult() + "\nworldid: "+uconnected.getWorldid());
//        }else{
//            System.out.println("Do not connect to world\n");
//        }
//    }
//
//    private void addWareHouse(int w_id, int x, int y, WorldAmazon.AConnect.Builder a_connect_builder){
//        WorldAmazon.AInitWarehouse.Builder a_init_wh_builder = WorldAmazon.AInitWarehouse.newBuilder();
//        a_init_wh_builder.setId(w_id)
//                .setX(x)
//                .setY(y);
//        a_connect_builder.addInitwh(a_init_wh_builder);
//    }
//
//    public static void main(String[] args) throws IOException {
//        // Connect to UPS
//        MockAmazon mockAmazon = new MockAmazon( 12316, "localhost");
//
//        // Recv WorldID and send ack
//        AmazonUps.UAConnect.Builder ua_connect = AmazonUps.UAConnect.newBuilder();
//        recvResponse(ua_connect, mockAmazon.getIn());
//
//        AmazonUps.UAConnected.Builder connected_ack = AmazonUps.UAConnected.newBuilder();
//        connected_ack.setResult("connected");
//        sendRequset(connected_ack.build(), mockAmazon.getOut());
//        System.out.println("Has sent connected message to UPS");
//
////        System.out.println("reach here before connect");
////        mockAmazon.connect_to_world();
////        System.out.println("reach here after connect");
//
//        // Throw a new thread to receive
//        MockAmazonReceiveHandler mockAmazonRecvHandler = new MockAmazonReceiveHandler(mockAmazon.getSocket());
//        new Thread(mockAmazonRecvHandler).start();
//
//        System.out.println("Preparing to send msg to UPS");
//        while(true){
//            Scanner input = new Scanner(System.in);
//            AmazonUps.AUCommands.Builder response = chooseAction(input.nextInt());
//            System.out.println("Has chosen a msg to send");
//
//            // Send and print the response
//            sendRequset(response.build(), mockAmazon.getOut());
//            System.out.println("MockAmazon Send Message: \n" + response.build());
//        }
//
//    }
//
//    public static AmazonUps.AUCommands.Builder chooseAction(Integer input){
//        AmazonUps.AUCommands.Builder response = AmazonUps.AUCommands.newBuilder();
//        switch (input){
////            message ARequestShipment {
////                required int32 whnum = 1; // Warehouse number
////                optional int64 ups_user_id = 2; // UPS user ID for authentication purposes
////                required int32 x = 3; // Destination X coordinate
////                required int32 y = 4; // Destination Y coordinate
////                required Product products = 5; // Product information
////                required int64 package_id = 6;//ADD
////                required int64 seqnum = 7;//ADD
////            }
////            message Product{
////                required int64 id = 1; // Unique product ID
////                required string description = 2; // Product description
////                required int32 count = 3; // Product count
////            }
//            case 1:{
//                AmazonUps.ARequestShipment.Builder a_request_shipment = AmazonUps.ARequestShipment.newBuilder();
//                // Add AWareHouseLocation
//                a_request_shipment.setWhnum(1).setX(2).setY(3);
//
//                // Gen new product
//                AmazonUps.Product.Builder prodect_builder = AmazonUps.Product.newBuilder()
//                        .setDescription("Apple").setCount(2).setId(1);
//                a_request_shipment.setProducts(prodect_builder);
////                AmazonUps.Product.Builder bananas = AmazonUps.Product.newBuilder().setDescription("Banananana").setCount(1);
//
//                // Add package id
//                a_request_shipment.setPackageId(1);
//
//                // Gen seqNum
//                a_request_shipment.setSeqnum(SeqGenerator.getInstance().get_cur_id());
//                response.addRequestPackageId(a_request_shipment);
//                break;
//            }
//
//            // Create AReadForShipmentNotification
////            message AReadyForShipment {
////                required int32 truck_id = 1; // Truck ID assigned by UPS
////                required int32 wh_id = 2; // Warehouse ID assigned by Amazon
////                repeated int64 package_id = 3; // List of package IDs to be shipped
////                required int64 seqnum = 4;// ADD
////            }
//            case 2:{
//                AmazonUps.AReadyForShipment.Builder a_ready_for_ship = AmazonUps.AReadyForShipment.newBuilder();
//                a_ready_for_ship.setTruckId(305).setWhId(1);
//
//                a_ready_for_ship.addPackageId(2);
////                a_ready_for_ship.addPackageId(2);
//                a_ready_for_ship.setSeqnum(SeqGenerator.getInstance().get_cur_id());
//                response.addPackagesLoaded(a_ready_for_ship);
//                break;
//            }
//
////            // Create AShipmentStatusUpdate
////            case 3:{
////                UpsAmazon.AShipmentStatusUpdate.Builder aShipUpdate = UpsAmazon.AShipmentStatusUpdate.newBuilder();
////
////                // define arr of AUShipmentUpdate
////                aShipUpdate.addAuShipmentUpdate(UpsAmazon.AUShipmentUpdate.newBuilder().setPackageId(1).setStatus("Packed"));
////                aShipUpdate.addAuShipmentUpdate(UpsAmazon.AUShipmentUpdate.newBuilder().setPackageId(2).setStatus("Packed"));
////
////                aShipUpdate.setSeqnum(SeqNumGenerator.getInstance().getCurrent_id());
////                response.addShipmentStatusUpdate(aShipUpdate);
////                break;
////            }
//
//        }
//
//        return response;
//    }
//}
