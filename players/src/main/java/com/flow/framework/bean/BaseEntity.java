package com.flow.framework.bean;

import java.io.Serializable;

public abstract class BaseEntity<T> implements Serializable {
    public abstract T parseJson(String str);
}
