package com.connect.api.service.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static void saveFile(MultipartFile file) throws IOException {
        Path filepath = Paths.get(System.getProperty("user.dir"), file.getOriginalFilename());
        file.transferTo(filepath);
    }
}
