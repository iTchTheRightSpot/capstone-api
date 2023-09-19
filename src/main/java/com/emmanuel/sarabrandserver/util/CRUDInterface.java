package com.emmanuel.sarabrandserver.util;

public interface CRUDInterface<T> {
    T create();
    T update();
    T delete();
}
