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
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "item")
public class ItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String itemcode;
    private String name;
    private int size;
    private Timestamp lastupdate;
    private int customweight;
    private int customcapitalprice;
    private int customdefaultprice;
    @Column(columnDefinition = "VARCHAR(5000)")
    @Convert(converter = StringListConverter.class)
    private List<String> files;
    private String status;

    @ManyToOne
    @JoinColumn(name = "typeid", referencedColumnName = "id")
    private TypeModel typeId;

    @ManyToOne
    @JoinColumn(name = "employeeid", referencedColumnName = "id")
    private EmployeeModel employeeId;

    public ItemModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemcode() {
        return itemcode;
    }

    public void setItemcode(String itemcode) {
        this.itemcode = itemcode;
    }

    public TypeModel getTypeId() {
        return typeId;
    }

    public void setTypeId(TypeModel typeId) {
        this.typeId = typeId;
    }

    public EmployeeModel getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(EmployeeModel employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Timestamp getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Timestamp lastupdate) {
        this.lastupdate = lastupdate;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public int getCustomweight() {
        return customweight;
    }

    public void setCustomweight(int customweight) {
        this.customweight = customweight;
    }

    public int getCustomcapitalprice() {
        return customcapitalprice;
    }

    public void setCustomcapitalprice(int customcapitalprice) {
        this.customcapitalprice = customcapitalprice;
    }

    public int getCustomdefaultprice() {
        return customdefaultprice;
    }

    public void setCustomdefaultprice(int customdefaultprice) {
        this.customdefaultprice = customdefaultprice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
