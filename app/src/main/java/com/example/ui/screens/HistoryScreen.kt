package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CoinConversion
import com.example.model.WithdrawRecord
import com.example.ui.components.BrightBlueHeader
import com.example.ui.components.CandyKingdomBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.WarningAmber

@Composable
fun HistoryScreen(
    coinBalance: Long,
    history: List<WithdrawRecord>,
    onBackClick: () -> Unit
) {
    CandyKingdomBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrightBlueHeader(
                coinBalance = coinBalance,
                title = stringResource(R.string.history_title),
                onBackClick = onBackClick
            )

            Box(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = CreamSurface.copy(alpha = 0.96f),
                            shadowElevation = 6.dp,
                            border = BorderStroke(2.dp, SkyBlueBright.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = SkyBlueBright,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.history_empty),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = SlateInk,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("history_empty_text")
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("history_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history, key = { it.id }) { record ->
                            HistoryRecordCard(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(record: WithdrawRecord) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CreamSurface.copy(alpha = 0.96f),
        shadowElevation = 4.dp,
        border = BorderStroke(1.5.dp, SkyBlueBright.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rp${CoinConversion.formatNumber(record.rupiahAmount)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = SkyBlueDeep,
                        fontWeight = FontWeight.Bold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = WarningAmber.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = record.status,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = "Tanggal: ${record.dateFormatted}",
                style = MaterialTheme.typography.bodyMedium.copy(color = SlateMuted)
            )
            Text(
                text = "Nama: ${record.fullName}",
                style = MaterialTheme.typography.bodyLarge.copy(color = SlateInk)
            )
            Text(
                text = "Nomor ${record.method}: ${record.danaNumber}",
                style = MaterialTheme.typography.bodyMedium.copy(color = SlateInk)
            )
        }
    }
}
