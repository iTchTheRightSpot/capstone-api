package com.sarabrandserver;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({ MockitoExtension.class, SpringExtension.class })
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class AbstractUnitTest{ }