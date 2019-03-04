package com.improbable.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.improbable.queue.model.UserStub;
import com.improbable.queue.service.IdentifierService;
import com.improbable.queue.service.QueueService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(QueueController.class)
public class QueueControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private QueueService queueService;

    @MockBean
    private IdentifierService identifierService;

    @Test
    public void joinShouldReturnIdentifier() throws Exception {
        String identifier = UUID.randomUUID().toString();
        given(queueService.join(any())).willReturn(identifier);

        UserStub mockObject = UserStub.builder().username("anything").build();
        MvcResult result = mvc.perform(post("/queue/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockObject)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).matches("^" + identifier + "$");
    }

    @Test
    public void pollWhenIdentifierIsInvalidShouldReturnBadRequest() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(false);
        mvc.perform(get("/queue/poll?identifier=blabla"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void pollWhenIdentifierIsNotInTheQueueShouldReturnNotFound() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(true);
        given(queueService.checkExists(any())).willReturn(false);
        mvc.perform(get("/queue/poll?identifier=blabla"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void pollWhenIdentifierIsValidShouldReturnPosition() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(true);
        given(queueService.checkExists(any())).willReturn(true);
        given(queueService.getPositionInQueue(any())).willReturn(5);
        MvcResult result = mvc.perform(get("/queue/poll?identifier=blabla"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).matches("5");
    }

    @Test
    public void pollWhenIdentifierIsAdmittableShouldReturnJoin() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(true);
        given(queueService.checkExists(any())).willReturn(true);
        given(queueService.isIdentifierAdmittable(any())).willReturn(true);
        MvcResult result = mvc.perform(get("/queue/poll?identifier=blabla"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).matches("JOIN");
    }

    @Test
    public void leaveWhenIdentifierIsInvalidShouldReturnBadRequest() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(false);
        mvc.perform(put("/queue/leave?identifier=blabla"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void leaveWhenIdentifierIsNotInTheQueueShouldReturnNotFound() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(true);
        given(queueService.checkExists(any())).willReturn(false);
        mvc.perform(put("/queue/leave?identifier=blabla"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void leaveWhenIdentifierIsValidShouldReturnOk() throws Exception {
        given(identifierService.validateIdentifier(any())).willReturn(true);
        given(queueService.checkExists(any())).willReturn(true);
        given(queueService.removeIdentifier(any())).willReturn(true);
        mvc.perform(get("/queue/poll?identifier=blabla"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void admitWhenQueueIsEmptyShouldReturnNotFound() throws Exception {
        given(queueService.isQueueEmpty()).willReturn(true);
        mvc.perform(post("/queue/admit"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void admitWhenFirstUserIsAdmittableShouldReturnConflict() throws Exception {
        given(queueService.isQueueEmpty()).willReturn(false);
        given(queueService.getFirst()).willReturn(UserStub.builder().isAdmittable(true).build());
        given(queueService.isIdentifierAdmittable(any())).willReturn(true);
        mvc.perform(post("/queue/admit"))
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    public void admitWhenFirstUserIsNotAdmittableShouldReturnOk() throws Exception {
        given(queueService.isQueueEmpty()).willReturn(false);
        given(queueService.getFirst()).willReturn(UserStub.builder().isAdmittable(false).build());
        given(queueService.isIdentifierAdmittable(any())).willReturn(false);
        mvc.perform(post("/queue/admit"))
                .andExpect(status().isOk())
                .andReturn();
    }
}
