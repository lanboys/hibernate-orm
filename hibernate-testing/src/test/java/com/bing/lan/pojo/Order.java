package com.bing.lan.pojo;

/**
 * Created by oopcoder at 2022/7/23 9:11 .
 */

public class Order {

  int id;
  String name;
  float price;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  @Override
  public String toString() {
    return "Order{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", price=" + price +
        '}';
  }
}
