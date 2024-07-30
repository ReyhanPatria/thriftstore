package com.LiqueStore.repository;

import com.LiqueStore.model.AbsensiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbsensiRepository extends JpaRepository<AbsensiModel, Integer> {
    List<AbsensiModel> getAbsensiByEmployeeid(int id);
}
