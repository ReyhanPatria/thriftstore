package com.LiqueStore.controller;

import com.LiqueStore.Response;
import com.LiqueStore.model.AbsensiModel;
import com.LiqueStore.model.EmployeeModel;
import com.LiqueStore.model.ItemModel;
import com.LiqueStore.model.OrdersModel;
import com.LiqueStore.model.TypeModel;
import com.LiqueStore.repository.AbsensiRepository;
import com.LiqueStore.repository.ItemRepository;
import com.LiqueStore.repository.OrdersRepository;
import com.LiqueStore.repository.TypeRepository;
import com.LiqueStore.service.FileStorageService;
import com.LiqueStore.service.LoginService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/backend/supervisor")
@CrossOrigin
public class SupervisorController {
    private static final Logger logger = Logger.getLogger(ManagerController.class.getName());
    boolean cekPasscode = false;
    String tempUsername;
    int tempId;
    boolean cekTanggal = false;
    int idxAbsensi = 0;

    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AbsensiRepository absensiRepository;
    @Autowired
    private OrdersRepository ordersRepository;

    @PostMapping("/clockin")
    public ResponseEntity<?> clockin(@RequestBody String passcode) throws JsonProcessingException {
        cekPasscode = false;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(passcode);
        String extractedPasscode = jsonNode.get("passcode").asText();
        logger.info("inputan passcode " + extractedPasscode);
        List<EmployeeModel> getPasscode = loginService.getEmployeesByAccessRightId(1);
        for (EmployeeModel employeeModel : getPasscode) {
            String nomorwa = employeeModel.getPhonenumber();
            String lastFourDigits = nomorwa.substring(nomorwa.length() - 4);
            logger.info(lastFourDigits);
            if (lastFourDigits.equals(extractedPasscode)) {
                cekPasscode = true;
                tempUsername = employeeModel.getUsername();
                tempId = employeeModel.getId();
            }
        }
        if (cekPasscode) {
            List<EmployeeModel> getEmployee = loginService.getUsersByUsername(tempUsername);
            List<AbsensiModel> getAbsensi = absensiRepository.getAbsensiByEmployeeid(tempId);
            if (!getAbsensi.isEmpty()) {
                for (int i = 0; i < getAbsensi.size(); i++) {
                    if (getAbsensi.get(i).getTodaydate().equals(Date.valueOf(LocalDate.now()))) {
                        cekTanggal = true;
                        idxAbsensi = i;
                    }
                }
                if (cekTanggal) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new Response(tempUsername + " Sudah Melakukan ClockIn"));
                }
                else {
                    AbsensiModel absensi = new AbsensiModel();
                    absensi.setEmployeeid(getEmployee.get(0).getId());
                    absensi.setUsername(getEmployee.get(0).getUsername());
                    absensi.setPassword(getEmployee.get(0).getPassword());
                    absensi.setClockin(Timestamp.valueOf(LocalDateTime.now()));
                    absensi.setTodaydate(Date.valueOf(LocalDate.now()));
                    absensiRepository.save(absensi);
                }
            }
            else {
                logger.info("bikin baru");
                AbsensiModel absensi = new AbsensiModel();
                absensi.setEmployeeid(getEmployee.get(0).getId());
                absensi.setUsername(getEmployee.get(0).getUsername());
                absensi.setPassword(getEmployee.get(0).getPassword());
                absensi.setClockin(Timestamp.valueOf(LocalDateTime.now()));
                absensi.setTodaydate(Date.valueOf(LocalDate.now()));
                absensiRepository.save(absensi);
            }
            return ResponseEntity.ok(new Response("Berhasil Clock In, " + tempUsername));
        }
        else {
            logger.info(String.valueOf(Date.valueOf(LocalDate.now())));
            logger.info("admin tidak ditermukan");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Passcode Salah"));
        }
    }

    @PostMapping("/clockout")
    public ResponseEntity<?> clockout(@RequestBody String passcode) throws JsonProcessingException {
        cekPasscode = false;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(passcode);
        String extractedPasscode = jsonNode.get("passcode").asText();
        logger.info("inputan passcode " + extractedPasscode);
        List<EmployeeModel> getPasscode = loginService.getEmployeesByAccessRightId(1);
        for (EmployeeModel employeeModel : getPasscode) {
            String nomorwa = employeeModel.getPhonenumber();
            String lastFourDigits = nomorwa.substring(nomorwa.length() - 4);
            logger.info(lastFourDigits);
            if (lastFourDigits.equals(extractedPasscode)) {
                cekPasscode = true;
                tempUsername = employeeModel.getUsername();
                tempId = employeeModel.getId();
            }
        }
        if (cekPasscode) {
            List<EmployeeModel> getEmployee = loginService.getUsersByUsername(tempUsername);
            List<AbsensiModel> getAbsensi = absensiRepository.getAbsensiByEmployeeid(tempId);
            if (!getAbsensi.isEmpty()) {
                for (int i = 0; i < getAbsensi.size(); i++) {
                    if (getAbsensi.get(i).getTodaydate().equals(Date.valueOf(LocalDate.now()))) {
                        cekTanggal = true;
                        idxAbsensi = i;
                    }
                }
                if (cekTanggal) {
                    getAbsensi.get(idxAbsensi).setClockout(Timestamp.valueOf(LocalDateTime.now()));
                    Timestamp temp = getAbsensi.get(idxAbsensi).getClockout();
                    logger.info(String.valueOf(temp));
                    absensiRepository.save(getAbsensi.get(idxAbsensi));

                    return ResponseEntity.ok(new Response("Berhasil Clock Out, " + tempUsername));
                }
                else {
                    AbsensiModel absensi = new AbsensiModel();
                    absensi.setEmployeeid(getEmployee.get(0).getId());
                    absensi.setUsername(getEmployee.get(0).getUsername());
                    absensi.setPassword(getEmployee.get(0).getPassword());
                    absensi.setClockin(Timestamp.valueOf(LocalDateTime.now()));
                    absensi.setTodaydate(Date.valueOf(LocalDate.now()));
                    absensiRepository.save(absensi);
                }
            }
            else {
                logger.info("bikin baru");
                AbsensiModel absensi = new AbsensiModel();
                absensi.setEmployeeid(getEmployee.get(0).getId());
                absensi.setUsername(getEmployee.get(0).getUsername());
                absensi.setPassword(getEmployee.get(0).getPassword());
                absensi.setClockin(Timestamp.valueOf(LocalDateTime.now()));
                absensi.setTodaydate(Date.valueOf(LocalDate.now()));
                absensiRepository.save(absensi);
            }
            return ResponseEntity.ok(new Response("Berhasil Clock In, " + tempUsername));
        }
        else {
            logger.info(String.valueOf(Date.valueOf(LocalDate.now())));
            logger.info("admin tidak ditermukan");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Passcode Salah"));
        }
    }

    @GetMapping("/dataInventori")
    public ResponseEntity<?> dataInventori() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<ItemModel> getAllItem = itemRepository.findAll();
        logger.info(String.valueOf(getAllItem));
        List<Map<String, Object>> itemData = getAllItem.stream().map(item -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("id", item.getId());
            empData.put("nama", item.getName());
            empData.put("jenisBarang", item.getTypeId().getNama());
            empData.put("customWeight", item.getCustomweight());
            empData.put("customCapitalPrice", item.getCustomcapitalprice());
            empData.put("customDefaultPrice", item.getCustomdefaultprice());
            empData.put("size", item.getSize());
            Timestamp lastUpdateDate = item.getLastupdate();
            if (lastUpdateDate != null) {
                LocalDateTime lastUpdateDateTime =
                        LocalDateTime.ofInstant(lastUpdateDate.toInstant(), ZoneId.systemDefault());
                empData.put("lastupdate", lastUpdateDateTime.format(dateFormatter));
            }
            else {
                empData.put("lastupdate", null);
            }
            empData.put("status", item.getStatus());
            return empData;
        }).collect(Collectors.toList());
        logger.info(String.valueOf(itemData));
        return ResponseEntity.ok(itemData);
    }

    @GetMapping("/daftarTipe")
    public ResponseEntity<?> daftarTipe() {
        List<TypeModel> getAllType = typeRepository.findAll();
        logger.info(String.valueOf(getAllType));
        return ResponseEntity.ok(getAllType);
    }

    @PostMapping(value = "/tambahInventori", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> tambahInventori(@RequestParam("name") String name,
            @RequestParam("typeId") int typeId,
            @RequestParam("employeeId") int employeeId,
            @RequestParam("customWeight") int customWeight,
            @RequestParam("customCapitalPrice") int customCapitalPrice,
            @RequestParam("customDefaultPrice") int customDefaultPrice,
            @RequestParam("size") int size,
            @RequestParam("files") List<MultipartFile> files) {
        Optional<TypeModel> optionalTypeModel = typeRepository.findById(typeId);
        String itemCode;
        if (optionalTypeModel.isPresent()) {
            TypeModel getTypeData = optionalTypeModel.get();
            LocalDate currentDate = LocalDate.now();
            // Dapatkan dua digit terakhir dari tahun dan bulan saat ini
            int year = currentDate.getYear();
            String yearString = String.valueOf(year).substring(2); // Mendapatkan dua digit terakhir dari tahun
            String monthString =
                    String.format("%02d", currentDate.getMonthValue()); // Mendapatkan bulan dengan dua digit
            String prefix = getTypeData.getTypecode() + yearString + monthString;
            List<ItemModel> existingTypeCode = itemRepository.findByItemcodeStartingWith(prefix);
            String sequenceString = String.format("%05d", existingTypeCode.size() + 1);
            logger.info(sequenceString);
            itemCode = prefix + sequenceString;
            logger.info(itemCode);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + typeId);
        }
        List<String> fileNames = fileStorageService.storeFiles(files);
        ItemModel itemModel = new ItemModel();
        itemModel.setName(name);
        itemModel.setTypeId(new TypeModel(typeId));
        itemModel.setEmployeeId(new EmployeeModel(employeeId));
        itemModel.setItemcode(itemCode);
        itemModel.setCustomweight(customWeight);
        itemModel.setCustomcapitalprice(customCapitalPrice);
        itemModel.setCustomdefaultprice(customDefaultPrice);
        itemModel.setSize(size);
        itemModel.setFiles(fileNames);
        itemModel.setStatus("available");
        ItemModel savedItem = itemRepository.save(itemModel);
        logger.info(String.valueOf(savedItem));
        return ResponseEntity.ok(savedItem);
    }

    @GetMapping("/dataTipe")
    public ResponseEntity<?> dataTipe() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<TypeModel> getAllTipe = typeRepository.findAll();
        logger.info(String.valueOf(getAllTipe));
        List<Map<String, Object>> itemData = getAllTipe.stream().map(item -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("id", item.getId());
            empData.put("nama", item.getNama());
            empData.put("varian", item.getVarian());
            empData.put("weight", item.getWeight());
            Timestamp lastUpdateDate = item.getLastupdate();
            if (lastUpdateDate != null) {
                LocalDateTime lastUpdateDateTime =
                        LocalDateTime.ofInstant(lastUpdateDate.toInstant(), ZoneId.systemDefault());
                empData.put("lastupdate", lastUpdateDateTime.format(dateFormatter));
            }
            else {
                empData.put("lastupdate", null);
            }
            return empData;
        }).collect(Collectors.toList());
        logger.info(String.valueOf(itemData));
        return ResponseEntity.ok(itemData);
    }

    @PostMapping("/tambahTipe")
    public ResponseEntity<?> tambahTipe(@RequestBody TypeModel typeModel) {
        // Ambil huruf pertama dari nama
        char firstNameLetter = typeModel.getNama().toUpperCase().charAt(0);
        char firstVariantLetter = typeModel.getVarian().toUpperCase().charAt(0);
        String varcharName = String.valueOf(firstNameLetter);
        String varcharVariant = String.valueOf(firstVariantLetter);
        String tipeKode = varcharName + varcharVariant;
        TypeModel addType = new TypeModel();
        addType.setNama(typeModel.getNama());
        addType.setWeight(typeModel.getWeight());
        addType.setVarian(typeModel.getVarian());
        addType.setTypecode(tipeKode);
        typeRepository.save(addType);
        logger.info(String.valueOf(addType));
        return ResponseEntity.ok(addType);
    }

    @PostMapping("/editTipe")
    public ResponseEntity<?> editTipe(@RequestBody TypeModel typeModel) {
        Optional<TypeModel> optionalTypeModel = typeRepository.findById(typeModel.getId());
        if (optionalTypeModel.isPresent()) {
            TypeModel changeType = optionalTypeModel.get();
            changeType.setNama(typeModel.getNama());
            changeType.setVarian(typeModel.getVarian());
            changeType.setWeight(typeModel.getWeight());
            changeType.setLastupdate(Timestamp.valueOf(LocalDateTime.now()));
            char firstNameLetter = typeModel.getNama().toUpperCase().charAt(0);
            char firstVariantLetter = typeModel.getVarian().toUpperCase().charAt(0);
            String varcharName = String.valueOf(firstNameLetter);
            String varcharVariant = String.valueOf(firstVariantLetter);
            String tipeKode = varcharName + varcharVariant;
            changeType.setTypecode(tipeKode);
            typeRepository.save(changeType);
            return ResponseEntity.ok(changeType);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + typeModel.getId());
        }
    }

    @DeleteMapping("/deleteTipe/{id}")
    public ResponseEntity<?> deleteTipe(@PathVariable int id) {
        Optional<TypeModel> optType = typeRepository.findById(id);

        if (optType.isPresent()) {
            typeRepository.deleteById(id);
            return ResponseEntity.ok().body("Type deleted successfully.");
        }
        else {
            return ResponseEntity.badRequest().body("Type not found with ID: " + id);
        }
    }

    @GetMapping("/dataOrder")
    public ResponseEntity<?> dataOrder() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<OrdersModel> getAllOrders = ordersRepository.findAll();
        List<ItemModel> getAllItem = itemRepository.findAll();
        // Create a map of item codes to item names
        Map<String, String> itemCodeToNameMap = getAllItem.stream()
                .collect(Collectors.toMap(ItemModel::getItemcode, ItemModel::getName));
        List<Map<String, Object>> orderData = getAllOrders.stream().map(orders -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("orderid", orders.getId());
            String itemName = orders.getItemidall().stream()
                    .map(itemCodeToNameMap::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("Unknown Item");
            empData.put("namabarang", itemName);
            empData.put("namacust", orders.getUsername());
            Timestamp checkoutdate = orders.getCheckoutdate();
            LocalDateTime firstJoinDateTime = LocalDateTime.ofInstant(checkoutdate.toInstant(), ZoneId.systemDefault());
            logger.info(String.valueOf(firstJoinDateTime));
            empData.put("checkoutdate", firstJoinDateTime.format(dateFormatter));
            empData.put("packingdate", orders.getPackingdate());
            empData.put("deliverydate", orders.getDeliverypickupdate());
            return empData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }

    @PostMapping("/updatePackingdate")
    public ResponseEntity<?> updatePackingdate(@RequestParam(name = "rowId") String id) {
        Optional<OrdersModel> optionalOrdersModel = ordersRepository.findById(id);
        logger.info(String.valueOf(optionalOrdersModel));
        if (optionalOrdersModel.isPresent()) {
            OrdersModel getSelectedOrder = optionalOrdersModel.get();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            getSelectedOrder.setPackingdate(timestamp);
            ordersRepository.save(getSelectedOrder);
            return ResponseEntity.ok(getSelectedOrder);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/updateDeliverydate")
    public ResponseEntity<?> updateDeliverydate(@RequestParam(name = "rowId") String id) {
        Optional<OrdersModel> optionalOrdersModel = ordersRepository.findById(id);
        logger.info(String.valueOf(optionalOrdersModel));
        if (optionalOrdersModel.isPresent()) {
            OrdersModel getSelectedOrder = optionalOrdersModel.get();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            getSelectedOrder.setDeliverypickupdate(timestamp);
            ordersRepository.save(getSelectedOrder);
            return ResponseEntity.ok(getSelectedOrder);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
        List<OrdersModel> getAllOrders = ordersRepository.findAll();
        List<ItemModel> getAllItem = itemRepository.findAll();
        // Create a map of item codes to item names
        Map<String, String> itemCodeToNameMap = getAllItem.stream()
                .collect(Collectors.toMap(ItemModel::getItemcode, ItemModel::getName));
        List<Map<String, Object>> orderData = getAllOrders.stream().map(orders -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("orderid", orders.getId());
            String[] itemDetails = orders.getItemidall().stream()
                    .map(itemCode -> {
                        ItemModel item = getAllItem.stream()
                                .filter(i -> i.getItemcode().equals(itemCode))
                                .findFirst()
                                .orElse(null);
                        if (item == null) {
                            return new String[] {"Unknown Item", "Unknown Type"};
                        }
                        Optional<TypeModel> getSelectedType = typeRepository.findById(item.getTypeId().getId());
                        String typeName = "";
                        if (getSelectedType.isPresent()) {
                            TypeModel typeModel = getSelectedType.get();
                            typeName = typeModel.getNama();
                        }
                        String itemName = itemCodeToNameMap.getOrDefault(itemCode, "Unknown Item");
                        return new String[] {itemName, typeName};
                    })
                    .findFirst()
                    .orElse(new String[] {"Unknown Item", "Unknown Type"});
            empData.put("namabarang", itemDetails[0]);
            empData.put("jenisbarang", itemDetails[1]);
            empData.put("namapembeli", orders.getUsername());
            Timestamp checkoutdate = orders.getCheckoutdate();
            LocalDateTime checkoutDateTime = LocalDateTime.ofInstant(checkoutdate.toInstant(), ZoneId.systemDefault());
            Timestamp paymentdate = orders.getCheckoutdate();
            LocalDateTime paymentDateTime = LocalDateTime.ofInstant(paymentdate.toInstant(), ZoneId.systemDefault());
            Timestamp packingdate = orders.getCheckoutdate();
            LocalDateTime packingDateTime = LocalDateTime.ofInstant(packingdate.toInstant(), ZoneId.systemDefault());
            Timestamp deliverypickupdate = orders.getCheckoutdate();
            LocalDateTime deliverypickupDateTime =
                    LocalDateTime.ofInstant(deliverypickupdate.toInstant(), ZoneId.systemDefault());
            Timestamp deliverydonedate = orders.getCheckoutdate();
            LocalDateTime deliverydoneDateTime =
                    LocalDateTime.ofInstant(deliverydonedate.toInstant(), ZoneId.systemDefault());
            empData.put("checkoutdate", checkoutDateTime.format(dateFormatter));
            empData.put("paymentdate", paymentDateTime.format(dateFormatter));
            empData.put("packingdate", packingDateTime.format(dateFormatter));
            empData.put("deliverypickupdate", deliverypickupDateTime.format(dateFormatter));
            empData.put("deliverydonedate", deliverydoneDateTime.format(dateFormatter));
            empData.put("status", orders.getStatus());
            return empData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }
}
