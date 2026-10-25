package com.example

import com.example.data.model.BookingStatus
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskPulseUnitTest {

    @Test
    fun testBookingStatusFlow() {
        val statuses = listOf(
            BookingStatus.SEARCHING,
            BookingStatus.ACCEPTED,
            BookingStatus.EN_ROUTE,
            BookingStatus.ARRIVED,
            BookingStatus.IN_PROGRESS,
            BookingStatus.COMPLETED
        )

        for (i in 0 until statuses.size - 1) {
            assertTrue(statuses[i].stepIndex < statuses[i + 1].stepIndex)
        }
    }

    @Test
    fun testPricingCalculation() {
        val category = ServiceCategory(
            id = "plumbing",
            name = "Plumbing",
            iconKey = "plumbing",
            basePrice = 45.0,
            hourlyRate = 65.0,
            avgEtaMinutes = 15,
            description = "Fix leaks",
            commonIssues = listOf("Leaking pipe")
        )

        val regularTotal = category.basePrice + category.hourlyRate
        assertEquals(110.0, regularTotal, 0.001)

        val emergencyFee = 25.0
        val emergencyTotal = regularTotal + emergencyFee
        assertEquals(135.0, emergencyTotal, 0.001)
    }

    @Test
    fun testServiceBookingCreation() {
        val booking = ServiceBooking(
            id = "bk_123",
            categoryId = "electrical",
            categoryName = "Electrical",
            issueTitle = "Tripped Breaker",
            issueDetails = "Master bedroom power is off",
            customerName = "Alex Morgan",
            customerPhone = "(555) 123-4567",
            customerAddress = "1428 Elm Street, Apt 4B",
            providerId = "prov_1",
            providerName = "Marcus Vance",
            providerRating = 4.95,
            providerPhone = "(555) 234-8901",
            status = BookingStatus.SEARCHING,
            isEmergency = true,
            scheduledTime = "Immediate Dispatch",
            estimatedCost = 150.0,
            finalCost = 150.0,
            currentEtaMinutes = 12,
            progressFraction = 0.1f,
            createdAt = System.currentTimeMillis()
        )

        assertNotNull(booking.id)
        assertEquals("Electrical", booking.categoryName)
        assertTrue(booking.isEmergency)
    }
}
