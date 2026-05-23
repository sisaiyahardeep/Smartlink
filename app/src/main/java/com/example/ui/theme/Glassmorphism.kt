package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Custom gorgeous colors for our cyber-glassmorphism neon blobs
val GlassPurple = Color(0xFF4F46E5) // Sleek Indigo
val GlassPink = Color(0xFFE11D48) // Sleek Rose
val GlassOrange = Color(0xFFF27121)
val GlassTeal = Color(0xFF10B981) // Emerald / Mint variant
val GlassBlue = Color(0xFF3B82F6) // Sleek Blue
val GlassDarkBg = Color(0xFF08080A) // Sleek Interface Dark Base Background

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
            .drawBehind {
                val width = size.width
                val height = size.height
                if (width > 0f && height > 0f) {
                    val radius1 = width * 1.0f
                    val radius2 = width * 1.1f

                    // Decorator Top-Left Indigo Gradient Blur
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassPurple.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(width * -0.1f, height * 0.15f),
                            radius = radius1
                        ),
                        radius = radius1,
                        center = Offset(width * -0.1f, height * 0.15f)
                    )

                    // Decorator Bottom-Right Rose Gradient Blur
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassPink.copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(width * 1.1f, height * 0.85f),
                            radius = radius2
                        ),
                        radius = radius2,
                        center = Offset(width * 1.1f, height * 0.85f)
                    )
                }
            }
    ) {
        content()
    }
}

// Custom Glassmorphic Card modifier
fun Modifier.glassCard(
    cornerRadius: Dp = 24.dp,
    borderAlpha: Float = 0.25f,
    bgAlpha: Float = 0.08f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.White.copy(alpha = bgAlpha))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.25f),
                Color.Transparent,
                Color.White.copy(alpha = borderAlpha * 0.15f)
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

// Dark version of glass card for better contrast if needed
fun Modifier.glassCardDark(
    cornerRadius: Dp = 24.dp,
    borderAlpha: Float = 0.18f,
    bgAlpha: Float = 0.45f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.Black.copy(alpha = bgAlpha))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.Transparent,
                Color.White.copy(alpha = borderAlpha * 0.4f)
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
