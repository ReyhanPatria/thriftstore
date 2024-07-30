package com.liquestore.repository;

import com.liquestore.model.OrderColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderColourRepository extends JpaRepository<OrderColor, Integer> {
    OrderColor findByColourcode(String colourcode);
}
