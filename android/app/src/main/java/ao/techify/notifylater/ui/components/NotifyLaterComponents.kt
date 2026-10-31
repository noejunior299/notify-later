package ao.techify.notifylater.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.R
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import ao.techify.notifylater.ui.theme.OnYellow
import ao.techify.notifylater.ui.theme.YellowClock

/**
 * Design System — Notify Later
 *
 * O mundo visual vem do logo: um sino a dormir com um relógio amarelo.
 * A interface trata as notificações como algo a descansar — superfícies
 * calmas, roxo profundo, e o relógio como único acento de tempo.
 */

// ---------------------------------------------------------------- primitives

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 1.dp)
        ) { content() }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) { content() }
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun BellWithClockBadge(
    bellIcon: ImageVector,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 18.dp
) {
    Box(modifier = modifier.size(52.dp), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(bellIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
        }
        // relógio amarelo — detalhe fofo recorrente
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(badgeSize)
                .clip(CircleShape)
                .background(YellowClock),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = OnYellow,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun PillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(color = containerColor, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun TimeBadge(text: String, modifier: Modifier = Modifier) {
    Surface(color = YellowClock, shape = RoundedCornerShape(50), modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = OnYellow, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = OnYellow)
        }
    }
}

@Composable
fun NotifyLogo(modifier: Modifier = Modifier, size: Dp = 36.dp) {
    Image(
        painter = painterResource(R.drawable.notify_later),
        contentDescription = stringResource(R.string.content_desc_app_icon),
        modifier = modifier.size(size)
    )
}

// ------------------------------------------------------------------- hero

/**
 * Hero da Home: o sino a dormir, o relógio a marcar o próximo momento,
 * e o estado atual do serviço em duas pílulas discretas.
 */
@Composable
fun QuietHero(
    heldCount: Int,
    accessGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val arc = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        arc.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
    }
    val onHero = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Relógio desenhado: um quarto de arco junto ao canto, como a borda de um mostrador.
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width, 0f)
            val radius = size.height * 0.62f
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(center.x - radius - 10.dp.toPx(), center.y - radius - 10.dp.toPx()),
                size = Size((radius + 10.dp.toPx()) * 2, (radius + 10.dp.toPx()) * 2),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawArc(
                color = YellowClock.copy(alpha = 0.8f),
                startAngle = 90f,
                sweepAngle = 90f * arc.value,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        NotifyLogo(size = 56.dp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_status_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = onHero
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.home_hero_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = onHero.copy(alpha = 0.78f)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroPill(
                    text = if (accessGranted) stringResource(R.string.home_watching_quietly)
                    else stringResource(R.string.home_access_needed),
                    dotColor = if (accessGranted) YellowClock else onHero.copy(alpha = 0.55f)
                )
                if (heldCount > 0) {
                    HeroPill(text = stringResource(R.string.home_held_count, heldCount))
                }
            }
        }
    }
}

@Composable
private fun HeroPill(text: String, dotColor: Color? = null) {
    val foreground = MaterialTheme.colorScheme.onPrimary
    Surface(color = foreground.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(text, style = MaterialTheme.typography.labelSmall, color = foreground)
        }
    }
}

// --------------------------------------------------------------- navigation

@Composable
fun SectionHeading(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(horizontal = 4.dp)
    )
}

/**
 * Tile de ação da Home. A grelha dá destaque ao que está a acontecer
 * (fila / logs) sem transformar a Home numa lista de cartões iguais.
 */
@Composable
fun ActionTile(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    val iconContainer = if (accent) YellowClock else MaterialTheme.colorScheme.primaryContainer
    val iconTint = if (accent) OnYellow else MaterialTheme.colorScheme.primary

    SoftCard(modifier = modifier.heightIn(min = 142.dp), onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconBadge(
                    icon = icon,
                    containerColor = iconContainer,
                    contentColor = iconTint,
                    size = 40.dp,
                    iconSize = 20.dp
                )
                if (badge != null) {
                    PillBadge(
                        text = badge,
                        containerColor = YellowClock,
                        contentColor = OnYellow
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------------------------------------ states

@Composable
fun QuietEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(124.dp)) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 14.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(YellowClock),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = OnYellow, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        if (badge != null) {
            Spacer(Modifier.height(14.dp))
            PillBadge(text = badge)
        }
    }
}

/**
 * Rótulo legível para um atraso em millis (presets da app + fallback).
 * Partilhado por Settings, Applications e Application Details.
 */
@Composable
fun delayLabelOf(delayMillis: Long): String = when (delayMillis) {
    0L -> stringResource(R.string.delay_off)
    30_000L -> stringResource(R.string.delay_30_seconds)
    60_000L -> stringResource(R.string.delay_1_minute)
    5 * 60_000L -> stringResource(R.string.delay_5_minutes)
    15 * 60_000L -> stringResource(R.string.delay_15_minutes)
    30 * 60_000L -> stringResource(R.string.delay_30_minutes)
    60 * 60_000L -> stringResource(R.string.delay_1_hour)
    2 * 60 * 60_000L -> stringResource(R.string.delay_2_hours)
    else -> stringResource(R.string.delay_seconds, (delayMillis / 1_000L).toInt())
}

/**
 * Avatar com a inicial da aplicação. As cores vêm da paleta da marca,
 * nunca de um hash arbitrário com dezenas de tons.
 */
@Composable
fun InitialAvatar(name: String, modifier: Modifier = Modifier) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary to Color.White,
        MaterialTheme.colorScheme.secondary to Color.White,
        YellowClock to OnYellow,
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    )
    val index = ((name.hashCode() % palette.size) + palette.size) % palette.size
    val (bg, fg) = palette[index]
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = fg
        )
    }
}

// --------------------------------------------------------------- structure

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifyScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = contentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_desc_navigate_back)
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = content
    )
}

/**
 * Ícone real da aplicação de origem; cai para a inicial quando o pacote
 * não é visível (desinstalado, oculto).
 */
@Composable
fun AppIcon(packageName: String, label: String = packageName, size: Dp = 44.dp) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bitmap = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.3f))
        )
    } else {
        InitialAvatar(name = label, modifier = Modifier.size(size))
    }
}
