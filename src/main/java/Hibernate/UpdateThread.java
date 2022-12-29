package Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class UpdateThread implements Runnable {
    private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    private final CountDownLatch latch;
    private final Random random = new Random();

    public UpdateThread(SessionFactory sessionFactory, CountDownLatch latch) {
        this.sessionFactory = sessionFactory;
        this.latch = latch;
    }


    public static int countHibernateEx = 0;
    public static int countOptimisticLockEx = 0;

    @Override
    public void run() {
        Session session1 = HibernateUtil.getSessionFactory().openSession();
        session1.getTransaction().begin();
        Random rnd = new Random();

        List<Item> items = new ArrayList<>();
        for (int j = 0; j < 40; j++) {
            items.add(new Item(0));
            session1.persist(items.get(j));
        }

        for (int j = 0; j < items.stream().count(); j++) {
            items.get(j).setValue(0);
        }

        session1.getTransaction().commit();
        session1.close();

        try {
            for (int i = 0; i < 20000; i++) {
                Session session = null;
                Transaction transaction = null;
                try {
                    session = sessionFactory.openSession();
                    transaction = session.beginTransaction();

                    // Выбираем случайную строку из таблицы
                    Item query = session.createQuery("from Item where id = :id", Item.class)
                            .setParameter("id", random.nextInt(1, 41))
                            .uniqueResult();

                    // Увеличиваем val этой строки на 1
                    query.IncValue();
                    session.update(query);

                    Thread.sleep(5);
                    transaction.commit();

                } catch (HibernateException e) {
                    countHibernateEx++;
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    throw e;
                }
                catch (OptimisticLockException e){
                    countOptimisticLockEx++;
                }

                finally {
                    if (session != null) {
                        session.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
            System.out.println("Optimistic: " + countOptimisticLockEx);
            System.out.println("Hibernate: " + countHibernateEx);
        }
    }
}


