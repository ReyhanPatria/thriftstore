package com.LiqueStore.repository;

import com.LiqueStore.model.TemporaryOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemporaryOrderRepository extends JpaRepository<TemporaryOrderModel, Integer> {
    TemporaryOrderModel findByOrderid(String orderid);

    List<TemporaryOrderModel> findAllByMasterorderid(String masterorderid);

    List<TemporaryOrderModel> findAllByOrderidIn(List<String> orderid);
}
