package Hibernate;

import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.*;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        pessimistic();
        //optimistic();
    }

    public static void Sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void optimistic() throws InterruptedException {
        Session session = HibernateUtil.getSessionFactory().openSession();

        session.getTransaction().begin();

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            items.add(new Item(0));
            session.persist(items.get(i));
        }

        for (int i = 0; i < items.stream().count(); i++){
            items.get(i).setValue(0);
        }

        session.getTransaction().commit();

        CountDownLatch cdl = new CountDownLatch(8);

        AtomicInteger tempHibernate = new AtomicInteger();
        AtomicInteger tempOptimisticLockException = new AtomicInteger();

        for (int i = 0; i < 8; i++) {
            new Thread(() -> {
                Session session_t = HibernateUtil.getSessionFactory().openSession();

                Random rnd = new Random();

                for (int j = 0; j < 20000; j++) {
                    try {
                        session_t.beginTransaction();
                        Item item = session_t.get(Item.class, rnd.nextInt(40) + 1);
                        item.setValue(item.getValue() + 1);
                        session_t.getTransaction().commit();
                    }
                    catch (HibernateException e) {
                        session_t.getTransaction().rollback();
                        j--;
                        tempHibernate.getAndIncrement();

                    }
                    catch (OptimisticLockException a) {
                        session_t.getTransaction().rollback();
                        j--;
                        tempOptimisticLockException.getAndIncrement();
                    }

                    Sleep(5);
                }

                System.out.println("Thread ready");
                System.out.println("Кол-во HibernateException: " + tempHibernate.get());
                System.out.println("Кол-во OptimisticLockException: " + tempOptimisticLockException.get());

                List<Item> item = session_t.createQuery("from Item", Item.class).list();

                int tempCount = 0;
                for (int q = 0; q < item.stream().count(); q++){
                    tempCount +=  item.get(q).getValue();
                }
                System.out.println(tempCount);

                session_t.close();

                cdl.countDown();
            }).start();
        }
        cdl.await();
        session.close();
    }

        public static void pessimistic() throws InterruptedException {
            Session session = HibernateUtil.getSessionFactory().openSession();

            session.getTransaction().begin();
            Random rnd = new Random();

            List<Item> items = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                items.add(new Item(0));
                session.persist(items.get(i));
            }

            for (int i = 0; i < items.stream().count(); i++) {
                items.get(i).setValue(0);
            }

            session.getTransaction().commit();

            AtomicInteger tempHibernate = new AtomicInteger();
            AtomicInteger tempOptimisticLockException = new AtomicInteger();

            AtomicInteger n = new AtomicInteger(20000);

            for (int i = 0; i < 8; i++) {
                new Thread(() -> {
                    Session session_t = HibernateUtil.getSessionFactory().openSession();
                    for (int j = 0; j < n.get(); j++) {
                        try {
                            session_t.beginTransaction();

                            Item query = session_t.createQuery("from Item where id = :id", Item.class)
                                    .setParameter("id", rnd.nextInt(1, 41)).getSingleResult();

                            Object res = session_t.getCurrentLockMode(query);

                            if(res != LockMode.PESSIMISTIC_WRITE){
                                session_t.lock(query, LockMode.PESSIMISTIC_WRITE);

                                query.IncValue();
                                session_t.persist(query);

                                session_t.getTransaction().commit();
                            }
                            else{
                                n.getAndIncrement();
                            }

//                            item.setValue(item.getValue() + 1);

                        } catch (HibernateException e) {
                            session_t.getTransaction().rollback();
                            e.printStackTrace();
                            j--;
                            tempHibernate.getAndIncrement();

                        } catch (OptimisticLockException ex) {
                            session_t.getTransaction().rollback();
                            ex.printStackTrace();
                            j--;
                            tempOptimisticLockException.getAndIncrement();
                        }
                        Sleep(5);

                    }

                    System.out.println("Thread ready");
                    System.out.println("Кол-во HibernateException: " + tempHibernate.get());
                    System.out.println("Кол-во PessimisticLockException: " + tempOptimisticLockException.get());

                    List<Item> item = session_t.createQuery("from Item", Item.class).list();

                    int tempCount = 0;
                    for (int q = 0; q < item.stream().count(); q++) {
                        tempCount += item.get(q).getValue();
                    }
                    System.out.println(tempCount);

                    session_t.close();

                }).start();


            }

            session.close();
        }

    private static int getSum() {
        int sum = 0;
        for (int i = 1; i < 41; i++) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Item it = session.get(Item.class, i);
            sum += it.getValue();
            session.getTransaction().commit();
            session.close();
        }
        return sum;
    }

private static void method2() throws InterruptedException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    // Запускаем 8 потоков с помощью CountDownLatch
    CountDownLatch latch = new CountDownLatch(1);
    for (int i = 0; i < 8; i++) {
        new Thread(new UpdateThread(sessionFactory, latch)).start();
    }
    latch.await();
}

//    private static void pisimistic(SessionFactory factory) {
//        CyclicBarrier cb = new CyclicBarrier(4);
//        Random rnd = new Random();
//        Thread[] thread = new Thread[4];
//        for (int i = 0; i < 4; i++) {
//            thread[i] = new Thread(() -> {
//                for (int j = 0; j < 20000; j++) {
//                    boolean check = false;
//                    while (!check) {
//                        Session session = factory.getCurrentSession();
//                        try {
//                            session.beginTransaction();
//                            Item it = session.createQuery("FROM Item WHERE id = " + rnd.nextInt(1, 41), Item.class)
//                                    .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList().get(0);
//                            it.incValue();
//                            Thread.sleep(5);
//                            session.save(it);
//                            session.getTransaction().commit();
//
//                            session.close();
//                            check = true;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        } catch (HibernateException | OptimisticLockException e) {
//                            session.getTransaction().rollback();
//                        }
//                    }
//                }
//                try {
//                    cb.await();
//                } catch (BrokenBarrierException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//        for (int i = 0; i < 4; i++)
//            thread[i].start();
//        try {
//            for (int i = 0; i < 4; i++) {
//                thread[i].join();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        pissSum = getSum();
//    }
}