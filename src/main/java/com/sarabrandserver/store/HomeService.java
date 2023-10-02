package com.sarabrandserver.store;

import com.sarabrandserver.aws.S3Service;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    private final S3Service s3Service;
    private final Environment environment;

    public HomeService(S3Service s3Service, Environment environment) {
        this.s3Service = s3Service;
        this.environment = environment;
    }

    /** Returns pre-signed url which is a background pictures. */
    public ResponseEntity<?> fetchHomeBackground() {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage") || profile.equals("dev");
        var bucket = this.environment.getProperty("aws.bucket", "");

        // TODO store key in table
        var arr = new HomeResponse[3];

        for (int i = 0; i < arr.length; i++) {
            int index = i + 1;
            var url = this.s3Service.getPreSignedUrl(bool, bucket, "sarre" + index +".jpg");
            arr[i] = new HomeResponse(url);
        }

        return new ResponseEntity<>(arr, HttpStatus.OK);
    }

}
