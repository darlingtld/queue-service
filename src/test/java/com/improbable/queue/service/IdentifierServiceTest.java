package com.improbable.queue.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IdentifierServiceTest {

    @Autowired
    private IdentifierService identifierService;

    @Test
    public void validateIdentifierWhenValidShouldReturnTrue() {
        String identifier = "61bbcfec-4272-4344-94d5-9db33f228c7a";
        assertThat(identifierService.validateIdentifier(identifier)).isTrue();
    }

    @Test
    public void validateIdentifierWhenInvalidShouldReturnFalse() {
        String identifier = "61bbcfec-4272-4344-94d5";
        assertThat(identifierService.validateIdentifier(identifier)).isFalse();
    }
}
