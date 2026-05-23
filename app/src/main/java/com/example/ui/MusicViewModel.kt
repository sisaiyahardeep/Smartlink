package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ClickAnalytic
import com.example.data.MusicDao
import com.example.data.MusicRepository
import com.example.data.SmartLink
import com.example.data.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Dynamically retrieve smart links for the currently authenticated User
    @OptIn(ExperimentalCoroutinesApi::class)
    val userSmartLinks: StateFlow<List<SmartLink>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getSmartLinksForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Retrieve ALL clicks to display aggregated stats
    val allClicks: StateFlow<List<ClickAnalytic>> = repository.allClicks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun initializeDemoIfNeeded(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.checkAndPrepopulateDemo(context)
            _isLoading.value = false
        }
    }

    fun login(usernameSecret: String, passwordSecret: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val user = repository.authenticateUser(usernameSecret, passwordSecret)
            if (user != null) {
                _currentUser.value = user
                onSuccess()
            } else {
                _authError.value = "Invalid username or password credentials."
            }
            _isLoading.value = false
        }
    }

    fun signup(username: String, passwordRaw: String, artistName: String, bio: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            if (username.length < 3 || passwordRaw.length < 4) {
                _authError.value = "Username (min 3 chars) or Password (min 4 chars) too short."
                _isLoading.value = false
                return@launch
            }
            if (artistName.isBlank()) {
                _authError.value = "Artist Name cannot be empty."
                _isLoading.value = false
                return@launch
            }
            val user = repository.registerUser(username, passwordRaw, artistName, bio)
            if (user != null) {
                _currentUser.value = user
                onSuccess()
            } else {
                _authError.value = "Username already taken."
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun addNewSmartLink(
        songTitle: String,
        releaseDate: String,
        spotify: String,
        apple: String,
        youtube: String,
        amazon: String,
        deezer: String,
        jioSaavn: String,
        slug: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (songTitle.isBlank()) {
            onFailure("Song Title is required.")
            return
        }
        if (slug.isBlank()) {
            onFailure("Unique link path slug is required.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.createSmartLink(
                userId = user.id,
                songTitle = songTitle,
                artistName = user.artistName,
                releaseDate = releaseDate,
                spotify = spotify,
                apple = apple,
                youtube = youtube,
                amazon = amazon,
                deezer = deezer,
                jioSaavn = jioSaavn,
                slug = slug
            )
            _isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                onFailure("The link path slug is already taken. Try another unique word/title.")
            }
        }
    }

    fun deleteLink(linkId: Int) {
        viewModelScope.launch {
            repository.deleteSmartLink(linkId)
        }
    }

    fun logClick(smartLinkId: Int, platform: String, context: Context) {
        viewModelScope.launch {
            repository.recordClick(smartLinkId, platform, context)
        }
    }

    class Factory(private val repository: MusicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                return MusicViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
