package com.liquestore.repository;

import com.liquestore.model.ItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<ItemModel, Integer> {
    List<ItemModel> findByItemcodeStartingWith(String prefix);

}
