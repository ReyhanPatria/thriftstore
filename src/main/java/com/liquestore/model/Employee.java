package com.liquestore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "accessrightid", referencedColumnName = "id")
    private AccessRight accessRightId;

    private String username;
    private String fullName;
    private String email;
    private Date birthdate;
    private String password;
    private String phoneNumber;
    private String status;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private String holidaySchedule;

    @DateTimeFormat(pattern = "HH:mm")
    private Time clockIn;

    public Employee(int id) {
        this.id = id;
    }
}
