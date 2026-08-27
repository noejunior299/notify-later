package ao.techify.notifylater.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.core.notifications.NotificationAccess
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.theme.YellowClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogs: () -> Unit = {}
) {
    val accessGranted = rememberAccessState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_navigate_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NotificationAccessCard(accessGranted = accessGranted)

            // Logs shortcut — fofo com sino + relógio
            SoftCard(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToLogs) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.logs_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.logs_header_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LogsCountBadge()
                }
            }

            SettingsSection(
                icon = Icons.Filled.VerifiedUser,
                title = stringResource(R.string.settings_language),
                description = stringResource(R.string.settings_language_description)
            )
            SettingsSection(
                icon = Icons.Filled.Shield,
                title = stringResource(R.string.settings_privacy),
                description = stringResource(R.string.settings_privacy_description)
            )
            SettingsSection(
                title = stringResource(R.string.settings_about),
                description = stringResource(R.string.settings_about_value, stringResource(R.string.settings_about_description), stringResource(R.string.settings_license))
            )
            Text(stringResource(R.string.settings_coming_soon), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LogsCountBadge() {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val captured by app.notificationRepository.notifications.collectAsState(initial = emptyList())
    if (captured.isEmpty()) return
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(YellowClock)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("${captured.size}", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF2B1D00))
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
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (accessGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = if (accessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.settings_notification_access_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (accessGranted) stringResource(R.string.settings_notification_access_granted) else stringResource(R.string.settings_notification_access_not_granted),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (accessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.settings_notification_access_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { runCatching { context.startActivity(NotificationAccess.settingsIntent()) } },
                enabled = !accessGranted,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.settings_notification_access_enable))
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
