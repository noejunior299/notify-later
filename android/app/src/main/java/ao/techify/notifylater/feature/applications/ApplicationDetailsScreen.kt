package ao.techify.notifylater.feature.applications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.R
import ao.techify.notifylater.ui.components.NotifyScaffold
import ao.techify.notifylater.ui.components.QuietEmptyState

@Composable
fun ApplicationDetailsScreen(
    packageName: String,
    onNavigateBack: () -> Unit
) {
    NotifyScaffold(
        title = stringResource(R.string.application_details_title),
        onNavigateBack = onNavigateBack,
        contentWindowInsets = WindowInsets.navigationBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            QuietEmptyState(
                icon = Icons.Rounded.Apps,
                title = stringResource(R.string.application_details_status),
                description = stringResource(
                    R.string.application_details_package_value,
                    stringResource(R.string.application_details_package),
                    packageName.ifEmpty { stringResource(R.string.application_details_not_configured) }
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.application_details_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
        }
    }
}
