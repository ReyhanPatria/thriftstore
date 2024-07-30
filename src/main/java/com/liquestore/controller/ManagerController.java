package com.liquestore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liquestore.dto.Response;
import com.liquestore.model.Attendance;
import com.liquestore.model.AccessRight;
import com.liquestore.model.Employee;
import com.liquestore.model.Item;
import com.liquestore.model.Order;
import com.liquestore.model.Type;
import com.liquestore.repository.AbsensiRepository;
import com.liquestore.repository.AccessRightRepository;
import com.liquestore.repository.EmployeeRepository;
import com.liquestore.repository.ItemRepository;
import com.liquestore.repository.OrdersRepository;
import com.liquestore.repository.TypeRepository;
import com.liquestore.service.FileStorageService;
import com.liquestore.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
@RequestMapping("/backend/manager")
@CrossOrigin
public class ManagerController {
    private static final Logger logger = Logger.getLogger(ManagerController.class.getName());
    boolean cekPasscode = false;
    String tempUsername;
    int tempId;
    boolean cekTanggal = false;
    int idxAbsensi = 0;

    @Autowired
    private LoginService loginService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AbsensiRepository absensiRepository;
    @Autowired
    private AccessRightRepository accessRightRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FileStorageService fileStorageService;

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
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.builder()
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

    @GetMapping("/dataKaryawan")
    public ResponseEntity<?> getAllEmployees() {
        boolean cekManager = false;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<Employee> allEmployee = employeeRepository.findAll();
        List<Employee> getAdminOnly = loginService.getEmployeesByAccessRightId(3);

        // Memeriksa apakah ada manager dalam daftar karyawan
        for (Employee employee : allEmployee) {
            if (employee.getAccessRightId().getId() == 3) {
                cekManager = true;
                break;
            }
        }

        List<Employee> filteredEmployees;
        if (!cekManager) {
            // Jika tidak ada manager, gunakan getAdminOnly
            filteredEmployees = getAdminOnly;
        }
        else {
            // Jika ada manager, filter out all employees with accessrightid = 3
            filteredEmployees = allEmployee.stream()
                    .filter(employee -> employee.getAccessRightId().getId() != 3)
                    .collect(Collectors.toList());
        }

        // Memetakan hasil filter menjadi Map
        List<Map<String, Object>> employeeData = filteredEmployees.stream()
                //                    .filter(employee -> {
                //                    if ("inactive".equalsIgnoreCase(employee.getStatus())) {
                //                        // Jika status 'inactive', cetak pesan dan kembalikan false untuk memfilter data ini
                //                        logger.info("Employee with ID " + employee.getId() + " is inactive and will not be included.");
                //                        return false;
                //                    }
                //                    return true;
                //                }
                .map(employee -> {
                    Map<String, Object> empData = new HashMap<>();
                    empData.put("id", employee.getId());
                    empData.put("username", employee.getUsername());
                    empData.put("fullname", employee.getFullName());
                    empData.put("email", employee.getEmail());
                    LocalDate birthDate = employee.getBirthdate().toLocalDate();
                    empData.put("tanggallahir", birthDate.format(dateFormatter));
                    empData.put("umur", Period.between(birthDate, LocalDate.now()).getYears());
                    empData.put("nomorwa", employee.getPhoneNumber());
                    empData.put("jam_masuk", employee.getClockIn());
                    empData.put("jadwal_libur", employee.getHolidaySchedule());
                    empData.put("status", employee.getStatus());
                    Timestamp firstJoinDate = employee.getCreatedDate();
                    LocalDateTime firstJoinDateTime =
                            LocalDateTime.ofInstant(firstJoinDate.toInstant(), ZoneId.systemDefault());
                    empData.put("firstjoindate", firstJoinDateTime.format(dateFormatter));
                    Timestamp lastUpdateDate = employee.getUpdatedDate();
                    if (lastUpdateDate != null) {
                        LocalDateTime lastUpdateDateTime =
                                LocalDateTime.ofInstant(lastUpdateDate.toInstant(), ZoneId.systemDefault());
                        empData.put("lastupdate", lastUpdateDateTime.format(dateFormatter));
                    }
                    else {
                        empData.put("lastupdate", null); // or some default value
                    }
                    empData.put("jabatan", employee.getAccessRightId().getPosition());
                    return empData;
                }).collect(Collectors.toList());

        logger.info(String.valueOf(employeeData));
        return ResponseEntity.ok(employeeData);
    }


