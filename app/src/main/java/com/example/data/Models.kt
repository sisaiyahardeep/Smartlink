package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String, // Securely checked password
    val artistName: String,
    val profileBio: String = "",
    val avatarColorSeed: Int = 0 // Used to generate beautiful glass-avatar background
)

@Entity(tableName = "smart_links")
data class SmartLink(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val songTitle: String,
    val artistName: String,
    val coverGradientStart: Int, // Hex value of gradient color
    val coverGradientEnd: Int, // Hex value of gradient color
    val releaseDate: String,
    val spotifyLink: String = "",
    val appleMusicLink: String = "",
    val youtubeMusicLink: String = "",
    val amazonMusicLink: String = "",
    val deezerLink: String = "",
    val jioSaavnLink: String = "",
    val uniqueSlug: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "click_analytics")
data class ClickAnalytic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val smartLinkId: Int,
    val platformName: String, // "Spotify", "Apple Music", "YouTube Music", "Amazon Music", "Deezer", "JioSaavn"
    val timestamp: Long = System.currentTimeMillis(),
    val deviceType: String = "Mobile", // Mobile, Desktop, Tablet
    val browser: String = "In-App"
)
