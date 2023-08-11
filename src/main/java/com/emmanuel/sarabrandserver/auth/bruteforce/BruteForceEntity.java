package com.emmanuel.sarabrandserver.auth.bruteforce;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class BruteForceEntity implements Serializable {
    private int failedAttempt;
    private String principal; // Represents username and email

    public BruteForceEntity(int failedAttempt, String principal) {
        this.failedAttempt = failedAttempt;
        this.principal = principal;
    }
}