    @PostMapping("/tambahKaryawan")
    public ResponseEntity<?> tambahKaryawan(@RequestBody Employee employee) {
        Employee existingUsername = employeeRepository.findByUsername(employee.getUsername());
        if (existingUsername != null) {
            return ResponseEntity.badRequest()
                    .body(Response.builder()
                            .message("Username sudah digunakan")
                            .build());
        }
        Employee existingEmail = employeeRepository.findByEmail(employee.getEmail());
        if (existingEmail != null) {
            return ResponseEntity.badRequest()
                    .body(Response.builder()
                            .message("Email sudah digunakan")
                            .build());
        }
        AccessRight accessRight = AccessRight.builder()
                .id(employee.getAccessRightId().getId())
                .build();
        Employee addEmployee = new Employee();
        addEmployee.setFullName(employee.getFullName());
        addEmployee.setAccessRightId(accessRight);
        addEmployee.setBirthdate(employee.getBirthdate());
        addEmployee.setPhoneNumber(employee.getPhoneNumber());
        addEmployee.setEmail(employee.getEmail());
        addEmployee.setUsername(employee.getUsername());
        addEmployee.setCreatedDate(employee.getCreatedDate());
        addEmployee.setClockIn(employee.getClockIn());
        addEmployee.setHolidaySchedule(employee.getHolidaySchedule());
        addEmployee.setPassword(passwordEncoder.encode("123"));
        addEmployee.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        addEmployee.setStatus("active");
        employeeRepository.save(addEmployee);
        logger.info(String.valueOf(addEmployee));
        return ResponseEntity.ok(addEmployee);
    }

    @GetMapping("/getRolesKaryawan")
    public ResponseEntity<?> getRolesKaryawan() {
        List<AccessRight> getAllRoles = accessRightRepository.findAll();
        logger.info(String.valueOf(getAllRoles));
        return ResponseEntity.ok(getAllRoles);
    }

