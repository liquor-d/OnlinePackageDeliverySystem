//import org.example.IO.AmazonConnect;
//import org.example.IO.WorldConnect;
//import org.example.WorldUpsCommunication.WorldReceiver;
//import org.example.WorldUpsCommunication.WorldSender;
//import org.example.amz.AmazonMsgHandler;
//import org.example.model.Database;
//import org.example.protocol.WorldUps;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//import java.io.IOException;
//
//public class AmazonUpsTest {
//    public static void main(String[] args) throws IOException {
//        Database db = new Database();
//        db.init();
//
//        WorldConnect world_connect = new WorldConnect(2, false); //run in the 1st world
//        world_connect.initConnection();
//
//        WorldUps.UGoPickup.Builder u_pickup_builder = WorldUps.UGoPickup.newBuilder();
//        u_pickup_builder.setWhid(1).setTruckid(2).setSeqnum(2);
//        world_connect.addToSend(u_pickup_builder, 1);
//
//        WorldSender world_sender = new WorldSender(world_connect.getWorld_socket(),
//                world_connect.getSend_list(), world_connect.getResend_list());
//        new Thread(world_sender).start();
//
//        WorldReceiver worldReceiver = new WorldReceiver(world_connect, world_connect.getWorld_socket(),
//                world_connect.getRecv_list(), new AmazonConnect(12316, 2, world_connect));
//    }
//}
