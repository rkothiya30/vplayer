package com.example.vplayer.model;

import java.util.LinkedHashMap;

public class HashMapModel {

    LinkedHashMap<String, String> a  = new LinkedHashMap<>();

    public LinkedHashMap<String, String> getA() {
        return a;
    }

    public void setA(LinkedHashMap<String, String> a) {
        this.a = a;
    }

    public HashMapModel() {
    }

    public HashMapModel(LinkedHashMap<String, String> a) {
        this.a = a;
    }
}
