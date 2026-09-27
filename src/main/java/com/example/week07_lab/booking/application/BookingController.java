package com.example.week07_lab.booking.application;

import com.example.week07_lab.booking.domain.BookingService;
import com.example.week07_lab.booking.dto.BookingRequest;
import com.example.week07_lab.booking.dto.BookingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/flights/book")
    public ResponseEntity<BookingResponse> book(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        BookingResponse created = bookingService.book(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/flight/book/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        return ResponseEntity.ok(bookingService.getById(email, id));
    }
}
