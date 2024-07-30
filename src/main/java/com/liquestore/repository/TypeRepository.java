package com.liquestore.repository;

import com.liquestore.model.TypeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<TypeModel, Integer> {
    TypeModel findByTypecode(String typecode);
}
