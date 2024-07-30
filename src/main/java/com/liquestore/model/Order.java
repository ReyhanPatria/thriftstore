package com.liquestore.model;

import com.liquestore.model.converter.StringListConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;

    @Convert(converter = StringListConverter.class)
    private List<String> itemIdList;

    private String username;
    private String phoneNumber;
    private Timestamp checkoutDate;
    private Timestamp paymentDate;
    private Timestamp packingDate;
    private Timestamp deliveryPickupDate;
    private Timestamp deliveryCompletionDate;
    private String status;
    private String deliveryReceiptNumber;
}
