package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SmartLink
import com.example.ui.MusicViewModel
import com.example.ui.theme.GlassDarkBg
import com.example.ui.theme.glassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicSmartLinkPreview(
    smartLink: SmartLink,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Vibrant background colors generated from the album cover gradients
    val trackGradientStart = Color(smartLink.coverGradientStart)
    val trackGradientEnd = Color(smartLink.coverGradientEnd)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
            .drawBehind {
                val width = size.width
                val height = size.height
                if (width > 0f && height > 0f) {
                    val radius = width * 1.1f
                    // Background radial circles based on song colors
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(trackGradientStart.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(width * 0.5f, height * 0.25f),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(width * 0.5f, height * 0.25f)
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Elegant Navigation Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("back_to_studio")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Public view badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "LIVE PREVIEW",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Web Content Layout container (Max-width restricted on large screens for absolute responsive consistency!)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Cover Art Display Card (Glowing Asymmetric Layout)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(230.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(trackGradientStart, trackGradientEnd)
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                    ) {
                        // Symbolic glowing vinyl / music record effect
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Vinyl logo",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(90.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Track Title & Artist Metadata
                    Text(
                        text = smartLink.songTitle,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "by ${smartLink.artistName}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Released: ${smartLink.releaseDate}",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Smart Link Address display with Clipboard integration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                            .clickable {
                                copyToClipboard(context, "https://soundlink.studio/track/${smartLink.uniqueSlug}")
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "soundlink.studio/track/${smartLink.uniqueSlug}",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy address link",
                            tint = trackGradientEnd,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Stacked Stream Link Portal CTAs (ONLY show if URL is populated!)
                    // 1. Spotify
                    if (smartLink.spotifyLink.isNotBlank()) {
                        StreamLinkCard(
                            platform = "Spotify",
                            ctaText = "Play",
                            brandColor = Color(0xFF1DB954),
                            onLaunch = {
                                viewModel.logClick(smartLink.id, "Spotify", context)
                                launchBrowser(context, smartLink.spotifyLink)
                            },
                            modifier = Modifier.testTag("stream_spotify")
                        )
                    }

                    // 2. Apple Music
                    if (smartLink.appleMusicLink.isNotBlank()) {
                        StreamLinkCard(
                            platform = "Apple Music",
                            ctaText = "Listen",
                            brandColor = Color(0xFFFC3C44),
                            onLaunch = {
                                viewModel.logClick(smartLink.id, "Apple Music", context)
                                launchBrowser(context, smartLink.appleMusicLink)
                            },
                            modifier = Modifier.testTag("stream_apple")
                        )
                    }

                    // 3. YouTube Music
                    if (smartLink.youtubeMusicLink.isNotBlank()) {
                        StreamLinkCard(
                            platform = "YouTube Music",
                            ctaText = "Watch",
                            brandColor = Color(0xFFFF0000),
                            onLaunch = {
                                viewModel.logClick(smartLink.id, "YouTube Music", context)
                                launchBrowser(context, smartLink.youtubeMusicLink)
                            },
                            modifier = Modifier.testTag("stream_youtube")
                        )
                    }

                    // 4. Amazon Music
                    if (smartLink.amazonMusicLink.isNotBlank()) {
                        StreamLinkCard(
                            platform = "Amazon Music",
                            ctaText = "Listen",
                            brandColor = Color(0xFF00A8E1),
                            onLaunch = {
                                viewModel.logClick(smartLink.id, "Amazon Music", context)
                                launchBrowser(context, smartLink.amazonMusicLink)
                            },
                            modifier = Modifier.testTag("stream_amazon")
                        )
                    }

                    // 5. Deezer
                    if (smartLink.deezerLink.isNotBlank()) {
                        StreamLinkCard(
                            platform = "Deezer",
                            ctaText = "Listen",
                            brandColor = Color(0xFFEF5461),
                            onLaunch = {
                                viewModel.logClick(smartLink.id, "Deezer", context)
                                launchBrowser(context, smartLink.deezerLink)
                            },
                            modifier = Modifier.testTag("stream_deezer")
                        )
                    }

                    // 6. JioSaavn
                    if (smartLink.jioSaavnLink.isNotBlank()) {
                        StreamLinkCard(
                            platform = "JioSaavn",
                            ctaText = "Stream",
                            brandColor = Color(0xFF3EC251),
                            onLaunch = {
                                viewModel.logClick(smartLink.id, "JioSaavn", context)
                                launchBrowser(context, smartLink.jioSaavnLink)
                            },
                            modifier = Modifier.testTag("stream_jiosaavn")
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Floating Glass Share Hub Button
                    Button(
                        onClick = {
                            shareText(
                                context = context,
                                text = "Listen to '${smartLink.songTitle}' by ${smartLink.artistName} on all platforms via SoundLink:\nhttps://soundlink.studio/track/${smartLink.uniqueSlug}"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(trackGradientStart, trackGradientEnd))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share smart link page",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Share With Listeners",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun StreamLinkCard(
    platform: String,
    ctaText: String,
    brandColor: Color,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .glassCard(cornerRadius = 16.dp, borderAlpha = 0.2f, bgAlpha = 0.08f)
            .clickable(onClick = onLaunch)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Platform Symbolic Brand Badge Colored Dot inside modern outline
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(brandColor)
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Platform Brand Text
        Text(
            text = platform,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Interactive Action Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = ctaText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// Global Browser Launch Support
private fun launchBrowser(context: Context, url: String) {
    try {
        val validatedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validatedUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open browser URL link.", Toast.LENGTH_SHORT).show()
    }
}

// Share text handler
private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Music Smart Link"))
}

// Copy to Clipboard
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("SoundLink Address", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Addresscopied to system clipboard!", Toast.LENGTH_SHORT).show()
}
