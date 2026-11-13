package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CoinConversion
import com.example.model.PaymentMethod
import com.example.model.WithdrawCatalog
import com.example.model.WithdrawNominal
import com.example.ui.WithdrawUiState
import com.example.ui.components.BrightBlueHeader
import com.example.ui.components.CandyKingdomBackground
import com.example.ui.components.GlossyGreenButton
import com.example.ui.components.PopupCardDialog
import com.example.ui.theme.CandyGreenMid
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted

@Composable
fun WithdrawScreen(
    uiState: WithdrawUiState,
    onBackClick: () -> Unit,
    onSelectMethod: (PaymentMethod) -> Unit,
    onSelectNominal: (WithdrawNominal) -> Unit,
    onFullNameChange: (String) -> Unit,
    onDanaNumberChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    CandyKingdomBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrightBlueHeader(
                coinBalance = uiState.coinBalance,
                title = stringResource(R.string.withdraw_title),
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance & Rupiah Conversion Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CreamSurface.copy(alpha = 0.96f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(2.dp, SkyBlueBright)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.withdraw_coin_balance),
                                style = MaterialTheme.typography.bodyMedium.copy(color = SlateMuted)
                            )
                            Text(
                                text = CoinConversion.formatNumber(uiState.coinBalance),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = SkyBlueDeep,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("withdraw_coin_balance")
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.withdraw_rupiah_value),
                                style = MaterialTheme.typography.bodyMedium.copy(color = SlateMuted)
                            )
                            Text(
                                text = "Rp${CoinConversion.formatNumber(uiState.rupiahValue)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = CandyGreenMid,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("withdraw_rupiah_value")
                            )
                        }
                    }
                }

                // Payment Methods
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CreamSurface.copy(alpha = 0.96f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.withdraw_method_title),
                            style = MaterialTheme.typography.titleMedium.copy(color = SlateInk)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                val isSelected = uiState.selectedMethod == method
                                val containerColor = when {
                                    !method.isAvailable -> Color(0xFFF1F5F9)
                                    isSelected -> SkyBlueBright
                                    else -> Color(0xFFE0F2FE)
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = containerColor,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SkyBlueDeep else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(enabled = method.isAvailable) {
                                            onSelectMethod(method)
                                        }
                                        .testTag("method_${method.name.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = method.label,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = when {
                                                    !method.isAvailable -> SlateMuted
                                                    isSelected -> Color.White
                                                    else -> SkyBlueDeep
                                                }
                                            ),
                                            maxLines = 1
                                        )
                                        if (!method.isAvailable) {
                                            Text(
                                                text = stringResource(R.string.withdraw_coming_soon),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = SlateMuted
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Nominal Selection
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CreamSurface.copy(alpha = 0.96f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.withdraw_amount_title),
                            style = MaterialTheme.typography.titleMedium.copy(color = SlateInk)
                        )
                        WithdrawCatalog.nominals.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { nominal ->
                                    val isSelected = uiState.selectedNominal == nominal
                                    val canAfford = uiState.coinBalance >= nominal.coinsRequired
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) SkyBlueBright else Color(0xFFF8FAFC),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SkyBlueDeep else Color(0xFFCBD5E1)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onSelectNominal(nominal) }
                                            .testTag("nominal_${nominal.rupiah}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Rp${CoinConversion.formatNumber(nominal.rupiah)}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = if (isSelected) Color.White else SlateInk,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = "${CoinConversion.formatNumber(nominal.coinsRequired)} koin",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = when {
                                                        isSelected -> Color.White.copy(alpha = 0.92f)
                                                        canAfford -> SkyBlueDeep
                                                        else -> ErrorCoral
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // DANA Form
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CreamSurface.copy(alpha = 0.96f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.fullName,
                            onValueChange = onFullNameChange,
                            label = { Text(stringResource(R.string.withdraw_full_name)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_fullname_input")
                        )

                        OutlinedTextField(
                            value = uiState.danaNumber,
                            onValueChange = onDanaNumberChange,
                            label = { Text(stringResource(R.string.withdraw_dana_number)) },
                            placeholder = { Text("08xxxxxxxxxx") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_dana_input")
                        )

                        if (uiState.errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorCoral.copy(alpha = 0.12f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = uiState.errorMessage,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = ErrorCoral,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("withdraw_error_text")
                                )
                            }
                        }

                        GlossyGreenButton(
                            text = stringResource(R.string.withdraw_submit),
                            onClick = onSubmitClick,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "withdraw_submit_button"
                        )
                    }
                }
            }
        }

        val success = uiState.successRecord
        if (success != null) {
            PopupCardDialog(
                title = stringResource(R.string.withdraw_success_title),
                message = stringResource(R.string.withdraw_success_message),
                badgeIcon = Icons.Filled.CheckCircle,
                badgeColor = CandyGreenMid,
                onDismissRequest = onDismissSuccess
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SkyBlueLight.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Nominal: Rp${CoinConversion.formatNumber(success.rupiahAmount)}",
                            style = MaterialTheme.typography.titleMedium.copy(color = SlateInk)
                        )
                        Text(
                            text = "Nama: ${success.fullName}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SlateInk)
                        )
                        Text(
                            text = "DANA: ${success.danaNumber}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SlateInk)
                        )
                        Text(
                            text = "Status: ${success.status}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SkyBlueDeep,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                GlossyGreenButton(
                    text = stringResource(R.string.btn_ok),
                    onClick = onDismissSuccess,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "withdraw_success_ok_button"
                )
            }
        }
    }
}
