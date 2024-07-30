package com.liquestore.repository;

import com.liquestore.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByItemcodeStartingWith(String prefix);

}
