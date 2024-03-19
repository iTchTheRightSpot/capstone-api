package dev.capstone.payment.projection;

import java.util.Date;

public interface OrderPojo {

    Date getTime();
    String getCurrency();
    Integer getTotal();
    String getPaymentId();
    String getDetail(); // json object

}
