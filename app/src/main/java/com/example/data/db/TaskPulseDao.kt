package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskPulseDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingByIdFlow(id: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveBookingFlow(): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun getActiveBookingsFlow(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("SELECT * FROM providers ORDER BY rating DESC")
    fun getAllProvidersFlow(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: String): ProviderEntity?

    @Query("SELECT * FROM providers WHERE categoryId = :categoryId AND isOnline = 1 ORDER BY rating DESC")
    fun getOnlineProvidersByCategoryFlow(categoryId: String): Flow<List<ProviderEntity>>

    @Query("SELECT COUNT(*) FROM providers")
    suspend fun getProviderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Query("UPDATE providers SET isOnline = :isOnline WHERE id = :id")
    suspend fun setProviderOnline(id: String, isOnline: Boolean)

    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun getChatMessagesFlow(bookingId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)
}
