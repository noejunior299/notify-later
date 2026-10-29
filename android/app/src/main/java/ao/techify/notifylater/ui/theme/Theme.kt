package ao.techify.notifylater.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleDark,
    secondary = PurpleMedium,
    onSecondary = Color.White,
    secondaryContainer = LightLilas,
    onSecondaryContainer = PurpleDark,
    tertiary = YellowClock,
    onTertiary = OnYellow,
    tertiaryContainer = Color(0xFFFFF0B8),
    onTertiaryContainer = Color(0xFF241A00),
    background = WhiteLilas,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = PurplePrimary,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE4E1F3),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFAFF),
    surfaceContainer = Color(0xFFF4F2FD),
    surfaceContainerHigh = Color(0xFFEEECFA),
    surfaceContainerHighest = Color(0xFFE7E4F6),
    outline = OutlineLight,
    outlineVariant = Color(0xFFE8E3FF),
    inverseSurface = Color(0xFF302E42),
    inverseOnSurface = Color(0xFFF3F0FF),
    inversePrimary = Color(0xFFB9A6FF),
    error = Color(0xFFBA1A1A),
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9A6FF),
    onPrimary = Color(0xFF2E1A8A),
    primaryContainer = PurpleContainerDark,
    onPrimaryContainer = LightLilas,
    secondary = LightLilas,
    onSecondary = Color(0xFF2E227A),
    secondaryContainer = Color(0xFF3A2E8A),
    onSecondaryContainer = LightLilas,
    tertiary = YellowClock,
    onTertiary = Color(0xFF3A2B00),
    tertiaryContainer = Color(0xFF5A4100),
    onTertiaryContainer = Color(0xFFFFEFC7),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = Color(0xFFB9A6FF),
    surfaceBright = Color(0xFF3B3852),
    surfaceDim = Color(0xFF14131F),
    surfaceContainerLowest = Color(0xFF0F0E17),
    surfaceContainerLow = Color(0xFF1A1926),
    surfaceContainer = Color(0xFF1E1D2A),
    surfaceContainerHigh = Color(0xFF292738),
    surfaceContainerHighest = Color(0xFF343145),
    outline = OutlineDark,
    outlineVariant = Color(0xFF3A3770),
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = Color(0xFF1B1B23),
    inversePrimary = PurplePrimary
)

// Notify Later shapes — cantos bem arredondados, fofo mas profissional
private val NotifyLaterShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NotifyLaterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desativado por padrão para manter identidade roxa/amarela consistente
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NotifyLaterShapes,
        content = content
    )
}
