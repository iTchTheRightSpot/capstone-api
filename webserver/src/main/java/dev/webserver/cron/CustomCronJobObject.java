package dev.webserver.cron;

import com.fasterxml.jackson.databind.JsonNode;
import dev.webserver.payment.OrderReservation;
import org.springframework.http.HttpStatus;

record CustomCronJobObject (OrderReservation reservation, JsonNode node, HttpStatus status) { }