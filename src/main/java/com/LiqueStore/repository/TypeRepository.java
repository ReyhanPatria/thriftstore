package com.LiqueStore.repository;

import com.LiqueStore.model.TypeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<TypeModel, Integer> {
    TypeModel findByTypecode(String typecode);
}
