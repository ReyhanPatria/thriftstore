package com.LiqueStore.controller;

import com.LiqueStore.model.AddressModel;
import com.LiqueStore.model.CustomerModel;
import com.LiqueStore.model.DetailOrdersModel;
import com.LiqueStore.model.TemporaryOrderModel;
import com.LiqueStore.repository.AddressRepository;
import com.LiqueStore.repository.CustomerRepository;
import com.LiqueStore.repository.DetailOrdersRepository;
import com.LiqueStore.repository.TemporaryOrderRepository;
import com.LiqueStore.service.RajaOngkirService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/backend/customer")
@CrossOrigin
public class CustomerController {
    private static final Logger logger = Logger.getLogger(ManagerController.class.getName());
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final MidtransSnapApi snapApi;

    @Autowired
    public CustomerController(MidtransSnapApi snapApi) {
        this.snapApi = snapApi;
    }

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TemporaryOrderRepository temporaryOrderRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private DetailOrdersRepository detailOrdersRepository;
    @Autowired
    private RajaOngkirService rajaOngkirService;

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

        Optional<CustomerModel> optionalCustomerModel = customerRepository.findById(customerid);
        if (optionalCustomerModel.isPresent()) {
            CustomerModel customerModel = optionalCustomerModel.get();
            //        Customer details
            Map<String, Object> customerDetails = new HashMap<>();
            customerDetails.put("first_name", customerModel.getUsername());
            customerDetails.put("email", customerModel.getEmail());
            customerDetails.put("phone", customerModel.getPhonenumber());
            params.put("customer_details", customerDetails);

            //        Shipping Address
            Map<String, Object> shippingAddress = new HashMap<>();
            shippingAddress.put("first_name", customerModel.getUsername());
            shippingAddress.put("phone", customerModel.getPhonenumber());
            shippingAddress.put("address", address);
            shippingAddress.put("city", city);
            shippingAddress.put("postal_code", zipcode);
            shippingAddress.put("country_code", "IDN");
            customerDetails.put("shipping_address", shippingAddress);
        }

        DetailOrdersModel addDetailOrders = new DetailOrdersModel();
        addDetailOrders.setOrderid(masterorderid);
        addDetailOrders.setTotalweight(weight);
        addDetailOrders.setDeliveryprice(deliveryprice);
        addDetailOrders.setTotalprice(totalprice);
        addDetailOrders.setPaymentdate(Timestamp.valueOf(LocalDateTime.now()));
        log.info("ini data detail order {}", addDetailOrders);
        detailOrdersRepository.save(addDetailOrders);

        String token = snapApi.createTransactionToken(params);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        logger.info(String.valueOf(response));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getCustData")
    public ResponseEntity<?> getCustData(@RequestParam(name = "id") int id) {
        boolean cekId = false;
        List<CustomerModel> getAllCust = customerRepository.findAll();
        for (int i = 0; i < getAllCust.size(); i++) {
            if (getAllCust.get(i).getId() == id) {
                cekId = true;
                break;
            }
        }
        if (cekId) {
            logger.info(String.valueOf(getAllCust));
            return ResponseEntity.ok(getAllCust);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + id);
        }
    }

    @GetMapping("/getOrderData")
    public ResponseEntity<?> getOrderData(@RequestParam(name = "id") String orderid) {
        TemporaryOrderModel temporaryOrderModel = temporaryOrderRepository.findByOrderid(orderid);
        String masterOrderId = temporaryOrderModel.getMasterorderid();
        List<TemporaryOrderModel> listTemporaryOrder = temporaryOrderRepository.findAllByMasterorderid(masterOrderId);
        // Hitung total harga dan total berat
        int totalPrice = listTemporaryOrder.stream().mapToInt(TemporaryOrderModel::getTotalprice).sum();
        int totalWeight = listTemporaryOrder.stream().mapToInt(TemporaryOrderModel::getTotalweight).sum();

        // Buat map untuk mengembalikan data
        Map<String, Object> result = new HashMap<>();
        result.put("listTempOrder", listTemporaryOrder);
        result.put("totalPrice", totalPrice);
        result.put("totalWeight", totalWeight);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAddressData")
    public ResponseEntity<?> getAddressData(@RequestParam(name = "id") int id) {
        List<AddressModel> getAddress = addressRepository.findAll();
        if (getAddress.isEmpty()) {
            return ResponseEntity.badRequest().body("tidak ada data");
        }

        // Memfilter alamat berdasarkan id pelanggan
        List<AddressModel> filteredAddress = getAddress.stream()
                .filter(address -> address.getCustomer().getId() == id)
                .collect(Collectors.toList());

        if (!filteredAddress.isEmpty()) {
            logger.info("address ditemukan");
            logger.info(String.valueOf(filteredAddress));
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
            AddressModel addressModel = new AddressModel();
            addressModel.setAddressname(addressname);
            addressModel.setAddressdetail(addressdetail);
            addressModel.setCity(city);
            addressModel.setState(state);
            addressModel.setZipcode(zipcode);
            addressModel.setNote(note);
            addressModel.setCustomer(new CustomerModel(customerid));
            addressModel.setCityid(cityid);
            addressRepository.save(addressModel);
            return ResponseEntity.ok("Berhasil Menambah Address");
        }
        else {
            Optional<AddressModel> optionalAddressModel = addressRepository.findById(id);
            if (optionalAddressModel.isPresent()) {
                AddressModel getAddress = optionalAddressModel.get();
                getAddress.setAddressname(addressname);
                getAddress.setAddressdetail(addressdetail);
                getAddress.setCity(city);
                getAddress.setState(state);
                getAddress.setZipcode(zipcode);
                getAddress.setNote(note);
                getAddress.setCityid(cityid);
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
