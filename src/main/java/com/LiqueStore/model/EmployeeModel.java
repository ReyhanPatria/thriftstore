package com.LiqueStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.format.annotation.DateTimeFormat;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "employee")
public class EmployeeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String fullname;
    private String email;
    private Date birthdate;
    private String password;
    private String phonenumber;
    private String status;
    private Timestamp firstjoindate;
    private Timestamp lastupdate;
    @DateTimeFormat(pattern = "HH:mm")
    private Time jam_masuk;
    private String jadwal_libur;

    @ManyToOne
    @JoinColumn(name = "accessrightid", referencedColumnName = "id")
    private AccessRightModel accessRight;

    public EmployeeModel() {
    }

    public EmployeeModel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccessRightModel getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(AccessRightModel accessRight) {
        this.accessRight = accessRight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getFirstjoindate() {
        return firstjoindate;
    }

    public void setFirstjoindate(Timestamp firstjoindate) {
        this.firstjoindate = firstjoindate;
    }

    public Timestamp getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Timestamp lastupdate) {
        this.lastupdate = lastupdate;
    }

    public Time getJam_masuk() {
        return jam_masuk;
    }

    public void setJam_masuk(Time jam_masuk) {
        this.jam_masuk = jam_masuk;
    }

    public String getJadwal_libur() {
        return jadwal_libur;
    }

    public void setJadwal_libur(String jadwal_libur) {
        this.jadwal_libur = jadwal_libur;
    }
}
