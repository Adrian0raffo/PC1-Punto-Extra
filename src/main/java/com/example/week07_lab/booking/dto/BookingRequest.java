package com.example.week07_lab.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "El flightId es requerido")
    private Long flightId;
}
