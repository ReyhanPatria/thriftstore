package com.liquestore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int accessRightId;
    private String username;
    private String fullName;
    private String email;
    private Date birthDate;
    private String password;
    private String phoneNumber;
    private String status;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private String holidaySchedule;
    private String scheduledClockIn;

    public Employee(int id) {
        this.id = id;
    }
}
