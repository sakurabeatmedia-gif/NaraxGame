package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.GameDialog
import com.example.ui.theme.CoinGold
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted

@Composable
fun GameDialogsHost(
    activeDialog: GameDialog?,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onContinueWithAd: () -> Unit,
    onQuitOutOfMoves: () -> Unit,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onBackToMenu: () -> Unit,
    onDismissSettings: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onResetToLevelOne: () -> Unit,
    onDismissAdUnavailable: () -> Unit
) {
    when (activeDialog) {
        null -> Unit
        is GameDialog.OutOfMoves -> {
            PopupCardDialog(
                title = stringResource(R.string.out_of_moves_title),
                message = stringResource(R.string.out_of_moves_message),
                onDismissRequest = {}
            ) {
                GlossyGreenButton(
                    text = stringResource(R.string.btn_continue_ad),
                    icon = Icons.Filled.PlayArrow,
                    onClick = onContinueWithAd,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_continue_ad_button"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onQuitOutOfMoves,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorCoral,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dialog_quit_ad_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_quit_ad),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        is GameDialog.LevelUp -> {
            PopupCardDialog(
                title = stringResource(R.string.level_up_title),
                message = stringResource(R.string.level_up_message, activeDialog.nextLevel),
                badgeIcon = Icons.Filled.EmojiEvents,
                badgeColor = CoinGold,
                onDismissRequest = {}
            ) {
                GlossyGreenButton(
                    text = stringResource(R.string.btn_next_level),
                    icon = Icons.Filled.PlayArrow,
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_next_level_button"
                )
            }
        }

        is GameDialog.GameOver -> {
            PopupCardDialog(
                title = stringResource(R.string.game_over_title),
                message = stringResource(R.string.game_over_message),
                onDismissRequest = {}
            ) {
                GlossyGreenButton(
                    text = stringResource(R.string.btn_retry_level),
                    icon = Icons.Filled.Refresh,
                    onClick = onRetryLevel,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_retry_button"
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBackToMenu,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dialog_back_menu_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_back_menu),
                        style = MaterialTheme.typography.titleMedium.copy(color = SkyBlueDeep)
                    )
                }
            }
        }

        is GameDialog.Settings -> {
            PopupCardDialog(
                title = stringResource(R.string.settings_title),
                message = null,
                badgeIcon = Icons.Filled.Settings,
                badgeColor = SkyBlueBright,
                onDismissRequest = onDismissSettings
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_sound),
                        style = MaterialTheme.typography.titleMedium.copy(color = SlateInk)
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound,
                        modifier = Modifier.testTag("settings_sound_switch")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_vibration),
                        style = MaterialTheme.typography.titleMedium.copy(color = SlateInk)
                    )
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = onToggleVibration,
                        modifier = Modifier.testTag("settings_vibration_switch")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onResetToLevelOne,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_reset_button")
                ) {
                    Text(
                        text = stringResource(R.string.settings_reset_progress),
                        color = ErrorCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                GlossyGreenButton(
                    text = stringResource(R.string.btn_ok),
                    onClick = onDismissSettings,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_close_button"
                )
            }
        }

        is GameDialog.AdUnavailable -> {
            PopupCardDialog(
                title = stringResource(R.string.ad_unavailable_title),
                message = stringResource(R.string.ad_unavailable_message),
                badgeIcon = Icons.Filled.Info,
                badgeColor = SkyBlueBright,
                onDismissRequest = onDismissAdUnavailable
            ) {
                GlossyGreenButton(
                    text = stringResource(R.string.btn_ok),
                    onClick = onDismissAdUnavailable,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "ad_unavailable_ok_button"
                )
            }
        }
    }
}

@Composable
fun PopupCardDialog(
    title: String,
    message: String?,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    badgeColor: Color = SkyBlueBright,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = CreamSurface,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, SkyBlueBright, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (badgeIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(badgeColor, badgeColor.copy(alpha = 0.8f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = SkyBlueDeep,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge.copy(color = SlateMuted),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                content()
            }
        }
    }
}
