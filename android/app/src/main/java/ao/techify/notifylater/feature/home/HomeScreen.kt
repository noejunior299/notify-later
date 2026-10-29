package ao.techify.notifylater.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import ao.techify.notifylater.core.permissions.PermissionHelper
import ao.techify.notifylater.domain.model.NotificationStatus
import ao.techify.notifylater.ui.components.ActionTile
import ao.techify.notifylater.ui.components.NotifyLogo
import ao.techify.notifylater.ui.components.QuietHero
import ao.techify.notifylater.ui.components.SectionHeading

@Composable
fun HomeScreen(
    onNavigateToApplications: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogs: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val captured by app.notificationRepository.notifications.collectAsState(initial = emptyList())
    val held = captured.filter { it.status == NotificationStatus.PENDING }
    val accessGranted = NotificationAccess.isEnabled(context) &&
        PermissionHelper.hasPostNotificationsPermission(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NotifyLogo(size = 34.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        QuietHero(heldCount = held.size, accessGranted = accessGranted)

        Spacer(Modifier.height(26.dp))
        SectionHeading(stringResource(R.string.home_manage))
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionTile(
                icon = Icons.Rounded.Apps,
                title = stringResource(R.string.home_applications),
                description = stringResource(R.string.home_applications_description),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onNavigateToApplications
            )
            ActionTile(
                icon = Icons.Rounded.NotificationsPaused,
                title = stringResource(R.string.home_queue),
                description = stringResource(R.string.home_queue_description),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                accent = true,
                onClick = onNavigateToQueue
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionTile(
                icon = Icons.Rounded.History,
                title = stringResource(R.string.home_logs),
                description = stringResource(R.string.home_logs_description),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                badge = if (captured.isNotEmpty()) "${captured.size}" else null,
                onClick = { onNavigateToLogs?.invoke() ?: onNavigateToQueue() }
            )
            ActionTile(
                icon = Icons.Rounded.Settings,
                title = stringResource(R.string.home_settings),
                description = stringResource(R.string.home_settings_description),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onNavigateToSettings
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.width(14.dp).height(14.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.home_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
