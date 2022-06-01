package uk.gov.hmcts.reform.pip.channel.management.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.models.external.subscriptionmanagement.Channel;
import uk.gov.hmcts.reform.pip.channel.management.models.external.subscriptionmanagement.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin" })
class ChannelManagementTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ROOT_URL = "/channel";
    private static final String EMAILS_URL = ROOT_URL + "/emails";

    //test accounts ids and emails that exist in account man staging
    private static final String VALID_USER_ID_1 = "651bbefc-f856-46a2-a7da-ed248cf1775b";
    private static final String VALID_USER_ID_2 = "653aea00-7f3a-4eef-84ce-b24ed9194c56";
    private static final String VALID_EMAIL_1 = "test_account@hmcts.com";
    private static final String VALID_EMAIL_2 = "test_account_admin@hmcts.com";
    private static final String INVALID_USER_ID = "0b802b2a-cab2-4dd2-aa82-fd0dde3d93fb";
    private static final String MAPS_MATCH_MESSAGE = "maps should match";

    private final ObjectMapper om = new ObjectMapper();

    Subscription subscriptionBuilder(Channel channel, String userId) {
        Subscription subscription = new Subscription();
        subscription.setChannel(channel);
        subscription.setUserId(userId);

        return subscription;
    }

    List<Subscription> createSubscriptions() {
        List<Subscription> subList = new ArrayList<>();
        subList.add(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1));
        subList.add(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1));
        subList.add(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_2));
        subList.add(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_2));
        return subList;
    }

    @Test
    void testMultipleUsersEmailMapReturned() throws Exception {
        List<Subscription> subs = createSubscriptions();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(EMAILS_URL)
            .content(om.writeValueAsString(subs))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        Map<String, List<Subscription>> expectedResult = new ConcurrentHashMap<>();
        expectedResult.put(VALID_EMAIL_1, List.of(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1),
                                                     subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1)));
        expectedResult.put(VALID_EMAIL_2, List.of(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_2),
                                                      subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_2)));

        ConcurrentHashMap<String, List<Subscription>> actualResponse = om.readValue(
            response.getResponse().getContentAsString(),
            new TypeReference<>() {});

        assertEquals(expectedResult, actualResponse, MAPS_MATCH_MESSAGE);
    }

    @Test
    void testSingleValidUserReturns() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(EMAILS_URL)
            .content(om.writeValueAsString(List.of(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1),
                                                   subscriptionBuilder(Channel.EMAIL, INVALID_USER_ID))))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        Map<String, List<Subscription>> expected = new ConcurrentHashMap<>();
        expected.put(VALID_EMAIL_1, List.of(subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1)));

        ConcurrentHashMap<String, List<Subscription>> actualResponse = om.readValue(
            response.getResponse().getContentAsString(),
            new TypeReference<>() {});

        assertEquals(expected, actualResponse, MAPS_MATCH_MESSAGE);
    }

    @Test
    void testAllInvalidUsersThrowsNotFound() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(EMAILS_URL)
            .content(om.writeValueAsString(List.of(subscriptionBuilder(Channel.EMAIL, INVALID_USER_ID),
                                                   subscriptionBuilder(Channel.EMAIL, INVALID_USER_ID))))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isNotFound()).andReturn();

        assertTrue(response.getResponse().getContentAsString()
                       .contains("No email channel found for any of the users provided"),
                   "Should contain expected not found message");
    }
}
