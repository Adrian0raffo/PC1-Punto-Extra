package com.example.week07_lab.booking.domain;

import java.time.LocalDateTime;

public record BookingConfirmedEvent(
        Long bookingId,
        String customerFirstName,
        String customerLastName,
        String customerEmail,
        String flightNumber,
        String airline,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        LocalDateTime bookingDate
) {
}
