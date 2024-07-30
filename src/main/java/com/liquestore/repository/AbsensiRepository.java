package com.liquestore.repository;

import com.liquestore.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbsensiRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> getAbsensiByEmployeeid(int id);
}
