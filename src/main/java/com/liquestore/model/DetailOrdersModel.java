package com.liquestore.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "detailorders")
public class DetailOrdersModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String orderid;
    private int totalweight;
    private int deliveryprice;
    private int totalprice;
    private Timestamp paymentdate;

    public DetailOrdersModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public int getTotalweight() {
        return totalweight;
    }

    public void setTotalweight(int totalweight) {
        this.totalweight = totalweight;
    }

    public int getDeliveryprice() {
        return deliveryprice;
    }

    public void setDeliveryprice(int deliveryprice) {
        this.deliveryprice = deliveryprice;
    }

    public int getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(int totalprice) {
        this.totalprice = totalprice;
    }

    public Timestamp getPaymentdate() {
        return paymentdate;
    }

    public void setPaymentdate(Timestamp paymentdate) {
        this.paymentdate = paymentdate;
    }
}
