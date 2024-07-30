package com.liquestore.repository;

import com.liquestore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByUsername(String username);

    Customer findByPhonenumber(String phonenumber);

    List<Customer> getCustomerByUsername(String username);
}
