package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class MusicRepository(private val musicDao: MusicDao) {

    // Reactive streams
    fun getSmartLinksForUser(userId: Int): Flow<List<SmartLink>> = musicDao.getSmartLinksForUser(userId)
    fun getClicksForLink(linkId: Int): Flow<List<ClickAnalytic>> = musicDao.getClicksForLink(linkId)
    val allClicks: Flow<List<ClickAnalytic>> = musicDao.getAllClicks()

    // Auth actions
    suspend fun authenticateUser(username: String, passwordRaw: String): User? {
        val user = musicDao.getUserByUsername(username.trim().lowercase())
        if (user != null && user.passwordHash == simpleHash(passwordRaw)) {
            return user
        }
        return null
    }

    suspend fun registerUser(username: String, passwordRaw: String, artistName: String, bio: String): User? {
        val trimmed = username.trim().lowercase()
        if (musicDao.getUserByUsername(trimmed) != null) return null // Already exists

        val user = User(
            username = trimmed,
            passwordHash = simpleHash(passwordRaw),
            artistName = artistName,
            profileBio = bio,
            avatarColorSeed = Random.nextInt(360)
        )
        val id = musicDao.insertUser(user)
        return user.copy(id = id.toInt())
    }

    suspend fun getUserById(userId: Int): User? = musicDao.getUserById(userId)

    // SmartLink CRUD
    suspend fun createSmartLink(
        userId: Int,
        songTitle: String,
        artistName: String,
        releaseDate: String,
        spotify: String,
        apple: String,
        youtube: String,
        amazon: String,
        deezer: String,
        jioSaavn: String,
        slug: String
    ): Boolean {
        // Ensure slug is clean and unique
        val cleanSlug = slug.trim().lowercase()
            .replace("\\s+".toRegex(), "-")
            .replace("[^a-z0-9\\-]".toRegex(), "")
        if (cleanSlug.isEmpty()) return false

        // Check if slug is already taken
        val existing = musicDao.getSmartLinkBySlug(cleanSlug)
        if (existing != null) return false

        // Random modern vivid gradients seeds
        val gradients = listOf(
            Pair(0xFFFF416C.toInt(), 0xFFFF4B2B.toInt()), // Sunset Red/Orange
            Pair(0xFF8A2387.toInt(), 0xFFE94057.toInt()), // Radiant Pink/Purple
            Pair(0xFF00B4DB.toInt(), 0xFF0083B0.toInt()), // Oceanic Blue
            Pair(0xFF093028.toInt(), 0xFF237A57.toInt()), // Deep Emerald
            Pair(0xFF11998e.toInt(), 0xFF38ef7d.toInt()), // Fresh Teal/Green
            Pair(0xFF7F00FF.toInt(), 0xFFE100FF.toInt()), // Cosmic Purple
            Pair(0xFFf12711.toInt(), 0xFFf5af19.toInt())  // Golden Sun
        )
        val selectedGrad = gradients.random()

        val newLink = SmartLink(
            userId = userId,
            songTitle = songTitle.trim(),
            artistName = artistName.trim(),
            coverGradientStart = selectedGrad.first,
            coverGradientEnd = selectedGrad.second,
            releaseDate = releaseDate,
            spotifyLink = spotify.trim(),
            appleMusicLink = apple.trim(),
            youtubeMusicLink = youtube.trim(),
            amazonMusicLink = amazon.trim(),
            deezerLink = deezer.trim(),
            jioSaavnLink = jioSaavn.trim(),
            uniqueSlug = cleanSlug
        )
        musicDao.insertSmartLink(newLink)
        return true
    }

    suspend fun deleteSmartLink(linkId: Int) {
        musicDao.deleteSmartLinkById(linkId)
    }

    suspend fun getSmartLinkBySlug(slug: String): SmartLink? {
        return musicDao.getSmartLinkBySlug(slug)
    }

    // Analytics record click
    suspend fun recordClick(smartLinkId: Int, platform: String, context: Context) {
        val isTablet = context.resources.configuration.screenLayout and 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK >= 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
        
        val deviceType = if (isTablet) "Tablet" else "Mobile"
        val browsers = listOf("Chrome Mobile", "Safari Mobile", "Firefox Focus", "Android Webview")
        
        val click = ClickAnalytic(
            smartLinkId = smartLinkId,
            platformName = platform,
            deviceType = deviceType,
            browser = browsers.random()
        )
        musicDao.insertClick(click)
    }

    // Hash Helper (Simple baseline SHA-256 equivalent logic for demo database safety)
    private fun simpleHash(pwd: String): String {
        return pwd.hashCode().toString() // Basic hashing in local app representation
    }

    // Pre-populate with deep richness for demo
    suspend fun checkAndPrepopulateDemo(context: Context) {
        val existingDemo = musicDao.getUserByUsername("demo")
        if (existingDemo == null) {
            // 1. Create a beautiful demo user "demo" with password "demo123"
            val demoUserId = musicDao.insertUser(User(
                username = "demo",
                passwordHash = simpleHash("demo123"),
                artistName = "Neon Horizon",
                profileBio = "Electronic synthwave producer blending retro analog vibes with modern crisp 808s. Visual artist & sound synthesizer designer.",
                avatarColorSeed = 280
            )).toInt()

            // 2. Add two amazing sample Tracks
            val track1Id = musicDao.insertSmartLink(SmartLink(
                userId = demoUserId,
                songTitle = "Starlight Pulse",
                artistName = "Neon Horizon",
                coverGradientStart = 0xFF7F00FF.toInt(), // Cosmic Purple
                coverGradientEnd = 0xFFE100FF.toInt(),
                releaseDate = "2026-04-12",
                spotifyLink = "https://open.spotify.com/track/4PTG3Z6ehGkBF36qH7669C",
                appleMusicLink = "https://music.apple.com/us/album/starlight-pulse",
                youtubeMusicLink = "https://music.youtube.com/watch?v=starlight",
                amazonMusicLink = "https://music.amazon.com/albums/starlight",
                deezerLink = "https://www.deezer.com/album/starlight",
                jioSaavnLink = "https://www.jiosaavn.com/album/starlight",
                uniqueSlug = "starlight-pulse"
            )).toInt()

            val track2Id = musicDao.insertSmartLink(SmartLink(
                userId = demoUserId,
                songTitle = "Midnight Velvet",
                artistName = "Neon Horizon",
                coverGradientStart = 0xFF00B4DB.toInt(), // Oceanic Blue
                coverGradientEnd = 0xFF0083B0.toInt(),
                releaseDate = "2026-05-18",
                spotifyLink = "https://open.spotify.com/track/midnightv",
                appleMusicLink = "https://music.apple.com/us/album/midnight-velvet",
                youtubeMusicLink = "https://music.youtube.com/watch?v=midnight",
                amazonMusicLink = "",
                deezerLink = "https://www.deezer.com/album/midnight",
                jioSaavnLink = "https://www.jiosaavn.com/album/midnight-velvet",
                uniqueSlug = "midnight-velvet"
            )).toInt()

            // 3. Populate a wide distribution of click analytics (for starlight-pulse)
            val platforms = listOf("Spotify", "Apple Music", "YouTube Music", "Amazon Music", "Deezer", "JioSaavn")
            val devices = listOf("Mobile", "Tablet", "Desktop")
            val browsers = listOf("Chrome Mobile", "Safari Mobile", "Firefox Focus", "Android Webview")

            // Let's create ~150 synthetic clicks for Track 1
            repeat(146) {
                // Bias towards Spotify and Apple Music
                val r = Random.nextFloat()
                val platform = when {
                    r < 0.40f -> "Spotify"
                    r < 0.65f -> "Apple Music"
                    r < 0.80f -> "YouTube Music"
                    r < 0.90f -> "JioSaavn"
                    r < 0.95f -> "Amazon Music"
                    else -> "Deezer"
                }

                val dev = when {
                    Random.nextFloat() < 0.75f -> "Mobile"
                    Random.nextFloat() < 0.90f -> "Desktop"
                    else -> "Tablet"
                }

                // Random timestamp in the last 7 days
                val ago = Random.nextLong(1000 * 60 * 60 * 24 * 7)
                musicDao.insertClick(ClickAnalytic(
                    smartLinkId = track1Id,
                    platformName = platform,
                    timestamp = System.currentTimeMillis() - ago,
                    deviceType = dev,
                    browser = browsers.random()
                ))
            }

            // Let's create ~55 clicks for Track 2 too
            repeat(55) {
                val r = Random.nextFloat()
                val platform = when {
                    r < 0.35f -> "Spotify"
                    r < 0.55f -> "Apple Music"
                    r < 0.75f -> "YouTube Music"
                    r < 0.85f -> "JioSaavn"
                    else -> "Deezer" // Amazon link is empty for track 2
                }

                val dev = if (Random.nextFloat() < 0.85f) "Mobile" else "Tablet"
                val ago = Random.nextLong(1000 * 60 * 60 * 24 * 3)
                musicDao.insertClick(ClickAnalytic(
                    smartLinkId = track2Id,
                    platformName = platform,
                    timestamp = System.currentTimeMillis() - ago,
                    deviceType = dev,
                    browser = browsers.random()
                ))
            }
        }
    }
}
