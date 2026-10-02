package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationDeliveryStatus

@Composable
fun NotificationStatusBadge(
    status: NotificationDeliveryStatus,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label, icon) = when (status) {
        NotificationDeliveryStatus.SENT -> Quadruple(
            Color(0xFFDCFCE7), Color(0xFF15803D), "Sent", Icons.Default.Check
        )
        NotificationDeliveryStatus.PENDING, NotificationDeliveryStatus.SENDING -> Quadruple(
            Color(0xFFFEF9C3), Color(0xFFA16207), "Sending", Icons.Default.HourglassEmpty
        )
        NotificationDeliveryStatus.FAILED -> Quadruple(
            Color(0xFFFEE2E2), Color(0xFFB91C1C), "Failed", Icons.Default.Close
        )
        NotificationDeliveryStatus.RETRY -> Quadruple(
            Color(0xFFFFEDD5), Color(0xFFC2410C), "Retrying", Icons.Default.Refresh
        )
        else -> Quadruple(
            Color.LightGray, Color.DarkGray, "Disabled", null
        )
    }

    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        if (status == NotificationDeliveryStatus.FAILED && onRetry != null) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRetry,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry Notification",
                    tint = fg,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
