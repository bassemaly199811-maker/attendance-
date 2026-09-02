package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Adds a responsive, high-end bouncy spring scale effect on touch press with ripple feedback.
 */
@Composable
fun Modifier.bounceClick(
  scaleDown: Float = 0.95f,
  enabled: Boolean = true,
  onClick: () -> Unit,
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) scaleDown else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "bounceClickScale",
  )

  return this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = interactionSource,
      indication = androidx.compose.foundation.LocalIndication.current,
      enabled = enabled,
      onClick = onClick,
    )
}

/**
 * Modifier to apply bouncy press scale when an existing interaction source is used (e.g. inside Buttons).
 */
@Composable
fun Modifier.bounceOnPress(
  interactionSource: MutableInteractionSource,
  scaleDown: Float = 0.96f,
  enabled: Boolean = true,
): Modifier {
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) scaleDown else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "bounceOnPressScale",
  )
  return this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}

/**
 * Shimmer effect modifier for active elements or loading states.
 */
@Composable
fun Modifier.shimmerEffect(
  highlightColor: Color = Color.White.copy(alpha = 0.25f),
  baseColor: Color = Color.Transparent,
  durationMillis: Int = 1800,
): Modifier {
  val transition = rememberInfiniteTransition(label = "shimmerTransition")
  val translateAnim by transition.animateFloat(
    initialValue = -300f,
    targetValue = 1200f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "shimmerTranslate",
  )

  return this.drawWithCache {
    val brush = Brush.linearGradient(
      colors = listOf(baseColor, highlightColor, baseColor),
      start = Offset(translateAnim, 0f),
      end = Offset(translateAnim + 300f, size.height),
    )
    onDrawWithContent {
      drawContent()
      drawRect(brush = brush)
    }
  }
}

/**
 * Pulsing glow effect modifier for radar / live status indicators.
 */
@Composable
fun Modifier.pulseEffect(
  minScale: Float = 0.92f,
  maxScale: Float = 1.08f,
  durationMillis: Int = 1400,
): Modifier {
  val transition = rememberInfiniteTransition(label = "pulseTransition")
  val scale by transition.animateFloat(
    initialValue = minScale,
    targetValue = maxScale,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulseScale",
  )

  return this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}
