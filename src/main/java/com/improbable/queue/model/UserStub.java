package com.improbable.queue.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStub {
    private String identifier;
    private String username;
    private Boolean isAdmittable;
}
