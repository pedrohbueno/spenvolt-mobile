package com.spenvolt.mobile.model;

/** Consumo (kWh) e custo (R$) somados de um ou mais aparelhos. */
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
