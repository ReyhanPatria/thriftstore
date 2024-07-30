package com.liquestore.model;

import com.liquestore.model.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "temporaryorder")
public class TemporaryOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "orderid", unique = true)
    private String orderId;

    @ManyToOne
    @JoinColumn(name = "colorid", referencedColumnName = "id")
    private OrderColor colorId;

    private String username;
    private String phoneNumber;
    private int totalPrice;
    private int totalWeight;
    private String link;
    private Timestamp paymentDate;
    private Timestamp checkoutDate;
    private String status;
    private String masterOrderId;
    private Boolean isActive;

    @Convert(converter = StringListConverter.class)
    private List<String> waitingList;

    @Convert(converter = StringListConverter.class)
    private List<String> itemIdList;
}
