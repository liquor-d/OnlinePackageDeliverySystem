package org.example.model;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
public class Database {
    private static SessionFactory sessionFactory;

    //    init() to connect with the postgresql
    public static void init() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ups", "postgres", "passw0rd");

            // drop truck table
            Statement statement = conn.createStatement();
            statement.executeUpdate("DROP TABLE truck");
            statement.close();

            conn.setAutoCommit(false);
            sessionFactory = buildSessionFactory();

//            deleteAllData();// clear data
            System.out.println("db connected!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllData() {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Query query1 = session.createQuery("delete from UpsPackage");
        Query query2 = session.createQuery("delete from Truck");
//        Query query3 = session.createQuery("delete from Account");

        query1.executeUpdate();
        query2.executeUpdate();
//        query3.executeUpdate();

        tx.commit();
    }


    private static SessionFactory buildSessionFactory() {
        try {
            // Load the hibernate.cfg.xml configuration file
            Configuration configuration = new Configuration().configure();
//            configuration.addAnnotatedClass(Account.class);
            configuration.addAnnotatedClass(UpsPackage.class);
            configuration.addAnnotatedClass(Truck.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }


}