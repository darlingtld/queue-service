package com.improbable.queue.service;

import com.improbable.queue.model.UserStub;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Test
    public void joinShouldReturnIdentifier() {
        String identifier = queueService.join(UserStub.builder().build());
        assertThat(identifier).matches("[0-9a-f]{8}-[0-9a-f]{4}-[34][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    @Test
    public void checkExistsWhenIdentifierExistsShouldReturnTrue() {
        String identifier = queueService.join(UserStub.builder().build());
        assertThat(queueService.checkExists(identifier)).isTrue();
    }

    @Test
    public void checkExistsWhenIdentifierNotExistsShouldReturnFalse() {
        queueService.join(UserStub.builder().build());
        assertThat(queueService.checkExists(UUID.randomUUID().toString())).isFalse();
    }

    @Test
    public void getPositionInQueueWhenIdentifierExistsShouldReturnPosition() {
        queueService.clear();
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        String identifier = queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.getPositionInQueue(identifier)).isEqualTo(3);
    }

    @Test
    public void getPositionInQueueWhenManyUsersShouldReturnPosition() {
        queueService.clear();
        UserStub userToCheck = null;
        for (int i = 0; i < 1_000_000; i++) {
            UserStub userStub = UserStub.builder().build();
            queueService.join(userStub);
            if (i == 234_567) {
                userToCheck = userStub;
            }
        }
        assertThat(queueService.getPositionInQueue(userToCheck.getIdentifier())).isEqualTo(234_567);
    }

    @Test
    public void getPositionInQueueWhenIdentifierNotExistsShouldReturnNegative() {
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.getPositionInQueue(UUID.randomUUID().toString())).isLessThan(0);
    }

    @Test
    public void isIdentifierAdmittableWhenIsAdmittableIsTrueShouldReturnTrue() {
        String identifier = queueService.join(UserStub.builder().isAdmittable(true).build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.isIdentifierAdmittable(identifier)).isTrue();
    }

    @Test
    public void isIdentifierAdmittableWhenIsAdmittableIsFalseShouldReturnFalse() {
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        String identifier = queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.isIdentifierAdmittable(identifier)).isFalse();
    }

    @Test
    public void removeIdentifierWhenIdentifierExistsShouldReturnTrue() {
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        String identifier = queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.removeIdentifier(identifier)).isTrue();
        assertThat(queueService.checkExists(identifier)).isFalse();
    }

    @Test
    public void removeIdentifierWhenIdentifierNotExistsShouldReturnFalse() {
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.removeIdentifier(UUID.randomUUID().toString())).isFalse();
    }

    @Test
    public void isQueueEmptyWhenJoinHappenedShouldReturnFalse() {
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.isQueueEmpty()).isFalse();
    }

    @Test
    public void isQueueEmptyShouldReturnTrue() {
        queueService.clear();
        assertThat(queueService.isQueueEmpty()).isTrue();
        String identifier = queueService.join(UserStub.builder().build());
        assertThat(queueService.isQueueEmpty()).isFalse();
        queueService.removeIdentifier(identifier);
        assertThat(queueService.isQueueEmpty()).isTrue();
    }

    @Test
    public void getFirstWhenQueueIsNotEmptyShouldReturnFirst() {
        queueService.clear();
        UserStub firstUser = UserStub.builder().build();
        queueService.join(firstUser);
        queueService.join(UserStub.builder().build());
        queueService.join(UserStub.builder().build());
        assertThat(queueService.getFirst()).isEqualTo(firstUser);
    }
}