    @GetMapping("/getEditDataKaryawan")
    public ResponseEntity<?> getEditDataKaryawan(@RequestParam(name = "idEmployee") String idEmployee) {
        Optional<Employee> getSelectedEmployee = employeeRepository.findById(Integer.valueOf(idEmployee));
        if (getSelectedEmployee.isPresent()) {
            return ResponseEntity.ok(getSelectedEmployee);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/editKaryawan")
    public ResponseEntity<?> editKaryawan(@RequestBody Employee employeeModel) {
        //        String hashedPassword = passwordEncoder.encode(employeeModel.getPassword());
        AccessRight accessRight = AccessRight.builder()
                .id(employeeModel.getAccessRightId().getId())
                .build();
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeModel.getId());
        logger.info(String.valueOf(employeeModel.getId()));
        logger.info(String.valueOf(optionalEmployee));
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setUsername(employeeModel.getUsername());
            employee.setFullName(employeeModel.getFullName());
            employee.setBirthdate(employeeModel.getBirthdate());
            employee.setPhoneNumber(employeeModel.getPhoneNumber());
            employee.setAccessRightId(accessRight);
            employee.setEmail(employeeModel.getEmail());
            employee.setCreatedDate(employeeModel.getCreatedDate());
            employee.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            employee.setClockIn(employeeModel.getClockIn());
            employee.setHolidaySchedule(employeeModel.getHolidaySchedule());
            employee.setStatus(employeeModel.getStatus());
            employeeRepository.save(employee);
            return ResponseEntity.ok(employee);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + employeeModel.getId());
        }
    }

    @DeleteMapping("/deleteKaryawan/{id}")
    public ResponseEntity<?> deleteKaryawan(@PathVariable int id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setStatus("inactive");
            employeeRepository.save(employee);
            return ResponseEntity.ok().body("Employee deleted successfully.");
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + id);
        }
    }

    @GetMapping("/daftarSelectKaryawan")
    public ResponseEntity<?> daftarSelectKaryawan() {
        List<Employee> listKaryawan = loginService.getEmployeesByAccessRightId(1);
        return ResponseEntity.ok(listKaryawan);
        //
    }

    @GetMapping("/pilihKaryawan")
    public ResponseEntity<?> pilihKaryawan(@RequestParam(name = "idAbsensi") String idAbsensi) {
        logger.info(idAbsensi);
        SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        List<Attendance> pilihKaryawan = absensiRepository.getAbsensiByEmployeeid(Integer.parseInt(idAbsensi));
        if (pilihKaryawan == null) {
            return ResponseEntity.ok(Response.builder()
                    .message("Data Karyawan null")
                    .build());
        }
        else {
            logger.info(String.valueOf(pilihKaryawan));
            List<Map<String, Object>> employeeData = pilihKaryawan.stream().map(absensi -> {
                Map<String, Object> empData = new HashMap<>();
                empData.put("id", absensi.getId());
                empData.put("username", absensi.getUsername());
                empData.put("tanggal", dateFormat.format(absensi.getDate()));
                if (absensi.getClockIn() != null) {
                    empData.put("clockIn", clockFormat.format(absensi.getClockIn()));
                }
                else {
                    empData.put("clockIn", "");
                }
                if (absensi.getClockOut() != null) {
                    empData.put("clockOut", clockFormat.format(absensi.getClockOut()));
                }
                else {
                    empData.put("clockOut", "");
                }
                Optional<Employee> optionalEmployeeModel = employeeRepository.findById(Integer.valueOf(idAbsensi));
                if (optionalEmployeeModel.isPresent()) {
                    logger.info("data masuk");
                    Employee employee = optionalEmployeeModel.get();
                    Time jamMasuk = employee.getClockIn();
                    String formattedJamMasuk = jamMasuk.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    empData.put("jam_masuk", formattedJamMasuk);
                    empData.put("jadwal_libur", employee.getHolidaySchedule());
                }
                else {
                    empData.put("jam_masuk", "");
                    empData.put("jadwal_libur", "");
                }
                return empData;
            }).collect(Collectors.toList());
            logger.info(String.valueOf(employeeData));
            return ResponseEntity.ok(employeeData);
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
            empData.put("checkoutdate", checkoutDateTime.format(dateFormatter));
            Timestamp paymentdate = orders.getPaymentDate();
            empData.put("paymentdate",
                    paymentdate != null ? LocalDateTime.ofInstant(paymentdate.toInstant(), ZoneId.systemDefault())
                            .format(dateFormatter) : null);

            Timestamp packingdate = orders.getPackingDate();
            empData.put("packingdate",
                    packingdate != null ? LocalDateTime.ofInstant(packingdate.toInstant(), ZoneId.systemDefault())
                            .format(dateFormatter) : "null");

            Timestamp deliverypickupdate = orders.getDeliveryPickupDate();
            empData.put("deliverypickupdate",
                    deliverypickupdate != null ? LocalDateTime.ofInstant(deliverypickupdate.toInstant(),
                            ZoneId.systemDefault()).format(dateFormatter) : null);

            Timestamp deliverydonedate = orders.getDeliveryCompletionDate();
            empData.put("deliverydonedate",
                    deliverydonedate != null ? LocalDateTime.ofInstant(deliverydonedate.toInstant(),
                            ZoneId.systemDefault()).format(dateFormatter) : null);

            empData.put("status", orders.getStatus());
            return empData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }

    @PostMapping("/editOrderDelivery")
    public ResponseEntity<?> editOrderDelivery(@RequestBody Order order) {
        Optional<Order> optionalOrdersModel = ordersRepository.findById(order.getId());
        if (optionalOrdersModel.isPresent()) {
            Order editOrder = optionalOrdersModel.get();
            editOrder.setCheckoutDate(order.getCheckoutDate());
            editOrder.setPaymentDate(order.getPaymentDate());
            editOrder.setPackingDate(order.getPackingDate());
            editOrder.setDeliveryPickupDate(order.getDeliveryPickupDate());
            editOrder.setDeliveryCompletionDate(order.getDeliveryCompletionDate());
            editOrder.setStatus(order.getStatus());
            ordersRepository.save(editOrder);
            return ResponseEntity.ok(editOrder);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + order.getId());
        }
    }

    @DeleteMapping("/deleteOrderDelivery/{id}")
    public ResponseEntity<?> deleteOrderDelivery(@PathVariable String id) {
        Optional<Order> optOrder = ordersRepository.findById(id);

        if (optOrder.isPresent()) {
            ordersRepository.deleteById(id);
            return ResponseEntity.ok().body("Type deleted successfully.");
        }
        else {
            return ResponseEntity.badRequest().body("Type not found with ID: " + id);
        }
    }
}
