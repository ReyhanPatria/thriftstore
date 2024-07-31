package com.liquestore.controller;

import com.liquestore.model.Employee;
import com.liquestore.model.Item;
import com.liquestore.model.Order;
import com.liquestore.model.OrderColor;
import com.liquestore.model.TemporaryOrder;
import com.liquestore.model.Type;
import com.liquestore.repository.ItemRepository;
import com.liquestore.repository.OrderColourRepository;
import com.liquestore.repository.OrdersRepository;
import com.liquestore.repository.TemporaryOrderRepository;
import com.liquestore.repository.TypeRepository;
import com.liquestore.service.FileStorageService;
import com.liquestore.service.MidtransService;
import com.liquestore.service.RajaOngkirService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/backend/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TypeRepository typeRepository;
    private final ItemRepository itemRepository;
    private final OrdersRepository ordersRepository;
    private final OrderColourRepository orderColourRepository;
    private final TemporaryOrderRepository temporaryOrderRepository;
    private final FileStorageService fileStorageService;
    private final MidtransService midtransService;
    private final RajaOngkirService rajaOngkirService;

    @GetMapping("/daftarTipe")
    public ResponseEntity<?> daftarTipe() {
        List<Type> getAllType = typeRepository.findAll();
        log.info(String.valueOf(getAllType));
        return ResponseEntity.ok(getAllType);
    }

    @GetMapping("/dataInventori")
    public ResponseEntity<?> dataInventori() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Item> getAllItem = itemRepository.findAll();
        log.info(String.valueOf(getAllItem));
        List<Map<String, Object>> itemData = getAllItem.stream().map(item -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("id", item.getId());
            empData.put("itemcode", item.getCode());
            empData.put("nama", item.getName());
            empData.put("jenisBarang", item.getTypeId().getName());
            empData.put("customWeight", item.getCustomWeight());
            empData.put("customCapitalPrice", item.getCustomCapitalPrice());
            empData.put("customDefaultPrice", item.getCustomDefaultPrice());
            empData.put("files", item.getFiles());
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
        log.info(String.valueOf(itemData));
        return ResponseEntity.ok(itemData);
    }

    @PostMapping(value = "/tambahInventori", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> tambahInventori(@RequestParam("name") String name,
            @RequestParam("typeId") int typeId,
            @RequestParam("employeeId") int employeeId,
            @RequestParam("customWeight") int customWeight,
            @RequestParam("customCapitalPrice") int customCapitalPrice,
            @RequestParam("customDefaultPrice") int customDefaultPrice,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
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
            log.info(sequenceString);
            itemCode = prefix + sequenceString;
            log.info(itemCode);
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + typeId);
        }

        Item item = new Item();
        item.setName(name);
        item.setTypeId(new Type(typeId));
        item.setEmployeeId(new Employee(employeeId));
        item.setCode(itemCode);
        item.setCustomWeight(customWeight);
        item.setCustomCapitalPrice(customCapitalPrice);
        item.setCustomDefaultPrice(customDefaultPrice);
        if (files != null && !files.isEmpty()) {
            List<String> fileNames = fileStorageService.storeFiles(files);
            item.setFiles(fileNames);
        }
        item.setStatus("available");
        item.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        Item savedItem = itemRepository.save(item);
        log.info(String.valueOf(savedItem));
        return ResponseEntity.ok(savedItem);
    }

    @PostMapping("/editInventori")
    public ResponseEntity<?> editInventori(@RequestParam("name") String name,
            @RequestParam("id") int id,
            @RequestParam("typeId") int typeId,
            @RequestParam("customWeight") int customWeight,
            @RequestParam("customCapitalPrice") int customCapitalPrice,
            @RequestParam("customDefaultPrice") int customDefaultPrice,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        Optional<Item> optionalItemModel = itemRepository.findById(id);
        if (optionalItemModel.isPresent()) {
            Item getItem = optionalItemModel.get();
            getItem.setName(name);
            getItem.setTypeId(new Type(typeId));
            getItem.setCustomWeight(customWeight);
            getItem.setCustomCapitalPrice(customCapitalPrice);
            getItem.setCustomDefaultPrice(customDefaultPrice);
            if (files != null && !files.isEmpty()) {
                List<String> fileNames = fileStorageService.storeFiles(files);
                getItem.setFiles(fileNames);
            }
            getItem.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            Item savedItem = itemRepository.save(getItem);
            return ResponseEntity.ok(savedItem);
        }
        else {
            return ResponseEntity.badRequest().body("item barang tidak ditemukan");
        }
    }

    @DeleteMapping("/deleteInventori/{id}")
    public ResponseEntity<?> deleteInventori(@PathVariable int id) {
        Optional<Item> optItem = itemRepository.findById(id);
        if (optItem.isPresent()) {
            itemRepository.deleteById(id);
            return ResponseEntity.ok().body("Item deleted successfully.");
        }
        else {
            return ResponseEntity.badRequest().body("Item not found with ID: " + id);
        }
    }

    @GetMapping("/dataTipe")
    public ResponseEntity<?> dataTipe() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Type> getAllTipe = typeRepository.findAll();
        log.info(String.valueOf(getAllTipe));
        List<Map<String, Object>> itemData = getAllTipe.stream().map(item -> {
            Map<String, Object> empData = new HashMap<>();
            empData.put("id", item.getId());
            empData.put("nama", item.getName());
            empData.put("varian", item.getVariant());
            empData.put("typecode", item.getCode());
            empData.put("weight", item.getWeight());
            Timestamp lastUpdateDate = item.getUpdatedDate();
            if (lastUpdateDate != null) {
                LocalDateTime lastUpdateDateTime =
                        LocalDateTime.ofInstant(lastUpdateDate.toInstant(), ZoneId.systemDefault());
                log.info(String.valueOf(lastUpdateDateTime.toLocalDate()));
                empData.put("lastupdate", lastUpdateDateTime.format(dateFormatter));
            }
            else {
                empData.put("lastupdate", "-");
            }
            return empData;
        }).collect(Collectors.toList());
        log.info(String.valueOf(itemData));
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
        addType.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        typeRepository.save(addType);
        log.info(String.valueOf(addType));
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

    @GetMapping("/getColour")
    public ResponseEntity<?> getColour() {
        List<OrderColor> getAllColour = orderColourRepository.findAll();
        log.info(String.valueOf(getAllColour));
        return ResponseEntity.ok(getAllColour);
    }

    @GetMapping("/getItem")
    public ResponseEntity<?> getItem() {
        List<Item> getAllItem = itemRepository.findAll();
        log.info(String.valueOf(getAllItem));
        return ResponseEntity.ok(getAllItem);
    }

    @GetMapping("/getColourOrder/{id}")
    public ResponseEntity<?> getColourOrder(@PathVariable int id) {
        Optional<OrderColor> optionalOrderColourModel = orderColourRepository.findById(id);
        if (optionalOrderColourModel.isPresent()) {
            OrderColor orderColor = optionalOrderColourModel.get();
            String firstChar = orderColor.getCode();
            return ResponseEntity.ok(orderColor);
        }
        else {
            return ResponseEntity.badRequest().body("Colour not found with ID: " + id);
        }
    }

    @PostMapping("/tambahTemporaryOrder")
    public ResponseEntity<?> tambahTemporaryOrder(@RequestParam("colourcode") String colourcode,
            @RequestParam("orderid") int orderid,
            @RequestParam("phonenumber") String phonenumber,
            @RequestParam("totalprice") int totalprice,
            @RequestParam("typecode") String typecode) {
        String ctrId = String.format("%03d", orderid);
        List<TemporaryOrder> listTemporaryOrder = temporaryOrderRepository.findAll();
        log.info(listTemporaryOrder.toString());
        OrderColor orderColor = orderColourRepository.findByColourcode(colourcode);
        Type type = typeRepository.findByTypecode(typecode);
        String tempOrderid = "";
        LocalDate today = LocalDate.now();
        // Format tanggal menjadi YYMMDD
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String formattedDate = today.format(formatter);
        if (listTemporaryOrder.isEmpty()) {
            log.info("temporary order masih kosong");
            TemporaryOrder addTemporaryOrder = new TemporaryOrder();
            tempOrderid = colourcode + ctrId + formattedDate;
            addTemporaryOrder.setOrderId(tempOrderid);
            addTemporaryOrder.setColorId(new OrderColor(orderColor.getId()));
            addTemporaryOrder.setPhoneNumber(phonenumber);
            addTemporaryOrder.setTotalPrice(totalprice);
            addTemporaryOrder.setTotalWeight(type.getWeight());
            addTemporaryOrder.setStatus("Payment Not Done");
            addTemporaryOrder.setCheckoutDate(Timestamp.valueOf(LocalDateTime.now()));
            addTemporaryOrder.setMasterOrderId(tempOrderid);
            addTemporaryOrder.setIsActive(true);
            TemporaryOrder savedTempOrder = temporaryOrderRepository.save(addTemporaryOrder);
            return ResponseEntity.ok(savedTempOrder);
        }
        else {
            boolean cekNomor = listTemporaryOrder.stream()
                    .map(order -> order.getOrderId().substring(1, 4))
                    .anyMatch(getNomor -> Integer.parseInt(getNomor) == orderid);
            boolean cekTemporderData = listTemporaryOrder.stream()
                    .anyMatch(order -> order.getColorId().getCode().equals(colourcode) &&
                            order.getOrderId().substring(4, 10).equals(formattedDate));
            if (cekTemporderData && cekNomor) {
                log.info("kodenya sama buk");
                return ResponseEntity.badRequest().body("Kode pemesanan sudah digunakan");
            }
            Optional<TemporaryOrder> lowestOrderIdOrder = listTemporaryOrder.stream()
                    .filter(order -> order.getPhoneNumber().equals(phonenumber))
                    .min(Comparator.comparingInt(order -> Integer.parseInt(order.getOrderId().substring(1, 4))));
            tempOrderid = colourcode + ctrId + formattedDate;
            TemporaryOrder addTemporaryOrder = new TemporaryOrder();
            addTemporaryOrder.setOrderId(tempOrderid);
            addTemporaryOrder.setColorId(new OrderColor(orderColor.getId()));
            addTemporaryOrder.setPhoneNumber(phonenumber);
            addTemporaryOrder.setTotalPrice(totalprice);
            addTemporaryOrder.setTotalWeight(type.getWeight());
            addTemporaryOrder.setStatus("Payment Not Done");
            addTemporaryOrder.setIsActive(true);
            addTemporaryOrder.setCheckoutDate(Timestamp.valueOf(LocalDateTime.now()));
            if (lowestOrderIdOrder.isPresent()) {
                TemporaryOrder temporaryOrder = lowestOrderIdOrder.get();
                addTemporaryOrder.setMasterOrderId(temporaryOrder.getMasterOrderId());
            }
            else {
                addTemporaryOrder.setMasterOrderId(tempOrderid);
            }
            TemporaryOrder savedTempOrder = temporaryOrderRepository.save(addTemporaryOrder);
            return ResponseEntity.ok(savedTempOrder);
        }
    }

    @PostMapping("/simpanCheckoutLink/{orderid}")
    public ResponseEntity<?> simpanCheckoutLink(@PathVariable String orderid) {
        TemporaryOrder temporaryOrder = temporaryOrderRepository.findByOrderid(orderid);
        temporaryOrder.setLink("http://localhost:3000/login?orderid=" + temporaryOrder.getOrderId());
        String link = "http://localhost:3000/login?orderid=" + temporaryOrder.getOrderId();
        log.info(link);
        temporaryOrderRepository.save(temporaryOrder);
        return ResponseEntity.ok(temporaryOrder);
    }

    @GetMapping("/getSelectedColour/{id}")
    public ResponseEntity<?> getSelectedColour(@PathVariable int id) {
        Optional<OrderColor> optionalOrderColourModel = orderColourRepository.findById(id);
        List<Map<String, Object>> matchedOrders = new ArrayList<>();
        if (optionalOrderColourModel.isPresent()) {
            OrderColor orderColor = optionalOrderColourModel.get();
            List<TemporaryOrder> temporaryOrder = temporaryOrderRepository.findAll();
            for (TemporaryOrder tempOrder : temporaryOrder) {
                String firstChar = tempOrder.getOrderId().substring(0, 1);
                String substringOrderId = tempOrder.getOrderId().substring(1, 4);
                if (substringOrderId.startsWith("00")) {
                    substringOrderId = substringOrderId.substring(2); // Menghapus angka '0' di depan jika ada
                }
                else if (substringOrderId.startsWith("0")) {
                    substringOrderId = substringOrderId.substring(1); // Menghapus angka '0' di depan jika ada
                }
                if (firstChar.equals(orderColor.getCode()) && tempOrder.getIsActive()) {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("colourid", tempOrder.getColorId());
                    orderMap.put("id", tempOrder.getId());
                    orderMap.put("itemidall", tempOrder.getItemIdList());
                    orderMap.put("orderid", tempOrder.getOrderId());
                    orderMap.put("phonenumber", tempOrder.getPhoneNumber());
                    orderMap.put("totalprice", tempOrder.getTotalPrice());
                    orderMap.put("totalweight", tempOrder.getTotalWeight());
                    orderMap.put("username", tempOrder.getUsername());
                    orderMap.put("waitinglist", tempOrder.getWaitingList());
                    orderMap.put("kodepemesanan", substringOrderId);
                    orderMap.put("link", tempOrder.getLink());
                    orderMap.put("status", tempOrder.getStatus());
                    orderMap.put("masterorderid", tempOrder.getMasterOrderId());
                    matchedOrders.add(orderMap);
                }
            }
            if (!matchedOrders.isEmpty()) {
                return ResponseEntity.ok(matchedOrders);
            }
            else {
                return ResponseEntity.badRequest().body("Tidak ditemukan kecocokan warna");
            }
        }
        else {
            return ResponseEntity.badRequest().body("Employee not found with ID: " + id);
        }
    }

    @PostMapping("/inputTemporaryOrder")
    public ResponseEntity<?> inputTemporaryOrder(@RequestParam("id") int id,
            @RequestParam("username") String username,
            @RequestParam("phonenumber") String phonenumber,
            @RequestParam("itemidall") List<String> itemidall,
            @RequestParam("totalweight") int totalweight,
            @RequestParam("totalprice") int totalprice,
            @RequestParam("waitinglist") List<String> waitinglist,
            @RequestParam("colourid") int colourid) {
        Optional<TemporaryOrder> temporaryOrderModel = temporaryOrderRepository.findById(id);
        if (temporaryOrderModel.isPresent()) {
            TemporaryOrder updateTemporaryOrder = temporaryOrderModel.get();
            updateTemporaryOrder.setUsername(username);
            updateTemporaryOrder.setPhoneNumber(phonenumber);
            updateTemporaryOrder.setItemIdList(itemidall);
            updateTemporaryOrder.setTotalPrice(totalprice);
            updateTemporaryOrder.setTotalWeight(totalweight);
            updateTemporaryOrder.setWaitingList(waitinglist);
            TemporaryOrder savedTemporaryOrder = temporaryOrderRepository.save(updateTemporaryOrder);
            return ResponseEntity.ok(savedTemporaryOrder);
        }
        else {
            String ctrId = String.format("%03d", id + 1);
            log.info(ctrId);
            String hurufDepanWarna = "";
            // Ambil tanggal hari ini
            LocalDate today = LocalDate.now();
            // Format tanggal menjadi YYMMDD
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
            String formattedDate = today.format(formatter);
            Optional<OrderColor> optionalOrderColourModel = orderColourRepository.findById(colourid);
            if (optionalOrderColourModel.isPresent()) {
                OrderColor orderColor = optionalOrderColourModel.get();
                hurufDepanWarna = orderColor.getCode();
            }
            String orderid = hurufDepanWarna + ctrId + formattedDate;
            TemporaryOrder addTemporaryOrder = new TemporaryOrder();
            addTemporaryOrder.setColorId(new OrderColor(colourid));
            addTemporaryOrder.setOrderId(orderid);
            addTemporaryOrder.setUsername(username);
            addTemporaryOrder.setPhoneNumber(phonenumber);
            addTemporaryOrder.setTotalPrice(totalprice);
            addTemporaryOrder.setTotalWeight(totalweight);
            addTemporaryOrder.setWaitingList(waitinglist);
            addTemporaryOrder.setItemIdList(itemidall);
            TemporaryOrder savedTemporaryOrder = temporaryOrderRepository.save(addTemporaryOrder);
            log.info(String.valueOf(savedTemporaryOrder));
            return ResponseEntity.ok(savedTemporaryOrder);
        }
    }

    @PostMapping("/inputOrder")
    public ResponseEntity<?> inputOrder(@RequestBody List<TemporaryOrder> orderData) {
        log.info("Received order data: {}", orderData);
        // Filter data to remove entries with null username
        List<TemporaryOrder> filteredOrderData = orderData.stream()
                .filter(order -> order.getUsername() != null)
                .toList();
        if (filteredOrderData.isEmpty()) {
            log.warn("No valid orders to process");
        }
        // Ambil informasi tambahan dari tabel TemporaryOrder berdasarkan orderid
        List<String> orderIds = filteredOrderData.stream()
                .map(TemporaryOrder::getOrderId)
                .toList();
        List<TemporaryOrder> temporaryOrders = temporaryOrderRepository.findAllByOrderidIn(orderIds);
        Map<String, TemporaryOrder> tempOrderMap = temporaryOrders.stream()
                .collect(Collectors.toMap(TemporaryOrder::getOrderId, order -> order));
        Map<String, List<TemporaryOrder>> groupedByUsername = filteredOrderData.stream()
                .collect(Collectors.groupingBy(TemporaryOrder::getUsername));
        log.info("Grouped order data by username: {}", groupedByUsername);
        List<Order> ordersToInsert = new ArrayList<>();
        for (Map.Entry<String, List<TemporaryOrder>> entry : groupedByUsername.entrySet()) {
            String username = entry.getKey();
            List<TemporaryOrder> userOrders = entry.getValue();
            log.info("Processing orders for user: {}", username);
            boolean allPaid = userOrders.stream().allMatch(order -> "On Packing".equals(order.getStatus()));
            if (allPaid) {
                log.info("All orders for user {} are paid", username);
                TemporaryOrder minOrder = userOrders.stream()
                        .min(Comparator.comparingInt(order -> Integer.parseInt(order.getOrderId().substring(1, 4))))
                        .orElse(null);
                log.info("ini data min order {}", minOrder);
                if (minOrder != null) {
                    TemporaryOrder fullOrderInfo = tempOrderMap.get(minOrder.getOrderId());
                    if (fullOrderInfo.getItemIdList() != null && !fullOrderInfo.getItemIdList().isEmpty()) {
                        Order addOrders = new Order();
                        addOrders.setId(fullOrderInfo.getOrderId());
                        addOrders.setStatus(fullOrderInfo.getStatus());
                        addOrders.setUsername(fullOrderInfo.getUsername());
                        addOrders.setPhoneNumber(fullOrderInfo.getPhoneNumber());
                        addOrders.setCheckoutDate(fullOrderInfo.getCheckoutDate());
                        addOrders.setPaymentDate(fullOrderInfo.getPaymentDate());
                        addOrders.setItemIdList(fullOrderInfo.getItemIdList());
                        ordersToInsert.add(addOrders);
                        log.info("Order for user {} added: {}", username, addOrders);

                        // Update isIsactive() to false for each found order
                        List<TemporaryOrder> updateIsActive =
                                temporaryOrderRepository.findAllByMasterorderid(minOrder.getOrderId());
                        for (TemporaryOrder orderToUpdate : updateIsActive) {
                            orderToUpdate.setIsActive(false);
                            temporaryOrderRepository.save(orderToUpdate);
                        }
                    }
                    else {
                        log.warn("Skipping order for user {} because itemidall is empty or null", username);
                    }
                }
            }
        }
        if (!ordersToInsert.isEmpty()) {
            ordersRepository.saveAll(ordersToInsert);
            log.info("Orders saved successfully");
            // Update isIsactive()() to false for the corresponding temporary orders
            for (Order order : ordersToInsert) {
                TemporaryOrder tempOrder = tempOrderMap.get(order.getId());
                if (tempOrder != null) {
                    tempOrder.setIsActive(false);
                    temporaryOrderRepository.save(tempOrder);
                }
            }
            return ResponseEntity.ok("Orders have been submitted successfully");
        }
        else {
            log.warn("No orders to submit");
            return ResponseEntity.badRequest().body("No orders to submit");
        }
    }

    @PostMapping("/checkUpdateTransaction")
    public ResponseEntity<?> checkUpdateTransaction(@RequestBody List<TemporaryOrder> temporaryOrders) {
        log.info("masuk transaksinya");
        List<String> failedOrders = new ArrayList<>();
        for (TemporaryOrder order : temporaryOrders) {
            TemporaryOrder temporaryOrder = temporaryOrderRepository.findByOrderid(order.getOrderId());
            if (temporaryOrder.getIsActive()) {
                try {
                    log.info(order.getMasterOrderId());
                    midtransService.checkAndUpdateOrderStatus(order.getMasterOrderId());
                }
                catch (Exception e) {
                    failedOrders.add(order.getOrderId());
                    log.info(e.getMessage());
                }
            }
        }

        if (failedOrders.isEmpty()) {
            return ResponseEntity.ok("berhasil update status");
        }
        else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Gagal update status untuk order: " + String.join(", ", failedOrders));
        }
    }

    @PostMapping("/deleteTemporaryOrder")
    public ResponseEntity<?> deleteTemporaryOrder(@RequestBody List<TemporaryOrder> orderData) {
        log.info(String.valueOf(orderData));
        if (orderData.isEmpty()) {
            log.info("Tidak ada data");
            return ResponseEntity.badRequest().body("Order Data tidak ada");
        }
        else {
            try {
                for (TemporaryOrder order : orderData) {
                    order.setIsActive(false);
                    temporaryOrderRepository.save(order);
                }
                return ResponseEntity.ok("Berhasil clear temporary order");
            }
            catch (Exception e) {
                log.info("Gagal menghapus temporary order");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gagal menghapus temporary order");
            }
        }
    }

    @PostMapping("/tambahWarna")
    public ResponseEntity<?> tambahWarna(@RequestBody OrderColor orderColor) {
        char firstLetter = orderColor.getName().toUpperCase().charAt(0);
        String varcharName = String.valueOf(firstLetter);
        OrderColor addColour = new OrderColor();
        addColour.setName(orderColor.getName());
        addColour.setCode(varcharName);
        addColour.setHex(orderColor.getHex());
        orderColourRepository.save(addColour);
        log.info(String.valueOf(addColour));
        return ResponseEntity.ok(addColour);
    }

    @GetMapping("/dataOrder")
    public ResponseEntity<?> dataOrder() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        List<Order> getAllOrders = ordersRepository.findAll();
        List<Item> getAllItem = itemRepository.findAll();
        // Create a map of item codes to item names
        Map<String, String> itemCodeToNameMap = getAllItem.stream()
                .collect(Collectors.toMap(Item::getCode, Item::getName));
        List<Map<String, Object>> orderData = getAllOrders.stream()
                .filter(order -> order.getPaymentDate() != null)
                .map(orders -> {
                    Map<String, Object> empData = new HashMap<>();
                    empData.put("id", orders.getId());
                    List<String> itemName = orders.getItemIdList().stream()
                            .map(itemCode -> itemCodeToNameMap.getOrDefault(itemCode, "Unknown Item"))
                            .collect(Collectors.toList());
                    empData.put("namabarang", itemName);
                    empData.put("namacust", orders.getUsername());
                    Timestamp checkoutdate = orders.getCheckoutDate();
                    LocalDateTime firstJoinDateTime =
                            LocalDateTime.ofInstant(checkoutdate.toInstant(), ZoneId.systemDefault());
                    log.info(String.valueOf(firstJoinDateTime));
                    empData.put("checkoutdate", firstJoinDateTime.format(dateFormatter));
                    empData.put("packingdate", orders.getPackingDate());
                    empData.put("deliverypickupdate", orders.getDeliveryPickupDate());
                    return empData;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }

    @PostMapping("/updatePackingdate")
    public ResponseEntity<?> updatePackingdate(@RequestParam(name = "rowId") String id) {
        Optional<Order> optionalOrdersModel = ordersRepository.findById(id);
        log.info(String.valueOf(optionalOrdersModel));
        if (optionalOrdersModel.isPresent()) {
            Order getSelectedOrder = optionalOrdersModel.get();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            getSelectedOrder.setPackingDate(timestamp);
            getSelectedOrder.setStatus("On Pick Up");
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
        log.info(String.valueOf(optionalOrdersModel));
        if (optionalOrdersModel.isPresent()) {
            Order getSelectedOrder = optionalOrdersModel.get();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            getSelectedOrder.setDeliveryPickupDate(timestamp);
            getSelectedOrder.setStatus("On Delivery");
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
            List<String[]> itemDetails = orders.getItemIdList().stream()
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
                    .toList();
            List<String> itemNames = itemDetails.stream()
                    .map(details -> details[0])
                    .toList();
            List<String> itemTypes = itemDetails.stream()
                    .map(details -> details[1])
                    .toList();
            empData.put("namabarang", String.join(", ", itemNames));
            empData.put("jenisbarang", String.join(", ", itemTypes));
            empData.put("namapembeli", orders.getUsername());
            Timestamp checkoutdate = orders.getCheckoutDate();
            if (checkoutdate != null) {
                LocalDateTime checkoutDateTime =
                        LocalDateTime.ofInstant(checkoutdate.toInstant(), ZoneId.systemDefault());
                empData.put("checkoutdate", checkoutDateTime.format(dateFormatter));
            }
            Timestamp paymentdate = orders.getPaymentDate();
            if (paymentdate != null) {
                LocalDateTime paymentDateTime =
                        LocalDateTime.ofInstant(paymentdate.toInstant(), ZoneId.systemDefault());
                empData.put("paymentdate", paymentDateTime.format(dateFormatter));
            }
            Timestamp packingdate = orders.getPackingDate();
            if (packingdate != null) {
                LocalDateTime packingDateTime =
                        LocalDateTime.ofInstant(packingdate.toInstant(), ZoneId.systemDefault());
                empData.put("packingdate", packingDateTime.format(dateFormatter));
            }
            Timestamp deliverypickupdate = orders.getDeliveryPickupDate();
            if (deliverypickupdate != null) {
                LocalDateTime deliverypickupDateTime =
                        LocalDateTime.ofInstant(deliverypickupdate.toInstant(), ZoneId.systemDefault());
                empData.put("deliverypickupdate", deliverypickupDateTime.format(dateFormatter));
            }
            Timestamp deliverydonedate = orders.getDeliveryCompletionDate();
            if (deliverydonedate != null) {
                LocalDateTime deliverydoneDateTime =
                        LocalDateTime.ofInstant(deliverydonedate.toInstant(), ZoneId.systemDefault());
                empData.put("deliverydonedate", deliverydoneDateTime.format(dateFormatter));
            }
            empData.put("status", orders.getStatus());
            return empData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(orderData);
    }

    @GetMapping("/api/rajaongkir/waybill")
    public ResponseEntity<?> waybill() {
        log.info("tesdt");
        boolean cekStatus = false;
        List<Order> getAll = ordersRepository.findAll();
        log.info(String.valueOf(getAll));
        for (Order order : getAll) {
            log.info(String.valueOf(order));
            String noResi = order.getDeliveryReceiptNumber();
            if (noResi == null || noResi.isEmpty()) {
                log.info("No resi kosong untuk order: " + order.getId());
                continue;
            }
            String status = rajaOngkirService.getDeliveryStatus(noResi);
            if ("DELIVERED".equals(status)) {
                order.setStatus("done");  // Pastikan field status yang benar digunakan
                order.setDeliveryCompletionDate(Timestamp.valueOf(LocalDateTime.now()));
                ordersRepository.save(order);  // Simpan perubahan ke database
                cekStatus = true;
            }
        }
        if (cekStatus) {
            return ResponseEntity.ok("Berhasil update order");
        }
        else {
            return ResponseEntity.ok("tidak ada perubahan data");
        }
    }

    @PostMapping("/api/excel/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row if needed
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }
            DataFormatter dataFormatter = new DataFormatter();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cellNoResi = row.getCell(0);
                Cell cellPhoneNumber = row.getCell(21);
                if (cellPhoneNumber != null) {
                    String getPhone = dataFormatter.formatCellValue(cellPhoneNumber);
                    log.info("Raw Phone Number: " + getPhone);

                    String phoneNumber = "0" + getPhone;
                    log.info("Formatted Phone Number: " + phoneNumber);
                    Order order = ordersRepository.findByPhonenumber(phoneNumber);
                    if (order != null) {
                        if (order.getDeliveryPickupDate() != null) {
                            if (order.getDeliveryReceiptNumber().isEmpty()) {
                                order.setDeliveryReceiptNumber(cellNoResi.getStringCellValue());
                                ordersRepository.save(order);
                            }
                        }
                    }
                }
            }

            workbook.close();
            inputStream.close();

            return ResponseEntity.ok("File uploaded successfully");
        }
        catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload file");
        }
    }
}
