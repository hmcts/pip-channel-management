package uk.gov.hmcts.reform.pip.channel.management.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.hmcts.reform.pip.model.subscription.Channel;
import uk.gov.hmcts.reform.pip.model.subscription.Subscription;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin" })
class ChannelManagementTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ROOT_URL = "/channel";
    private static final String EMAILS_URL = ROOT_URL + "/emails";
    private static final String API_URL = ROOT_URL + "/api";

    //test accounts ids and emails that exist in account man staging
    private static final String VALID_USER_ID_1 = "bcd9c298-697e-4d7d-abcd-f0ca8bdbe039";
    private static final String VALID_USER_ID_2 = "ca707a5d-3161-46d1-9f49-09d47eb77372";
    private static final String VALID_EMAIL_1 = "test_account@hmcts.com";
    private static final String VALID_EMAIL_2 = "test_account_admin@hmcts.com";
    private static final String INVALID_USER_ID = "0b802b2a-cab2-4dd2-aa82-fd0dde3d93fb";

    private static final Subscription SUBSCRIPTION_VALID_USER_1 = subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_1);
    private static final Subscription SUBSCRIPTION_VALID_USER_2 = subscriptionBuilder(Channel.EMAIL, VALID_USER_ID_2);
    private static final Subscription SUBSCRIPTION_INVALID_USER = subscriptionBuilder(Channel.EMAIL, INVALID_USER_ID);
    private static final Subscription COURTEL_SUBSCRIPTION = subscriptionBuilder(Channel.API_COURTEL, VALID_USER_ID_1);
    private static final String MAPS_MATCH_MESSAGE = "maps should match";

    private static ObjectMapper om = new ObjectMapper();

    static Subscription subscriptionBuilder(Channel channel, String userId) {
        Subscription subscription = new Subscription();
        subscription.setChannel(channel);
        subscription.setUserId(userId);

        return subscription;
    }

    List<Subscription> createSubscriptions() {
        return List.of(
            SUBSCRIPTION_VALID_USER_1,
            SUBSCRIPTION_VALID_USER_1,
            SUBSCRIPTION_VALID_USER_2,
            SUBSCRIPTION_VALID_USER_2
        );
    }

    @BeforeAll
    public static void setup() {
        om.findAndRegisterModules();
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
        expectedResult.put(VALID_EMAIL_1, List.of(SUBSCRIPTION_VALID_USER_1, SUBSCRIPTION_VALID_USER_1));
        expectedResult.put(VALID_EMAIL_2, List.of(SUBSCRIPTION_VALID_USER_2, SUBSCRIPTION_VALID_USER_2));

        ConcurrentHashMap<String, List<Subscription>> actualResponse = om.readValue(
            response.getResponse().getContentAsString(),
            new TypeReference<>() {});

        assertEquals(expectedResult, actualResponse, MAPS_MATCH_MESSAGE);
    }

    @Test
    void testSingleValidUserReturns() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(EMAILS_URL)
            .content(om.writeValueAsString(List.of(SUBSCRIPTION_VALID_USER_1, SUBSCRIPTION_INVALID_USER)))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        Map<String, List<Subscription>> expected = new ConcurrentHashMap<>();
        expected.put(VALID_EMAIL_1, List.of(SUBSCRIPTION_VALID_USER_1));

        ConcurrentHashMap<String, List<Subscription>> actualResponse = om.readValue(
            response.getResponse().getContentAsString(),
            new TypeReference<>() {});

        assertEquals(expected, actualResponse, MAPS_MATCH_MESSAGE);
    }

    @Test
    void testAllInvalidUsersThrowsNotFound() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(EMAILS_URL)
            .content(om.writeValueAsString(List.of(SUBSCRIPTION_INVALID_USER, SUBSCRIPTION_INVALID_USER)))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isNotFound()).andReturn();

        assertTrue(response.getResponse().getContentAsString()
                       .contains("No email channel found for any of the users provided"),
                   "Should contain expected not found message");
    }

    @Test
    void testReturnThirdPartyApi() throws Exception {
        List<Subscription> subscriptions = List.of(COURTEL_SUBSCRIPTION, COURTEL_SUBSCRIPTION);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(API_URL)
            .content(om.writeValueAsString(subscriptions))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        Map<String, List<Subscription>> expected = new ConcurrentHashMap<>();
        expected.put("testCourtelApi", subscriptions);

        ConcurrentHashMap<String, List<Subscription>> mappedResponse = om.readValue(response.getResponse()
                                                                                        .getContentAsString(),
                                                                      new TypeReference<>() {});

        assertEquals(expected, mappedResponse, MAPS_MATCH_MESSAGE);
    }

    @Test
    void testReturnThirdPartyApiReturnsNotFound() throws Exception {
        List<Subscription> subscriptions = List.of(SUBSCRIPTION_VALID_USER_1, COURTEL_SUBSCRIPTION);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post(API_URL)
            .content(om.writeValueAsString(subscriptions))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(request).andExpect(status().isNotFound()).andReturn();

        assertTrue(response.getResponse().getContentAsString()
                       .contains("Invalid channel for API subscriptions: EMAIL"), "Messages should match");
    }
}
