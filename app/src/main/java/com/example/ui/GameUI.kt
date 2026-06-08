package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.*
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

@Composable
fun GameUI(viewModel: GameViewModel) {
    val playerProfile by viewModel.playerProfile.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = viewModel.gameState,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith
                        fadeOut(animationSpec = tween(250))
            },
            label = "ScreenTransition"
        ) { state ->
            when (state) {
                GameState.MainMenu -> MainMenuScreen(viewModel, playerProfile)
                GameState.Upgrades -> ResearchScreen(viewModel, playerProfile)
                GameState.Playing -> PlayingScreen(viewModel)
                GameState.Help -> HowToPlayScreen(viewModel)
                GameState.GameOver -> GameOverScreen(viewModel)
                GameState.Leaderboard -> LeaderboardScreen(viewModel, playerProfile)
            }
        }
    }
}

@Composable
fun MainMenuScreen(viewModel: GameViewModel, profile: com.example.data.PlayerProfile) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var showAuthDialog by remember { mutableStateOf(false) }
    var showEditTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf(profile.gameTagName ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Reward Points",
                        tint = CyberPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "COMPILER CHIPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberBlue.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${profile.rewardPoints} RP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "High Score",
                        tint = CyberGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SYSTEM RECORD",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberBlue.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${profile.highScore} PTS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gamer Profile / Google Identity Card
        GamerProfileCard(
            viewModel = viewModel,
            profile = profile,
            onShowAuth = { showAuthDialog = true },
            onShowEditTag = { 
                newTagInput = profile.gameTagName ?: ""
                showEditTagDialog = true 
            }
        )

        // Title and Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, CyberBlue, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Robo Icon",
                    modifier = Modifier.size(40.dp),
                    tint = CyberBlue
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ROBO DEFENSE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp
                ),
                color = CyberBlue,
                textAlign = TextAlign.Center
            )
            Text(
                text = "SURGE DEFENDER CODEBASE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp
                ),
                color = CyberPurple.copy(alpha = alphaScale)
            )
        }

        // Sector Choice (Immersive Theme)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TACTICAL BATTLEFIELD SECTOR",
                color = CyberBlue.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(MapSector.values()) { sector ->
                    val isSelected = viewModel.selectedMapSector == sector
                    Box(
                        modifier = Modifier
                            .width(86.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) sector.bgColor.copy(alpha = 0.2f) else CyberSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) sector.gridDotColor else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectedMapSector = sector }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (sector) {
                                    MapSector.Classic -> Icons.Default.Settings
                                    MapSector.Winter -> Icons.Default.Star
                                    MapSector.Desert -> Icons.Default.Warning
                                    MapSector.Forest -> Icons.Default.Place
                                    MapSector.Lava -> Icons.Default.Favorite
                                    MapSector.Coastal -> Icons.Default.Info
                                    MapSector.Garden -> Icons.Default.ThumbUp
                                    MapSector.Highway -> Icons.Default.Menu
                                    MapSector.River -> Icons.Default.PlayArrow
                                    MapSector.Ruins -> Icons.Default.Build
                                    MapSector.StoneBrickRoad -> Icons.Default.Home
                                    MapSector.Valley -> Icons.Default.List
                                },
                                contentDescription = sector.label,
                                tint = if (isSelected) sector.gridDotColor else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sector.label.split(" ").firstOrNull() ?: "",
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Short Description of selected sector
            Text(
                text = viewModel.selectedMapSector.description,
                color = OnCyberSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.setScreen(GameState.Playing) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_game_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INITIALIZE DEFENSE",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setScreen(GameState.Upgrades) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, CyberPurple, RoundedCornerShape(12.dp))
                        .testTag("upgrades_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = CyberPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESEARCH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { viewModel.setScreen(GameState.Leaderboard) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, CyberGreen, RoundedCornerShape(12.dp))
                        .testTag("leaderboard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LEADERBOARD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = { viewModel.setScreen(GameState.Help) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CyberBlue.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEFENSIVE COMMAND GUIDES",
                    color = CyberBlue.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // AUTHENTIC GOOGLE CHOOSE ACCOUNT DIALOG
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Standard Google visual branding icon colors
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose a Google Account to synchronize Robo Defense progress, records and network highscore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Account Option 1: TCGKennedy (personalized based on real user email metadata!)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.signInWithGoogle("TCGKennedy@gmail.com", "TCGKennedy")
                                showAuthDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, CyberBlue.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("T", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("TCGKennedy", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("TCGKennedy@gmail.com", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }

                    // Account Option 2: Generic Guest / Alternative testing ID
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.signInWithGoogle("cyber.defender.pro@gmail.com", "CyberDefender")
                                showAuthDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("C", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("CyberDefender", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("cyber.defender.pro@gmail.com", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // EDIT GAMER TAG DIALOG
    if (showEditTagDialog) {
        AlertDialog(
            onDismissRequest = { showEditTagDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInput.trim().isNotEmpty()) {
                            viewModel.updateGameTagName(newTagInput.trim())
                            showEditTagDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                ) {
                    Text("Apply Code", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTagDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = {
                Text("Reprogram Gamer Tag", color = CyberBlue, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Configure your decentralized network matrix tag. Other cyber defense grids will see this name on leadership databases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it.take(24) },
                        label = { Text("Network Cyber Tag") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = CyberBlue,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ResearchScreen(viewModel: GameViewModel, profile: com.example.data.PlayerProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setScreen(GameState.MainMenu) },
                modifier = Modifier
                    .border(1.dp, CyberBlue.copy(alpha = 0.5f), CircleShape)
                    .background(CyberSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberBlue
                )
            }

            Text(
                text = "UPGRADE LAB",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = CyberBlue
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                modifier = Modifier.border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "RP",
                        tint = CyberPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${profile.rewardPoints} RP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Analyze scrap code to permanently strengthen defensive weaponry and fortress structural grids.",
            color = OnCyberSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                UpgradeItemRow(
                    title = "Bullet Damage Protocol",
                    description = "Gives Gun Towers +15% damage per level of particle output.",
                    currentLevel = profile.bulletDamageLevel,
                    cost = (profile.bulletDamageLevel + 1) * 200,
                    tint = CyberBlue,
                    onBuy = { viewModel.buyUpgrade("bullet") },
                    hasFunds = profile.rewardPoints >= (profile.bulletDamageLevel + 1) * 200
                )
            }
            item {
                UpgradeItemRow(
                    title = "Explosive Blast Core",
                    description = "Expands Rocket ammunition blast splash radius by +15% per upgrade.",
                    currentLevel = profile.explosiveBlastLevel,
                    cost = (profile.explosiveBlastLevel + 1) * 250,
                    tint = CyberOrange,
                    onBuy = { viewModel.buyUpgrade("explosive") },
                    hasFunds = profile.rewardPoints >= (profile.explosiveBlastLevel + 1) * 250
                )
            }
            item {
                UpgradeItemRow(
                    title = "Flak Firing Accelerator",
                    description = "Shortens Anti-Aircraft reload cycle time by +15% for rapid fire.",
                    currentLevel = profile.flakFireRateLevel,
                    cost = (profile.flakFireRateLevel + 1) * 220,
                    tint = CyberPurple,
                    onBuy = { viewModel.buyUpgrade("flak") },
                    hasFunds = profile.rewardPoints >= (profile.flakFireRateLevel + 1) * 220
                )
            }
            item {
                UpgradeItemRow(
                    title = "Initial Funding Grant",
                    description = "Grants +20% additional gold credits at the start of each map.",
                    currentLevel = profile.startingGoldLevel,
                    cost = (profile.startingGoldLevel + 1) * 300,
                    tint = CyberGreen,
                    onBuy = { viewModel.buyUpgrade("gold") },
                    hasFunds = profile.rewardPoints >= (profile.startingGoldLevel + 1) * 300
                )
            }
            item {
                UpgradeItemRow(
                    title = "Fortress Structural Armor",
                    description = "Increases starter mainframe processing health buffer by +5 points.",
                    currentLevel = profile.fortressHealthLevel,
                    cost = (profile.fortressHealthLevel + 1) * 150,
                    tint = Color.White,
                    onBuy = { viewModel.buyUpgrade("health") },
                    hasFunds = profile.rewardPoints >= (profile.fortressHealthLevel + 1) * 150
                )
            }
        }
    }
}

@Composable
fun UpgradeItemRow(
    title: String,
    description: String,
    currentLevel: Int,
    cost: Int,
    tint: Color,
    onBuy: () -> Unit,
    hasFunds: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tint
                )
                Text(
                    text = "LVL $currentLevel/10",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OnCyberSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar notches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(10) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (index < currentLevel) tint else Color.Gray.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentLevel < 10) {
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasFunds) tint else Color.Gray.copy(alpha = 0.2f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                    ),
                    enabled = hasFunds,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (hasFunds) Color.Black else Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESEARCH UNIT • $cost RP",
                        color = if (hasFunds) Color.Black else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.2f)),
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = CyberGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROTOTYPE MAXIMIZED",
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HowToPlayScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setScreen(GameState.MainMenu) },
                modifier = Modifier
                    .border(1.dp, CyberBlue.copy(alpha = 0.5f), CircleShape)
                    .background(CyberSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberBlue
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "COMMAND INSTRUCTIONS",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = CyberBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 BASIC RULES",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyberBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Robots enter from the left port and follow the shortest open path to the right base portal.\n\nPlace towers to construct a tactical maze, forcing robots to walk the longest possible path while taking maximum exposure damage.\n\n⚠️ YOU CANNOT FULLY BLOCK THE PATH. If a placement leaves no path to the exit, the system will prevent it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnCyberSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🚀 FLYING DEFENSE ALERT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyberOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Flying Jet enemies bypass your built-up maze corridors entirely, flying in a dead straight line from spawn port to exit!\n\nBuild Anti-Aircraft (SAM Flak) Towers or Rocket Launchers to handle these high-altitude robotic threats. Gun Towers cannot track flying entities.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnCyberSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔬 COGNITIVE LIFECYCLE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyberPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Each robot destroyed yields gold (used to build/upgrade towers in real-time) and permanent COMPILER CHIP Reward Points (RP).\n\nUse your collected points inside the Main Menu Research Shop to purchase permanent multipliers for damage, health, and starting gold allocations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnCyberSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Game Over",
            modifier = Modifier
                .size(80.dp)
                .border(2.dp, MaterialTheme.colorScheme.error, CircleShape)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CRITICAL CORES OFFLINE",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = "MAINFRAME OVERLOADED & COMPROMISED",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FINAL SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberBlue
                )
                Text(
                    text = "${viewModel.score}",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = CyberBlue.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SURVIVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberBlue.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "WAVE ${viewModel.currentWave}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EARNED",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberBlue.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "+${15 * viewModel.currentWave} RP",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CyberPurple
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.setScreen(GameState.Playing) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("restart_game_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REBOOT BLUEPRINT",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = { viewModel.setScreen(GameState.MainMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, CyberBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = CyberBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXIT TO HOME",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PlayingScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val towersState by viewModel.towers.collectAsStateWithLifecycle()
    val enemiesState by viewModel.enemies.collectAsStateWithLifecycle()
    val projectilesState by viewModel.projectiles.collectAsStateWithLifecycle()
    val particlesState by viewModel.particles.collectAsStateWithLifecycle()
    val explosionsState by viewModel.explosions.collectAsStateWithLifecycle()

    val infiniteBgTransition = rememberInfiniteTransition(label = "BackgroundParticles")
    val animBgPhase by infiniteBgTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgPhase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(CyberDeepBg)
    ) {
        // TOP META-BAR (Immersive UI theme redesign)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SECTOR 01",
                    color = CyberBlue.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "WAVE ${viewModel.currentWave}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                    // Pulsing incoming dot indicator
                    val infiniteDotTransition = rememberInfiniteTransition(label = "dot_pulse")
                    val dotAlpha by infiniteDotTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotAlpha"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (viewModel.isWaveActive) Color.Red.copy(alpha = dotAlpha)
                                    else CyberBlue.copy(alpha = dotAlpha)
                                )
                        )
                        Text(
                            text = if (viewModel.isWaveActive) "INCOMING" else if (viewModel.autoStartSecondsLeft != null) "AUTOSTART IN ${viewModel.autoStartSecondsLeft}s" else "STANDBY",
                            color = if (viewModel.isWaveActive) Color.Red else if (viewModel.autoStartSecondsLeft != null) CyberOrange else CyberBlue.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Text(
                    text = "${viewModel.score} PTS",
                    color = CyberBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Resources Tray Container (Immersive style)
            Row(
                modifier = Modifier
                    .background(CyberSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lives
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HEALTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Lives",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${viewModel.lives}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // Scrap Cash
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SCRAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Gold",
                            tint = ImmersiveGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${viewModel.gold}",
                            color = ImmersiveGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Speed Selector + Options Menu
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { viewModel.toggleSpeed() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.gameSpeed > 1) CyberOrange else CyberSurface,
                        contentColor = if (viewModel.gameSpeed > 1) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .height(32.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${viewModel.gameSpeed}X",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.setScreen(GameState.MainMenu) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(CyberSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // BATTLEFIELD CANVAS (Dynamic ratio adapted to rotated grid dimension)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .aspectRatio(viewModel.gridWidth.toFloat() / viewModel.gridHeight.toFloat())
                .background(viewModel.selectedMapSector.bgColor)
                .testTag("game_board")
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            val gridW = viewModel.gridWidth
            val gridH = viewModel.gridHeight

            val cellW = widthPx / gridW
            val cellH = heightPx / gridH

            // High performance game rendering
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val clickedCol = (offset.x / cellW).toInt().coerceIn(0, gridW - 1)
                            val clickedRow = (offset.y / cellH).toInt().coerceIn(0, gridH - 1)
                            viewModel.selectCell(clickedCol, clickedRow)
                        }
                    }
            ) {
                // Draw sector-specific animated decorations before grid overlay so they are behind elements
                drawDecorations(viewModel.selectedMapSector, animBgPhase, cellW, cellH, gridW, gridH)

                // A. Grid Background Overlay & Corner Dot matrices
                for (x in 1 until gridW) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(x * cellW, 0f),
                        end = Offset(x * cellW, heightPx),
                        strokeWidth = 0.8f.dp.toPx()
                    )
                }
                for (y in 1 until gridH) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(0f, y * cellH),
                        end = Offset(widthPx, y * cellH),
                        strokeWidth = 0.8f.dp.toPx()
                    )
                }
                // High-fidelity glowing intersections
                for (x in 1 until gridW) {
                    for (y in 1 until gridH) {
                        drawCircle(
                            color = viewModel.selectedMapSector.gridDotColor.copy(alpha = 0.25f),
                            radius = 1.2f.dp.toPx(),
                            center = Offset(x * cellW, y * cellH)
                        )
                    }
                }

                // B. Dash Shortest path visualization
                val path = viewModel.currentShortestPath
                if (path.size > 1) {
                    for (i in 0 until path.size - 1) {
                        val n1 = path[i]
                        val n2 = path[i + 1]
                        drawLine(
                            color = viewModel.selectedMapSector.pathColor.copy(alpha = 0.35f),
                            start = Offset((n1.x + 0.5f) * cellW, (n1.y + 0.5f) * cellH),
                            end = Offset((n2.x + 0.5f) * cellW, (n2.y + 0.5f) * cellH),
                            strokeWidth = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    }
                }

                // C. Entrances and Exits Neon Portals
                drawCircle(
                    color = viewModel.selectedMapSector.portalColor.copy(alpha = 0.4f),
                    radius = cellW * 0.42f,
                    center = Offset((viewModel.spawnPoint.x + 0.5f) * cellW, (viewModel.spawnPoint.y + 0.5f) * cellH),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = viewModel.selectedMapSector.portalColor.copy(alpha = 0.4f),
                    radius = cellW * 0.42f,
                    center = Offset((viewModel.exitPoint.x + 0.5f) * cellW, (viewModel.exitPoint.y + 0.5f) * cellH),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw Core Labels
                // Entry
                drawCircle(
                    color = viewModel.selectedMapSector.portalColor,
                    radius = 4.dp.toPx(),
                    center = Offset((viewModel.spawnPoint.x + 0.5f) * cellW, (viewModel.spawnPoint.y + 0.5f) * cellH)
                )
                // Exit
                drawCircle(
                    color = viewModel.selectedMapSector.portalColor,
                    radius = 4.dp.toPx(),
                    center = Offset((viewModel.exitPoint.x + 0.5f) * cellW, (viewModel.exitPoint.y + 0.5f) * cellH)
                )

                // D. Draw selection box highlight & Tower Range rings
                val sel = viewModel.selectedCell
                if (sel != null) {
                    drawRect(
                        color = CyberBlue.copy(alpha = 0.25f),
                        topLeft = Offset(sel.x * cellW, sel.y * cellH),
                        size = Size(cellW, cellH)
                    )
                    drawRect(
                        color = CyberBlue,
                        topLeft = Offset(sel.x * cellW, sel.y * cellH),
                        size = Size(cellW, cellH),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Draw Range Ring of selected cell or tower
                    val activeTower = viewModel.selectedTower
                    if (activeTower != null) {
                        drawCircle(
                            color = CyberBlue.copy(alpha = 0.15f),
                            radius = activeTower.range * cellW,
                            center = Offset((activeTower.x + 0.5f) * cellW, (activeTower.y + 0.5f) * cellH)
                        )
                        drawCircle(
                            color = CyberBlue.copy(alpha = 0.35f),
                            radius = activeTower.range * cellW,
                            center = Offset((activeTower.x + 0.5f) * cellW, (activeTower.y + 0.5f) * cellH),
                            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )
                    }
                }

                // E. Draw Tower objects
                for (tower in towersState) {
                    val tx = (tower.x + 0.5f) * cellW
                    val ty = (tower.y + 0.5f) * cellH
                    val rad = cellW * 0.38f

                    // Base chassis
                    drawCircle(
                        color = CyberSurface,
                        radius = rad,
                        center = Offset(tx, ty)
                    )
                    drawCircle(
                        color = when (tower.type) {
                            TowerType.Gun -> CyberBlue
                            TowerType.Rocket -> CyberOrange
                            TowerType.AntiAir -> CyberPurple
                        }.copy(alpha = 0.5f),
                        radius = rad,
                        center = Offset(tx, ty),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Gun Turret turning / barrel
                    // Determine barrel angle pointing to closest candidate enemy
                    val targetEnemy = enemiesState.find { enemy ->
                        val isAA = tower.type == TowerType.AntiAir
                        val isGun = tower.type == TowerType.Gun
                        val typeMatch = when {
                            isAA -> enemy.isAir
                            isGun -> !enemy.isAir
                            else -> true
                        }
                        if (typeMatch) {
                            val dx = enemy.x - tower.x
                            val dy = enemy.y - tower.y
                            (dx * dx + dy * dy) <= (tower.range * tower.range)
                        } else false
                    }

                    val angle = if (targetEnemy != null) {
                        atan2((targetEnemy.y - tower.y).toDouble(), (targetEnemy.x - tower.x).toDouble()).toFloat()
                    } else {
                        0.0f
                    }

                    // Rotating nozzle
                    val length = rad * 1.1f
                    val barrelX = tx + length * cos(angle)
                    val barrelY = ty + length * sin(angle)
                    drawLine(
                        color = Color.White,
                        start = Offset(tx, ty),
                        end = Offset(barrelX, barrelY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Inner design Core
                    drawCircle(
                        color = when (tower.type) {
                            TowerType.Gun -> CyberBlue
                            TowerType.Rocket -> CyberOrange
                            TowerType.AntiAir -> CyberPurple
                        },
                        radius = rad * 0.35f,
                        center = Offset(tx, ty)
                    )

                    // Tier Stars
                    repeat(tower.tier) { idx ->
                        val stx = tx + (idx - (tower.tier - 1) * 0.5f) * 8.dp.toPx()
                        val sty = ty - rad * 0.6f
                        drawCircle(
                            color = Color.Yellow,
                            radius = 2.dp.toPx(),
                            center = Offset(stx, sty)
                        )
                    }
                }

                // F. Draw Enemy Robots
                for (enemy in enemiesState) {
                    val ex = enemy.x * cellW + cellW * 0.5f
                    val ey = enemy.y * cellH + cellH * 0.5f
                    val sizePx = cellW * 0.30f

                    // Color based on Robot classes
                    val col = when (enemy.type) {
                        EnemyType.Scout -> CyberBlue // Cyan Drones
                        EnemyType.Soldier -> CyberPurple // Magenta Solders
                        EnemyType.Tank -> CyberOrange // Orange Goliaths
                        EnemyType.Jet -> Color.Yellow // Yellow Wing jets
                    }

                    // Draw body shape
                    if (enemy.isAir) {
                        // Wing design
                        val pathObj = Path().apply {
                            moveTo(ex, ey - sizePx * 1.2f)
                            lineTo(ex - sizePx, ey + sizePx)
                            lineTo(ex, ey + sizePx * 0.4f)
                            lineTo(ex + sizePx, ey + sizePx)
                            close()
                        }
                        drawPath(path = pathObj, color = col)
                        drawPath(path = pathObj, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))
                    } else {
                        // Ground Hex or Octagonal robotic tank
                        drawCircle(
                            color = col,
                            radius = sizePx,
                            center = Offset(ex, ey)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = sizePx * 0.45f,
                            center = Offset(ex, ey)
                        )
                    }

                    // Mini Core HP Health bars
                    val barW = cellW * 0.6f
                    val barH = 4.dp.toPx()
                    val barX = ex - barW / 2
                    val barY = ey - sizePx - 8.dp.toPx()

                    drawRect(
                        color = Color.Gray.copy(alpha = 0.3f),
                        topLeft = Offset(barX, barY),
                        size = Size(barW, barH)
                    )

                    val hpRatio = (enemy.health / enemy.maxHealth).coerceIn(0.0f, 1.0f)
                    drawRect(
                        color = if (hpRatio > 0.5f) CyberGreen else if (hpRatio > 0.2f) CyberOrange else Color.Red,
                        topLeft = Offset(barX, barY),
                        size = Size(barW * hpRatio, barH)
                    )
                }

                // G. Draw Moving Projectiles
                for (proj in projectilesState) {
                    val px = proj.x * cellW + cellW * 0.5f
                    val py = proj.y * cellH + cellH * 0.5f
                    val pColor = when (proj.type) {
                        ProjectileType.Bullet -> CyberBlue
                        ProjectileType.Rocket -> CyberOrange
                        ProjectileType.Flak -> CyberPurple
                    }

                    if (proj.type == ProjectileType.Rocket) {
                        // Rocket missile Capsule with flame tail backtrace
                        drawCircle(
                            color = pColor,
                            radius = 4.dp.toPx(),
                            center = Offset(px, py)
                        )
                        // Flame spark tail trail
                        drawCircle(
                            color = Color.Yellow,
                            radius = 2.dp.toPx(),
                            center = Offset(px - 10f * cos(proj.currentProgress), py - 10f * sin(proj.currentProgress))
                        )
                    } else {
                        drawCircle(
                            color = pColor,
                            radius = 3.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                // H. Draw Spikes and Spark Particles
                for (part in particlesState) {
                    val px = part.x * cellW + cellW * 0.5f
                    val py = part.y * cellH + cellH * 0.5f
                    val col = if (part.color == 0) CyberOrange else CyberBlue
                    val sizeR = 2.dp.toPx() * (1.0f - part.age.toFloat() / part.maxLifetime)

                    drawCircle(
                        color = col.copy(alpha = 1.0f - part.age.toFloat() / part.maxLifetime),
                        radius = sizeR.coerceAtLeast(0.5f),
                        center = Offset(px, py)
                    )
                }

                // I. Draw Flame Explosions rings
                for (exp in explosionsState) {
                    val ex = exp.x * cellW + cellW * 0.5f
                    val ey = exp.y * cellH + cellH * 0.5f
                    val rx = exp.radius * cellW

                    // Blazing orange explosion ring
                    drawCircle(
                        color = CyberOrange.copy(alpha = exp.alpha),
                        radius = rx,
                        center = Offset(ex, ey)
                    )
                    drawCircle(
                        color = Color.Yellow.copy(alpha = exp.alpha),
                        radius = rx * 0.6f,
                        center = Offset(ex, ey)
                    )
                }
            }
        }

        // WARNING OVERLAY FOR PATH BLOCKING
        AnimatedVisibility(
            visible = viewModel.blockingWarningMessage != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Red.copy(alpha = 0.2f))
                    .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.blockingWarningMessage ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // DOWN COMMAND CONSOLE (UPGRADES & CONSTRUCTIONS)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, CyberBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val cell = viewModel.selectedCell
                val tower = viewModel.selectedTower

                if (cell == null) {
                    // Welcome screen: Prompt build
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Selection instructions",
                            tint = CyberBlue.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SELECT BATTLEFIELD TILE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Tap any grid tile above to build or upgrade tactical guns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnCyberSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (tower == null) {
                    // EMPTY CELL Selected -> Buy Menu Options
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CONSTRUCT AT GRID [ ${cell.x} , ${cell.y} ]",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyberBlue,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Build Gun Tower
                            BuildOptionButton(
                                type = TowerType.Gun,
                                currentGold = viewModel.gold,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = { viewModel.buildTower(TowerType.Gun) }
                            )

                            // Build Rocket Tower
                            BuildOptionButton(
                                type = TowerType.Rocket,
                                currentGold = viewModel.gold,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = { viewModel.buildTower(TowerType.Rocket) }
                            )

                            // Build Flak SAM
                            BuildOptionButton(
                                type = TowerType.AntiAir,
                                currentGold = viewModel.gold,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = { viewModel.buildTower(TowerType.AntiAir) }
                            )
                        }
                    }
                } else {
                    // ACTIVE TOWER Selected -> Upgrade and Sell Menu
                    val upgradeCost = (tower.type.baseCost * 0.8 * tower.tier).toInt()
                    val refund = (tower.type.baseCost * 0.7 * tower.tier).toInt()
                    val nextUpgradeName = when (tower.type) {
                        TowerType.Gun -> if (tower.tier == 1) "Machine Gun" else "Plasma Gatling"
                        TowerType.Rocket -> if (tower.tier == 1) "Guided Missile" else "Devastator MIRV"
                        TowerType.AntiAir -> if (tower.tier == 1) "Heavy Flak Storm" else "SAM Decimator"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tower.type.label} • TIER ${tower.tier}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (tower.type) {
                                        TowerType.Gun -> CyberBlue
                                        TowerType.Rocket -> CyberOrange
                                        TowerType.AntiAir -> CyberPurple
                                    }
                                )

                                IconButton(onClick = { viewModel.selectedCell = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Deselect",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (tower.upgradeName != null) {
                                Text(
                                    text = "MOD: ${tower.upgradeName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Yellow,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // Stats specification
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "RANGE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnCyberSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = String.format("%.1f Tiles", tower.range),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = "DAMAGE/SHOT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnCyberSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = String.format("%.0f DMG", tower.damage),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = "RELOAD CYCLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnCyberSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = String.format("%.2fs", tower.cooldown),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Real-time Actions inside Console
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Upgrade turret Button
                            Button(
                                onClick = { viewModel.upgradeTower(tower) },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                                    .testTag("upgrade_tower_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberBlue,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                                ),
                                enabled = tower.tier < 3 && viewModel.gold >= upgradeCost,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = if (tower.tier < 3 && viewModel.gold >= upgradeCost) Color.Black else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (tower.tier < 3) "UPGRADE TO $nextUpgradeName • $$upgradeCost" else "MAX TIER REACHED",
                                    fontSize = 11.sp,
                                    color = if (tower.tier < 3 && viewModel.gold >= upgradeCost) Color.Black else Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Refund/Sell Turret Button
                            Button(
                                onClick = { viewModel.sellTower(tower) },
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(44.dp)
                                    .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .testTag("sell_tower_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SELL • +$$refund",
                                    fontSize = 11.sp,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // WAVE TRIGGER FAB AT THE VERY BOTTOM ROW
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.startNextWave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("next_wave_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isWaveActive) CyberSurface else if (viewModel.autoStartSecondsLeft != null) CyberOrange else CyberPurple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !viewModel.isWaveActive
                ) {
                    if (viewModel.isWaveActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WAVE INFILTRATION SURGE DETECTED",
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Wave",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (viewModel.currentWave == 0) "DEPLOY WAVE 1 SEQUENCE" else if (viewModel.autoStartSecondsLeft != null) "AUTOSTART SEQUENCE IN ${viewModel.autoStartSecondsLeft}S" else "LAUNCH NEXT WAVE SEQUENCE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM DIAGNOSTIC UTILITY BAR (From Immersive UI theme)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Diagnostics",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reboot",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "SYSTEMS_STABLE: 100%   //   GRID_INTEGRITY: OK",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
fun BuildOptionButton(
    type: TowerType,
    currentGold: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val affordable = currentGold >= type.baseCost
    val colorAccent = when (type) {
        TowerType.Gun -> CyberBlue
        TowerType.Rocket -> CyberOrange
        TowerType.AntiAir -> CyberPurple
    }

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (affordable) colorAccent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = affordable, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (affordable) CyberSurface else Color.White.copy(alpha = 0.02f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Glow badge box for the action icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (affordable) colorAccent.copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            TowerType.Gun -> Icons.Default.Star
                            TowerType.Rocket -> Icons.Default.Send
                            TowerType.AntiAir -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = if (affordable) colorAccent else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = type.label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (affordable) Color.White else Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = type.description,
                    fontSize = 7.5.sp,
                    lineHeight = 9.sp,
                    color = OnCyberSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = "$${type.baseCost}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (affordable) ImmersiveGold else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun DrawScope.drawDecorations(
    sector: MapSector,
    phase: Float,
    cellW: Float,
    cellH: Float,
    gridWidth: Int,
    gridHeight: Int
) {
    val widthPx = size.width
    val heightPx = size.height

    when (sector) {
        MapSector.Classic -> {
            // Cyber Grid - Holographic rings and cyber tracks
            drawCircle(
                color = sector.portalColor.copy(alpha = 0.08f),
                radius = cellW * 3.5f + (phase * 1.5f) % (cellW * 2f),
                center = Offset(widthPx * 0.3f, heightPx * 0.5f),
                style = Stroke(width = 1.5f.dp.toPx())
            )
            drawCircle(
                color = sector.gridDotColor.copy(alpha = 0.08f),
                radius = cellW * 4.5f - (phase * 1.0f) % (cellW * 1.5f),
                center = Offset(widthPx * 0.7f, heightPx * 0.4f),
                style = Stroke(width = 1f.dp.toPx())
            )
            val lineAlpha = 0.12f
            drawLine(
                color = sector.pathColor.copy(alpha = lineAlpha),
                start = Offset(cellW * 1.5f, cellH * 2.5f),
                end = Offset(cellW * 4.5f, cellH * 2.5f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = sector.pathColor.copy(alpha = lineAlpha),
                start = Offset(cellW * 4.5f, cellH * 2.5f),
                end = Offset(cellW * 5.5f, cellH * 5.5f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = sector.pathColor.copy(alpha = lineAlpha),
                start = Offset(cellW * 7.5f, cellH * 6.5f),
                end = Offset(cellW * 10.5f, cellH * 6.5f),
                strokeWidth = 2.dp.toPx()
            )
        }
        MapSector.Winter -> {
            // Frostbite Tundra - overlapping snow mounds at bottom + snowflakes
            val moundPath = Path().apply {
                moveTo(0f, heightPx)
                quadraticTo(widthPx * 0.25f, heightPx - cellH * 1.2f, widthPx * 0.5f, heightPx - cellH * 0.6f)
                quadraticTo(widthPx * 0.75f, heightPx - cellH * 1.4f, widthPx, heightPx - cellH * 0.4f)
                lineTo(widthPx, heightPx)
                lineTo(0f, heightPx)
                close()
            }
            drawPath(path = moundPath, color = Color(0x1AFFFFFF))

            val snowflakeCount = 18
            for (i in 0 until snowflakeCount) {
                val seedX = (i * 123.4f) % widthPx
                val seedY = (i * 567.8f) % heightPx
                val speedY = 15f + (i * 3.1f) % 20f
                val speedX = 5f + (i * 1.3f) % 10f
                val sizeVal = 2f.dp.toPx() + (i % 3) * 1.5f.dp.toPx()

                val yPos = (seedY + phase * speedY) % heightPx
                val xPos = (seedX + phase * speedX) % widthPx

                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = sizeVal,
                    center = Offset(xPos, yPos)
                )
            }
        }
        MapSector.Coastal -> {
            // Neon Reef - marine bubbles float upwards + coral structures
            val bubbleCount = 18
            for (i in 0 until bubbleCount) {
                val seedX = (i * 197.3f) % widthPx
                val seedY = (i * 311.9f) % heightPx
                val bubbleSpeed = 10f + (i * 3.5f) % 15f
                val radiusVal = 3f.dp.toPx() + (i % 4) * 2.5f.dp.toPx()

                val yPos = heightPx - ((seedY + phase * bubbleSpeed) % heightPx)
                val wobbleX = seedX + sin((phase * 0.2f) + i) * 12.dp.toPx()

                drawCircle(
                    color = sector.pathColor.copy(alpha = 0.12f),
                    radius = radiusVal,
                    center = Offset(wobbleX, yPos)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.20f),
                    radius = radiusVal,
                    center = Offset(wobbleX, yPos),
                    style = Stroke(width = 0.6f.dp.toPx())
                )
            }
            val coralBrush = Brush.linearGradient(
                colors = listOf(Color(0xFFFF4081).copy(alpha = 0.20f), sector.gridDotColor.copy(alpha = 0.10f))
            )
            drawRect(
                brush = coralBrush,
                topLeft = Offset(0f, heightPx - cellH * 0.5f),
                size = Size(widthPx, cellH * 0.5f)
            )
        }
        MapSector.Desert -> {
            // Scorched Dunes - Sandstorms & static Cacti
            val whirlCount = 5
            for (i in 0 until whirlCount) {
                val seedX = (i * 415.7f) % widthPx
                val seedY = (i * 229.1f) % heightPx
                val radius = cellW * (1f + (i % 3) * 0.5f)
                val angle = (phase * (0.8f + i * 0.1f)) % 360f

                drawArc(
                    color = sector.gridDotColor.copy(alpha = 0.08f),
                    startAngle = angle,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(seedX - radius, seedY - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            val cactiPositions = listOf(
                Offset(cellW * 1.5f, cellH * 7.5f),
                Offset(cellW * 9.5f, cellH * 1.5f),
                Offset(cellW * 10.2f, cellH * 6.8f)
            )
            cactiPositions.forEach { pos ->
                drawRect(
                    color = sector.gridDotColor.copy(alpha = 0.15f),
                    topLeft = Offset(pos.x - 4.dp.toPx(), pos.y - 16.dp.toPx()),
                    size = Size(8.dp.toPx(), 24.dp.toPx())
                )
                drawLine(
                    color = sector.gridDotColor.copy(alpha = 0.15f),
                    start = Offset(pos.x - 4.dp.toPx(), pos.y - 8.dp.toPx()),
                    end = Offset(pos.x - 12.dp.toPx(), pos.y - 8.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
                drawLine(
                    color = sector.gridDotColor.copy(alpha = 0.15f),
                    start = Offset(pos.x - 12.dp.toPx(), pos.y - 8.dp.toPx()),
                    end = Offset(pos.x - 12.dp.toPx(), pos.y - 14.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
        MapSector.Forest -> {
            // Overgrown Core - mossy mounds and floating leaves
            val mossPath = Path().apply {
                moveTo(0f, heightPx)
                quadraticTo(widthPx * 0.3f, heightPx - cellH * 0.8f, widthPx * 0.6f, heightPx - cellH * 0.4f)
                quadraticTo(widthPx * 0.85f, heightPx - cellH * 1.2f, widthPx, heightPx - cellH * 0.3f)
                lineTo(widthPx, heightPx)
                lineTo(0f, heightPx)
                close()
            }
            drawPath(path = mossPath, color = sector.gridDotColor.copy(alpha = 0.12f))

            val leafCount = 14
            for (i in 0 until leafCount) {
                val seedX = (i * 245.1f) % widthPx
                val seedY = (i * 189.7f) % heightPx
                val fallSpeed = 8f + (i * 2.5f) % 10f
                val swaySpeed = 0.5f + (i * 0.2f) % 1f

                val yPos = (seedY + phase * fallSpeed) % heightPx
                val xPos = (seedX + sin(phase * swaySpeed) * 15.dp.toPx()) % widthPx

                val leafPath = Path().apply {
                    moveTo(xPos, yPos)
                    quadraticTo(xPos + 6.dp.toPx(), yPos - 3.dp.toPx(), xPos + 12.dp.toPx(), yPos)
                    quadraticTo(xPos + 6.dp.toPx(), yPos + 3.dp.toPx(), xPos, yPos)
                    close()
                }
                drawPath(leafPath, color = sector.portalColor.copy(alpha = 0.20f))
            }
        }
        MapSector.Lava -> {
            // Lava Crucible - Magma bubbles bursting + smoke vents
            val bubbleCount = 7
            for (i in 0 until bubbleCount) {
                val seedX = (i * 383.1f) % widthPx
                val seedY = (i * 157.9f) % heightPx
                val bubblePhase = (phase * (0.4f + i * 0.1f) + i * 15f) % 100f
                val bProgress = bubblePhase / 100f

                val maxRad = cellW * 1.6f
                val curRad = bProgress * maxRad
                val bubbleAlpha = (1.0f - bProgress) * 0.18f

                drawCircle(
                    color = sector.portalColor.copy(alpha = bubbleAlpha),
                    radius = curRad,
                    center = Offset(seedX, seedY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            val ashCount = 12
            for (i in 0 until ashCount) {
                val seedX = (i * 291.5f) % widthPx
                val seedY = (i * 377.3f) % heightPx
                val riseSpeed = 15f + (i * 3f) % 15f

                val yPos = heightPx - ((seedY + phase * riseSpeed) % heightPx)
                val ashAlpha = (yPos / heightPx) * 0.18f

                drawCircle(
                    color = Color.DarkGray.copy(alpha = ashAlpha),
                    radius = 3.dp.toPx() + (i % 3) * 1.5f.dp.toPx(),
                    center = Offset(seedX + sin(phase * 0.5f + i) * 8.dp.toPx(), yPos)
                )
            }
        }
        MapSector.Garden -> {
            // Zen Botanical - drifting cherry petals, concentric stones
            val stonePositions = listOf(
                Offset(cellW * 2.5f, cellH * 2.2f),
                Offset(cellW * 9.5f, cellH * 6.8f)
            )
            stonePositions.forEach { pos ->
                for (r in 1..4) {
                    drawCircle(
                        color = sector.gridDotColor.copy(alpha = 0.08f),
                        radius = cellW * 0.4f * r,
                        center = pos,
                        style = Stroke(width = 0.8f.dp.toPx())
                    )
                }
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.20f),
                    radius = 8.dp.toPx(),
                    center = pos
                )
            }
            val petalCount = 20
            for (i in 0 until petalCount) {
                val seedX = (i * 153.9f) % widthPx
                val seedY = (i * 287.1f) % heightPx
                val speedY = 7f + (i * 1.5f) % 7f
                val speedX = 5f + (i * 1.1f) % 5f

                val yPos = (seedY + phase * speedY) % heightPx
                val xPos = (seedX - phase * speedX) % widthPx

                drawCircle(
                    color = sector.gridDotColor.copy(alpha = 0.30f),
                    radius = 3.dp.toPx() + (i % 3),
                    center = Offset(xPos, yPos)
                )
            }
        }
        MapSector.Highway -> {
            // Cyber Highway - light lanes zooming past, hazard warnings
            val lineY = listOf(
                cellH * 1.5f, cellH * 3.8f, cellH * 5.2f, cellH * 7.5f
            )
            lineY.forEachIndexed { idx, yVal ->
                val dir = if (idx % 2 == 0) 1f else -1f
                val offset = (phase * 60f * dir) % 180f
                drawLine(
                    color = sector.pathColor.copy(alpha = 0.10f),
                    start = Offset(0f, yVal),
                    end = Offset(widthPx, yVal),
                    strokeWidth = 2.5f.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 60f), offset)
                )
            }
            drawChevronCorner(sector.gridDotColor.copy(alpha = 0.12f), 10.dp.toPx())
        }
        MapSector.River -> {
            // Digital Torrent - overlapping electric wave flowing lines
            val waveCount = 5
            for (w in 0 until waveCount) {
                val waveY = cellH * (2f + w * 1.2f)
                val wavePath = Path().apply {
                    val startY = waveY + sin(phase * 0.1f + w) * 12.dp.toPx()
                    moveTo(0f, startY)
                    for (x in 0..gridWidth) {
                        val screenX = x * cellW
                        val angle = (x.toFloat() / gridWidth.toFloat()) * 2f * 3.14159f + (phase * 0.05f) + w.toFloat()
                        val screenY = waveY + sin(angle) * 15.dp.toPx()
                        lineTo(screenX, screenY)
                    }
                }
                drawPath(
                    path = wavePath,
                    color = sector.portalColor.copy(alpha = 0.10f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        MapSector.Ruins -> {
            // Ancient Tech - Broken stone pillars + scattered shards
            val pillarSpots = listOf(
                Offset(cellW * 2.2f, cellH * 1.8f),
                Offset(cellW * 6.8f, cellH * 7.2f),
                Offset(cellW * 9.8f, cellH * 2.8f)
            )
            val mossGreenColors = sector.gridDotColor.copy(alpha = 0.20f)
            pillarSpots.forEach { spot ->
                drawRect(
                    color = mossGreenColors,
                    topLeft = Offset(spot.x - 14.dp.toPx(), spot.y - 10.dp.toPx()),
                    size = Size(28.dp.toPx(), 40.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawRect(
                    color = mossGreenColors.copy(alpha = 0.10f),
                    topLeft = Offset(spot.x - 10.dp.toPx(), spot.y - 6.dp.toPx()),
                    size = Size(20.dp.toPx(), 32.dp.toPx())
                )
                drawLine(
                    color = mossGreenColors,
                    start = Offset(spot.x - 14.dp.toPx(), spot.y - 10.dp.toPx()),
                    end = Offset(spot.x + 14.dp.toPx(), spot.y + 30.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        MapSector.StoneBrickRoad -> {
            // Cobble Gridway - Cracked stone outlines + glowing lanterns
            for (x in 0 until gridWidth step 2) {
                for (y in 0 until gridHeight step 2) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.03f),
                        topLeft = Offset(x * cellW + 2.dp.toPx(), y * cellH + 2.dp.toPx()),
                        size = Size(cellW * 2f - 4.dp.toPx(), cellH * 2f - 4.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            val lanternSpots = listOf(
                Offset(cellW * 0.8f, cellH * 2.2f),
                Offset(cellW * 11.2f, cellH * 6.8f),
                Offset(cellW * 5.5f, cellH * 1.2f)
            )
            val pulseGlow = 0.08f + sin(phase * 0.1f) * 0.03f
            lanternSpots.forEach { spot ->
                drawCircle(
                    color = sector.portalColor.copy(alpha = pulseGlow),
                    radius = cellW * 0.6f,
                    center = spot
                )
                drawCircle(
                    color = sector.portalColor,
                    radius = 4.dp.toPx(),
                    center = spot
                )
            }
        }
        MapSector.Valley -> {
            // Echo Valley - alpine crests and fog drifting
            val peakPath = Path().apply {
                moveTo(0f, heightPx)
                lineTo(cellW * 1.5f, heightPx - cellH * 1.8f)
                lineTo(cellW * 3.5f, heightPx - cellH * 0.4f)
                lineTo(cellW * 5.5f, heightPx - cellH * 2.4f)
                lineTo(cellW * 7.5f, heightPx - cellH * 0.8f)
                lineTo(cellW * 10f, heightPx - cellH * 2.0f)
                lineTo(widthPx, heightPx)
                close()
            }
            drawPath(path = peakPath, color = sector.gridDotColor.copy(alpha = 0.10f))

            val fogCount = 4
            for (f in 0 until fogCount) {
                val seedY = cellH * (2f + f * 1.5f)
                val scrollOffset = (phase * (5f + f * 2f)) % widthPx
                drawOval(
                    color = sector.portalColor.copy(alpha = 0.06f),
                    topLeft = Offset(scrollOffset - cellW * 3f, seedY),
                    size = Size(cellW * 6f, cellH * 0.9f)
                )
                drawOval(
                    color = sector.portalColor.copy(alpha = 0.06f),
                    topLeft = Offset(scrollOffset - cellW * 3f + widthPx, seedY),
                    size = Size(cellW * 6f, cellH * 0.9f)
                )
            }
        }
    }
}

private fun DrawScope.drawChevronCorner(color: Color, chevronSize: Float) {
    val h = size.height
    val wd = size.width
    val stroke = 2.dp.toPx()
    val w = chevronSize
    // Top-left
    drawLine(color, Offset(4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx() + w, 4.dp.toPx()), strokeWidth = stroke)
    drawLine(color, Offset(4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx(), 4.dp.toPx() + w), strokeWidth = stroke)
    // Bottom-left
    drawLine(color, Offset(4.dp.toPx(), h - 4.dp.toPx()), Offset(4.dp.toPx() + w, h - 4.dp.toPx()), strokeWidth = stroke)
    drawLine(color, Offset(4.dp.toPx(), h - 4.dp.toPx()), Offset(4.dp.toPx(), h - 4.dp.toPx() - w), strokeWidth = stroke)
    // Top-right
    drawLine(color, Offset(wd - 4.dp.toPx(), 4.dp.toPx()), Offset(wd - 4.dp.toPx() - w, 4.dp.toPx()), strokeWidth = stroke)
    drawLine(color, Offset(wd - 4.dp.toPx(), 4.dp.toPx()), Offset(wd - 4.dp.toPx(), 4.dp.toPx() + w), strokeWidth = stroke)
    // Bottom-right
    drawLine(color, Offset(wd - 4.dp.toPx(), h - 4.dp.toPx()), Offset(wd - 4.dp.toPx() - w, h - 4.dp.toPx()), strokeWidth = stroke)
    drawLine(color, Offset(wd - 4.dp.toPx(), h - 4.dp.toPx()), Offset(wd - 4.dp.toPx(), h - 4.dp.toPx() - w), strokeWidth = stroke)
}

@Composable
fun GamerProfileCard(
    viewModel: GameViewModel,
    profile: com.example.data.PlayerProfile,
    onShowAuth: () -> Unit,
    onShowEditTag: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isLoggedIn = profile.googleEmail != null
            if (isLoggedIn) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyberBlue.copy(alpha = 0.15f))
                            .border(1.5.dp, CyberBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile.googleDisplayName?.firstOrNull()?.toString() ?: "D").uppercase(),
                            color = CyberBlue,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.gameTagName ?: "Def_Defender",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Gamer Tag",
                                tint = CyberBlue,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onShowEditTag() }
                            )
                        }
                        Text(
                            text = profile.googleEmail ?: "",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CyberGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GOOGLE CONNECTED",
                                color = CyberGreen,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { viewModel.signOutOfGoogle() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("LOGOUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Guest Avatar",
                            tint = Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OFFLINE COGNITIVE UNIT",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Authenticate matrices for cloud high-scores.",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onShowAuth() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("SIGN IN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen(viewModel: GameViewModel, profile: com.example.data.PlayerProfile) {
    val leaderboardEntries by viewModel.leaderboard.collectAsStateWithLifecycle()
    val isSyncing = viewModel.isSyncingLeaderboard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setScreen(GameState.MainMenu) },
                modifier = Modifier
                    .border(1.dp, CyberBlue.copy(alpha = 0.5f), CircleShape)
                    .background(CyberSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberBlue
                )
            }

            Text(
                text = "CYBER CORE BOARD",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                color = CyberBlue
            )

            IconButton(
                onClick = { viewModel.syncLeaderboardFromServer() },
                modifier = Modifier
                    .border(1.dp, CyberGreen.copy(alpha = 0.5f), CircleShape)
                    .background(CyberSurface)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        color = CyberGreen,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = CyberGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Global defense matrices real-time security scoreboards. Higher wave capacity maximizes reward point chips.",
            color = OnCyberSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Stats Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberPurple.copy(alpha = 0.15f))
                            .border(1.5.dp, CyberPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Personal Status",
                            tint = CyberPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.gameTagName ?: "Def_Defender (Offline)",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Your Highest Score Record",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Text(
                    text = "${profile.highScore} PTS",
                    color = CyberPurple,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DECENTRALIZED LEADERBOARD ENTRIES",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = CyberBlue.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Leaderboard List
        if (leaderboardEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(leaderboardEntries) { index, entry ->
                    val rank = index + 1
                    val isUser = entry.isCurrentUser || (entry.username == profile.gameTagName)
                    LeaderboardRowItem(rank = rank, entry = entry, isCurrentUser = isUser)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRowItem(rank: Int, entry: com.example.data.LeaderboardEntry, isCurrentUser: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentUser) 1.5.dp else 1.dp,
                color = if (isCurrentUser) CyberGreen else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) CyberGreen.copy(alpha = 0.08f) else CyberSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Rank Number / Medal
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> ImmersiveGold.copy(alpha = 0.2f)
                                2 -> Color.LightGray.copy(alpha = 0.2f)
                                3 -> CyberOrange.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.05f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        color = when (rank) {
                            1 -> ImmersiveGold
                            2 -> Color.White
                            3 -> CyberOrange
                            else -> Color.Gray
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.username,
                            color = if (isCurrentUser) CyberGreen else Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberGreen)
                            ) {
                                Text(
                                    text = "YOU",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Wave capacity achieved: ${entry.waveReached}  •  ${entry.dateString}",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = "${entry.score}",
                color = if (isCurrentUser) CyberGreen else CyberBlue,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}
