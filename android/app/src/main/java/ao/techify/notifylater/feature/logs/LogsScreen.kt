package ao.techify.notifylater.feature.logs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.domain.model.NotificationStatus
import ao.techify.notifylater.ui.components.BellWithClockBadge
import ao.techify.notifylater.ui.components.InitialAvatar
import ao.techify.notifylater.ui.components.AppIcon
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.QuietEmptyState
import ao.techify.notifylater.ui.components.SoftCard
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import ao.techify.notifylater.ui.components.TimeBadge
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun LogsScreen(onNavigateBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val captured by app.notificationRepository.notifications.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    NotifyScaffold(title = stringResource(R.string.logs_title), onNavigateBack = onNavigateBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BellWithClockBadge(bellIcon = Icons.Rounded.Notifications)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.logs_header), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.logs_header_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (captured.isNotEmpty()) {
                    TextButton(onClick = { scope.launch { app.notificationRepository.clearCompleted() } }) {
                        Text(stringResource(R.string.logs_clear))
                    }
                }
            }

            if (captured.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    QuietEmptyState(
                        icon = Icons.Rounded.NotificationsPaused,
                        title = stringResource(R.string.logs_empty_title),
                        description = stringResource(R.string.logs_empty_description)
                    )
                    Spacer(Modifier.height(24.dp))
                }
            } else {
                Text(
                    "${captured.size} ${stringResource(R.string.logs_count)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(
                    stringResource(R.string.logs_spike_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(captured.reversed()) { n ->
                        val appName = n.sourceApplicationName?.takeIf { it.isNotBlank() } ?: n.sourcePackage
                        SoftCard(modifier = Modifier.fillMaxWidth().animateItem()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(packageName = n.sourcePackage)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appName,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        TimeBadge(
                                            DateFormat.getTimeInstance(DateFormat.SHORT)
                                                .format(Date(n.timestamp))
                                        )
                                    }
                                    if (n.title != null) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            n.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (n.text != null) {
                                        Text(
                                            n.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        n.sourcePackage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val statusColors = statusColors(n.status)
                                        PillBadge(
                                            text = statusLabel(n.status),
                                            containerColor = statusColors.first,
                                            contentColor = statusColors.second
                                        )
                                        val statusTime = when (n.status) {
                                            NotificationStatus.DELIVERED -> n.deliveredAt
                                            NotificationStatus.PENDING -> n.dueAt
                                            else -> null
                                        }
                                        if (statusTime != null) {
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = (if (n.status == NotificationStatus.DELIVERED) {
                                                    stringResource(R.string.logs_delivered_at)
                                                } else {
                                                    stringResource(R.string.logs_due_at)
                                                }) + " " + DateFormat.getTimeInstance(DateFormat.SHORT)
                                                    .format(Date(statusTime)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: NotificationStatus): String = when (status) {
    NotificationStatus.PENDING -> stringResource(R.string.queue_pending)
    NotificationStatus.DELIVERED -> stringResource(R.string.queue_delivered)
    NotificationStatus.DISMISSED -> stringResource(R.string.queue_dismissed)
    NotificationStatus.EXPIRED -> stringResource(R.string.logs_expired)
}

@Composable
private fun statusColors(status: NotificationStatus): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        NotificationStatus.PENDING -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        NotificationStatus.DELIVERED -> scheme.primaryContainer to scheme.onPrimaryContainer
        NotificationStatus.DISMISSED -> scheme.surfaceVariant to scheme.onSurfaceVariant
        NotificationStatus.EXPIRED -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
}
