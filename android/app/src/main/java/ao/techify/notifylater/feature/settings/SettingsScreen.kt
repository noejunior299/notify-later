package ao.techify.notifylater.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.core.notifications.NotificationAccess
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * Settings screen. Phase 1 additions: notification access status and a
 * temporary debug section listing captured notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    // Recomputed on each entry/recomposition; the system settings screen is a
    // separate task, so returning here recreates this composable.
    val accessGranted = rememberAccessState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_navigate_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NotificationAccessCard(accessGranted = accessGranted)
            SettingsSection(
                title = stringResource(R.string.settings_language),
                description = stringResource(R.string.settings_language_description)
            )
            SettingsSection(
                title = stringResource(R.string.settings_privacy),
                description = stringResource(R.string.settings_privacy_description)
            )
            SettingsSection(
                title = stringResource(R.string.settings_about),
                description = stringResource(
                    R.string.settings_about_value,
                    stringResource(R.string.settings_about_description),
                    stringResource(R.string.settings_license)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CapturedNotificationsSection(modifier = Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
private fun rememberAccessState(): Boolean {
    val context = LocalContext.current
    return NotificationAccess.isEnabled(context)
}

@Composable
private fun NotificationAccessCard(accessGranted: Boolean) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_notification_access_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (accessGranted) {
                    stringResource(R.string.settings_notification_access_granted)
                } else {
                    stringResource(R.string.settings_notification_access_not_granted)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_notification_access_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    runCatching {
                        context.startActivity(NotificationAccess.settingsIntent())
                    }
                },
                enabled = !accessGranted
            ) {
                Text(text = stringResource(R.string.settings_notification_access_enable))
            }
        }
    }
}

/** Temporary Phase 1 debug section. Not part of the final product UI. */
@Composable
private fun CapturedNotificationsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val captured by app.notificationRepository.notifications.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.debug_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.debug_captured_notifications),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (captured.isEmpty()) {
                Text(
                    text = stringResource(R.string.debug_no_captured),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                captured.asReversed().forEach { notification ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = notification.sourceApplicationName
                                    ?: notification.sourcePackage,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = listOfNotNull(notification.title, notification.text)
                                    .joinToString(" — "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Text(
                                text = DateFormat.getTimeInstance(DateFormat.SHORT)
                                    .format(Date(notification.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TextButton(onClick = { scope.launch { app.notificationRepository.clear() } }) {
                    Text(text = stringResource(R.string.debug_clear))
                }
            }
            Text(
                text = stringResource(R.string.debug_spike_notice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
