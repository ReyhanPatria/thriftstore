package com.liquestore.model;

import com.liquestore.model.converter.StringListConverter;
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
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "typeid", referencedColumnName = "id")
    private Type typeId;

    @ManyToOne
    @JoinColumn(name = "employeeid", referencedColumnName = "id")
    private Employee employeeId;

    private String code;
    private String name;
    private int size;
    private Timestamp updatedDate;
    private int customWeight;
    private int customCapitalPrice;
    private int customDefaultPrice;
    private String status;

    @Convert(converter = StringListConverter.class)
    private List<String> files;
}
