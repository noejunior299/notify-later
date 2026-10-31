package ao.techify.notifylater.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ao.techify.notifylater.R
import ao.techify.notifylater.ui.components.NotifyLogo
import ao.techify.notifylater.ui.theme.PurpleDeep
import ao.techify.notifylater.ui.theme.PurplePrimary
import ao.techify.notifylater.ui.theme.YellowClock
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

/**
 * Splash animado: o logo entra com mola, ondas calmas expandem-se à volta,
 * um arco amarelo (o "relógio") completa uma volta e o nome aparece.
 * Chama [onFinished] quando a sequência termina (~1,6 s).
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val sweep = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        val a = async { logoAlpha.animateTo(1f, tween(320)) }
        val s = async {
            logoScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
        }
        val c = async {
            delay(180)
            sweep.animateTo(360f, tween(900, easing = FastOutSlowInEasing))
        }
        delay(380)
        val t = async { textAlpha.animateTo(1f, tween(420)) }
        textOffset.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
        a.await(); s.await(); c.await(); t.await()
        delay(260)
        onFinished()
    }

    // Ícones claros na barra de estado sobre o roxo; repõe ao sair.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            previousStatus?.let { controller.isAppearanceLightStatusBars = it }
            previousNav?.let { controller.isAppearanceLightNavigationBars = it }
        }
    }

    val ripple = rememberInfiniteTransition(label = "ripple")
    val wave by ripple.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(PurplePrimary, PurpleDeep),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val base = size.minDimension / 4.6f
                    // Duas ondas desfasadas, a expandir e a desvanecer.
                    listOf(wave, (wave + 0.5f) % 1f).forEach { p ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.18f * (1f - p) * logoAlpha.value),
                            radius = base + p * base * 1.25f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    // Arco do relógio.
                    val r = base + 14.dp.toPx()
                    drawArc(
                        color = YellowClock,
                        startAngle = -90f,
                        sweepAngle = sweep.value,
                        useCenter = false,
                        topLeft = center.copy(x = center.x - r, y = center.y - r),
                        size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                NotifyLogo(
                    size = 92.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .graphicsLayer { translationY = textOffset.value.dp.toPx() }
            ) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
