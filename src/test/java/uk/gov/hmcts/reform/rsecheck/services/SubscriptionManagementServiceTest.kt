package uk.gov.hmcts.reform.rsecheck.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.hmcts.reform.demo.models.Subscription
import uk.gov.hmcts.reform.demo.services.SubscriptionManagementService
import uk.gov.hmcts.reform.rsecheck.services.SubscriptionManagementServiceTest
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

internal class SubscriptionManagementServiceTest {
    var subscriptionManagementService = SubscriptionManagementService()
    @Test
    fun unduplicatedMap() {
        val subscriptionList: MutableList<Subscription> = ArrayList()
        SUB1.userId = USER1
        SUB2.userId = USER2
        subscriptionList.add(SUB1)
        subscriptionList.add(SUB2)
        val expectedResponse: MutableMap<String, List<Subscription>> = ConcurrentHashMap()
        expectedResponse[USER1] = java.util.List.of(SUB1)
        expectedResponse[USER2] = java.util.List.of(SUB2)
        val response = subscriptionManagementService.deduplicateSubscriptions(subscriptionList)
        Assertions.assertEquals(response, expectedResponse, "should return a map of users to subscription lists")
    }

    @Test
    fun duplicatedMap() {
        SUB1.userId = USER1
        SUB2.userId = USER1
        SUB3.userId = USER2
        val subscriptionList: MutableList<Subscription> = ArrayList()
        subscriptionList.add(SUB1)
        subscriptionList.add(SUB2)
        subscriptionList.add(SUB3)
        val expectedResponse: MutableMap<String, List<Subscription>> = ConcurrentHashMap()
        expectedResponse[USER1] = java.util.List.of(SUB1, SUB2)
        expectedResponse[USER2] = java.util.List.of(SUB3)
        val response = subscriptionManagementService.deduplicateSubscriptions(subscriptionList)
        Assertions.assertEquals(response, expectedResponse, "should return a deduplicated map of users to subscription "
                + "lists")
    }

    companion object {
        private val SUB1 = Subscription()
        private val SUB2 = Subscription()
        private val SUB3 = Subscription()
        private const val USER1 = "testUser1"
        private const val USER2 = "testUser2"
    }
}
