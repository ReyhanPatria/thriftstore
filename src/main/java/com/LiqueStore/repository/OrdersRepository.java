package com.LiqueStore.repository;

import com.LiqueStore.model.OrdersModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<OrdersModel, String> {
    OrdersModel findByPhonenumber(String phonenumber);
}
