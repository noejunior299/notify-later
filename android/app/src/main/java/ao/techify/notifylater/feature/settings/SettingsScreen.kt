package ao.techify.notifylater.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.core.notifications.NotificationAccess
import ao.techify.notifylater.ui.components.IconBadge
import ao.techify.notifylater.ui.components.NotifyLogo
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.SectionHeading
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.theme.OnYellow
import ao.techify.notifylater.ui.theme.YellowClock

@Composable
fun SettingsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToLogs: () -> Unit = {}
) {
    val accessGranted = NotificationAccess.isEnabled(LocalContext.current)

    NotifyScaffold(title = stringResource(R.string.settings_title), onNavigateBack = onNavigateBack) { padding ->
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
            NotificationAccessCard(accessGranted = accessGranted)

            Spacer(Modifier.height(22.dp))
            SectionHeading(stringResource(R.string.settings_group_preferences))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.VerifiedUser,
                    title = stringResource(R.string.settings_language),
                    description = stringResource(R.string.settings_language_description)
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
                    description = stringResource(R.string.settings_privacy_description)
                )
                SettingsDivider()
                AboutRow()
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
private fun NotificationAccessCard(accessGranted: Boolean) {
    val context = LocalContext.current
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Rounded.NotificationsActive,
                    containerColor = if (accessGranted) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = if (accessGranted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onTertiaryContainer,
                    size = 44.dp,
                    iconSize = 22.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.settings_notification_access_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(3.dp))
                    PillBadge(
                        text = if (accessGranted) stringResource(R.string.settings_notification_access_granted)
                        else stringResource(R.string.settings_notification_access_not_granted),
                        containerColor = if (accessGranted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (accessGranted) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.settings_notification_access_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            if (accessGranted) {
                OutlinedButton(
                    onClick = { runCatching { context.startActivity(NotificationAccess.settingsIntent()) } },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.settings_notification_access_manage))
                }
            } else {
                Button(
                    onClick = { runCatching { context.startActivity(NotificationAccess.settingsIntent()) } },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.settings_notification_access_enable))
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
private fun AboutRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
