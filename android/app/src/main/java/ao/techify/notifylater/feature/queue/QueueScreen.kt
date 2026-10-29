package ao.techify.notifylater.feature.queue

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.domain.model.Notification
import ao.techify.notifylater.domain.model.NotificationStatus
import ao.techify.notifylater.ui.components.InitialAvatar
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.QuietEmptyState
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.components.TimeBadge
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@Composable
fun QueueScreen(onNavigateBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val captured by app.notificationRepository.notifications.collectAsState(initial = emptyList())
    val held = captured
        .filter { it.status == NotificationStatus.PENDING }
        .sortedBy { it.dueAt ?: Long.MAX_VALUE }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(held.size) {
        while (held.isNotEmpty()) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    NotifyScaffold(
        title = stringResource(R.string.queue_title),
        onNavigateBack = onNavigateBack,
        actions = {
            if (held.isNotEmpty()) {
                PillBadge(text = "${held.size}")
            }
        }
    ) { padding ->
        if (held.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))
                QuietEmptyState(
                    icon = Icons.Rounded.NotificationsPaused,
                    title = stringResource(R.string.queue_empty_title),
                    description = stringResource(R.string.queue_empty_description)
                )
                Spacer(Modifier.weight(1f))
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.queue_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.queue_active_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(held, key = { it.id }) { notification ->
                    HeldNotificationCard(notification = notification, now = now)
                }
            }
        }
    }
}

@Composable
private fun HeldNotificationCard(notification: Notification, now: Long) {
    val appName = notification.sourceApplicationName ?: notification.sourcePackage
    val dueAt = notification.dueAt
    val remaining = ((dueAt ?: now) - now).coerceAtLeast(0L)
    val countdown = if (dueAt == null || remaining <= 0L) {
        stringResource(R.string.queue_delivering_now)
    } else {
        DateUtils.getRelativeTimeSpanString(
            dueAt,
            now,
            DateUtils.SECOND_IN_MILLIS
        ).toString()
    }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                InitialAvatar(name = appName)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        appName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!notification.title.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            notification.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!notification.text.isNullOrBlank()) {
                        Text(
                            notification.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (dueAt != null) {
                    Spacer(Modifier.width(8.dp))
                    TimeBadge(
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(dueAt))
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillBadge(text = stringResource(R.string.queue_pending))
                Spacer(Modifier.weight(1f))
                Text(
                    countdown,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
