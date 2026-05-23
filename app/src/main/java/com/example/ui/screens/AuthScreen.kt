package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var isSignUp by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var profileBio by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Clear details on toggle
    LaunchedEffect(isSignUp) {
        username = ""
        password = ""
        artistName = ""
        profileBio = ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 28.dp, borderAlpha = 0.3f, bgAlpha = 0.12f)
                .padding(28.dp)
        ) {
            // Elegant Music Link Logo Mark
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GlassPink, GlassPurple, GlassTeal)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Logo icon",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SoundLink",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Artist Smart Link Studio",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Slidable Glass Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (!isSignUp) Color.White.copy(alpha = 0.15f) else Color.Transparent
                        )
                        .clickable { isSignUp = false }
                        .testTag("tab_signin")
                ) {
                    Text(
                        text = "Sign In",
                        color = if (!isSignUp) Color.White else Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSignUp) Color.White.copy(alpha = 0.15f) else Color.Transparent
                        )
                        .clickable { isSignUp = true }
                        .testTag("tab_signup")
                ) {
                    Text(
                        text = "Sign Up",
                        color = if (isSignUp) Color.White else Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            GlassOutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                icon = Icons.Outlined.Person,
                testTag = "username_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                icon = Icons.Outlined.Lock,
                isPassword = true,
                showPassword = showPassword,
                onPasswordToggle = { showPassword = !showPassword },
                testTag = "password_input"
            )

            // Conditional Sign Up Fields
            AnimatedVisibility(visible = isSignUp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassOutlinedTextField(
                        value = artistName,
                        onValueChange = { artistName = it },
                        label = "Artist / Band Name",
                        icon = Icons.Default.Brush,
                        testTag = "artist_name_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    GlassOutlinedTextField(
                        value = profileBio,
                        onValueChange = { profileBio = it },
                        label = "Artist Profile Bio",
                        icon = Icons.Default.Notes,
                        singleLine = false,
                        testTag = "artist_bio_input"
                    )
                }
            }

            // Error displays
            AnimatedVisibility(visible = authError != null) {
                authError?.let {
                    Text(
                        text = it,
                        color = GlassPink,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = GlassTeal,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                // Submit Button
                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.signup(username, password, artistName, profileBio) {}
                        } else {
                            viewModel.login(username, password) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GlassPink, GlassPurple)
                            )
                        )
                        .testTag("auth_submit_button")
                ) {
                    Text(
                        text = if (isSignUp) "Create Artist Account" else "Connect Studio",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.15f)
                    )
                    Text(
                        text = " OR ",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.15f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Demo Login Button
                OutlinedButton(
                    onClick = {
                        // Prepopulate database and log in instantly with demo/demo123
                        viewModel.initializeDemoIfNeeded(context)
                        viewModel.login("demo", "demo123") {}
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassTeal),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(GlassTeal, GlassBlue))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quick_demo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = "Demo icon",
                        tint = GlassTeal,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Explore Demo Studio",
                        color = GlassTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    singleLine: Boolean = true,
    testTag: String = ""
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = Color.White.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 4,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.04f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = GlassTeal,
            focusedIndicatorColor = GlassTeal,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.15f),
            disabledIndicatorColor = Color.White.copy(alpha = 0.05f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag(testTag)
    )
}
