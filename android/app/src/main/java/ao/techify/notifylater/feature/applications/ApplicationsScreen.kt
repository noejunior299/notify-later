package ao.techify.notifylater.feature.applications

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ao.techify.notifylater.ui.components.QuietEmptyState
import ao.techify.notifylater.ui.components.SectionHeading
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.components.delayLabelOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

/**
 * Lists the launchable apps on the device. Each row shows the delay that
 * currently applies to that app: its own rule, or the global default.
 */
@Composable
fun ApplicationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLaterApplicationHolder
    val rules by app.appDelayRulesRepository.rules.collectAsState(initial = emptyMap())
    val settings by app.settingsRepository.settings.collectAsState(initial = AppSettings())

    var installed by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        installed = withContext(Dispatchers.IO) { loadInstalledApps(context) }
        loading = false
    }

    val visibleApps = remember(installed, query, rules) {
        installed
            .filter { appInfo ->
                query.isBlank() ||
                    appInfo.label.contains(query, ignoreCase = true) ||
                    appInfo.packageName.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<InstalledApp> { rules.containsKey(it.packageName) }
                    .thenBy { it.label.lowercase() }
            )
    }

    // Rules for packages no longer listed (uninstalled or non-launcher apps)
    // must not inflate the counter shown above the list.
    val configuredCount = remember(installed, rules) {
        installed.count { rules.containsKey(it.packageName) }
    }

    NotifyScaffold(
        title = stringResource(R.string.applications_title),
        onNavigateBack = onNavigateBack,
        contentWindowInsets = WindowInsets.navigationBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.applications_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.applications_search_hint)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Text(
                        text = stringResource(R.string.applications_loading),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                visibleApps.isEmpty() && query.isBlank() -> {
                    QuietEmptyState(
                        icon = Icons.Rounded.Search,
                        title = stringResource(R.string.applications_empty),
                        description = stringResource(R.string.applications_empty_description)
                    )
                }
                visibleApps.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.common_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = if (configuredCount == 0) {
                            stringResource(R.string.applications_count_zero)
                        } else {
                            stringResource(
                                if (configuredCount == 1) {
                                    R.string.applications_count_one
                                } else {
                                    R.string.applications_count
                                },
                                configuredCount
                            )
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(visibleApps, key = { it.packageName }) { appInfo ->
                            ApplicationRow(
                                appInfo = appInfo,
                                delayMillis = rules[appInfo.packageName]
                                    ?: settings.deliveryDelayMillis,
                                isCustom = rules.containsKey(appInfo.packageName),
                                onClick = { onNavigateToDetails(appInfo.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationRow(
    appInfo: InstalledApp,
    delayMillis: Long,
    isCustom: Boolean,
    onClick: () -> Unit
) {
    val iconBitmap = remember(appInfo.icon) {
        runCatching { appInfo.icon?.toBitmap(96, 96)?.asImageBitmap() }.getOrNull()
    }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                InitialAvatar(name = appInfo.label)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo.label,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            PillBadge(
                text = if (isCustom) delayLabelOf(delayMillis)
                else stringResource(R.string.applications_global_default),
                containerColor = if (isCustom) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (isCustom) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
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

private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    return runCatching { packageManager.queryIntentActivities(intent, 0) }
        .getOrDefault(emptyList())
        .mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            // Never list ourselves: the listener ignores self notifications anyway.
            if (packageName == context.packageName) return@mapNotNull null
            val label = resolveInfo.loadLabel(packageManager)?.toString()
                ?.ifBlank { packageName } ?: packageName
            InstalledApp(
                packageName = packageName,
                label = label,
                icon = runCatching { resolveInfo.loadIcon(packageManager) }.getOrNull()
            )
        }
        .distinctBy { it.packageName }
}
