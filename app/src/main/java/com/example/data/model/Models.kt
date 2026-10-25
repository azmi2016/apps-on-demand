package com.example.data.model

enum class BookingStatus(val displayName: String, val stepIndex: Int) {
    SEARCHING("Finding Nearby Pro", 0),
    ACCEPTED("Pro Accepted", 1),
    EN_ROUTE("En Route", 2),
    ARRIVED("Pro Arrived", 3),
    IN_PROGRESS("In Progress", 4),
    COMPLETED("Completed", 5),
    CANCELLED("Cancelled", -1)
}

enum class UserMode {
    CUSTOMER,
    PROVIDER
}

data class ServiceCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val basePrice: Double,
    val hourlyRate: Double,
    val avgEtaMinutes: Int,
    val description: String,
    val commonIssues: List<String>
)

data class ServiceProvider(
    val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val rating: Double,
    val reviewCount: Int,
    val hourlyRate: Double,
    val distanceKm: Double,
    val phone: String,
    val completedJobs: Int,
    val isOnline: Boolean = true,
    val bio: String,
    val verified: Boolean = true
)

data class ServiceBooking(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val issueTitle: String,
    val issueDetails: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val providerId: String?,
    val providerName: String?,
    val providerRating: Double?,
    val providerPhone: String?,
    val status: BookingStatus,
    val isEmergency: Boolean,
    val scheduledTime: String, // "Instant / ASAP" or formatted date
    val estimatedCost: Double,
    val finalCost: Double,
    val currentEtaMinutes: Int,
    val progressFraction: Float, // 0.0 to 1.0 (for en-route travel and progress bar)
    val createdAt: Long,
    val completedAt: Long? = null,
    val rating: Int? = null,
    val reviewComment: String? = null,
    val tipAmount: Double = 0.0
)

data class ChatMessage(
    val id: String,
    val bookingId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long,
    val isFromCustomer: Boolean
)
