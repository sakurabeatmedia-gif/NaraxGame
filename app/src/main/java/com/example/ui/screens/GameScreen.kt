package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.GridPos
import com.example.model.BoosterType
import com.example.ui.GameUiState
import com.example.ui.components.BoosterBar
import com.example.ui.components.BrightBlueHeader
import com.example.ui.components.CandyBoard
import com.example.ui.components.CandyKingdomBackground
import com.example.ui.components.CandyTileCanvas
import com.example.ui.components.GameDialogsHost
import com.example.ui.components.TreasureChestIndicator
import com.example.ui.theme.CoinGoldLight
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight

@Composable
fun GameScreen(
    uiState: GameUiState,
    coinBalance: Long,
    onBackClick: () -> Unit,
    onSwipe: (GridPos, GridPos) -> Unit,
    onBoosterClick: (BoosterType) -> Unit,
    onBoosterTileTap: (GridPos) -> Unit,
    onContinueWithAd: () -> Unit,
    onQuitOutOfMoves: () -> Unit,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onBackToMenuFromGameOver: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onResetToLevelOne: () -> Unit,
    onDismissAdUnavailable: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(uiState.hapticPulseTick) {
        if (uiState.hapticPulseTick > 0 && uiState.vibrationEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    CandyKingdomBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bright Blue Header showing ONLY coin balance number + nav/settings
            BrightBlueHeader(
                coinBalance = coinBalance,
                onBackClick = onBackClick,
                onSettingsClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Game HUD: Level, Target Color, Moves Left, Treasure Chest 0/5..5/5
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level + Target Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SkyBlueDeep.copy(alpha = 0.88f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SkyBlueLight.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("hud_level_target")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.game_level_label, uiState.level),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SkyBlueLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${uiState.targetCollected}/${uiState.targetCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            CandyTileCanvas(
                                color = uiState.targetColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Moves Remaining Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SkyBlueDeep.copy(alpha = 0.88f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = if (uiState.movesLeft <= 3) Color(0xFFFCA5A5) else SkyBlueLight.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("hud_moves_left")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.game_moves_label),
                                style = MaterialTheme.typography.labelMedium.copy(color = SkyBlueLight)
                            )
                            Text(
                                text = uiState.movesLeft.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (uiState.movesLeft <= 3) Color(0xFFFCA5A5) else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Treasure Chest 0/5 .. 5/5
                    TreasureChestIndicator(chestCount = uiState.chestCount)
                }

                AnimatedVisibility(visible = uiState.activeBooster != null) {
                    val active = uiState.activeBooster
                    if (active != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF15803D).copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, CoinGoldLight, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${active.title}: ${active.hint}",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 7x7 Glossy Candy Board
            CandyBoard(
                board = uiState.board,
                invalidSwapPair = uiState.invalidSwapPair,
                explodingTiles = uiState.explodingTiles,
                fallRowDeltas = uiState.fallRowDeltas,
                activeBooster = uiState.activeBooster,
                isInputEnabled = !uiState.isAnimating && uiState.activeDialog == null,
                onSwipe = onSwipe,
                onBoosterTileTap = onBoosterTileTap
            )

            Spacer(modifier = Modifier.weight(1f))

            // 4 Boosters: Palu, Ember, Kereta, Kuas
            Box(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                BoosterBar(
                    boosters = uiState.boosters,
                    activeBooster = uiState.activeBooster,
                    enabled = !uiState.isAnimating && uiState.activeDialog == null,
                    onBoosterClick = onBoosterClick
                )
            }
        }

        GameDialogsHost(
            activeDialog = uiState.activeDialog,
            soundEnabled = uiState.soundEnabled,
            vibrationEnabled = uiState.vibrationEnabled,
            onContinueWithAd = onContinueWithAd,
            onQuitOutOfMoves = onQuitOutOfMoves,
            onNextLevel = onNextLevel,
            onRetryLevel = onRetryLevel,
            onBackToMenu = onBackToMenuFromGameOver,
            onDismissSettings = onDismissSettings,
            onToggleSound = onToggleSound,
            onToggleVibration = onToggleVibration,
            onResetToLevelOne = onResetToLevelOne,
            onDismissAdUnavailable = onDismissAdUnavailable
        )
    }
}
