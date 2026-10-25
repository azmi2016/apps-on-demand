package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BookingStatus
import com.example.data.model.ChatMessage
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceProvider

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val rating: Double,
    val reviewCount: Int,
    val hourlyRate: Double,
    val distanceKm: Double,
    val phone: String,
    val completedJobs: Int,
    val isOnline: Boolean,
    val bio: String,
    val verified: Boolean
) {
    fun toModel(): ServiceProvider = ServiceProvider(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        rating = rating,
        reviewCount = reviewCount,
        hourlyRate = hourlyRate,
        distanceKm = distanceKm,
        phone = phone,
        completedJobs = completedJobs,
        isOnline = isOnline,
        bio = bio,
        verified = verified
    )

    companion object {
        fun fromModel(model: ServiceProvider): ProviderEntity = ProviderEntity(
            id = model.id,
            name = model.name,
            categoryId = model.categoryId,
            categoryName = model.categoryName,
            rating = model.rating,
            reviewCount = model.reviewCount,
            hourlyRate = model.hourlyRate,
            distanceKm = model.distanceKm,
            phone = model.phone,
            completedJobs = model.completedJobs,
            isOnline = model.isOnline,
            bio = model.bio,
            verified = model.verified
        )
    }
}

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
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
    val status: String,
    val isEmergency: Boolean,
    val scheduledTime: String,
    val estimatedCost: Double,
    val finalCost: Double,
    val currentEtaMinutes: Int,
    val progressFraction: Float,
    val createdAt: Long,
    val completedAt: Long?,
    val rating: Int?,
    val reviewComment: String?,
    val tipAmount: Double
) {
    fun toModel(): ServiceBooking = ServiceBooking(
        id = id,
        categoryId = categoryId,
        categoryName = categoryName,
        issueTitle = issueTitle,
        issueDetails = issueDetails,
        customerName = customerName,
        customerPhone = customerPhone,
        customerAddress = customerAddress,
        providerId = providerId,
        providerName = providerName,
        providerRating = providerRating,
        providerPhone = providerPhone,
        status = try { BookingStatus.valueOf(status) } catch (e: Exception) { BookingStatus.SEARCHING },
        isEmergency = isEmergency,
        scheduledTime = scheduledTime,
        estimatedCost = estimatedCost,
        finalCost = finalCost,
        currentEtaMinutes = currentEtaMinutes,
        progressFraction = progressFraction,
        createdAt = createdAt,
        completedAt = completedAt,
        rating = rating,
        reviewComment = reviewComment,
        tipAmount = tipAmount
    )

    companion object {
        fun fromModel(model: ServiceBooking): BookingEntity = BookingEntity(
            id = model.id,
            categoryId = model.categoryId,
            categoryName = model.categoryName,
            issueTitle = model.issueTitle,
            issueDetails = model.issueDetails,
            customerName = model.customerName,
            customerPhone = model.customerPhone,
            customerAddress = model.customerAddress,
            providerId = model.providerId,
            providerName = model.providerName,
            providerRating = model.providerRating,
            providerPhone = model.providerPhone,
            status = model.status.name,
            isEmergency = model.isEmergency,
            scheduledTime = model.scheduledTime,
            estimatedCost = model.estimatedCost,
            finalCost = model.finalCost,
            currentEtaMinutes = model.currentEtaMinutes,
            progressFraction = model.progressFraction,
            createdAt = model.createdAt,
            completedAt = model.completedAt,
            rating = model.rating,
            reviewComment = model.reviewComment,
            tipAmount = model.tipAmount
        )
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long,
    val isFromCustomer: Boolean
) {
    fun toModel(): ChatMessage = ChatMessage(
        id = id,
        bookingId = bookingId,
        senderName = senderName,
        message = message,
        timestamp = timestamp,
        isFromCustomer = isFromCustomer
    )

    companion object {
        fun fromModel(model: ChatMessage): ChatMessageEntity = ChatMessageEntity(
            id = model.id,
            bookingId = model.bookingId,
            senderName = model.senderName,
            message = model.message,
            timestamp = model.timestamp,
            isFromCustomer = model.isFromCustomer
        )
    }
}
