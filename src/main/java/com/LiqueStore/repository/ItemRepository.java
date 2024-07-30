package com.LiqueStore.repository;

import com.LiqueStore.model.ItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<ItemModel, Integer> {
    List<ItemModel> findByItemcodeStartingWith(String prefix);

}
