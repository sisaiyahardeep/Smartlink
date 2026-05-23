package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClickAnalytic
import com.example.data.SmartLink
import com.example.data.User
import com.example.ui.MusicViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@Composable
fun DashboardScreen(
    viewModel: MusicViewModel,
    onPreviewLink: (SmartLink) -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val smartLinks by viewModel.userSmartLinks.collectAsState()
    val clicks by viewModel.allClicks.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    val activeUser = user ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Sleek Static Header with Artist Avatar (Matches profile styling!)
        ArtistProfileHeader(
            user = activeUser,
            totalTracks = smartLinks.size,
            onLogout = { viewModel.logout() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Tab Menu (My Links / Create / Analytics)
        DashboardTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Dynamic Screen Selector
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> MyLinksView(
                    smartLinks = smartLinks,
                    clicks = clicks,
                    onPreviewLink = onPreviewLink,
                    onDeleteLink = { viewModel.deleteLink(it) }
                )
                1 -> CreateLinkView(
                    viewModel = viewModel,
                    onSuccess = {
                        selectedTab = 0 // Auto focus track list on success
                    }
                )
                2 -> AnalyticsView(
                    smartLinks = smartLinks,
                    clicks = clicks
                )
            }
        }
    }
}

@Composable
fun ArtistProfileHeader(
    user: User,
    totalTracks: Int,
    onLogout: () -> Unit
) {
    // Unique seed gradient based on artist credentials
    val gradientColor1 = remember(user.avatarColorSeed) {
        Color.hsv((user.avatarColorSeed % 360).toFloat(), 0.7f, 0.9f)
    }
    val gradientColor2 = remember(user.avatarColorSeed) {
        Color.hsv(((user.avatarColorSeed + 120) % 360).toFloat(), 0.8f, 0.8f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .glassCard(cornerRadius = 24.dp, borderAlpha = 0.2f, bgAlpha = 0.1f)
            .padding(18.dp)
    ) {
        // Floating Gradient Profile Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(gradientColor1, gradientColor2)))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
        ) {
            Text(
                text = user.artistName.take(1).uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.artistName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (totalTracks == 1) "1 Active Track" else "$totalTracks Active Tracks",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp
            )
        }

        // Logout Studio Action
        IconButton(
            onClick = onLogout,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .testTag("logout_button")
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = GlassPink
            )
        }
    }
}

