package ao.techify.notifylater.feature.onboarding

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ao.techify.notifylater.R
import ao.techify.notifylater.ui.components.IconBadge
import ao.techify.notifylater.ui.components.NotifyLogo
import ao.techify.notifylater.ui.components.PillBadge
import ao.techify.notifylater.ui.components.SoftCard
import ao.techify.notifylater.ui.theme.OnYellow
import ao.techify.notifylater.ui.theme.YellowClock

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))

        // O logo é o herói: o sino a dormir com o relógio amarelo.
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
            )
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    NotifyLogo(size = 84.dp)
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .background(YellowClock, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = OnYellow,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        PillBadge(
            text = stringResource(R.string.home_hero_badge),
            containerColor = YellowClock,
            contentColor = OnYellow
        )

        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.onboarding_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(26.dp))
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            FeatureRow(
                icon = Icons.Rounded.VerifiedUser,
                title = stringResource(R.string.onboarding_privacy_title),
                desc = stringResource(R.string.onboarding_privacy_description)
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            FeatureRow(
                icon = Icons.Rounded.Schedule,
                title = stringResource(R.string.onboarding_local_title),
                desc = stringResource(R.string.onboarding_local_description)
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            FeatureRow(
                icon = Icons.Rounded.CheckCircle,
                title = stringResource(R.string.onboarding_language_title),
                desc = stringResource(R.string.onboarding_language_description)
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
        ) {
            Text(stringResource(R.string.onboarding_get_started), style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onComplete) {
            Text(stringResource(R.string.onboarding_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            stringResource(
                R.string.tagline_combined,
                stringResource(R.string.tagline_line1),
                stringResource(R.string.tagline_line2)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = icon, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
