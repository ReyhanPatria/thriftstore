package com.LiqueStore.model;

import com.LiqueStore.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrdersModel {
    @Id
    private String id;
    @Column(columnDefinition = "VARCHAR(255)")
    @Convert(converter = StringListConverter.class)
    private List<String> itemidall;
    private String username;
    private String phonenumber;
    private Timestamp checkoutdate;
    private Timestamp paymentdate;
    private Timestamp packingdate;
    private Timestamp deliverypickupdate;
    private Timestamp deliverydonedate;
    private String status;
    private String no_resi;

    public OrdersModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getItemidall() {
        return itemidall;
    }

    public void setItemidall(List<String> itemidall) {
        this.itemidall = itemidall;
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

    public Timestamp getCheckoutdate() {
        return checkoutdate;
    }

    public void setCheckoutdate(Timestamp checkoutdate) {
        this.checkoutdate = checkoutdate;
    }

    public Timestamp getPaymentdate() {
        return paymentdate;
    }

    public void setPaymentdate(Timestamp paymentdate) {
        this.paymentdate = paymentdate;
    }

    public Timestamp getPackingdate() {
        return packingdate;
    }

    public void setPackingdate(Timestamp packingdate) {
        this.packingdate = packingdate;
    }

    public Timestamp getDeliverypickupdate() {
        return deliverypickupdate;
    }

    public void setDeliverypickupdate(Timestamp deliverypickupdate) {
        this.deliverypickupdate = deliverypickupdate;
    }

    public Timestamp getDeliverydonedate() {
        return deliverydonedate;
    }

    public void setDeliverydonedate(Timestamp deliverydonedate) {
        this.deliverydonedate = deliverydonedate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNo_resi() {
        return no_resi;
    }

    public void setNo_resi(String no_resi) {
        this.no_resi = no_resi;
    }
}