@Composable
fun DashboardTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(52.dp)
            .glassCard(cornerRadius = 16.dp, borderAlpha = 0.15f, bgAlpha = 0.06f)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabData = listOf(
            Triple(0, "My Tracks", Icons.Default.LibraryMusic),
            Triple(1, "Create Link", Icons.Default.AddBox),
            Triple(2, "Analytics", Icons.Default.Leaderboard)
        )

        tabData.forEach { (index, title, icon) ->
            val isActive = selectedTab == index
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) Color.White.copy(alpha = 0.12f) else Color.Transparent
                    )
                    .clickable { onTabSelected(index) }
                    .testTag("tab_nav_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isActive) Color.White else Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// VIEW 1: MY LINKS VIEW
// ==========================================
@Composable
fun MyLinksView(
    smartLinks: List<SmartLink>,
    clicks: List<ClickAnalytic>,
    onPreviewLink: (SmartLink) -> Unit,
    onDeleteLink: (Int) -> Unit
) {
    if (smartLinks.isEmpty()) {
        EmptyStateView(
            title = "No Music Links Yet",
            tip = "Switch to 'Create Link' to add your first track and configure direct smart links!"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            items(smartLinks, key = { it.id }) { link ->
                val trackClicks = clicks.filter { it.smartLinkId == link.id }.size
                TrackSmartLinkCard(
                    link = link,
                    totalClicks = trackClicks,
                    onPreview = { onPreviewLink(link) },
                    onDelete = { onDeleteLink(link.id) }
                )
            }
        }
    }
}

@Composable
fun TrackSmartLinkCard(
    link: SmartLink,
    totalClicks: Int,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val coverStart = Color(link.coverGradientStart)
    val coverEnd = Color(link.coverGradientEnd)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 20.dp, borderAlpha = 0.2f, bgAlpha = 0.1f)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Visual cover display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(coverStart, coverEnd)))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = link.songTitle,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "/track/${link.uniqueSlug}",
                color = GlassTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    val url = "https://soundlink.studio/track/${link.uniqueSlug}"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("SoundLink Address", url))
                    Toast.makeText(context, "Copied smart link address!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stats Quick Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Released: ${link.releaseDate}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(coverStart.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$totalClicks Clicks",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Actions Block
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // View / Preview Public Landing Page
            IconButton(
                onClick = onPreview,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .testTag("preview_${link.uniqueSlug}")
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = "View page",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete link
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .testTag("delete_${link.uniqueSlug}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete smart link",
                    tint = GlassPink,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==========================================
// VIEW 2: CREATE LINK SCREEN
// ==========================================
@Composable
fun CreateLinkView(
    viewModel: MusicViewModel,
    onSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var songTitle by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }

    // Stream Links
    var spotifyUrl by remember { mutableStateOf("") }
    var appleUrl by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var amazonUrl by remember { mutableStateOf("") }
    var deezerUrl by remember { mutableStateOf("") }
    var jioSaavnUrl by remember { mutableStateOf("") }

    // Auto generate clean slug as title changes
    LaunchedEffect(songTitle) {
        if (slug.isEmpty() || slug == songTitle.lowercase().replace("\\s+".toRegex(), "-").replace("[^a-z0-9\\-]".toRegex(), "")) {
            slug = songTitle.trim().lowercase()
                .replace("\\s+".toRegex(), "-")
                .replace("[^a-z0-9\\-]".toRegex(), "")
        }
    }

    // Default release date if empty
    LaunchedEffect(Unit) {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        releaseDate = df.format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .glassCard(cornerRadius = 24.dp, borderAlpha = 0.15f, bgAlpha = 0.08f)
            .padding(20.dp)
    ) {
        Text(
            text = "Configure Track Metadata",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tracks fields
        MiniGlassTextField(
            value = songTitle,
            onValueChange = { songTitle = it },
            label = "Song / Release Title*",
            placeholder = "e.g., Violet Echoes",
            testTag = "input_song_title"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1.1f)) {
                MiniGlassTextField(
                    value = releaseDate,
                    onValueChange = { releaseDate = it },
                    label = "Release Date*",
                    placeholder = "YYYY-MM-DD",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_release_date"
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                MiniGlassTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    label = "Smart Slug*",
                    placeholder = "violet-echoes",
                    testTag = "input_slug"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Paste Streaming Platform Links",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Leave empty to omit specific platform buttons on landing page.",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Fields per streaming provider
        MiniGlassTextField(
            value = spotifyUrl,
            onValueChange = { spotifyUrl = it },
            label = "Spotify Track Link",
            placeholder = "https://open.spotify.com/track/...",
            testTag = "url_spotify"
        )
        Spacer(modifier = Modifier.height(10.dp))

        MiniGlassTextField(
            value = appleUrl,
            onValueChange = { appleUrl = it },
            label = "Apple Music Link",
            placeholder = "https://music.apple.com/...",
            testTag = "url_apple"
        )
        Spacer(modifier = Modifier.height(10.dp))

        MiniGlassTextField(
            value = youtubeUrl,
            onValueChange = { youtubeUrl = it },
            label = "YouTube Music Link",
            placeholder = "https://music.youtube.com/...",
            testTag = "url_youtube"
        )
        Spacer(modifier = Modifier.height(10.dp))

        MiniGlassTextField(
            value = amazonUrl,
            onValueChange = { amazonUrl = it },
            label = "Amazon Music Link",
            placeholder = "https://music.amazon.com/...",
            testTag = "url_amazon"
        )
        Spacer(modifier = Modifier.height(10.dp))

        MiniGlassTextField(
            value = deezerUrl,
            onValueChange = { deezerUrl = it },
            label = "Deezer Album/Track link",
            placeholder = "https://www.deezer.com/...",
            testTag = "url_deezer"
        )
        Spacer(modifier = Modifier.height(10.dp))

        MiniGlassTextField(
            value = jioSaavnUrl,
            onValueChange = { jioSaavnUrl = it },
            label = "JioSaavn Link",
            placeholder = "https://www.jiosaavn.com/...",
            testTag = "url_jiosaavn"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Create Button
        Button(
            onClick = {
                viewModel.addNewSmartLink(
                    songTitle = songTitle,
                    releaseDate = releaseDate,
                    spotify = spotifyUrl,
                    apple = appleUrl,
                    youtube = youtubeUrl,
                    amazon = amazonUrl,
                    deezer = deezerUrl,
                    jioSaavn = jioSaavnUrl,
                    slug = slug,
                    onSuccess = {
                        Toast.makeText(context, "Successfully created smart link!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    },
                    onFailure = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(GlassTeal, GlassBlue)
                    )
                )
                .testTag("submit_new_link")
        ) {
            Text(
                text = "Generate Smart Link Page",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MiniGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.25f),
                    fontSize = 13.sp
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 13.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            )
        }
    }
}

// Custom simple BasicTextField to avoid full XML/Compose styling clashes
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        cursorBrush = Brush.linearGradient(listOf(GlassTeal, GlassBlue)),
        modifier = modifier
    )
}

// ==========================================
// VIEW 3: ANALYTICS VIEW (CHARTS & LOGS)
// ==========================================
@Composable
fun AnalyticsView(
    smartLinks: List<SmartLink>,
    clicks: List<ClickAnalytic>
) {
    val scrollState = rememberScrollState()

    // 1. Calculate overall metrics
    val totalClicks = clicks.size
    val recentClicks = clicks.take(15) // last 15 detailed clicks for the live stream logs

    // Group clocks count per platform
    val platformCounts = remember(clicks) {
        val map = mutableMapOf<String, Int>()
        clicks.forEach { map[it.platformName] = (map[it.platformName] ?: 0) + 1 }
        map.toList().sortedByDescending { it.second }
    }

    // Group for 7 Day Canvas Graph Array
    val sevenDaysData = remember(clicks) {
        val cal = Calendar.getInstance()
        val data = MutableList(7) { 0 }
        val df = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        val dayKeys = (0..6).map { offset ->
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -offset)
            df.format(c.time)
        }.reversed()

        clicks.forEach { click ->
            val dStr = df.format(Date(click.timestamp))
            val idx = dayKeys.indexOf(dStr)
            if (idx != -1) {
                data[idx] += 1
            }
        }
        data
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stat Widgets Card Row
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                value = "$totalClicks",
                label = "Total Stream Clicks",
                icon = Icons.Default.TrendingUp,
                accentColor = GlassTeal,
                modifier = Modifier.weight(1.1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                value = "${smartLinks.size}",
                label = "Active Smartlinks",
                icon = Icons.Default.Link,
                accentColor = GlassPurple,
                modifier = Modifier.weight(0.9f)
            )
        }

        // Custom Canvas 7 Days History Graph!
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 24.dp, borderAlpha = 0.2f, bgAlpha = 0.08f)
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "Weekly Clicks Wave",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aggregated listeners clicks performance trend",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                CustomSparklineGraph(
                    dataPoints = sevenDaysData,
                    accentColor = GlassTeal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }
        }

        // Platform breakdown progressive bars (glowing!)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 24.dp, borderAlpha = 0.15f, bgAlpha = 0.07f)
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "Streaming Channels Distribution",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                if (platformCounts.isEmpty()) {
                    Text(
                        text = "No platform records yet.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    val maxCount = max(platformCounts.first().second, 1)
                    platformCounts.forEach { (platform, count) ->
                        val brandColor = when (platform) {
                            "Spotify" -> Color(0xFF1DB954)
                            "Apple Music" -> Color(0xFFFC3C44)
                            "YouTube Music" -> Color(0xFFFF0000)
                            "Amazon Music" -> Color(0xFF00A8E1)
                            "Deezer" -> Color(0xFFEF5461)
                            "JioSaavn" -> Color(0xFF3EC251)
                            else -> GlassTeal
                        }

                        PlatformStatsRow(
                            platformName = platform,
                            count = count,
                            percentage = count.toFloat() / maxCount.toFloat(),
                            brandColor = brandColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Live Feed click logs
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 24.dp, borderAlpha = 0.15f, bgAlpha = 0.07f)
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "Live Traffic Logs",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Recent real-time listeners action streams",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                if (recentClicks.isEmpty()) {
                    Text(
                        text = "Awaiting listener interactions...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    recentClicks.forEach { click ->
                        val timeStr = remember(click.timestamp) {
                            val sdf = SimpleDateFormat("HH:mm · MMM dd", Locale.getDefault())
                            sdf.format(Date(click.timestamp))
                        }
                        LiveLogItem(
                            platform = click.platformName,
                            time = timeStr,
                            device = click.deviceType,
                            browser = click.browser
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassCard(cornerRadius = 18.dp, borderAlpha = 0.18f, bgAlpha = 0.07f)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun PlatformStatsRow(
    platformName: String,
    count: Int,
    percentage: Float,
    brandColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = platformName,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$count clicks",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Custom Neon Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(brandColor)
            )
        }
    }
}

@Composable
fun LiveLogItem(
    platform: String,
    time: String,
    device: String,
    browser: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (device) {
            "Desktop" -> Icons.Default.Computer
            "Tablet" -> Icons.Default.TabletMac
            else -> Icons.Default.Smartphone
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = platform,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "click",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }

            Text(
                text = "via $browser",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp
            )
        }

        Text(
            text = time,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Sparkline Canvas wave designer
@Composable
fun CustomSparklineGraph(
    dataPoints: List<Int>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (dataPoints.isEmpty()) return@Canvas

        val maxVal = max(dataPoints.maxOrNull() ?: 1, 1)
        val path = Path()
        val fillPath = Path()

        val width = size.width
        val height = size.height
        val stepX = width / (dataPoints.size - 1)

        // Generate coordinate matrices
        val points = dataPoints.mapIndexed { index, valPoint ->
            val x = index * stepX
            // Inverse Y because Canvas coordinates start at top left (0,0) as 0, and peak at Y-max
            val y = height - (valPoint.toFloat() / maxVal.toFloat() * (height - 20f)) - 10f
            Offset(x, y)
        }

        // Draw helper horizontal grid line metrics
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, height * 0.25f),
            end = Offset(width, height * 0.25f),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, height * 0.75f),
            end = Offset(width, height * 0.75f),
            strokeWidth = 1f
        )

        // Build path outlines
        path.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, height)
        fillPath.lineTo(points.first().x, points.first().y)

        // Use cubic curves for high-quality wave visuals
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val current = points[i]
            val control1 = Offset(prev.x + stepX / 2f, prev.y)
            val control2 = Offset(current.x - stepX / 2f, current.y)

            path.cubicTo(
                control1.x, control1.y,
                control2.x, control2.y,
                current.x, current.y
            )

            fillPath.cubicTo(
                control1.x, control1.y,
                control2.x, control2.y,
                current.x, current.y
            )
        }

        fillPath.lineTo(points.last().x, height)
        fillPath.close()

        // Draw fading glow area under graph
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(accentColor.copy(alpha = 0.22f), Color.Transparent)
            )
        )

        // Draw glow line stroke wave
        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw highlighted glowing points
        points.forEach { pt ->
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = accentColor,
                radius = 8.dp.toPx(),
                center = pt,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    tip: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .glassCard(cornerRadius = 24.dp, borderAlpha = 0.15f, bgAlpha = 0.06f)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = tip,
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
