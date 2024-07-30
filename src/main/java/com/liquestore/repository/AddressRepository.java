package com.liquestore.repository;

import com.liquestore.model.AddressModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<AddressModel, Integer> {
}
