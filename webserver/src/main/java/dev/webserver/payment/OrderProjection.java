package dev.webserver.payment;

import java.util.Date;

public interface OrderProjection {

    Date getTime();
    String getCurrency();
    Integer getTotal();
    String getPaymentId();
    String getDetail(); // json object

}
