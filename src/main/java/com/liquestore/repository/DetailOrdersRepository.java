package com.liquestore.repository;

import com.liquestore.model.DetailOrdersModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailOrdersRepository extends JpaRepository<DetailOrdersModel, Integer> {
}
