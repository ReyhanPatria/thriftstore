package com.LiqueStore.model;

import com.LiqueStore.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "temporaryorder")
public class TemporaryOrderModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "orderid", unique = true)
    private String orderid;
    private String username;
    private String phonenumber;
    private int totalprice;
    private int totalweight;
    @Column(columnDefinition = "VARCHAR(255)")
    @Convert(converter = StringListConverter.class)
    private List<String> waitinglist;
    @Column(columnDefinition = "VARCHAR(255)")
    @Convert(converter = StringListConverter.class)
    private List<String> itemidall;
    private String link;
    private Timestamp paymentdate;
    private Timestamp checkoutdate;
    private String status;
    private String masterorderid;
    private boolean isactive;
    @ManyToOne
    @JoinColumn(name = "colourid", referencedColumnName = "id")
    private OrderColourModel colourid;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public int getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(int totalprice) {
        this.totalprice = totalprice;
    }

    public int getTotalweight() {
        return totalweight;
    }

    public void setTotalweight(int totalweight) {
        this.totalweight = totalweight;
    }

    public OrderColourModel getColourid() {
        return colourid;
    }

    public void setColourid(OrderColourModel colourid) {
        this.colourid = colourid;
    }

    public List<String> getWaitinglist() {
        return waitinglist;
    }

    public void setWaitinglist(List<String> waitinglist) {
        this.waitinglist = waitinglist;
    }

    public List<String> getItemidall() {
        return itemidall;
    }

    public void setItemidall(List<String> itemidall) {
        this.itemidall = itemidall;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Timestamp getPaymentdate() {
        return paymentdate;
    }

    public void setPaymentdate(Timestamp paymentdate) {
        this.paymentdate = paymentdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCheckoutdate() {
        return checkoutdate;
    }

    public void setCheckoutdate(Timestamp checkoutdate) {
        this.checkoutdate = checkoutdate;
    }

    public String getMasterorderid() {
        return masterorderid;
    }

    public void setMasterorderid(String masterorderid) {
        this.masterorderid = masterorderid;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }
}
