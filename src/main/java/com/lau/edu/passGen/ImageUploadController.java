package com.lau.edu.passGen;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.nio.file.*;

import static com.lau.edu.passGen.ImageEncryptor;
import static com.lau.edu.passGen.ImageEncryptor.MainFunction;

@RestController
public class ImageUploadController {

    private static String UPLOAD_DIR = "uploads/";

    @PostMapping("/fingerPrint")
    public ResponseEntity<String> uploadImage1(@RequestParam("image") MultipartFile image) {
        return saveImage(image, "fingerPrint");
    }
    @PostMapping("/process")
    public String processInput(@RequestBody String input,@RequestBody int size) throws Exception {
        return MainFunction(input,size); // Your Java function
    }

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage2(@RequestParam("image") MultipartFile image) {
        return saveImage(image, "image");
    }

    private ResponseEntity<String> saveImage(MultipartFile image, String imageName) {
        if (image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No image uploaded");
        }

        try {
            Path path = Paths.get(UPLOAD_DIR + imageName + "-" + image.getOriginalFilename());
            Files.createDirectories(path.getParent());
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("File successfully uploaded: " + path.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not store the image. Error: " + e.getMessage());
        }
    }
}
