package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

fun formatCurrency(amount: Double): String {
    return "₹${String.format("%,.0f", amount)}"
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("stat_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "ACTIVE", "VERIFIED", "SUCCESSFUL", "PAID", "ACCEPTED", "SETTLED" ->
            Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
        "OVERDUE", "REJECTED", "SUSPENDED", "FAILED" ->
            Pair(OverdueRed.copy(alpha = 0.15f), OverdueRed)
        "PENDING", "UNDER_REVIEW", "PENDING_DISBURSEMENT", "OFFER_SENT" ->
            Pair(WarningAmber.copy(alpha = 0.15f), WarningAmber)
        else ->
            Pair(InfoCyan.copy(alpha = 0.15f), InfoCyan)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VerificationBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    if (status == VerificationStatus.VERIFIED) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(SuccessGreen.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Financier",
                tint = SuccessGreen,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Verified",
                color = SuccessGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        StatusBadge(status = status.name, modifier = modifier)
    }
}

@Composable
fun SimulatedQrCodeView(
    payeeName: String,
    amount: Double?,
    loanId: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SCAN & PAY VIA UPI",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            // QR Code pattern simulation
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(7) { col ->
                                val isBlock = (row % 2 == 0 && col % 2 == 0) || (row < 2 && col < 2) || (row > 4 && col > 4) || (row < 2 && col > 4) || (row > 4 && col < 2) || (row == 3 && col == 3)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(if (isBlock) Color(0xFF0F172A) else Color.White, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
                // Center LoanConnect emblem
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00D09C),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("LC", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = payeeName,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 15.sp
            )
            if (loanId != null) {
                Text(
                    text = "Ref: $loanId",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            if (amount != null && amount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(amount),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF008764),
                    fontSize = 18.sp
                )
            }
        }
    }
}
