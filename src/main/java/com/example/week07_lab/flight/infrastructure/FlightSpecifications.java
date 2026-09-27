package com.example.week07_lab.flight.infrastructure;

import com.example.week07_lab.flight.domain.Flight;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class FlightSpecifications {

    private FlightSpecifications() {
    }

    public static Specification<Flight> search(String flightNumber, String airline,
                                               LocalDateTime departureFrom, LocalDateTime departureTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(flightNumber)) {
                predicates.add(cb.like(cb.upper(root.get("flightNumber")), containsPattern(flightNumber.toUpperCase()), '\\'));
            }

            if (StringUtils.hasText(airline)) {
                predicates.add(cb.like(cb.lower(root.get("airline")), containsPattern(airline.toLowerCase()), '\\'));
            }

            if (departureFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureTime"), departureFrom));
            }

            if (departureTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("departureTime"), departureTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String containsPattern(String value) {
        String escaped = value.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
