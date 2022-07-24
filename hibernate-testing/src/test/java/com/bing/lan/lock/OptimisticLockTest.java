package com.bing.lan.lock;

import com.bing.lan.pojo.Product;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OptimisticLockTest {

  SessionFactory sessionFactory;

  @Before
  public void setup() {
    sessionFactory = new Configuration().configure("com/bing/lan/lock/op-lock-hibernate.cfg.xml").buildSessionFactory();

    //不要每次都执行，影响后面的测试流程
    //initData();

    System.out.println("========== setup 结束 ===========");
  }

  private void initData() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();
    // 检查是否存在默认的数据
    Product product = session.get(Product.class, 1);
    if (product == null) {
      product = new Product();
      product.setId(1);
      product.setVersion(0);
      product.setPrice(100);
      product.setName("phone");
      // 保存
      session.save(product);
    }
    session.getTransaction().commit();

    session.close();
  }

  @After
  public void tearDown() {
    sessionFactory.close();
  }

  @Test
  public void testModifyVersionByManual() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    Product p1 = session.get(Product.class, 1);
    p1.setPrice(p1.getPrice() + 1000);

    // 查询后，手动修改版本号 不生效, 还是获取前面查询时 缓存 中的版本号
    p1.setVersion(p1.getVersion() + 5);

    session.getTransaction().commit();
    session.close();

    sessionFactory.close();
  }

  @Test
  public void testOptimisticLockException() throws InterruptedException {
    // 测试并发修改，乐观锁失败的问题
    Session s1 = sessionFactory.openSession();
    Session s2 = sessionFactory.openSession();

    s1.beginTransaction();
    s2.beginTransaction();

    Product p1 = s1.get(Product.class, 1);
    p1.setPrice(p1.getPrice() + 1000);

    Product p2 = s2.get(Product.class, 1);
    p2.setPrice(p2.getPrice() + 1000);

    s1.getTransaction().commit();
    s1.close();

    // 为了保证第二个事务稍后执行, 否则容易竞争锁
    Thread.sleep(5000);

    s2.getTransaction().commit();
    s2.close();

    sessionFactory.close();
  }
}
