package com.insulet.code_challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(viewModel: NoticeViewModel = remember { NoticeViewModel() }) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insulet NoticeCoordinator Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Coordinator State",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(
                            label = "Visible",
                            isActive = state.isNoticeVisible,
                            activeColor = Color(0xFF2E7D32)
                        )
                        StatusChip(
                            label = "Attached",
                            isActive = state.isAttached,
                            activeColor = Color(0xFF1565C0)
                        )
                        StatusChip(
                            label = "Pending",
                            isActive = state.isPending,
                            activeColor = Color(0xFFEF6C00)
                        )
                        StatusChip(
                            label = "Dismissed",
                            isActive = state.isDismissed,
                            activeColor = Color(0xFFC62828)
                        )
                    }
                }
            }

            // Actions Section
            Text(
                text = "Interactive Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(shouldShow = true, blocking = false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Show Notice")
                    }
                    Button(
                        onClick = { viewModel.onEvent(shouldShow = false, blocking = false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Dismiss Condition")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(shouldShow = true, blocking = true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Blocking Flow")
                    }
                    Button(
                        onClick = { viewModel.toggleAttach() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(if (state.isAttached) "Detach Host" else "Attach Host")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.onUserConfirmed() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
                    Button(
                        onClick = { viewModel.onUserCancelled() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.simulateAppRestart() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔄 Simulate App Restart")
                }
            }

            // Event Logs Audit Trail
            Text(
                text = "Audit Trail & Logs",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.logs) { log ->
                        Text(
                            text = "• $log",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Notice Dialog when visible
        if (state.isNoticeVisible && state.isAttached) {
            AlertDialog(
                onDismissRequest = { viewModel.onUserCancelled() },
                title = { Text(stringResource(R.string.dialog_title)) },
                text = { Text(stringResource(R.string.dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onUserConfirmed() }) {
                        Text(stringResource(R.string.dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onUserCancelled() }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun StatusChip(label: String, isActive: Boolean, activeColor: Color) {
    val backgroundColor = if (isActive) activeColor else Color.Gray.copy(alpha = 0.3f)
    val contentColor = if (isActive) Color.White else Color.DarkGray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label: ${if (isActive) "YES" else "NO"}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
