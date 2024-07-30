package com.liquestore.repository;

import com.liquestore.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailOrdersRepository extends JpaRepository<OrderDetail, Integer> {
}
