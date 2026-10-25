package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BookingStatus
import com.example.data.model.ChatMessage
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.UserMode
import com.example.data.repository.TaskPulseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN,
    LIVE_TRACKING,
    CHAT
}

class TaskPulseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskPulseRepository(application)

    val categories = repository.categories

    private val _userMode = MutableStateFlow(UserMode.CUSTOMER)
    val userMode: StateFlow<UserMode> = _userMode.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Explore/Home, 1: History
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Active bottom sheet
    private val _bookingSheetCategory = MutableStateFlow<ServiceCategory?>(null)
    val bookingSheetCategory: StateFlow<ServiceCategory?> = _bookingSheetCategory.asStateFlow()

    private val _preSelectedProvider = MutableStateFlow<ServiceProvider?>(null)
    val preSelectedProvider: StateFlow<ServiceProvider?> = _preSelectedProvider.asStateFlow()

    // Tracking / Detail selection
    private val _focusedBookingId = MutableStateFlow<String?>(null)
    val focusedBookingId: StateFlow<String?> = _focusedBookingId.asStateFlow()

    val allBookings: StateFlow<List<ServiceBooking>> = repository.getAllBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBooking: StateFlow<ServiceBooking?> = repository.getActiveBooking()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProviders: StateFlow<List<ServiceProvider>> = repository.getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusedBooking: StateFlow<ServiceBooking?> = _focusedBookingId.flatMapLatest { id ->
        if (id != null) repository.getBookingById(id) else repository.getActiveBooking()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<ChatMessage>> = _focusedBookingId.flatMapLatest { id ->
        val targetId = id ?: activeBooking.value?.id
        if (targetId != null) repository.getChatMessages(targetId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserMode(mode: UserMode) {
        _userMode.value = mode
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun openBookingSheet(category: ServiceCategory, provider: ServiceProvider? = null) {
        _bookingSheetCategory.value = category
        _preSelectedProvider.value = provider
    }

    fun closeBookingSheet() {
        _bookingSheetCategory.value = null
        _preSelectedProvider.value = null
    }

    fun createBooking(
        category: ServiceCategory,
        issueTitle: String,
        issueDetails: String,
        isEmergency: Boolean,
        customerAddress: String
    ) {
        viewModelScope.launch {
            val bookingId = repository.createBooking(
                category = category,
                issueTitle = issueTitle,
                issueDetails = issueDetails,
                isEmergency = isEmergency,
                customerAddress = customerAddress,
                chosenProvider = _preSelectedProvider.value
            )
            closeBookingSheet()
            _focusedBookingId.value = bookingId
            _currentScreen.value = AppScreen.LIVE_TRACKING
        }
    }

    fun navigateToTracking(booking: ServiceBooking) {
        _focusedBookingId.value = booking.id
        _currentScreen.value = AppScreen.LIVE_TRACKING
    }

    fun navigateToChat(booking: ServiceBooking) {
        _focusedBookingId.value = booking.id
        _currentScreen.value = AppScreen.CHAT
    }

    fun navigateBack() {
        when (_currentScreen.value) {
            AppScreen.CHAT -> _currentScreen.value = AppScreen.LIVE_TRACKING
            AppScreen.LIVE_TRACKING -> _currentScreen.value = AppScreen.MAIN
            AppScreen.MAIN -> {}
        }
    }

    fun advanceCurrentBookingStep() {
        val targetId = _focusedBookingId.value ?: activeBooking.value?.id ?: return
        viewModelScope.launch {
            repository.advanceBookingStep(targetId)
        }
    }

    fun cancelBooking() {
        val targetId = _focusedBookingId.value ?: activeBooking.value?.id ?: return
        viewModelScope.launch {
            repository.cancelBooking(targetId)
            _currentScreen.value = AppScreen.MAIN
        }
    }

    fun completeBooking() {
        val targetId = _focusedBookingId.value ?: activeBooking.value?.id ?: return
        viewModelScope.launch {
            repository.completeBooking(targetId)
        }
    }

    fun rateBooking(stars: Int, comment: String, tip: Double) {
        val targetId = _focusedBookingId.value ?: activeBooking.value?.id ?: return
        viewModelScope.launch {
            repository.rateBooking(targetId, stars, comment, tip)
        }
    }

    fun sendChatMessage(text: String) {
        val targetId = _focusedBookingId.value ?: activeBooking.value?.id ?: return
        val isCustomer = _userMode.value == UserMode.CUSTOMER
        val senderName = if (isCustomer) "Alex Morgan" else "Marcus Vance"
        viewModelScope.launch {
            repository.sendChatMessage(targetId, text, isCustomer, senderName)
        }
    }

    fun toggleProviderOnline(providerId: String, isOnline: Boolean) {
        viewModelScope.launch {
            repository.setProviderOnline(providerId, isOnline)
        }
    }

    fun providerAcceptJob(booking: ServiceBooking, provider: ServiceProvider) {
        viewModelScope.launch {
            repository.providerAcceptJob(booking.id, provider)
            _focusedBookingId.value = booking.id
        }
    }
}
