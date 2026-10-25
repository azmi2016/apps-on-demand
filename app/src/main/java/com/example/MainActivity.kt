package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BookingStatus
import com.example.data.model.ServiceProvider
import com.example.data.model.UserMode
import com.example.ui.AppScreen
import com.example.ui.TaskPulseViewModel
import com.example.ui.components.RoleModeToggle
import com.example.ui.screens.BookingBottomSheet
import com.example.ui.screens.CustomerHistoryScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.InAppChatScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.ProviderDashboardScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaskPulseApp()
            }
        }
    }
}

@Composable
fun TaskPulseApp(viewModel: TaskPulseViewModel = viewModel()) {
    val userMode by viewModel.userMode.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val activeBooking by viewModel.activeBooking.collectAsState()
    val allProviders by viewModel.allProviders.collectAsState()
    val focusedBooking by viewModel.focusedBooking.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val bookingSheetCategory by viewModel.bookingSheetCategory.collectAsState()
    val preSelectedProvider by viewModel.preSelectedProvider.collectAsState()

    // Handle Android system back button
    BackHandler(enabled = currentScreen != AppScreen.MAIN) {
        viewModel.navigateBack()
    }

    val defaultPro = allProviders.firstOrNull() ?: ServiceProvider(
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
        bio = "Certified Master Electrician with 12+ years experience.",
        verified = true
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            if (currentScreen == AppScreen.MAIN) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Top Bar Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "TaskPulse Logo",
                                tint = AccentAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TaskPulse",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            )
                            if (userMode == UserMode.CUSTOMER) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "1428 Elm Street, Apt 4B",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Provider Dispatch Console",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Dual Role Mode Switcher (Customer <-> Service Pro)
                    RoleModeToggle(
                        currentMode = userMode,
                        onModeChanged = { viewModel.setUserMode(it) }
                    )
                }
            }
        },
        bottomBar = {
            if (currentScreen == AppScreen.MAIN) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    if (userMode == UserMode.CUSTOMER) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setSelectedTab(0) },
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
                            label = { Text("Explore") },
                            modifier = Modifier.testTag("nav_explore")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setSelectedTab(1) },
                            icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
                            label = { Text("Activity") },
                            modifier = Modifier.testTag("nav_activity")
                        )
                    } else {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setSelectedTab(0) },
                            icon = { Icon(Icons.Default.Radar, contentDescription = "Live Radar") },
                            label = { Text("Radar") },
                            modifier = Modifier.testTag("nav_radar")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setSelectedTab(1) },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Earnings") },
                            label = { Text("History") },
                            modifier = Modifier.testTag("nav_earnings")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.MAIN -> {
                        if (userMode == UserMode.CUSTOMER) {
                            if (selectedTab == 0) {
                                CustomerHomeScreen(
                                    categories = viewModel.categories,
                                    providers = allProviders,
                                    activeBooking = activeBooking,
                                    onCategoryClick = { category ->
                                        viewModel.openBookingSheet(category)
                                    },
                                    onProviderBookClick = { provider ->
                                        val cat = viewModel.categories.firstOrNull { it.id == provider.categoryId }
                                            ?: viewModel.categories.first()
                                        viewModel.openBookingSheet(cat, provider)
                                    },
                                    onActiveBookingClick = { booking ->
                                        viewModel.navigateToTracking(booking)
                                    },
                                    onEmergencyClick = {
                                        val emergencyCat = viewModel.categories.firstOrNull { it.id == "plumbing" }
                                            ?: viewModel.categories.first()
                                        viewModel.openBookingSheet(emergencyCat)
                                    }
                                )
                            } else {
                                CustomerHistoryScreen(
                                    bookings = allBookings,
                                    onSelectBooking = { booking ->
                                        viewModel.navigateToTracking(booking)
                                    }
                                )
                            }
                        } else {
                            // Service Pro Mode
                            val incomingRequests = allBookings.filter { it.status == BookingStatus.SEARCHING }
                            val completedBookings = allBookings.filter { it.status == BookingStatus.COMPLETED }
                            val proActiveJob = activeBooking ?: allBookings.firstOrNull {
                                it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED
                            }

                            ProviderDashboardScreen(
                                currentProvider = defaultPro,
                                activeJob = proActiveJob,
                                incomingRequests = incomingRequests,
                                completedBookings = completedBookings,
                                onToggleOnline = { isOnline ->
                                    viewModel.toggleProviderOnline(defaultPro.id, isOnline)
                                },
                                onAcceptJob = { booking ->
                                    viewModel.providerAcceptJob(booking, defaultPro)
                                },
                                onAdvanceMilestone = {
                                    viewModel.advanceCurrentBookingStep()
                                },
                                onOpenChat = { booking ->
                                    viewModel.navigateToChat(booking)
                                }
                            )
                        }
                    }

                    AppScreen.LIVE_TRACKING -> {
                        val bookingToTrack = focusedBooking ?: activeBooking
                        if (bookingToTrack != null) {
                            LiveTrackingScreen(
                                booking = bookingToTrack,
                                onBack = { viewModel.navigateBack() },
                                onOpenChat = { viewModel.navigateToChat(bookingToTrack) },
                                onAdvanceStep = { viewModel.advanceCurrentBookingStep() },
                                onCancelBooking = { viewModel.cancelBooking() },
                                onCompleteBooking = { viewModel.completeBooking() },
                                onRateBooking = { stars, comment, tip ->
                                    viewModel.rateBooking(stars, comment, tip)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No active dispatch selected.")
                            }
                        }
                    }

                    AppScreen.CHAT -> {
                        val bookingForChat = focusedBooking ?: activeBooking
                        if (bookingForChat != null) {
                            InAppChatScreen(
                                booking = bookingForChat,
                                messages = chatMessages,
                                onSendMessage = { text ->
                                    viewModel.sendChatMessage(text)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No active chat found.")
                            }
                        }
                    }
                }
            }

            // Booking Modal Bottom Sheet
            if (bookingSheetCategory != null) {
                BookingBottomSheet(
                    category = bookingSheetCategory!!,
                    preSelectedProvider = preSelectedProvider,
                    onDismiss = { viewModel.closeBookingSheet() },
                    onConfirmBooking = { issueTitle, issueDetails, isEmergency, address ->
                        viewModel.createBooking(
                            category = bookingSheetCategory!!,
                            issueTitle = issueTitle,
                            issueDetails = issueDetails,
                            isEmergency = isEmergency,
                            customerAddress = address
                        )
                    }
                )
            }
        }
    }
}
