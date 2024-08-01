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
@Table(name = "temporary_order")
public class TemporaryOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int orderId;
    private int colorId;
    private String username;
    private String phoneNumber;
    private int totalPrice;
    private int totalWeight;
    private String link;
    private Timestamp paymentDate;
    private Timestamp checkoutDate;
    private String status;
    private int masterOrderId;
    private Boolean isActive;

    @Convert(converter = StringListConverter.class)
    private List<String> waitingList;

    @Convert(converter = StringListConverter.class)
    private List<String> itemIdList;
}
