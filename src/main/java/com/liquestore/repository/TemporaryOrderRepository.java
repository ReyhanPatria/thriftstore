package com.liquestore.repository;

import com.liquestore.model.TemporaryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemporaryOrderRepository extends JpaRepository<TemporaryOrder, Integer> {
    TemporaryOrder findByOrderid(String orderid);

    List<TemporaryOrder> findAllByMasterorderid(String masterorderid);

    List<TemporaryOrder> findAllByOrderidIn(List<String> orderid);
}
