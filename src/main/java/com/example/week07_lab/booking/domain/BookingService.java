package com.example.week07_lab.booking.domain;

import com.example.week07_lab.booking.dto.BookingRequest;
import com.example.week07_lab.booking.dto.BookingResponse;
import com.example.week07_lab.booking.infrastructure.BookingRepository;
import com.example.week07_lab.flight.domain.Flight;
import com.example.week07_lab.flight.infrastructure.FlightRepository;
import com.example.week07_lab.shared.exception.BadRequestException;
import com.example.week07_lab.shared.exception.ConflictException;
import com.example.week07_lab.shared.exception.ForbiddenException;
import com.example.week07_lab.shared.exception.NotFoundException;
import com.example.week07_lab.user.domain.User;
import com.example.week07_lab.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingResponse book(String customerEmail, BookingRequest request) {
        User customer = userService.getByEmail(customerEmail);

        Flight flight = flightRepository.findByIdForUpdate(request.getFlightId())
                .orElseThrow(() -> new NotFoundException("No existe el vuelo con id " + request.getFlightId()));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        if (!flight.getDepartureTime().isAfter(now)) {
            boolean inTransit = flight.getArrivalTime().isAfter(now);
            String reason = inTransit
                    ? "El vuelo " + flight.getFlightNumber() + " ya esta en transito"
                    : "El vuelo " + flight.getFlightNumber() + " ya partio (vuelo pasado)";
            throw new BadRequestException(reason);
        }

        if (flight.getAvailableSeats() <= 0) {
            throw new ConflictException("El vuelo " + flight.getFlightNumber() + " no tiene asientos disponibles");
        }

        boolean hasConflict = bookingRepository.existsScheduleConflict(
                customer.getId(), flight.getDepartureTime(), flight.getArrivalTime());
        if (hasConflict) {
            throw new ConflictException("Ya tienes una reserva en un vuelo que se superpone con el horario del vuelo "
                    + flight.getFlightNumber());
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);

        Booking newBooking = new Booking();
        newBooking.setCustomer(customer);
        newBooking.setFlight(flight);
        newBooking.setCustomerFirstName(customer.getFirstName());
        newBooking.setCustomerLastName(customer.getLastName());
        newBooking.setBookingDate(now);

        Booking saved = bookingRepository.save(newBooking);

        eventPublisher.publishEvent(new BookingConfirmedEvent(
                saved.getId(),
                saved.getCustomerFirstName(),
                saved.getCustomerLastName(),
                customer.getEmail(),
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                saved.getBookingDate()
        ));

        return BookingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(String customerEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("No existe la reserva con id " + bookingId));

        String ownerEmail = booking.getCustomer().getEmail();
        if (!ownerEmail.equals(customerEmail)) {
            throw new ForbiddenException("La reserva " + bookingId + " pertenece a otro usuario");
        }

        return BookingResponse.from(booking);
    }
}
