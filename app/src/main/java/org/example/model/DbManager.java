package org.example.model;

import org.hibernate.Session;
import org.hibernate.*;
import org.hibernate.Transaction;
import org.example.model.UpsPackage;
import org.example.protocol.WorldUps;
import org.hibernate.query.Query;

import java.util.List;

//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Root;

public class DbManager {
    public DbManager(){}

    public void addPackage(int x, int y, int truckId, long amzPackageID, long userID, String detail, Session session){
        Transaction tx = session.beginTransaction();
        UpsPackage p= new UpsPackage(x, y, truckId, amzPackageID, userID, detail);
        session.save(p);
        session.flush();
        tx.commit();
    }

    public int findTruck(Session session){
//        Transaction t = session.beginTransaction();
        String sql_find_truck="SELECT truckID FROM Truck WHERE status = :a";
        Query query1 = session.createQuery(sql_find_truck);
        query1.setParameter("a", "IDLE");
        List<Integer> query_truck_id = query1.list();
//        System.out.println("Here truck list length is :" + query_truck_id.size());

        int truck_id;
        if(!query_truck_id.isEmpty()){
            truck_id = query_truck_id.get(0);
        }else{
            truck_id = -1;
        }
        return truck_id;
//        session.flush();
//        t.commit();
    }

    public long findPackage(int truck_id, Session session){
//        Transaction t = session.beginTransaction();
        String sql_find_package="SELECT amzPackageID FROM UpsPackage WHERE truckID = :a";
        Query query1 = session.createQuery(sql_find_package);
        query1.setParameter("a", truck_id);
        List<Long> query_package_id = query1.list();
//        System.out.println("Here truck list length is :" + query_truck_id.size());

        long package_id;
        if(!query_package_id.isEmpty()){
            package_id = query_package_id.get(0);
        }else{
            package_id = -1;
        }
        return package_id;
//        session.flush();
//        t.commit();
    }

    public void updateTruckStatus(int truck_id, String status, Session session){
        Transaction t = session.beginTransaction();
        String sql_update = "UPDATE Truck SET status= :s WHERE truckID = :id";
        Query query = session.createQuery(sql_update);
        query.setParameter("s", status);
        query.setParameter("id", truck_id);
        int result = query.executeUpdate();

        session.flush();
        t.commit();
    }

    public void updateTruckWhid(int truck_id, int wh_id, Session session){
        Transaction t = session.beginTransaction();
        String sql_update = "UPDATE Truck SET wh_id= :s WHERE truckID = :id";
        Query query = session.createQuery(sql_update);
        query.setParameter("s", wh_id);
        query.setParameter("id", truck_id);
        int result = query.executeUpdate();

        session.flush();
        t.commit();
    }

    public void updatePackageStatus(long package_id, String status, Session session){
        Transaction t = session.beginTransaction();
        String sql_update = "UPDATE UpsPackage SET status= :s WHERE amzPackageID = :id";
        Query query = session.createQuery(sql_update);
        query.setParameter("s", status);
        query.setParameter("id", package_id);
        int result = query.executeUpdate();

        session.flush();
        t.commit();
    }

    public WorldUps.UDeliveryLocation getDelLoc(long package_id, Session session){
//        String sql_find_package="SELECT * FROM UpsPackage WHERE amzPackageID = :a";
//        Query query1 = session.createQuery(sql_find_package);
//        query1.setParameter("a", package_id);
//        List<UpsPackage> query_package = query1.list();
        String sql_find_x="SELECT x FROM UpsPackage WHERE amzPackageID = :a";
        Query query1 = session.createQuery(sql_find_x);
        query1.setParameter("a", package_id);
        List<Integer> x_list = query1.list();

        String sql_find_y="SELECT y FROM UpsPackage WHERE amzPackageID = :a";
        Query query2 = session.createQuery(sql_find_y);
        query2.setParameter("a", package_id);
        List<Integer> y_list = query2.list();

        if(y_list.isEmpty()){
            System.out.println("Error: not find package");
            return null;
        }else{
            WorldUps.UDeliveryLocation.Builder u_del_loc_builder = WorldUps.UDeliveryLocation.newBuilder();
            u_del_loc_builder.setPackageid(package_id)
//                    .setX(query_package.get(0).getX())
//                    .setY(query_package.get(0).getY());
                    .setX(x_list.get(0))
                    .setY(y_list.get(0));
            return u_del_loc_builder.build();
        }

    }

}
