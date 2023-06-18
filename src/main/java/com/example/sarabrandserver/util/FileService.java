package com.example.sarabrandserver.util;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileService {

    /**
     * Methods receives an image path to return the image name from the path
     * @param photo meaning the path of the image
     * @return String
     * */
    public String getImageName(String photo) {
        return Paths.get(photo).getFileName().toString();
    }

    /**
     * Methods receives an image path to return the mediaType of the image
     * e.g. image.png -> png
     * @param photo meaning the path of the image
     * @return String
     * */
    public String getMediaType(String photo) throws IOException {
        return Files.probeContentType(Paths.get(photo));
    }

}
