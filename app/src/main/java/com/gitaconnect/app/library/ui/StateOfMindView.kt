package com.gitaconnect.app.library.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StateOfMindView(
    intensity: Float,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Define Mood Label & Colors
    val moodLabel = when (intensity) {
        in 0.0f..0.2f -> "Very Unpleasant"
        in 0.2f..0.4f -> "Unpleasant"
        in 0.4f..0.6f -> "Neutral"
        in 0.6f..0.8f -> "Pleasant"
        else -> "Very Pleasant"
    }

    val moodColors = when {
        intensity < 0.2f -> listOf(Color(0xFFF22640), Color(0xFF80001A)) // Crimson
        intensity < 0.4f -> listOf(Color(0xFF8C59FF), Color(0xFF4D1A66)) // Purple
        intensity < 0.6f -> listOf(Color(0xFF00CCFF), Color(0xFF0080E6)) // Azure
        intensity < 0.8f -> listOf(Color(0xFF4DFF99), Color(0xFF00B366)) // Spring Green
        else -> listOf(Color(0xFFFFE64D), Color(0xFFFF9900)) // Yellow/Orange
    }

    // 2. Setup Infinite Time Animation (VSYNC synced)
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val startMillis = androidx.compose.runtime.withFrameMillis { it }
        while (true) {
            time = (androidx.compose.runtime.withFrameMillis { it } - startMillis) / 1000f
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. The Procedural Mathematical Animation
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.99f }) {
                val canvasSize = size
                val centerOffset = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                val baseRadius = minOf(canvasSize.width, canvasSize.height) * 0.4f

                // Base Shadow / Glow (Blurred)
                val shadowPath = getPerfectMoodPath(canvasSize, intensity, time, 0)
                drawPath(
                    path = shadowPath,
                    color = Color.Black.copy(alpha = 0.2f),
                )

                // 5 Overlapping Petal Layers with BlendMode.Screen
                val radialGradient = Brush.radialGradient(
                    colors = listOf(moodColors[0].copy(alpha = 0.9f), moodColors[1].copy(alpha = 0.3f)),
                    center = centerOffset,
                    radius = baseRadius
                )

                val masterRotation = time * (3f + intensity * 5f)

                withTransform({
                    rotate(degrees = masterRotation, pivot = centerOffset)
                }) {
                    for (petalIndex in 4 downTo 0) {
                        val rotationOffset = petalIndex * 12f + (time * (5f + intensity * 5f))
                        val scaleWobble = 0.95f + sin(time * 0.5f + petalIndex) * 0.05f

                        withTransform({
                            rotate(degrees = rotationOffset, pivot = centerOffset)
                            scale(scaleX = scaleWobble, scaleY = scaleWobble, pivot = centerOffset)
                        }) {
                            drawPath(
                                path = getPerfectMoodPath(canvasSize, intensity, time, petalIndex),
                                brush = radialGradient,
                                blendMode = BlendMode.Screen
                            )
                        }
                    }

                    // Stroke Overlay Highlight
                    val strokeGradient = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.2f)),
                        start = Offset(0f, 0f),
                        end = Offset(canvasSize.width, canvasSize.height)
                    )
                    drawPath(
                        path = getPerfectMoodPath(canvasSize, intensity, time, 0),
                        brush = strokeGradient,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
                        blendMode = BlendMode.Overlay
                    )

                    // Inner Core Glow
                    val coreGradient = Brush.linearGradient(
                        colors = listOf(moodColors[0].copy(alpha = 0.5f), Color.Transparent),
                        start = centerOffset,
                        end = Offset(centerOffset.x, canvasSize.height)
                    )
                    withTransform({
                        scale(0.5f, 0.5f, centerOffset)
                    }) {
                        drawPath(
                            path = getPerfectMoodPath(canvasSize, intensity, time, 0),
                            brush = coreGradient,
                            blendMode = BlendMode.Plus
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Mood Title
        Text(
            text = moodLabel,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = GitaCharcoal,
            modifier = Modifier.animateContentSize()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Custom Track Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("VERY UNPLEASANT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GitaCharcoalSoft)
                Text("VERY PLEASANT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GitaCharcoalSoft)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            onIntensityChange((down.position.x / size.width).coerceIn(0f, 1f))
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()
                                if (change != null) {
                                    change.consume()
                                    onIntensityChange((change.position.x / size.width).coerceIn(0f, 1f))
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
            ) {
                val trackWidth = constraints.maxWidth.toFloat()
                val handleSizeDp = 28.dp
                val handleSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { handleSizeDp.toPx() }
                
                // Track Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Gray.copy(alpha = 0.15f))
                )

                // Colored Active Track
                val activeWidth = maxOf(handleSizeDp.value / 2, maxWidth.value * intensity).dp
                Box(
                    modifier = Modifier
                        .width(activeWidth)
                        .height(20.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(moodColors[1], moodColors[0])
                            )
                        )
                )

                // Draggable Handle
                val clampedX = maxOf(0f, minOf(trackWidth, trackWidth * intensity))
                val handleOffsetX = maxOf(0f, minOf(clampedX - handleSizePx / 2f, trackWidth - handleSizePx))
                
                Box(
                    modifier = Modifier
                        .offset(x = with(androidx.compose.ui.platform.LocalDensity.current) { handleOffsetX.toDp() })
                        .align(Alignment.CenterStart)
                        .size(handleSizeDp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

private fun getPerfectMoodPath(size: androidx.compose.ui.geometry.Size, intensity: Float, time: Float, index: Int = 0): Path {
    val path = Path()
    val center = Offset(size.width / 2f, size.height / 2f)
    val petals = 7.0f
    val baseRadius = minOf(size.width, size.height) / 2f
    val distortion = (intensity - 0.5f) * 2.0f
    val stepCount = 360

    for (angleIndex in 0 until stepCount) {
        val angle = (angleIndex.toFloat() / stepCount) * Math.PI.toFloat() * 2f
        
        val wave: Float = if (distortion > 0) {
            cos(angle * petals)
        } else {
            val rawWave = sin(angle * petals)
            rawWave * rawWave * rawWave
        }
        
        val wobble = sin(time * 1.5f + index.toFloat()) * 0.05f
        val radius = baseRadius * (0.8f + (wave * 0.15f * distortion) + wobble)
        
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        
        if (angleIndex == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}
