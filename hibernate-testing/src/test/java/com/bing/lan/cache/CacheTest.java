package com.bing.lan.cache;

import com.bing.lan.BaseTest;
import com.bing.lan.pojo.Order;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheTest extends BaseTest {

  Logger logger = LoggerFactory.getLogger(CacheTest.class);

  SessionFactory sessionFactory;

  @Before
  public void setup() {
    sessionFactory = new Configuration().configure("com/bing/lan/cache/cache-hibernate.cfg.xml").buildSessionFactory();

    //不要每次都执行，影响后面的测试流程
    //initData();

    System.out.println("========== setup 结束 ===========");
  }

  private void initData() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();
    // 检查是否存在默认的数据
    Order order = session.get(Order.class, 1);
    if (order == null) {
      order = new Order();
      order.setId(1);
      order.setPrice(100);
      order.setName("taobao");
      // 保存
      session.save(order);
    }
    session.getTransaction().commit();

    session.close();
  }

  @After
  public void tearDown() {
    sessionFactory.close();
  }

  @Test
  public void testGet1() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    Order order = session.get(Order.class, 1);
    System.out.println("testGet(): 此时已经发送sql");
    System.out.println("testGet(): " + order);

    order = session.get(Order.class, 1);
    System.out.println("testGet(): 从缓存获取值，不再发送sql");
    System.out.println("testGet(): " + order);

    session.getTransaction().commit();
    session.close();
  }

  @Test
  public void testGet2() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    Order order = session.get(Order.class, 1);
    System.out.println("testGet(): 此时已经发送sql");
    System.out.println("testGet(): " + order);

    // 修改了Persistent对象，在事务提交的时候【可能会】自动提交更新sql, 可以看看打印出来的sql语句
    // 为啥是可能会：因为需要比较看看数据是否发生变化了，发生变化了才发送更新sql
    order.setName("cache" + System.currentTimeMillis());
    // 这一句是不是立马执行，好像还要看 session.setFlushMode(...)
    //session.update(order);

    order = session.get(Order.class, 1);
    System.out.println("testGet(): 从缓存获取值，并且是修改后的值，不再发送sql");
    System.out.println("testGet(): " + order);

    session.getTransaction().commit();
    session.close();
  }

  @Test
  public void testLoad() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    Order order = session.load(Order.class, 1);
    System.out.println("testLoad(): 此时并没有发送sql");
    System.out.println("testLoad(): " + order);

    order = session.load(Order.class, 1);
    System.out.println("testLoad(): 从缓存获取值，不再发送sql");
    System.out.println("testLoad(): " + order);

    session.getTransaction().commit();
    session.close();
  }

  @Test
  public void testSave() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    Order order = new Order();
    order.setPrice(1234);
    order.setName("car");

    // 新增保存立马执行sql，估计是为了获取id
    session.save(order);
    System.out.println("testSave(): 此时已经执行sql");
    System.out.println("testSave(): " + order);

    session.getTransaction().commit();
    session.close();
  }

  @Test
  public void testSave1() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    Order order = new Order();
    order.setPrice(1234);
    order.setName("car");

    // 新增保存立马执行sql，估计是为了获取id
    session.save(order);
    System.out.println("testSave(): 此时已经执行sql");
    System.out.println("testSave(): " + order);

    // 更新没有立马执行sql
    order.setName("cache" + System.currentTimeMillis());
    System.out.println("testSave(): " + order);

    session.getTransaction().commit();
    session.close();
  }
}
