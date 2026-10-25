package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.UserMode
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusSuccess

@Composable
fun LiveRadarMapCanvas(
    progressFraction: Float, // 0.0f (far) to 1.0f (arrived at destination)
    status: BookingStatus,
    etaMinutes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // Background Grid / Simulated City Blocks
            val gridColor = Color(0xFF1E293B)
            val roadColor = Color(0xFF334155)

            // Draw horizontal streets
            for (y in 40 until height.toInt() step 50) {
                drawLine(
                    color = if (y % 100 == 0) roadColor else gridColor,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(width, y.toFloat()),
                    strokeWidth = if (y % 100 == 0) 5f else 2f
                )
            }

            // Draw vertical streets
            for (x in 40 until width.toInt() step 60) {
                drawLine(
                    color = if (x % 120 == 0) roadColor else gridColor,
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), height),
                    strokeWidth = if (x % 120 == 0) 5f else 2f
                )
            }

            // Customer Destination (fixed near top right: 75% width, 30% height)
            val customerDest = Offset(width * 0.78f, height * 0.32f)

            // Provider Start (fixed near bottom left: 20% width, 78% height)
            val providerStart = Offset(width * 0.18f, height * 0.78f)

            // Midpoint waypoint for realistic curved urban path
            val wayPoint1 = Offset(width * 0.35f, height * 0.48f)
            val wayPoint2 = Offset(width * 0.60f, height * 0.52f)

            // Calculate current provider position along the multi-segment route
            val clampedProgress = progressFraction.coerceIn(0.05f, 1.0f)
            val providerPos = when {
                clampedProgress <= 0.35f -> {
                    val p = clampedProgress / 0.35f
                    Offset(
                        providerStart.x + (wayPoint1.x - providerStart.x) * p,
                        providerStart.y + (wayPoint1.y - providerStart.y) * p
                    )
                }
                clampedProgress <= 0.7f -> {
                    val p = (clampedProgress - 0.35f) / 0.35f
                    Offset(
                        wayPoint1.x + (wayPoint2.x - wayPoint1.x) * p,
                        wayPoint1.y + (wayPoint2.y - wayPoint1.y) * p
                    )
                }
                else -> {
                    val p = (clampedProgress - 0.7f) / 0.3f
                    Offset(
                        wayPoint2.x + (customerDest.x - wayPoint2.x) * p,
                        wayPoint2.y + (customerDest.y - wayPoint2.y) * p
                    )
                }
            }

            // Draw Route Path
            val routePath = Path().apply {
                moveTo(providerStart.x, providerStart.y)
                lineTo(wayPoint1.x, wayPoint1.y)
                lineTo(wayPoint2.x, wayPoint2.y)
                lineTo(customerDest.x, customerDest.y)
            }

            // Route background shadow
            drawPath(
                path = routePath,
                color = Color(0x332563EB),
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )

            // Active dotted/solid route line
            drawPath(
                path = routePath,
                color = PrimaryBlueLight,
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Pulse ring around Customer Home
            drawCircle(
                color = AccentAmber.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = customerDest,
                style = Stroke(width = 3f)
            )

            // Customer Destination Marker
            drawCircle(
                color = AccentAmber,
                radius = 14f,
                center = customerDest
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = customerDest
            )

            // Provider Vehicle / Location Marker
            if (status != BookingStatus.SEARCHING) {
                // Pulse around Provider
                drawCircle(
                    color = PrimaryBlueLight.copy(alpha = 0.35f),
                    radius = 24f,
                    center = providerPos
                )
                drawCircle(
                    color = PrimaryBlue,
                    radius = 16f,
                    center = providerPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = providerPos
                )
            }
        }

        // Overlay Badge Labels
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color(0xDD0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (status == BookingStatus.COMPLETED) StatusSuccess else AccentOrange,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (status) {
                    BookingStatus.SEARCHING -> "Broadcasting to Nearby Techs"
                    BookingStatus.ACCEPTED -> "Technician Assigned"
                    BookingStatus.EN_ROUTE -> "Live GPS • ETA $etaMinutes min"
                    BookingStatus.ARRIVED -> "Technician Arrived Outside"
                    BookingStatus.IN_PROGRESS -> "Work In Progress"
                    BookingStatus.COMPLETED -> "Service Completed"
                    BookingStatus.CANCELLED -> "Cancelled"
                },
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Home destination label
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Color(0xDD0F172A), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = AccentAmber,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Your Home",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusTimelineIndicator(
    currentStatus: BookingStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        BookingStatus.SEARCHING to "Search",
        BookingStatus.ACCEPTED to "Accepted",
        BookingStatus.EN_ROUTE to "En Route",
        BookingStatus.ARRIVED to "Arrived",
        BookingStatus.IN_PROGRESS to "In Progress",
        BookingStatus.COMPLETED to "Done"
    )

    val currentIndex = currentStatus.stepIndex

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (stepStatus, label) ->
            val isPassed = currentIndex > index || currentStatus == BookingStatus.COMPLETED
            val isCurrent = currentIndex == index && currentStatus != BookingStatus.COMPLETED

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> StatusSuccess
                                isCurrent -> PrimaryBlue
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.6f)
                        .background(
                            if (isPassed) StatusSuccess else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
fun RoleModeToggle(
    currentMode: UserMode,
    onModeChanged: (UserMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("role_mode_toggle")
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (currentMode == UserMode.CUSTOMER) PrimaryBlue else Color.Transparent
                    )
                    .clickable { onModeChanged(UserMode.CUSTOMER) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tab_customer_mode")
            ) {
                Text(
                    text = "Customer View",
                    color = if (currentMode == UserMode.CUSTOMER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (currentMode == UserMode.PROVIDER) AccentOrange else Color.Transparent
                    )
                    .clickable { onModeChanged(UserMode.PROVIDER) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tab_provider_mode")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(StatusSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Service Pro Mode",
                        color = if (currentMode == UserMode.PROVIDER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("category_card_${category.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(getCategoryColor(category.id).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconKey),
                        contentDescription = category.name,
                        tint = getCategoryColor(category.id),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "~${category.avgEtaMinutes}m ETA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = category.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "From $${category.basePrice.toInt()}",
                fontSize = 12.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProviderCard(
    provider: ServiceProvider,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = provider.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (provider.verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Pro",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "${provider.categoryName} • ${provider.distanceKm} km away",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = AccentAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${provider.rating} (${provider.reviewCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${provider.hourlyRate.toInt()}/hr",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue,
                modifier = Modifier
                    .clickable { onBookClick() }
                    .testTag("book_provider_${provider.id}")
            ) {
                Text(
                    text = "Request",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

fun getCategoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "plumbing" -> Icons.Default.Plumbing
    "electrical" -> Icons.Default.ElectricBolt
    "hvac" -> Icons.Default.Thermostat
    "cleaning" -> Icons.Default.CleaningServices
    "appliance" -> Icons.Default.Kitchen
    "locksmith" -> Icons.Default.Lock
    else -> Icons.Default.Build
}

fun getCategoryColor(categoryId: String): Color = when (categoryId) {
    "plumbing" -> Color(0xFF0284C7)
    "electrical" -> Color(0xFFEAB308)
    "hvac" -> Color(0xFF06B6D4)
    "cleaning" -> Color(0xFF10B981)
    "appliance" -> Color(0xFF8B5CF6)
    "locksmith" -> Color(0xFFF97316)
    else -> Color(0xFF64748B)
}
