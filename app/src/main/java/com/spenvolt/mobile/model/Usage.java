package com.spenvolt.mobile.model;

public class Usage {

    public double kwh;
    public double cost;

    public Usage(double kwh, double cost) {
        this.kwh = kwh;
        this.cost = cost;
    }

    public void add(double kwh, double cost) {
        this.kwh += kwh;
        this.cost += cost;
    }
}
