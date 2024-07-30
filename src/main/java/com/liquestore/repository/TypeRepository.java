package com.liquestore.repository;

import com.liquestore.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<Type, Integer> {
    Type findByTypecode(String typecode);
}
