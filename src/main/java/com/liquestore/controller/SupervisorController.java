package com.liquestore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liquestore.dto.Response;
import com.liquestore.model.Attendance;
import com.liquestore.model.Employee;
import com.liquestore.model.Item;
import com.liquestore.model.Order;
import com.liquestore.model.Type;
import com.liquestore.repository.AbsensiRepository;
import com.liquestore.repository.ItemRepository;
import com.liquestore.repository.OrdersRepository;
import com.liquestore.repository.TypeRepository;
import com.liquestore.service.FileStorageService;
import com.liquestore.service.LoginService;
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
        List<Employee> getPasscode = loginService.getEmployeesByAccessRightId(1);
        for (Employee employee : getPasscode) {
            String nomorwa = employee.getPhoneNumber();
            String lastFourDigits = nomorwa.substring(nomorwa.length() - 4);
            logger.info(lastFourDigits);
            if (lastFourDigits.equals(extractedPasscode)) {
                cekPasscode = true;
                tempUsername = employee.getUsername();
                tempId = employee.getId();
            }
        }
        if (cekPasscode) {
            List<Employee> getEmployee = loginService.getUsersByUsername(tempUsername);
            List<Attendance> getAbsensi = absensiRepository.getAbsensiByEmployeeid(tempId);
            if (!getAbsensi.isEmpty()) {
                for (int i = 0; i < getAbsensi.size(); i++) {
                    if (getAbsensi.get(i).getDate().equals(Date.valueOf(LocalDate.now()))) {
                        cekTanggal = true;
                        idxAbsensi = i;
                    }
                }
                if (cekTanggal) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(Response.builder()
                                    .message(tempUsername + " Sudah Melakukan ClockIn")
                                    .build());
                }
                else {
                    Attendance absensi = new Attendance();
                    absensi.setEmployeeId(getEmployee.get(0).getId());
                    absensi.setUsername(getEmployee.get(0).getUsername());
                    absensi.setPassword(getEmployee.get(0).getPassword());
                    absensi.setClockIn(Timestamp.valueOf(LocalDateTime.now()));
                    absensi.setDate(Date.valueOf(LocalDate.now()));
                    absensiRepository.save(absensi);
                }
            }
            else {
                logger.info("bikin baru");
                Attendance absensi = new Attendance();
                absensi.setEmployeeId(getEmployee.get(0).getId());
                absensi.setUsername(getEmployee.get(0).getUsername());
                absensi.setPassword(getEmployee.get(0).getPassword());
                absensi.setClockIn(Timestamp.valueOf(LocalDateTime.now()));
                absensi.setDate(Date.valueOf(LocalDate.now()));
                absensiRepository.save(absensi);
            }
            return ResponseEntity.ok(Response.builder()
                    .message("Berhasil Clock In, " + tempUsername)
                    .build());
        }
        else {
            logger.info(String.valueOf(Date.valueOf(LocalDate.now())));
            logger.info("admin tidak ditermukan");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Response.builder()
                            .message("Passcode Salah")
                            .build());
        }
    }

    @PostMapping("/clockout")
    public ResponseEntity<?> clockout(@RequestBody String passcode) throws JsonProcessingException {
        cekPasscode = false;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(passcode);
        String extractedPasscode = jsonNode.get("passcode").asText();
        logger.info("inputan passcode " + extractedPasscode);
        List<Employee> getPasscode = loginService.getEmployeesByAccessRightId(1);
        for (Employee employee : getPasscode) {
            String nomorwa = employee.getPhoneNumber();
            String lastFourDigits = nomorwa.substring(nomorwa.length() - 4);
            logger.info(lastFourDigits);
            if (lastFourDigits.equals(extractedPasscode)) {
                cekPasscode = true;
                tempUsername = employee.getUsername();
                tempId = employee.getId();
            }
        }
        if (cekPasscode) {
            List<Employee> getEmployee = loginService.getUsersByUsername(tempUsername);
            List<Attendance> getAbsensi = absensiRepository.getAbsensiByEmployeeid(tempId);
            if (!getAbsensi.isEmpty()) {
                for (int i = 0; i < getAbsensi.size(); i++) {
                    if (getAbsensi.get(i).getDate().equals(Date.valueOf(LocalDate.now()))) {
                        cekTanggal = true;
                        idxAbsensi = i;
                    }
                }
                if (cekTanggal) {
                    getAbsensi.get(idxAbsensi).setClockOut(Timestamp.valueOf(LocalDateTime.now()));
                    Timestamp temp = getAbsensi.get(idxAbsensi).getClockOut();
                    logger.info(String.valueOf(temp));
                    absensiRepository.save(getAbsensi.get(idxAbsensi));

                    return ResponseEntity.ok(Response.builder()
                            .message("Berhasil Clock Out, " + tempUsername)
                            .build());
                }
                else {
                    Attendance absensi = new Attendance();
                    absensi.setEmployeeId(getEmployee.get(0).getId());
                    absensi.setUsername(getEmployee.get(0).getUsername());
                    absensi.setPassword(getEmployee.get(0).getPassword());
                    absensi.setClockIn(Timestamp.valueOf(LocalDateTime.now()));
                    absensi.setDate(Date.valueOf(LocalDate.now()));
                    absensiRepository.save(absensi);
                }
            }
            else {
                logger.info("bikin baru");
                Attendance absensi = new Attendance();
                absensi.setEmployeeId(getEmployee.get(0).getId());
                absensi.setUsername(getEmployee.get(0).getUsername());
                absensi.setPassword(getEmployee.get(0).getPassword());
                absensi.setClockIn(Timestamp.valueOf(LocalDateTime.now()));
                absensi.setDate(Date.valueOf(LocalDate.now()));
                absensiRepository.save(absensi);
            }
            return ResponseEntity.ok(Response.builder()
                    .message("Berhasil Clock In, " + tempUsername)
                    .build());
        }
        else {
            logger.info(String.valueOf(Date.valueOf(LocalDate.now())));
            logger.info("admin tidak ditermukan");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Response.builder()
                            .message("Passcode Salah")
                            .build());
        }
    }

    @GetMapping("/dataInventori")
    public ResponseEntity<?> dataInventori() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<Item> getAllItem = itemRepository.findAll();
        logger.info(String.valueOf(getAllItem));
        List<Map<String, Object>> itemData = getAllItem.stream().map(item -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("id", item.getId());
            empData.put("nama", item.getName());
            empData.put("jenisBarang", item.getTypeId().getName());
            empData.put("customWeight", item.getCustomWeight());
            empData.put("customCapitalPrice", item.getCustomCapitalPrice());
            empData.put("customDefaultPrice", item.getCustomDefaultPrice());
            empData.put("size", item.getSize());
            Timestamp lastUpdateDate = item.getUpdatedDate();
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
        List<Type> getAllType = typeRepository.findAll();
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
        Optional<Type> optionalTypeModel = typeRepository.findById(typeId);
        String itemCode;
        if (optionalTypeModel.isPresent()) {
            Type getTypeData = optionalTypeModel.get();
            LocalDate currentDate = LocalDate.now();
            // Dapatkan dua digit terakhir dari tahun dan bulan saat ini
            int year = currentDate.getYear();
            String yearString = String.valueOf(year).substring(2); // Mendapatkan dua digit terakhir dari tahun
            String monthString =
                    String.format("%02d", currentDate.getMonthValue()); // Mendapatkan bulan dengan dua digit
            String prefix = getTypeData.getCode() + yearString + monthString;
            List<Item> existingTypeCode = itemRepository.findByItemcodeStartingWith(prefix);
            String sequenceString = String.format("%05d", existingTypeCode.size() + 1);
            logger.info(sequenceString);
            itemCode = prefix + sequenceString;
            logger.info(itemCode);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + typeId);
        }
        List<String> fileNames = fileStorageService.storeFiles(files);
        Item item = new Item();
        item.setName(name);
        item.setTypeId(new Type(typeId));
        item.setEmployeeId(new Employee(employeeId));
        item.setCode(itemCode);
        item.setCustomWeight(customWeight);
        item.setCustomCapitalPrice(customCapitalPrice);
        item.setCustomDefaultPrice(customDefaultPrice);
        item.setSize(size);
        item.setFiles(fileNames);
        item.setStatus("available");
        Item savedItem = itemRepository.save(item);
        logger.info(String.valueOf(savedItem));
        return ResponseEntity.ok(savedItem);
    }

    @GetMapping("/dataTipe")
    public ResponseEntity<?> dataTipe() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<Type> getAllTipe = typeRepository.findAll();
        logger.info(String.valueOf(getAllTipe));
        List<Map<String, Object>> itemData = getAllTipe.stream().map(item -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("id", item.getId());
            empData.put("nama", item.getName());
            empData.put("varian", item.getVariant());
            empData.put("weight", item.getWeight());
            Timestamp lastUpdateDate = item.getUpdatedDate();
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
    public ResponseEntity<?> tambahTipe(@RequestBody Type type) {
        // Ambil huruf pertama dari nama
        char firstNameLetter = type.getName().toUpperCase().charAt(0);
        char firstVariantLetter = type.getVariant().toUpperCase().charAt(0);
        String varcharName = String.valueOf(firstNameLetter);
        String varcharVariant = String.valueOf(firstVariantLetter);
        String tipeKode = varcharName + varcharVariant;
        Type addType = new Type();
        addType.setName(type.getName());
        addType.setWeight(type.getWeight());
        addType.setVariant(type.getVariant());
        addType.setCode(tipeKode);
        typeRepository.save(addType);
        logger.info(String.valueOf(addType));
        return ResponseEntity.ok(addType);
    }

    @PostMapping("/editTipe")
    public ResponseEntity<?> editTipe(@RequestBody Type type) {
        Optional<Type> optionalTypeModel = typeRepository.findById(type.getId());
        if (optionalTypeModel.isPresent()) {
            Type changeType = optionalTypeModel.get();
            changeType.setName(type.getName());
            changeType.setVariant(type.getVariant());
            changeType.setWeight(type.getWeight());
            changeType.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            char firstNameLetter = type.getName().toUpperCase().charAt(0);
            char firstVariantLetter = type.getVariant().toUpperCase().charAt(0);
            String varcharName = String.valueOf(firstNameLetter);
            String varcharVariant = String.valueOf(firstVariantLetter);
            String tipeKode = varcharName + varcharVariant;
            changeType.setCode(tipeKode);
            typeRepository.save(changeType);
            return ResponseEntity.ok(changeType);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + type.getId());
        }
    }

    @DeleteMapping("/deleteTipe/{id}")
    public ResponseEntity<?> deleteTipe(@PathVariable int id) {
        Optional<Type> optType = typeRepository.findById(id);

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
        List<Order> getAllOrders = ordersRepository.findAll();
        List<Item> getAllItem = itemRepository.findAll();
        // Create a map of item codes to item names
        Map<String, String> itemCodeToNameMap = getAllItem.stream()
                .collect(Collectors.toMap(Item::getCode, Item::getName));
        List<Map<String, Object>> orderData = getAllOrders.stream().map(orders -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("orderid", orders.getId());
            String itemName = orders.getItemIdList().stream()
                    .map(itemCodeToNameMap::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("Unknown Item");
            empData.put("namabarang", itemName);
            empData.put("namacust", orders.getUsername());
            Timestamp checkoutdate = orders.getCheckoutDate();
            LocalDateTime firstJoinDateTime = LocalDateTime.ofInstant(checkoutdate.toInstant(), ZoneId.systemDefault());
            logger.info(String.valueOf(firstJoinDateTime));
            empData.put("checkoutdate", firstJoinDateTime.format(dateFormatter));
            empData.put("packingdate", orders.getPackingDate());
            empData.put("deliverydate", orders.getDeliveryPickupDate());
            return empData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }

    @PostMapping("/updatePackingdate")
    public ResponseEntity<?> updatePackingdate(@RequestParam(name = "rowId") String id) {
        Optional<Order> optionalOrdersModel = ordersRepository.findById(id);
        logger.info(String.valueOf(optionalOrdersModel));
        if (optionalOrdersModel.isPresent()) {
            Order getSelectedOrder = optionalOrdersModel.get();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            getSelectedOrder.setPackingDate(timestamp);
            ordersRepository.save(getSelectedOrder);
            return ResponseEntity.ok(getSelectedOrder);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/updateDeliverydate")
    public ResponseEntity<?> updateDeliverydate(@RequestParam(name = "rowId") String id) {
        Optional<Order> optionalOrdersModel = ordersRepository.findById(id);
        logger.info(String.valueOf(optionalOrdersModel));
        if (optionalOrdersModel.isPresent()) {
            Order getSelectedOrder = optionalOrdersModel.get();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            getSelectedOrder.setDeliveryPickupDate(timestamp);
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
        List<Order> getAllOrders = ordersRepository.findAll();
        List<Item> getAllItem = itemRepository.findAll();
        // Create a map of item codes to item names
        Map<String, String> itemCodeToNameMap = getAllItem.stream()
                .collect(Collectors.toMap(Item::getCode, Item::getName));
        List<Map<String, Object>> orderData = getAllOrders.stream().map(orders -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("orderid", orders.getId());
            String[] itemDetails = orders.getItemIdList().stream()
                    .map(itemCode -> {
                        Item item = getAllItem.stream()
                                .filter(i -> i.getCode().equals(itemCode))
                                .findFirst()
                                .orElse(null);
                        if (item == null) {
                            return new String[] {"Unknown Item", "Unknown Type"};
                        }
                        Optional<Type> getSelectedType = typeRepository.findById(item.getTypeId().getId());
                        String typeName = "";
                        if (getSelectedType.isPresent()) {
                            Type type = getSelectedType.get();
                            typeName = type.getName();
                        }
                        String itemName = itemCodeToNameMap.getOrDefault(itemCode, "Unknown Item");
                        return new String[] {itemName, typeName};
                    })
                    .findFirst()
                    .orElse(new String[] {"Unknown Item", "Unknown Type"});
            empData.put("namabarang", itemDetails[0]);
            empData.put("jenisbarang", itemDetails[1]);
            empData.put("namapembeli", orders.getUsername());
            Timestamp checkoutdate = orders.getCheckoutDate();
            LocalDateTime checkoutDateTime = LocalDateTime.ofInstant(checkoutdate.toInstant(), ZoneId.systemDefault());
            Timestamp paymentdate = orders.getCheckoutDate();
            LocalDateTime paymentDateTime = LocalDateTime.ofInstant(paymentdate.toInstant(), ZoneId.systemDefault());
            Timestamp packingdate = orders.getCheckoutDate();
            LocalDateTime packingDateTime = LocalDateTime.ofInstant(packingdate.toInstant(), ZoneId.systemDefault());
            Timestamp deliverypickupdate = orders.getCheckoutDate();
            LocalDateTime deliverypickupDateTime =
                    LocalDateTime.ofInstant(deliverypickupdate.toInstant(), ZoneId.systemDefault());
            Timestamp deliverydonedate = orders.getCheckoutDate();
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
