package ao.techify.notifylater.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.core.notifications.NotificationAccess
import ao.techify.notifylater.core.permissions.PermissionHelper
import ao.techify.notifylater.core.settings.AppSettings
import ao.techify.notifylater.ui.components.IconBadge
import ao.techify.notifylater.ui.components.NotifyLogo
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.SectionHeading
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.theme.OnYellow
import ao.techify.notifylater.ui.theme.YellowClock
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToLogs: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val settings by app.settingsRepository.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()

    var accessGranted by remember { mutableStateOf(NotificationAccess.isEnabled(context)) }
    var postGranted by remember {
        mutableStateOf(PermissionHelper.hasPostNotificationsPermission(context))
    }
    var showDelayDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessGranted = NotificationAccess.isEnabled(context)
                postGranted = PermissionHelper.hasPostNotificationsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val postPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        postGranted = PermissionHelper.hasPostNotificationsPermission(context)
    }

    NotifyScaffold(
        title = stringResource(R.string.settings_title),
        onNavigateBack = onNavigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SectionHeading(stringResource(R.string.settings_group_permissions))
            Spacer(Modifier.height(8.dp))
            PermissionCard(
                icon = Icons.Rounded.NotificationsActive,
                title = stringResource(R.string.settings_notification_access_title),
                description = stringResource(R.string.settings_notification_access_description),
                granted = accessGranted,
                actionLabel = if (accessGranted) {
                    stringResource(R.string.settings_notification_access_manage)
                } else {
                    stringResource(R.string.settings_notification_access_enable)
                },
                onAction = {
                    runCatching { context.startActivity(NotificationAccess.settingsIntent()) }
                }
            )
            if (PermissionHelper.isPostNotificationsRequired() && !postGranted) {
                Spacer(Modifier.height(10.dp))
                PermissionCard(
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(R.string.settings_post_notifications_title),
                    description = stringResource(R.string.settings_post_notifications_description),
                    granted = false,
                    actionLabel = stringResource(R.string.settings_post_notifications_enable),
                    onAction = {
                        postPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }

            Spacer(Modifier.height(22.dp))
            SectionHeading(stringResource(R.string.settings_group_preferences))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Enable Notify Later",
                    description = "When disabled, notifications are delivered immediately.",
                    trailing = {
                        Switch(
                            checked = settings.enabled,
                            onCheckedChange = { value ->
                                scope.launch { app.settingsRepository.setEnabled(value) }
                            }
                        )
                    }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Schedule,
                    title = stringResource(R.string.settings_delivery_delay),
                    description = stringResource(R.string.settings_delivery_delay_description),
                    trailing = {
                        Text(
                            delayLabel(settings.deliveryDelayMillis),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { showDelayDialog = true }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.VerifiedUser,
                    title = stringResource(R.string.settings_language),
                    description = stringResource(R.string.settings_language_description),
                    onClick = { showLanguageDialog = true }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.logs_title),
                    description = stringResource(R.string.logs_header_subtitle),
                    trailing = { LogsCountBadge() },
                    onClick = onNavigateToLogs
                )
            }

            Spacer(Modifier.height(22.dp))
            SectionHeading(stringResource(R.string.settings_group_about))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Shield,
                    title = stringResource(R.string.settings_privacy),
                    description = stringResource(R.string.settings_privacy_description),
                    onClick = onNavigateToPrivacy
                )
                SettingsDivider()
                AboutRow(onClick = onNavigateToAbout)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.settings_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    listOf("en" to "English", "pt" to "Português").forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        app.settingsRepository.setLanguage(code)
                                        showLanguageDialog = false
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.language == code,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.common_done))
                }
            }
        )
    }

    if (showDelayDialog) {
        DelayDialog(
            selectedDelayMillis = settings.deliveryDelayMillis,
            onSelect = { delayMillis ->
                scope.launch { app.settingsRepository.setDeliveryDelayMillis(delayMillis) }
                showDelayDialog = false
            },
            onDismiss = { showDelayDialog = false }
        )
    }
}

@Composable
private fun DelayDialog(
    selectedDelayMillis: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_delivery_delay_dialog_title)) },
        text = {
            Column {
                AppSettings.DELAY_OPTIONS_MILLIS.forEach { delayMillis ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(delayMillis) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDelayMillis == delayMillis,
                            onClick = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            delayLabel(delayMillis),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_done))
            }
        }
    )
}

@Composable
private fun delayLabel(delayMillis: Long): String = when (delayMillis) {
    0L -> stringResource(R.string.delay_off)
    30_000L -> stringResource(R.string.delay_30_seconds)
    60_000L -> stringResource(R.string.delay_1_minute)
    5 * 60_000L -> stringResource(R.string.delay_5_minutes)
    15 * 60_000L -> stringResource(R.string.delay_15_minutes)
    60 * 60_000L -> stringResource(R.string.delay_1_hour)
    else -> stringResource(R.string.delay_seconds, (delayMillis / 1_000L).toInt())
}

@Composable
private fun LogsCountBadge() {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val captured by app.notificationRepository.notifications.collectAsState(initial = emptyList())
    if (captured.isEmpty()) return
    PillBadge(
        text = "${captured.size}",
        containerColor = YellowClock,
        contentColor = OnYellow
    )
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = icon,
                    containerColor = if (granted) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = if (granted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onTertiaryContainer,
                    size = 44.dp,
                    iconSize = 22.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(3.dp))
                    PillBadge(
                        text = if (granted) {
                            stringResource(R.string.settings_notification_access_granted)
                        } else {
                            stringResource(R.string.settings_notification_access_not_granted)
                        },
                        containerColor = if (granted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (granted) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            if (granted) {
                OutlinedButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(actionLabel)
                }
            } else {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }
    Row(
        modifier = rowModifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = icon,
            size = 40.dp,
            iconSize = 20.dp,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AboutRow(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotifyLogo(size = 44.dp)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(R.string.settings_about_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
