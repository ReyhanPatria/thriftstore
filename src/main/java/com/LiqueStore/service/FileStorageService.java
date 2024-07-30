package com.LiqueStore.service;

import com.LiqueStore.FileConfig;
import com.LiqueStore.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    public FileStorageService(FileConfig fileConfig) {
        this.fileStorageLocation = Paths.get(fileConfig.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        }
        catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<String> storeFiles(List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            //            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new RuntimeException("File name is null. Please provide a valid file name.");
            }
            try {
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                fileNames.add(fileName);
            }
            catch (IOException ex) {
                throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
            }
        }
        return fileNames;
    }
}
