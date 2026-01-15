package pl.electricshop.product_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.electricshop.product_service.service.FileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {




    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {
        // File names of current / original file
        String originalFileName = image.getOriginalFilename();

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;


        // Check if path exist and create
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Upload to server
        Files.copy(image.getInputStream(), Paths.get(filePath));

        // return file name
        return fileName;
    }
}
