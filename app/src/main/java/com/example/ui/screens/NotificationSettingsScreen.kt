package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.NotificationStatusBadge
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    adminViewModel: AdminViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by adminViewModel.notificationSettings.collectAsState()
    val waConfig by adminViewModel.whatsAppConfig.collectAsState()
    val pushConfig by adminViewModel.pushConfig.collectAsState()
    val logs by adminViewModel.notificationLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Channels, 1: WhatsApp Config, 2: Push Config, 3: Delivery Logs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Center", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Master") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("WhatsApp") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Push FCM") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Logs (${logs.size})") })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> MasterChannelSettingsView(
                        settings = settings ?: NotificationSettingEntity(),
                        onSaveChannel = { ch ->
                            adminViewModel.updateNotificationChannel(ch, currentUser.name)
                            Toast.makeText(context, "Notification channel set to $ch", Toast.LENGTH_SHORT).show()
                        }
                    )

                    1 -> WhatsAppAdminPanelView(
                        config = waConfig ?: WhatsAppConfigEntity(),
                        onSave = { cfg ->
                            adminViewModel.saveWhatsAppConfig(cfg, currentUser.name)
                            Toast.makeText(context, "WhatsApp configuration saved securely", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> PushAdminPanelView(
                        config = pushConfig ?: PushConfigEntity(),
                        onSave = { cfg ->
                            adminViewModel.savePushConfig(cfg, currentUser.name)
                            Toast.makeText(context, "Push configuration saved", Toast.LENGTH_SHORT).show()
                        }
                    )

                    3 -> DeliveryLogsView(
                        logs = logs,
                        onRetry = { log ->
                            adminViewModel.retryFailedNotification(log, currentUser.name)
                            Toast.makeText(context, "Retrying notification for ${log.studentName}...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MasterChannelSettingsView(
    settings: NotificationSettingEntity,
    onSaveChannel: (NotificationChannelType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Master Notification Channel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Choose which notification dispatch method is activated when students check in:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotificationChannelType.values().forEach { ch ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.channel == ch,
                        onClick = { onSaveChannel(ch) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(ch.name, fontWeight = FontWeight.Bold)
                        Text(
                            when (ch) {
                                NotificationChannelType.PUSH -> "Send real-time mobile push notifications to parent app"
                                NotificationChannelType.WHATSAPP -> "Send real-time WhatsApp messages to parent phone number"
                                NotificationChannelType.BOTH -> "Dispatch both Push and WhatsApp notifications simultaneously"
                                NotificationChannelType.DISABLED -> "Disable all automated parent notifications"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsAppAdminPanelView(
    config: WhatsAppConfigEntity,
    onSave: (WhatsAppConfigEntity) -> Unit
) {
    val context = LocalContext.current
    var provider by remember { mutableStateOf(config.provider) }
    var endpoint by remember { mutableStateOf(config.apiEndpoint) }
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var phoneId by remember { mutableStateOf(config.phoneNumberId) }
    var sender by remember { mutableStateOf(config.senderNumber) }
    var template by remember { mutableStateOf(config.messageTemplate) }
    var enabled by remember { mutableStateOf(config.enabled) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WhatsApp Gateway Config", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = provider, onValueChange = { provider = it }, label = { Text("Provider (e.g. WhatsApp Cloud API)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text("API Endpoint URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key / Bearer Token") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phoneId, onValueChange = { phoneId = it }, label = { Text("Phone Number ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sender, onValueChange = { sender = it }, label = { Text("Sender Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = template, onValueChange = { template = it }, label = { Text("Message Template") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onSave(
                                    config.copy(
                                        provider = provider,
                                        apiEndpoint = endpoint,
                                        apiKey = apiKey,
                                        phoneNumberId = phoneId,
                                        senderNumber = sender,
                                        messageTemplate = template,
                                        enabled = enabled
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Config")
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Testing WhatsApp Connection to $endpoint...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Gateway")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PushAdminPanelView(
    config: PushConfigEntity,
    onSave: (PushConfigEntity) -> Unit
) {
    val context = LocalContext.current
    var serverKey by remember { mutableStateOf(config.serverKey) }
    var projectId by remember { mutableStateOf(config.projectId) }
    var template by remember { mutableStateOf(config.messageTemplate) }
    var enabled by remember { mutableStateOf(config.enabled) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Firebase Cloud Messaging (FCM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = projectId, onValueChange = { projectId = it }, label = { Text("FCM Project ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = serverKey, onValueChange = { serverKey = it }, label = { Text("FCM Server Key") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = template, onValueChange = { template = it }, label = { Text("Notification Template") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSave(config.copy(projectId = projectId, serverKey = serverKey, messageTemplate = template, enabled = enabled))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Push Configuration")
                }
            }
        }
    }
}

@Composable
private fun DeliveryLogsView(
    logs: List<NotificationLogEntity>,
    onRetry: (NotificationLogEntity) -> Unit
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No notification delivery logs recorded.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(log.studentName, fontWeight = FontWeight.Bold)
                            NotificationStatusBadge(status = log.status, onRetry = { onRetry(log) })
                        }
                        Text("Channel: ${log.channel} • Recipient: ${log.parentPhone}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (log.errorDetails.isNotEmpty()) {
                            Text("Error: ${log.errorDetails}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
