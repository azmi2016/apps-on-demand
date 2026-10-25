package com.example.data.repository

import android.content.Context
import com.example.data.db.BookingEntity
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProviderEntity
import com.example.data.db.TaskPulseDatabase
import com.example.data.model.BookingStatus
import com.example.data.model.ChatMessage
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class TaskPulseRepository(context: Context) {
    private val database = TaskPulseDatabase.getDatabase(context)
    private val dao = database.dao()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var simulationJob: Job? = null

    val categories: List<ServiceCategory> = listOf(
        ServiceCategory(
            id = "plumbing",
            name = "Plumbing",
            iconKey = "plumbing",
            basePrice = 45.0,
            hourlyRate = 65.0,
            avgEtaMinutes = 14,
            description = "Pipe leaks, drain clogs, water heaters, toilet & fixture repairs.",
            commonIssues = listOf("Leaking pipe or faucet", "Clogged sink or drain", "Toilet running / overflowing", "Water heater inspection", "Garbage disposal jammed")
        ),
        ServiceCategory(
            id = "electrical",
            name = "Electrical",
            iconKey = "electrical",
            basePrice = 50.0,
            hourlyRate = 75.0,
            avgEtaMinutes = 18,
            description = "Tripped breakers, flickering lights, outlet install, ceiling fans.",
            commonIssues = listOf("Breaker tripping repeatedly", "Dead power outlet", "Light switch / fixture issue", "EV charger setup inspection", "Ceiling fan installation")
        ),
        ServiceCategory(
            id = "hvac",
            name = "HVAC & AC",
            iconKey = "hvac",
            basePrice = 55.0,
            hourlyRate = 80.0,
            avgEtaMinutes = 20,
            description = "AC not cooling, furnace tune-up, thermostat replacement, filters.",
            commonIssues = listOf("AC blowing warm air", "Noisy HVAC unit", "Thermostat unresponsive", "Filter replacement & airflow", "Refrigerant leak diagnostic")
        ),
        ServiceCategory(
            id = "cleaning",
            name = "Home Cleaning",
            iconKey = "cleaning",
            basePrice = 35.0,
            hourlyRate = 45.0,
            avgEtaMinutes = 25,
            description = "Deep cleaning, move-in/out sanitization, kitchen & bathroom overhaul.",
            commonIssues = listOf("Standard residential clean", "Deep kitchen & bath clean", "Post-renovation cleanup", "Move-out inspection clean", "Window & upholstery detail")
        ),
        ServiceCategory(
            id = "appliance",
            name = "Appliance Repair",
            iconKey = "appliance",
            basePrice = 40.0,
            hourlyRate = 60.0,
            avgEtaMinutes = 19,
            description = "Washers, dryers, refrigerators, dishwashers, and ovens.",
            commonIssues = listOf("Refrigerator not cooling", "Washing machine won't spin", "Dryer not heating", "Dishwasher leaking water", "Oven temperature erratic")
        ),
        ServiceCategory(
            id = "locksmith",
            name = "Locksmith",
            iconKey = "locksmith",
            basePrice = 50.0,
            hourlyRate = 70.0,
            avgEtaMinutes = 11,
            description = "Emergency lockout, smart lock install, re-keying, deadbolt repair.",
            commonIssues = listOf("Locked out of house/apartment", "Broken key in lock", "Smart keypad lock installation", "Deadbolt replacement", "Key re-keying & duplication")
        ),
        ServiceCategory(
            id = "handyman",
            name = "Handyman",
            iconKey = "handyman",
            basePrice = 35.0,
            hourlyRate = 50.0,
            avgEtaMinutes = 22,
            description = "Furniture assembly, TV wall mounting, drywall patch, door hanging.",
            commonIssues = listOf("TV wall mounting (up to 75\")", "IKEA furniture assembly", "Drywall hole patching", "Door alignment & squeak fix", "Curtain rod & shelf hanging")
        )
    )

    init {
        scope.launch {
            seedDefaultProvidersIfEmpty()
        }
    }

    private suspend fun seedDefaultProvidersIfEmpty() {
        if (dao.getProviderCount() == 0) {
            val initialProviders = listOf(
                ProviderEntity(
                    id = "prov_1",
                    name = "Marcus Vance",
                    categoryId = "electrical",
                    categoryName = "Electrical",
                    rating = 4.95,
                    reviewCount = 186,
                    hourlyRate = 75.0,
                    distanceKm = 1.4,
                    phone = "(555) 234-8901",
                    completedJobs = 342,
                    isOnline = true,
                    bio = "Certified Master Electrician with 12+ years experience. Specializes in emergency troubleshooting and residential panel upgrades.",
                    verified = true
                ),
                ProviderEntity(
                    id = "prov_2",
                    name = "Dave Reynolds",
                    categoryId = "plumbing",
                    categoryName = "Plumbing",
                    rating = 4.92,
                    reviewCount = 210,
                    hourlyRate = 65.0,
                    distanceKm = 2.1,
                    phone = "(555) 876-5432",
                    completedJobs = 415,
                    isOnline = true,
                    bio = "Licensed Journeyman Plumber. Equipped with full truck inventory for instantaneous leak stops and drain jetting.",
                    verified = true
                ),
                ProviderEntity(
                    id = "prov_3",
                    name = "Elena Rostova",
                    categoryId = "cleaning",
                    categoryName = "Home Cleaning",
                    rating = 4.98,
                    reviewCount = 154,
                    hourlyRate = 45.0,
                    distanceKm = 2.8,
                    phone = "(555) 432-9871",
                    completedJobs = 280,
                    isOnline = true,
                    bio = "Eco-friendly professional residential cleaning team lead. Detail-oriented hospital-grade disinfection certification.",
                    verified = true
                ),
                ProviderEntity(
                    id = "prov_4",
                    name = "Carlos Morales",
                    categoryId = "hvac",
                    categoryName = "HVAC & AC",
                    rating = 4.88,
                    reviewCount = 98,
                    hourlyRate = 80.0,
                    distanceKm = 3.2,
                    phone = "(555) 345-6789",
                    completedJobs = 195,
                    isOnline = true,
                    bio = "EPA universal certified HVAC specialist. Fast response on cooling compressor failures and smart thermostat setups.",
                    verified = true
                ),
                ProviderEntity(
                    id = "prov_5",
                    name = "Arthur Pendelton",
                    categoryId = "locksmith",
                    categoryName = "Locksmith",
                    rating = 4.97,
                    reviewCount = 312,
                    hourlyRate = 70.0,
                    distanceKm = 0.9,
                    phone = "(555) 998-1122",
                    completedJobs = 520,
                    isOnline = true,
                    bio = "Rapid mobile locksmith response unit. High-security deadbolts, digital locks, and non-destructive entry specialist.",
                    verified = true
                ),
                ProviderEntity(
                    id = "prov_6",
                    name = "Kenji Sato",
                    categoryId = "appliance",
                    categoryName = "Appliance Repair",
                    rating = 4.91,
                    reviewCount = 143,
                    hourlyRate = 60.0,
                    distanceKm = 2.4,
                    phone = "(555) 554-3321",
                    completedJobs = 260,
                    isOnline = true,
                    bio = "Certified factory technician for Samsung, LG, Whirlpool, and Bosch appliances. 90-day parts warranty guaranteed.",
                    verified = true
                ),
                ProviderEntity(
                    id = "prov_7",
                    name = "Leo Sterling",
                    categoryId = "handyman",
                    categoryName = "Handyman",
                    rating = 4.89,
                    reviewCount = 177,
                    hourlyRate = 50.0,
                    distanceKm = 1.7,
                    phone = "(555) 667-8899",
                    completedJobs = 310,
                    isOnline = true,
                    bio = "Carpentry, drywall, TV mounting, and home repairs. Fast, tidy, and brings laser levelers and heavy duty anchors.",
                    verified = true
                )
            )
            dao.insertProviders(initialProviders)
        }
    }

    fun getAllBookings(): Flow<List<ServiceBooking>> =
        dao.getAllBookingsFlow().map { list -> list.map { it.toModel() } }

    fun getActiveBooking(): Flow<ServiceBooking?> =
        dao.getActiveBookingFlow().map { it?.toModel() }

    fun getActiveBookings(): Flow<List<ServiceBooking>> =
        dao.getActiveBookingsFlow().map { list -> list.map { it.toModel() } }

    fun getBookingById(id: String): Flow<ServiceBooking?> =
        dao.getBookingByIdFlow(id).map { it?.toModel() }

    fun getAllProviders(): Flow<List<ServiceProvider>> =
        dao.getAllProvidersFlow().map { list -> list.map { it.toModel() } }

    fun getChatMessages(bookingId: String): Flow<List<ChatMessage>> =
        dao.getChatMessagesFlow(bookingId).map { list -> list.map { it.toModel() } }

    suspend fun createBooking(
        category: ServiceCategory,
        issueTitle: String,
        issueDetails: String,
        isEmergency: Boolean,
        customerAddress: String,
        customerName: String = "Alex Morgan",
        customerPhone: String = "(555) 123-4567",
        chosenProvider: ServiceProvider? = null
    ): String {
        val bookingId = "bk_" + UUID.randomUUID().toString().substring(0, 8)
        val initialEta = if (isEmergency) 12 else category.avgEtaMinutes
        val emergencySurcharge = if (isEmergency) 25.0 else 0.0
        val estimatedCost = category.basePrice + category.hourlyRate + emergencySurcharge

        val provider = chosenProvider

        val bookingEntity = BookingEntity(
            id = bookingId,
            categoryId = category.id,
            categoryName = category.name,
            issueTitle = issueTitle,
            issueDetails = issueDetails,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            providerId = provider?.id,
            providerName = provider?.name,
            providerRating = provider?.rating,
            providerPhone = provider?.phone,
            status = if (provider != null) BookingStatus.ACCEPTED.name else BookingStatus.SEARCHING.name,
            isEmergency = isEmergency,
            scheduledTime = if (isEmergency) "Immediate Dispatch" else "Today, ASAP",
            estimatedCost = estimatedCost,
            finalCost = estimatedCost,
            currentEtaMinutes = initialEta,
            progressFraction = if (provider != null) 0.15f else 0.05f,
            createdAt = System.currentTimeMillis(),
            completedAt = null,
            rating = null,
            reviewComment = null,
            tipAmount = 0.0
        )
        dao.insertBooking(bookingEntity)

        // Add welcome system chat message
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                bookingId = bookingId,
                senderName = "TaskPulse Dispatch",
                message = "Order placed for $issueTitle. Broadcasting to nearby verified technicians...",
                timestamp = System.currentTimeMillis(),
                isFromCustomer = false
            )
        )

        // Launch simulated real-time lifecycle
        startLifecycleSimulation(bookingId, category.id, provider)

        return bookingId
    }

    private fun startLifecycleSimulation(bookingId: String, categoryId: String, preAssignedProvider: ServiceProvider?) {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            // Step 1: Finding provider if not chosen
            if (preAssignedProvider == null) {
                delay(2800) // simulated search time
                val current = dao.getBookingById(bookingId) ?: return@launch
                if (current.status == BookingStatus.CANCELLED.name) return@launch

                // Pick best matching online provider
                val allProv = dao.getProviderById("prov_2") ?: dao.getProviderById("prov_1")
                val provider = allProv?.toModel() ?: ServiceProvider(
                    id = "prov_auto",
                    name = "Marcus Vance",
                    categoryId = categoryId,
                    categoryName = "Certified Specialist",
                    rating = 4.95,
                    reviewCount = 186,
                    hourlyRate = 70.0,
                    distanceKm = 1.6,
                    phone = "(555) 234-8901",
                    completedJobs = 340,
                    bio = "Verified local technician on dispatch.",
                    verified = true
                )

                val updated = current.copy(
                    providerId = provider.id,
                    providerName = provider.name,
                    providerRating = provider.rating,
                    providerPhone = provider.phone,
                    status = BookingStatus.ACCEPTED.name,
                    progressFraction = 0.2f
                )
                dao.updateBooking(updated)

                dao.insertChatMessage(
                    ChatMessageEntity(
                        id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                        bookingId = bookingId,
                        senderName = provider.name,
                        message = "Hi Alex, I accepted your request! Gathering my tools and heading to my service van now.",
                        timestamp = System.currentTimeMillis(),
                        isFromCustomer = false
                    )
                )
            }

            // Step 2: En Route Transition
            delay(3500)
            val currentEnRoute = dao.getBookingById(bookingId) ?: return@launch
            if (currentEnRoute.status == BookingStatus.CANCELLED.name || currentEnRoute.status == BookingStatus.COMPLETED.name) return@launch

            var currentEta = currentEnRoute.currentEtaMinutes
            dao.updateBooking(
                currentEnRoute.copy(
                    status = BookingStatus.EN_ROUTE.name,
                    progressFraction = 0.35f
                )
            )

            // Step 3: Realistic travel progression (updates ETA and progressFraction)
            val totalSteps = 4
            for (step in 1..totalSteps) {
                delay(3000)
                val live = dao.getBookingById(bookingId) ?: return@launch
                if (live.status != BookingStatus.EN_ROUTE.name) return@launch
                currentEta = maxOf(1, currentEta - 3)
                val progress = 0.35f + (0.45f * (step.toFloat() / totalSteps))
                dao.updateBooking(live.copy(currentEtaMinutes = currentEta, progressFraction = progress))
            }

            // Step 4: Arrived
            delay(2500)
            val currentArrived = dao.getBookingById(bookingId) ?: return@launch
            if (currentArrived.status != BookingStatus.EN_ROUTE.name) return@launch

            dao.updateBooking(
                currentArrived.copy(
                    status = BookingStatus.ARRIVED.name,
                    currentEtaMinutes = 0,
                    progressFraction = 0.82f
                )
            )

            dao.insertChatMessage(
                ChatMessageEntity(
                    id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                    bookingId = bookingId,
                    senderName = currentArrived.providerName ?: "Technician",
                    message = "I have arrived at your address! Parking out front.",
                    timestamp = System.currentTimeMillis(),
                    isFromCustomer = false
                )
            )

            // Step 5: In Progress
            delay(4000)
            val currentWork = dao.getBookingById(bookingId) ?: return@launch
            if (currentWork.status != BookingStatus.ARRIVED.name) return@launch

            dao.updateBooking(
                currentWork.copy(
                    status = BookingStatus.IN_PROGRESS.name,
                    progressFraction = 0.92f
                )
            )
        }
    }

    suspend fun advanceBookingStep(bookingId: String) {
        val current = dao.getBookingById(bookingId) ?: return
        val currentStatus = try { BookingStatus.valueOf(current.status) } catch (e: Exception) { BookingStatus.SEARCHING }

        val (nextStatus, nextEta, nextProgress) = when (currentStatus) {
            BookingStatus.SEARCHING -> Triple(BookingStatus.ACCEPTED, current.currentEtaMinutes, 0.25f)
            BookingStatus.ACCEPTED -> Triple(BookingStatus.EN_ROUTE, current.currentEtaMinutes, 0.45f)
            BookingStatus.EN_ROUTE -> Triple(BookingStatus.ARRIVED, 0, 0.85f)
            BookingStatus.ARRIVED -> Triple(BookingStatus.IN_PROGRESS, 0, 0.95f)
            BookingStatus.IN_PROGRESS -> Triple(BookingStatus.COMPLETED, 0, 1.0f)
            BookingStatus.COMPLETED, BookingStatus.CANCELLED -> return
        }

        val updated = current.copy(
            status = nextStatus.name,
            currentEtaMinutes = nextEta,
            progressFraction = nextProgress,
            completedAt = if (nextStatus == BookingStatus.COMPLETED) System.currentTimeMillis() else current.completedAt,
            providerName = if (current.providerName == null && nextStatus != BookingStatus.CANCELLED) "Marcus Vance" else current.providerName,
            providerPhone = if (current.providerPhone == null) "(555) 234-8901" else current.providerPhone,
            providerRating = if (current.providerRating == null) 4.95 else current.providerRating
        )
        dao.updateBooking(updated)

        // Post chat update
        val statusMessage = when (nextStatus) {
            BookingStatus.ACCEPTED -> "Pro accepted your request and is preparing equipment."
            BookingStatus.EN_ROUTE -> "Technician is en route with your repair tools."
            BookingStatus.ARRIVED -> "Technician arrived outside."
            BookingStatus.IN_PROGRESS -> "Work officially started on site."
            BookingStatus.COMPLETED -> "Service complete! Diagnostics and repair verified. Receipt ready."
            else -> ""
        }
        if (statusMessage.isNotEmpty()) {
            dao.insertChatMessage(
                ChatMessageEntity(
                    id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                    bookingId = bookingId,
                    senderName = updated.providerName ?: "Service Pro",
                    message = statusMessage,
                    timestamp = System.currentTimeMillis(),
                    isFromCustomer = false
                )
            )
        }
    }

    suspend fun cancelBooking(bookingId: String) {
        simulationJob?.cancel()
        val current = dao.getBookingById(bookingId) ?: return
        dao.updateBooking(current.copy(status = BookingStatus.CANCELLED.name))
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                bookingId = bookingId,
                senderName = "TaskPulse Dispatch",
                message = "Booking request was cancelled.",
                timestamp = System.currentTimeMillis(),
                isFromCustomer = false
            )
        )
    }

    suspend fun completeBooking(bookingId: String) {
        val current = dao.getBookingById(bookingId) ?: return
        dao.updateBooking(
            current.copy(
                status = BookingStatus.COMPLETED.name,
                progressFraction = 1.0f,
                completedAt = System.currentTimeMillis()
            )
        )
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                bookingId = bookingId,
                senderName = "TaskPulse Dispatch",
                message = "Job finished successfully. Total billed: $${"%.2f".format(current.finalCost)}",
                timestamp = System.currentTimeMillis(),
                isFromCustomer = false
            )
        )
    }

    suspend fun rateBooking(bookingId: String, stars: Int, review: String, tip: Double) {
        val current = dao.getBookingById(bookingId) ?: return
        val newFinal = current.finalCost + tip
        dao.updateBooking(
            current.copy(
                rating = stars,
                reviewComment = review,
                tipAmount = tip,
                finalCost = newFinal
            )
        )
    }

    suspend fun sendChatMessage(bookingId: String, messageText: String, isCustomer: Boolean, senderName: String) {
        val msg = ChatMessageEntity(
            id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
            bookingId = bookingId,
            senderName = senderName,
            message = messageText,
            timestamp = System.currentTimeMillis(),
            isFromCustomer = isCustomer
        )
        dao.insertChatMessage(msg)

        // Automated smart reply if customer sent message during active booking
        if (isCustomer) {
            scope.launch {
                delay(1200)
                val booking = dao.getBookingById(bookingId) ?: return@launch
                val reply = when {
                    messageText.contains("gate", ignoreCase = true) || messageText.contains("code", ignoreCase = true) ->
                        "Got the access code, thank you! Be right in."
                    messageText.contains("where", ignoreCase = true) || messageText.contains("eta", ignoreCase = true) || messageText.contains("long", ignoreCase = true) ->
                        "Just a few minutes away, navigating nearby traffic!"
                    messageText.contains("park", ignoreCase = true) ->
                        "Understood, parking in the visitor spot."
                    else ->
                        "Thanks for the update! See you in a moment."
                }
                dao.insertChatMessage(
                    ChatMessageEntity(
                        id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                        bookingId = bookingId,
                        senderName = booking.providerName ?: "Technician",
                        message = reply,
                        timestamp = System.currentTimeMillis(),
                        isFromCustomer = false
                    )
                )
            }
        }
    }

    suspend fun setProviderOnline(providerId: String, isOnline: Boolean) {
        dao.setProviderOnline(providerId, isOnline)
    }

    suspend fun providerAcceptJob(bookingId: String, provider: ServiceProvider) {
        val current = dao.getBookingById(bookingId) ?: return
        dao.updateBooking(
            current.copy(
                providerId = provider.id,
                providerName = provider.name,
                providerPhone = provider.phone,
                providerRating = provider.rating,
                status = BookingStatus.ACCEPTED.name,
                progressFraction = 0.25f
            )
        )
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
                bookingId = bookingId,
                senderName = provider.name,
                message = "I have accepted this dispatch request and am getting ready to head to your location.",
                timestamp = System.currentTimeMillis(),
                isFromCustomer = false
            )
        )
    }
}
