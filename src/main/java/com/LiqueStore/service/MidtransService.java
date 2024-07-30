package com.LiqueStore.service;

import com.LiqueStore.model.CustomerModel;
import com.LiqueStore.model.TemporaryOrderModel;
import com.LiqueStore.repository.CustomerRepository;
import com.LiqueStore.repository.TemporaryOrderRepository;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransCoreApi;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
@CrossOrigin
public class MidtransService {
    private static final Logger log = LoggerFactory.getLogger(MidtransService.class);
    private final MidtransCoreApi midtransCoreApi;
    private final TemporaryOrderRepository temporaryOrderRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public MidtransService(MidtransCoreApi midtransCoreApi, TemporaryOrderRepository temporaryOrderRepository,
            CustomerRepository customerRepository) {
        this.midtransCoreApi = midtransCoreApi;
        this.temporaryOrderRepository = temporaryOrderRepository;
        this.customerRepository = customerRepository;
    }

    public static Timestamp convertStringToTimestamp(String timestampStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date parsedDate = dateFormat.parse(timestampStr);
            return new Timestamp(parsedDate.getTime());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to convert string to timestamp", e);
        }
    }

    public void checkAndUpdateOrderStatus(String masterOrderId) {
        try {
            // Get transaction status from Midtrans
            JSONObject responseBody = midtransCoreApi.checkTransaction(masterOrderId);
            log.info("ini response body{}", responseBody);

            String statusCode = responseBody.getString("status_code");
            String transactionStatus = responseBody.getString("transaction_status");
            String transactionTime = responseBody.getString("transaction_time");
            String orderId = responseBody.getString("order_id");
            log.info("order id dari midtrans{}", orderId);
            TemporaryOrderModel tempOdModel = temporaryOrderRepository.findByOrderid(orderId);
            CustomerModel custModel = customerRepository.findByPhonenumber(tempOdModel.getPhonenumber());
            // Convert transactionTime to Timestamp
            Timestamp timestamp = convertStringToTimestamp(transactionTime);

            // If transaction is settled
            if ("200".equals(statusCode) && "settlement".equals(transactionStatus)) {
                List<TemporaryOrderModel> temporaryOrders =
                        temporaryOrderRepository.findAllByMasterorderid(masterOrderId);
                for (TemporaryOrderModel temporaryOrderModel : temporaryOrders) {
                    // Update the status, payment date, and payment id
                    temporaryOrderModel.setStatus("On Packing");
                    temporaryOrderModel.setPaymentdate(timestamp);
                    temporaryOrderModel.setUsername(custModel.getUsername());
                    temporaryOrderRepository.save(temporaryOrderModel);
                    // Log the updated order information
                    log.info("Order {} updated successfully. Status: {}, Payment Date: {}",
                            temporaryOrderModel.getOrderid(),
                            temporaryOrderModel.getStatus(),
                            temporaryOrderModel.getPaymentdate());
                }
            }
            else {
                log.warn("Failed to update order {}. Status code: {}, Transaction status: {}",
                        masterOrderId,
                        statusCode,
                        transactionStatus);
            }
        }
        catch (MidtransError | RuntimeException e) {
            log.error("Failed to check and update order status for orderId {}: {}", masterOrderId, e.getMessage());
        }
    }
}
