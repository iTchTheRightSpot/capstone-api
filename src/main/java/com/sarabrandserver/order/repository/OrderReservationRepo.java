package com.sarabrandserver.order.repository;

import com.sarabrandserver.order.entity.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReservationRepo extends JpaRepository<OrderReservation, Long> {}