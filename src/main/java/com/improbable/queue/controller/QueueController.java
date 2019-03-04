package com.improbable.queue.controller;

import com.improbable.queue.model.UserStub;
import com.improbable.queue.service.IdentifierService;
import com.improbable.queue.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("queue")
public class QueueController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private IdentifierService identifierService;

    @PostMapping(value = "join")
    public ResponseEntity join(String username) {
        String identifier = queueService.join(UserStub.builder().username(username).build());
        return ResponseEntity.status(HttpStatus.OK).body(identifier);
    }

    @GetMapping("poll")
    public ResponseEntity poll(@RequestParam("identifier") String identifier) {
//        validate identifier
        if (!identifierService.validateIdentifier(identifier)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid identifier");
        }
        if (queueService.checkExists(identifier)) {
            if (queueService.isIdentifierAdmittable(identifier)) {
                return ResponseEntity.status(HttpStatus.OK).body("JOIN");
            } else {
                long position = queueService.getPositionInQueue(identifier);
                return ResponseEntity.status(HttpStatus.OK).body(position);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PutMapping("leave")
    public ResponseEntity leave(@RequestParam("identifier") String identifier) {
//        validate identifier
        if (!identifierService.validateIdentifier(identifier)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid identifier");
        }
        if (queueService.checkExists(identifier)) {
            boolean isSuccessful = queueService.removeIdentifier(identifier);
            return isSuccessful ? ResponseEntity.status(HttpStatus.OK).build() : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping(value = "admit")
    public ResponseEntity admit() {
        if(queueService.isQueueEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        UserStub userStub = queueService.getFirst();
        boolean isAdmittable = queueService.isIdentifierAdmittable(userStub.getIdentifier());
        if (isAdmittable) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            userStub.setIsAdmittable(Boolean.TRUE);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }
}
