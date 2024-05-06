package dev.webserver.cron;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}cron")
@RequiredArgsConstructor
class CronController {

    private static final Logger log = LoggerFactory.getLogger(CronController.class);

    @Value(value = "${spring.profiles.active}")
    private String profile;

    private final CronJob cronJob;

    @ResponseStatus(OK)
    @GetMapping
    public void testCronMethodNativeMode() {
        log.info("Cron Controller current profile {}", profile);

        cronJob.onDeleteOrderReservations();
    }

}
