package com.sarabrandserver.cart.projection;

public interface CartPojo {

    String getName();
    String getSession();
    String getKey(); // s3 key
    String getSku();
    Integer getQty();

}