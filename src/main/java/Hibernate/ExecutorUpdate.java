package Hibernate;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Hibernate.Main.Sleep;

public class ExecutorUpdate {


    public static void Update(){
        ExecutorService executor = Executors.newFixedThreadPool(8);

        for(int i = 0; i < 8; i++) {
           // Runnable worker = new WorkerThread();
           // executor.execute(worker);
        }

        executor.shutdown();
    }



}
