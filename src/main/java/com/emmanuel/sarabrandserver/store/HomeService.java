package com.emmanuel.sarabrandserver.store;

import com.emmanuel.sarabrandserver.aws.S3Service;
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

    /** Returns pre-signed url which is a background video. */
    public ResponseEntity<?> fetchHomeBackground() {
        var profile = this.environment.getProperty("spring.profiles.active", "");
//        boolean bool = profile.equals("prod") || profile.equals("stage") || profile.equals("dev");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        // TODO store key in table
        String url = this.s3Service.getPreSignedUrl(bool, bucket, "sara-brand-home-display.mp4");

        if (url.isEmpty()) {
            return new ResponseEntity<>(new HomeResponse(""), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new HomeResponse(url), HttpStatus.OK);
    }

}
