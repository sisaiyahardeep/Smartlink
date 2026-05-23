package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // User authentication queries
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // SmartLink CRUD
    @Query("SELECT * FROM smart_links WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSmartLinksForUser(userId: Int): Flow<List<SmartLink>>

    @Query("SELECT * FROM smart_links WHERE uniqueSlug = :slug LIMIT 1")
    suspend fun getSmartLinkBySlug(slug: String): SmartLink?

    @Query("SELECT * FROM smart_links WHERE id = :id LIMIT 1")
    suspend fun getSmartLinkById(id: Int): SmartLink?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartLink(link: SmartLink): Long

    @Query("DELETE FROM smart_links WHERE id = :linkId")
    suspend fun deleteSmartLinkById(linkId: Int)

    // Click Analytics queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClick(click: ClickAnalytic)

    @Query("SELECT * FROM click_analytics WHERE smartLinkId = :linkId ORDER BY timestamp DESC")
    fun getClicksForLink(linkId: Int): Flow<List<ClickAnalytic>>

    @Query("SELECT * FROM click_analytics ORDER BY timestamp DESC")
    fun getAllClicks(): Flow<List<ClickAnalytic>>

    // Query to count clicks per platform for a specific link
    @Query("SELECT COUNT(*) FROM click_analytics WHERE smartLinkId = :linkId AND platformName = :platformName")
    suspend fun getClickCountForPlatform(linkId: Int, platformName: String): Int
}
