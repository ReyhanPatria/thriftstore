package com.LiqueStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "absensi")
public class AbsensiModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int employeeid;
    private String username;
    private String password;
    private Timestamp clockin;
    private Timestamp clockout;
    private Date todaydate;

    public AbsensiModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeid() {
        return employeeid;
    }

    public void setEmployeeid(int employeeid) {
        this.employeeid = employeeid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getClockin() {
        return clockin;
    }

    public void setClockin(Timestamp clockin) {
        this.clockin = clockin;
    }

    public Timestamp getClockout() {
        return clockout;
    }

    public void setClockout(Timestamp clockout) {
        this.clockout = clockout;
    }

    public Date getTodaydate() {
        return todaydate;
    }

    public void setTodaydate(Date todaydate) {
        this.todaydate = todaydate;
    }
}
