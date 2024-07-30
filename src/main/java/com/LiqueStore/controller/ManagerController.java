package com.LiqueStore.controller;

import com.LiqueStore.Response;
import com.LiqueStore.model.AbsensiModel;
import com.LiqueStore.model.AccessRightModel;
import com.LiqueStore.model.EmployeeModel;
import com.LiqueStore.model.ItemModel;
import com.LiqueStore.model.OrdersModel;
import com.LiqueStore.model.TypeModel;
import com.LiqueStore.repository.AbsensiRepository;
import com.LiqueStore.repository.AccessRightRepository;
import com.LiqueStore.repository.EmployeeRepository;
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

    @GetMapping("/dataKaryawan")
    public ResponseEntity<?> getAllEmployees() {
        boolean cekManager = false;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<EmployeeModel> allEmployee = employeeRepository.findAll();
        List<EmployeeModel> getAdminOnly = loginService.getEmployeesByAccessRightId(3);

        // Memeriksa apakah ada manager dalam daftar karyawan
        for (EmployeeModel employee : allEmployee) {
            if (employee.getAccessRight().getId() == 3) {
                cekManager = true;
                break;
            }
        }

        List<EmployeeModel> filteredEmployees;
        if (!cekManager) {
            // Jika tidak ada manager, gunakan getAdminOnly
            filteredEmployees = getAdminOnly;
        }
        else {
            // Jika ada manager, filter out all employees with accessrightid = 3
            filteredEmployees = allEmployee.stream()
                    .filter(employee -> employee.getAccessRight().getId() != 3)
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
                    empData.put("fullname", employee.getFullname());
                    empData.put("email", employee.getEmail());
                    LocalDate birthDate = employee.getBirthdate().toLocalDate();
                    empData.put("tanggallahir", birthDate.format(dateFormatter));
                    empData.put("umur", Period.between(birthDate, LocalDate.now()).getYears());
                    empData.put("nomorwa", employee.getPhonenumber());
                    empData.put("jam_masuk", employee.getJam_masuk());
                    empData.put("jadwal_libur", employee.getJadwal_libur());
                    empData.put("status", employee.getStatus());
                    Timestamp firstJoinDate = employee.getFirstjoindate();
                    LocalDateTime firstJoinDateTime =
                            LocalDateTime.ofInstant(firstJoinDate.toInstant(), ZoneId.systemDefault());
                    empData.put("firstjoindate", firstJoinDateTime.format(dateFormatter));
                    Timestamp lastUpdateDate = employee.getLastupdate();
                    if (lastUpdateDate != null) {
                        LocalDateTime lastUpdateDateTime =
                                LocalDateTime.ofInstant(lastUpdateDate.toInstant(), ZoneId.systemDefault());
                        empData.put("lastupdate", lastUpdateDateTime.format(dateFormatter));
                    }
                    else {
                        empData.put("lastupdate", null); // or some default value
                    }
                    empData.put("jabatan", employee.getAccessRight().getPosition());
                    return empData;
                }).collect(Collectors.toList());

        logger.info(String.valueOf(employeeData));
        return ResponseEntity.ok(employeeData);
    }


    @PostMapping("/tambahKaryawan")
    public ResponseEntity<?> tambahKaryawan(@RequestBody EmployeeModel employeeModel) {
        EmployeeModel existingUsername = employeeRepository.findByUsername(employeeModel.getUsername());
        if (existingUsername != null) {
            return ResponseEntity.badRequest().body(new Response("Username sudah digunakan"));
        }
        EmployeeModel existingEmail = employeeRepository.findByEmail(employeeModel.getEmail());
        if (existingEmail != null) {
            return ResponseEntity.badRequest().body(new Response("Email sudah digunakan"));
        }
        AccessRightModel accessRightModel = new AccessRightModel(employeeModel.getAccessRight().getId());
        EmployeeModel addEmployee = new EmployeeModel();
        addEmployee.setFullname(employeeModel.getFullname());
        addEmployee.setAccessRight(accessRightModel);
        addEmployee.setBirthdate(employeeModel.getBirthdate());
        addEmployee.setPhonenumber(employeeModel.getPhonenumber());
        addEmployee.setEmail(employeeModel.getEmail());
        addEmployee.setUsername(employeeModel.getUsername());
        addEmployee.setFirstjoindate(employeeModel.getFirstjoindate());
        addEmployee.setJam_masuk(employeeModel.getJam_masuk());
        addEmployee.setJadwal_libur(employeeModel.getJadwal_libur());
        addEmployee.setPassword(passwordEncoder.encode("123"));
        addEmployee.setLastupdate(Timestamp.valueOf(LocalDateTime.now()));
        addEmployee.setStatus("active");
        employeeRepository.save(addEmployee);
        logger.info(String.valueOf(addEmployee));
        return ResponseEntity.ok(addEmployee);
    }

    @GetMapping("/getRolesKaryawan")
    public ResponseEntity<?> getRolesKaryawan() {
        List<AccessRightModel> getAllRoles = accessRightRepository.findAll();
        logger.info(String.valueOf(getAllRoles));
        return ResponseEntity.ok(getAllRoles);
    }

    @GetMapping("/getEditDataKaryawan")
    public ResponseEntity<?> getEditDataKaryawan(@RequestParam(name = "idEmployee") String idEmployee) {
        Optional<EmployeeModel> getSelectedEmployee = employeeRepository.findById(Integer.valueOf(idEmployee));
        if (getSelectedEmployee.isPresent()) {
            return ResponseEntity.ok(getSelectedEmployee);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/editKaryawan")
    public ResponseEntity<?> editKaryawan(@RequestBody EmployeeModel employeeModel) {
        //        String hashedPassword = passwordEncoder.encode(employeeModel.getPassword());
        AccessRightModel accessRightModel = new AccessRightModel(employeeModel.getAccessRight().getId());
        Optional<EmployeeModel> optionalEmployee = employeeRepository.findById(employeeModel.getId());
        logger.info(String.valueOf(employeeModel.getId()));
        logger.info(String.valueOf(optionalEmployee));
        if (optionalEmployee.isPresent()) {
            EmployeeModel employee = optionalEmployee.get();
            employee.setUsername(employeeModel.getUsername());
            employee.setFullname(employeeModel.getFullname());
            employee.setBirthdate(employeeModel.getBirthdate());
            employee.setPhonenumber(employeeModel.getPhonenumber());
            employee.setAccessRight(accessRightModel);
            employee.setEmail(employeeModel.getEmail());
            employee.setFirstjoindate(employeeModel.getFirstjoindate());
            employee.setLastupdate(Timestamp.valueOf(LocalDateTime.now()));
            employee.setJam_masuk(employeeModel.getJam_masuk());
            employee.setJadwal_libur(employeeModel.getJadwal_libur());
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
        Optional<EmployeeModel> optionalEmployee = employeeRepository.findById(id);

        if (optionalEmployee.isPresent()) {
            EmployeeModel employee = optionalEmployee.get();
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
        List<EmployeeModel> listKaryawan = loginService.getEmployeesByAccessRightId(1);
        return ResponseEntity.ok(listKaryawan);
        //
    }

    @GetMapping("/pilihKaryawan")
    public ResponseEntity<?> pilihKaryawan(@RequestParam(name = "idAbsensi") String idAbsensi) {
        logger.info(idAbsensi);
        SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        List<AbsensiModel> pilihKaryawan = absensiRepository.getAbsensiByEmployeeid(Integer.parseInt(idAbsensi));
        if (pilihKaryawan == null) {
            return ResponseEntity.ok(new Response("Data Karyawan null"));
        }
        else {
            logger.info(String.valueOf(pilihKaryawan));
            List<Map<String, Object>> employeeData = pilihKaryawan.stream().map(absensi -> {
                Map<String, Object> empData = new HashMap<>();
                empData.put("id", absensi.getId());
                empData.put("username", absensi.getUsername());
                empData.put("tanggal", dateFormat.format(absensi.getTodaydate()));
                if (absensi.getClockin() != null) {
                    empData.put("clockIn", clockFormat.format(absensi.getClockin()));
                }
                else {
                    empData.put("clockIn", "");
                }
                if (absensi.getClockout() != null) {
                    empData.put("clockOut", clockFormat.format(absensi.getClockout()));
                }
                else {
                    empData.put("clockOut", "");
                }
                Optional<EmployeeModel> optionalEmployeeModel = employeeRepository.findById(Integer.valueOf(idAbsensi));
                if (optionalEmployeeModel.isPresent()) {
                    logger.info("data masuk");
                    EmployeeModel employee = optionalEmployeeModel.get();
                    Time jamMasuk = employee.getJam_masuk();
                    String formattedJamMasuk = jamMasuk.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    empData.put("jam_masuk", formattedJamMasuk);
                    empData.put("jadwal_libur", employee.getJadwal_libur());
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
            empData.put("checkoutdate", checkoutDateTime.format(dateFormatter));
            Timestamp paymentdate = orders.getPaymentdate();
            empData.put("paymentdate",
                    paymentdate != null ? LocalDateTime.ofInstant(paymentdate.toInstant(), ZoneId.systemDefault())
                            .format(dateFormatter) : null);

            Timestamp packingdate = orders.getPackingdate();
            empData.put("packingdate",
                    packingdate != null ? LocalDateTime.ofInstant(packingdate.toInstant(), ZoneId.systemDefault())
                            .format(dateFormatter) : "null");

            Timestamp deliverypickupdate = orders.getDeliverypickupdate();
            empData.put("deliverypickupdate",
                    deliverypickupdate != null ? LocalDateTime.ofInstant(deliverypickupdate.toInstant(),
                            ZoneId.systemDefault()).format(dateFormatter) : null);

            Timestamp deliverydonedate = orders.getDeliverydonedate();
            empData.put("deliverydonedate",
                    deliverydonedate != null ? LocalDateTime.ofInstant(deliverydonedate.toInstant(),
                            ZoneId.systemDefault()).format(dateFormatter) : null);

            empData.put("status", orders.getStatus());
            return empData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }

    @PostMapping("/editOrderDelivery")
    public ResponseEntity<?> editOrderDelivery(@RequestBody OrdersModel ordersModel) {
        Optional<OrdersModel> optionalOrdersModel = ordersRepository.findById(ordersModel.getId());
        if (optionalOrdersModel.isPresent()) {
            OrdersModel editOrder = optionalOrdersModel.get();
            editOrder.setCheckoutdate(ordersModel.getCheckoutdate());
            editOrder.setPaymentdate(ordersModel.getPaymentdate());
            editOrder.setPackingdate(ordersModel.getPackingdate());
            editOrder.setDeliverypickupdate(ordersModel.getDeliverypickupdate());
            editOrder.setDeliverydonedate(ordersModel.getDeliverydonedate());
            editOrder.setStatus(ordersModel.getStatus());
            ordersRepository.save(editOrder);
            return ResponseEntity.ok(editOrder);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + ordersModel.getId());
        }
    }

    @DeleteMapping("/deleteOrderDelivery/{id}")
    public ResponseEntity<?> deleteOrderDelivery(@PathVariable String id) {
        Optional<OrdersModel> optOrder = ordersRepository.findById(id);

        if (optOrder.isPresent()) {
            ordersRepository.deleteById(id);
            return ResponseEntity.ok().body("Type deleted successfully.");
        }
        else {
            return ResponseEntity.badRequest().body("Type not found with ID: " + id);
        }
    }
}
