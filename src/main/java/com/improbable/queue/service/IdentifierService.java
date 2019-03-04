package com.improbable.queue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IdentifierService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierService.class);

    public String generateIdentifier() {
        return UUID.randomUUID().toString();
    }

    public boolean validateIdentifier(String identifier) {
        try {
            UUID.fromString(identifier);
            return true;
        } catch (Exception e) {
            LOGGER.error("identifier {} is invalid.  error is {}", identifier, e.getMessage());
            return false;
        }
    }
}
