package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.GameDialog
import com.example.ui.GameUiState
import com.example.ui.components.BrightBlueHeader
import com.example.ui.components.CandyKingdomBackground
import com.example.ui.components.GameDialogsHost
import com.example.ui.components.GlossyGreenButton
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SlateInk

@Composable
fun MainMenuScreen(
    gameState: GameUiState,
    coinBalance: Long,
    onPlayClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onResetToLevelOne: () -> Unit
) {
    CandyKingdomBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            BrightBlueHeader(
                coinBalance = coinBalance,
                onSettingsClick = onOpenSettings
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hero Banner Artwork
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .shadow(12.dp, RoundedCornerShape(26.dp))
                            .clip(RoundedCornerShape(26.dp))
                            .border(3.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(26.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nara_hero),
                            contentDescription = "NARA-Game",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, SkyBlueDeep.copy(alpha = 0.88f))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SkyBlueLight
                                ) {
                                    Text(
                                        text = stringResource(R.string.game_level_label, gameState.level),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = SkyBlueDeep,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Glossy Green Play Button
                    GlossyGreenButton(
                        text = if (gameState.level > 1 || gameState.targetCollected > 0) {
                            "${stringResource(R.string.menu_continue)} ${gameState.level}"
                        } else {
                            stringResource(R.string.menu_play)
                        },
                        icon = Icons.Filled.PlayArrow,
                        onClick = onPlayClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        testTag = "menu_play_button"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuActionCard(
                            title = stringResource(R.string.menu_withdraw),
                            icon = Icons.Filled.AccountBalanceWallet,
                            onClick = onWithdrawClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_withdraw_button")
                        )
                        MenuActionCard(
                            title = stringResource(R.string.menu_history),
                            icon = Icons.Filled.History,
                            onClick = onHistoryClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_history_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuActionCard(
                        title = stringResource(R.string.menu_settings),
                        icon = Icons.Filled.Settings,
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("menu_settings_button")
                    )
                }
            }
        }

        if (gameState.activeDialog is GameDialog.Settings) {
            GameDialogsHost(
                activeDialog = gameState.activeDialog,
                soundEnabled = gameState.soundEnabled,
                vibrationEnabled = gameState.vibrationEnabled,
                onContinueWithAd = {},
                onQuitOutOfMoves = {},
                onNextLevel = {},
                onRetryLevel = {},
                onBackToMenu = {},
                onDismissSettings = onDismissSettings,
                onToggleSound = onToggleSound,
                onToggleVibration = onToggleVibration,
                onResetToLevelOne = onResetToLevelOne,
                onDismissAdUnavailable = {}
            )
        }
    }
}

@Composable
private fun MenuActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CreamSurface.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, SkyBlueBright.copy(alpha = 0.6f)),
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SkyBlueBright,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = SlateInk,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}
