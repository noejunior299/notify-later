package ao.techify.notifylater.feature.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.R
import ao.techify.notifylater.ui.components.NotifyLogo
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.SectionHeading
import ao.techify.notifylater.ui.components.SoftCard

private const val REPO_URL = "https://github.com/noejunior299/notify-later"

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    NotifyScaffold(
        title = stringResource(R.string.about_title),
        onNavigateBack = onNavigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Staggered(0) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NotifyLogo(size = 88.dp)
                    Spacer(Modifier.height(14.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.splash_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    PillBadge(text = stringResource(R.string.settings_version, version))
                }
            }

            Staggered(1) {
                Column {
                    SectionHeading(stringResource(R.string.about_open_source))
                    Spacer(Modifier.height(8.dp))
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        LinkRow(
                            icon = Icons.Rounded.Gavel,
                            title = stringResource(R.string.settings_license),
                            body = stringResource(R.string.about_license_body),
                            onClick = { openUrl(context, "$REPO_URL/blob/main/LICENSE") }
                        )
                        LinkRow(
                            icon = Icons.Rounded.Code,
                            title = stringResource(R.string.about_source),
                            body = stringResource(R.string.about_source_body),
                            onClick = { openUrl(context, REPO_URL) }
                        )
                        LinkRow(
                            icon = Icons.Rounded.BugReport,
                            title = stringResource(R.string.about_report),
                            body = stringResource(R.string.about_report_body),
                            onClick = { openUrl(context, "$REPO_URL/issues") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Staggered(2) {
                Column {
                    SectionHeading(stringResource(R.string.settings_privacy))
                    Spacer(Modifier.height(8.dp))
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        LinkRow(
                            icon = Icons.Rounded.Shield,
                            title = stringResource(R.string.privacy_title),
                            body = stringResource(R.string.settings_privacy_description),
                            external = false,
                            onClick = onNavigateToPrivacy
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Staggered(3) {
                Column {
                    SectionHeading(stringResource(R.string.about_libraries))
                    Spacer(Modifier.height(8.dp))
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        InfoRow(
                            icon = Icons.Rounded.Widgets,
                            title = stringResource(R.string.about_libraries),
                            body = stringResource(R.string.about_libraries_body)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.about_made_with),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
fun PrivacyScreen(onNavigateBack: () -> Unit) {
    NotifyScaffold(
        title = stringResource(R.string.privacy_title),
        onNavigateBack = onNavigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Staggered(0) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.privacy_headline),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            val points = listOf(
                Triple(Icons.Rounded.PhoneAndroid, R.string.privacy_local_title, R.string.privacy_local_body),
                Triple(Icons.Rounded.WifiOff, R.string.privacy_network_title, R.string.privacy_network_body),
                Triple(Icons.Rounded.VisibilityOff, R.string.privacy_tracking_title, R.string.privacy_tracking_body),
                Triple(Icons.Rounded.Shield, R.string.privacy_access_title, R.string.privacy_access_body)
            )
            points.forEachIndexed { index, (icon, title, body) ->
                Staggered(index + 1) {
                    SoftCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        InfoRow(icon = icon, title = stringResource(title), body = stringResource(body))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Entrada escalonada: cada bloco desliza e aparece um pouco depois do anterior. */
@Composable
private fun Staggered(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(380, delayMillis = index * 70)) +
            slideInVertically(tween(420, delayMillis = index * 70)) { it / 5 }
    ) { content() }
}

@Composable
private fun ColumnScope.LinkRow(
    icon: ImageVector,
    title: String,
    body: String,
    external: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (external) {
            Icon(
                Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, body: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
        RowIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
