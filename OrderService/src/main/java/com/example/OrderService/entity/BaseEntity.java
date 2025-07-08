package com.example.OrderService.entity;

import java.io.Serializable;

public interface BaseEntity<T> extends Serializable {
    T get();

    void setId(T id);
}
