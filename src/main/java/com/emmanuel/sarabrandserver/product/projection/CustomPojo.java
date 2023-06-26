package com.emmanuel.sarabrandserver.product.projection;

import java.math.BigDecimal;
import java.util.List;

public interface CustomPojo {
    String getName();
    String getDesc();
    BigDecimal getPrice();
    String getCurrency();
    List<Detail> getDetail();

    interface Detail {
        String getSku();
        String getColour();
        String getSize();
//        List<Imagez> getImagez();
//        interface Imagez {
//            String getUrl();
//        }
    }
}
