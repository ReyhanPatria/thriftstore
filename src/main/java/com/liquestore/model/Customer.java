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
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int accessRightId;
    private String name;
    private String username;
    private String password;
    private String email;
    private String usernameIg;
    private String phoneNumber;
    private Date birthdate;
    private String status;

    public Customer(int id) {
        this.id = id;
    }
}
