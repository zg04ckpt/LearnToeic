package com.example.learntoeic.models;

import java.io.Serializable;
import java.util.Date;

public class Unit implements Serializable {
    public String name, id;
    public Date createdDate;

    public Unit(String id, String name, Date createdDate) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
    }
}
