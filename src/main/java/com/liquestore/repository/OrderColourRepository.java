package com.liquestore.repository;

import com.liquestore.model.OrderColourModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderColourRepository extends JpaRepository<OrderColourModel, Integer> {
    OrderColourModel findByColourcode(String colourcode);
}
