package ao.techify.notifylater.feature.applications

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.R
import ao.techify.notifylater.core.settings.AppSettings
import ao.techify.notifylater.ui.components.InitialAvatar
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.SectionHeading
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.components.delayLabelOf
import kotlinx.coroutines.launch

/** Presets offered per application. `null` = follow the global delay. */
private val PER_APP_DELAY_OPTIONS_MILLIS: List<Long?> = listOf(
    null,
    60_000L,
    5 * 60_000L,
    15 * 60_000L,
    30 * 60_000L,
    60 * 60_000L,
    2 * 60 * 60_000L
)

/**
 * Configures when notifications from one app are released. The selection is
 * persisted immediately and applies to notifications captured afterwards.
 */
@Composable
fun ApplicationDetailsScreen(
    packageName: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val rules by app.appDelayRulesRepository.rules.collectAsState(initial = emptyMap())
    val settings by app.settingsRepository.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()

    val appInfo = remember(packageName) { resolveAppInfo(context, packageName) }
    val currentRule = rules[packageName]

    NotifyScaffold(
        title = appInfo?.label ?: stringResource(R.string.application_details_title),
        onNavigateBack = onNavigateBack,
        contentWindowInsets = WindowInsets.navigationBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(appInfo = appInfo, label = appInfo?.label ?: packageName)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appInfo?.label ?: packageName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        PillBadge(
                            text = if (currentRule != null) {
                                stringResource(
                                    R.string.application_details_current_rule,
                                    delayLabelOf(currentRule)
                                )
                            } else {
                                stringResource(
                                    R.string.application_details_current_rule,
                                    stringResource(R.string.applications_global_default)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeading(stringResource(R.string.application_details_delay))
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.application_details_delay_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    PER_APP_DELAY_OPTIONS_MILLIS.forEachIndexed { index, option ->
                        if (index > 0) {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        DelayOptionRow(
                            label = option?.let { delayLabelOf(it) }
                                ?: stringResource(
                                    R.string.application_details_use_global,
                                    delayLabelOf(settings.deliveryDelayMillis)
                                ),
                            selected = option == currentRule,
                            onClick = {
                                scope.launch {
                                    app.appDelayRulesRepository.setDelay(packageName, option)
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DelayOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AppIcon(appInfo: AppInfo?, label: String) {
    val iconBitmap = remember(appInfo) {
        runCatching { appInfo?.icon?.toBitmap(128, 128)?.asImageBitmap() }.getOrNull()
    }
    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        InitialAvatar(name = label)
    }
}

internal data class AppInfo(
    val label: String,
    val icon: Drawable?
)

private fun resolveAppInfo(context: Context, packageName: String): AppInfo? = runCatching {
    val packageManager = context.packageManager
    AppInfo(
        label = packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, 0)
        )?.toString() ?: packageName,
        icon = packageManager.getApplicationIcon(packageName)
    )
}.getOrNull()
