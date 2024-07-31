package com.liquestore.controller;

import com.liquestore.model.Address;
import com.liquestore.model.Customer;
import com.liquestore.model.OrderDetail;
import com.liquestore.model.TemporaryOrder;
import com.liquestore.repository.AddressRepository;
import com.liquestore.repository.CustomerRepository;
import com.liquestore.repository.DetailOrdersRepository;
import com.liquestore.repository.TemporaryOrderRepository;
import com.liquestore.service.RajaOngkirService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/backend/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final RajaOngkirService rajaOngkirService;

    private final CustomerRepository customerRepository;
    private final TemporaryOrderRepository temporaryOrderRepository;
    private final AddressRepository addressRepository;
    private final DetailOrdersRepository detailOrdersRepository;

    private final MidtransSnapApi snapApi;

    @PostMapping("/api/payment")
    public ResponseEntity<?> paymentGetaway(@RequestParam("masterorderid") String masterorderid,
            @RequestParam("customerid") int customerid,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("zipcode") String zipcode,
            @RequestParam("weight") int weight,
            @RequestParam("deliveryprice") int deliveryprice,
            @RequestParam("totalprice") int totalprice) throws MidtransError {
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", masterorderid);
        transactionDetails.put("gross_amount", totalprice);

        Map<String, Object> params = new HashMap<>();
        params.put("transaction_details", transactionDetails);
        params.put("enabled_payments", new String[] {"bca_va", "shopeepay", "qris", "ovo"});

        Optional<Customer> optionalCustomerModel = customerRepository.findById(customerid);
        if (optionalCustomerModel.isPresent()) {
            Customer customer = optionalCustomerModel.get();
            //        Customer details
            Map<String, Object> customerDetails = new HashMap<>();
            customerDetails.put("first_name", customer.getUsername());
            customerDetails.put("email", customer.getEmail());
            customerDetails.put("phone", customer.getPhoneNumber());
            params.put("customer_details", customerDetails);

            //        Shipping Address
            Map<String, Object> shippingAddress = new HashMap<>();
            shippingAddress.put("first_name", customer.getUsername());
            shippingAddress.put("phone", customer.getPhoneNumber());
            shippingAddress.put("address", address);
            shippingAddress.put("city", city);
            shippingAddress.put("postal_code", zipcode);
            shippingAddress.put("country_code", "IDN");
            customerDetails.put("shipping_address", shippingAddress);
        }

        OrderDetail addDetailOrders = new OrderDetail();
        addDetailOrders.setOrderId(masterorderid);
        addDetailOrders.setTotalWeight(weight);
        addDetailOrders.setDeliveryPrice(deliveryprice);
        addDetailOrders.setTotalPrice(totalprice);
        addDetailOrders.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        log.info("ini data detail order {}", addDetailOrders);
        detailOrdersRepository.save(addDetailOrders);

        String token = snapApi.createTransactionToken(params);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        log.info(String.valueOf(response));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getCustData")
    public ResponseEntity<?> getCustData(@RequestParam(name = "id") int id) {
        boolean cekId = false;
        List<Customer> getAllCust = customerRepository.findAll();
        for (int i = 0; i < getAllCust.size(); i++) {
            if (getAllCust.get(i).getId() == id) {
                cekId = true;
                break;
            }
        }
        if (cekId) {
            log.info(String.valueOf(getAllCust));
            return ResponseEntity.ok(getAllCust);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + id);
        }
    }

    @GetMapping("/getOrderData")
    public ResponseEntity<?> getOrderData(@RequestParam(name = "id") String orderid) {
        TemporaryOrder temporaryOrder = temporaryOrderRepository.findByOrderid(orderid);
        String masterOrderId = temporaryOrder.getMasterOrderId();
        List<TemporaryOrder> listTemporaryOrder = temporaryOrderRepository.findAllByMasterorderid(masterOrderId);
        // Hitung total harga dan total berat
        int totalPrice = listTemporaryOrder.stream().mapToInt(TemporaryOrder::getTotalPrice).sum();
        int totalWeight = listTemporaryOrder.stream().mapToInt(TemporaryOrder::getTotalWeight).sum();

        // Buat map untuk mengembalikan data
        Map<String, Object> result = new HashMap<>();
        result.put("listTempOrder", listTemporaryOrder);
        result.put("totalPrice", totalPrice);
        result.put("totalWeight", totalWeight);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAddressData")
    public ResponseEntity<?> getAddressData(@RequestParam(name = "id") int id) {
        List<Address> getAddress = addressRepository.findAll();
        if (getAddress.isEmpty()) {
            return ResponseEntity.badRequest().body("tidak ada data");
        }

        // Memfilter alamat berdasarkan id pelanggan
        List<Address> filteredAddress = getAddress.stream()
                .filter(address -> address.getCustomer().getId() == id)
                .collect(Collectors.toList());

        if (!filteredAddress.isEmpty()) {
            log.info("address ditemukan");
            log.info(String.valueOf(filteredAddress));
            return ResponseEntity.ok(filteredAddress);
        }
        else {
            return ResponseEntity.badRequest().body("address tidak ditemukan");
        }
    }

    @PostMapping("/tambahAddress")
    public ResponseEntity<?> tambahAddress(@RequestParam(value = "id", required = false) Integer id,
            @RequestParam("addressname") String addressname,
            @RequestParam("addressdetail") String addressdetail,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("country") String country,
            @RequestParam("zipcode") int zipcode,
            @RequestParam("note") String note,
            @RequestParam("customerid") int customerid,
            @RequestParam("cityId") int cityid
    ) {
        if (id == null) {
            Address address = new Address();
            address.setName(addressname);
            address.setDetail(addressdetail);
            address.setCity(city);
            address.setState(state);
            address.setZipcode(zipcode);
            address.setNote(note);
            address.setCustomer(new Customer(customerid));
            address.setCityId(cityid);
            addressRepository.save(address);
            return ResponseEntity.ok("Berhasil Menambah Address");
        }
        else {
            Optional<Address> optionalAddressModel = addressRepository.findById(id);
            if (optionalAddressModel.isPresent()) {
                Address getAddress = optionalAddressModel.get();
                getAddress.setName(addressname);
                getAddress.setDetail(addressdetail);
                getAddress.setCity(city);
                getAddress.setState(state);
                getAddress.setZipcode(zipcode);
                getAddress.setNote(note);
                getAddress.setCityId(cityid);
                addressRepository.save(getAddress);
                return ResponseEntity.ok("Berhasil Mengubah Address");
            }
            else {
                return ResponseEntity.badRequest().body("address not found");
            }
        }
    }

    @GetMapping("/api/rajaongkir/provinces")
    public String getProvinces() {
        return rajaOngkirService.getProvinces();
    }

    @GetMapping("/api/rajaongkir/cities/{provinceId}")
    public String getCities(@PathVariable int provinceId) {
        log.info("masuk pilih kota");
        return rajaOngkirService.getCities(provinceId);
    }

    @GetMapping("/api/rajaongkir/cost")
    public String getShippingCost(@RequestParam String originType,
            @RequestParam int origin,
            @RequestParam String destinationType,
            @RequestParam int destination,
            @RequestParam int weight) {
        return rajaOngkirService.getShippingCost(originType, origin, destinationType, destination, weight);
    }

}
