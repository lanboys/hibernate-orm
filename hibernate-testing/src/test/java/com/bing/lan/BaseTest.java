package com.bing.lan;

import org.junit.BeforeClass;

/**
 * Created by oopcoder at 2022/7/24 16:35 .
 */

public class BaseTest {


  @BeforeClass
  public static void beforeClass() throws Exception {
    // org.jboss.logging.LoggerProviders
    System.setProperty("org.jboss.logging.provider", "slf4j");
  }

}
