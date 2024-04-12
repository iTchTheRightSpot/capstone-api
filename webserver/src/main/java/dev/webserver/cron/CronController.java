package dev.webserver.cron;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}cron")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
class CronController {

    private final CronJob cronJob;

    @ResponseStatus(OK)
    @GetMapping
    public void testCronMethodNativeMode() {
        cronJob.onDeleteOrderReservations();
    }

}
