package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoosterCounts
import com.example.model.BoosterType
import com.example.model.CoinConversion
import com.example.ui.theme.CandyGreenBorder
import com.example.ui.theme.CandyGreenBottom
import com.example.ui.theme.CandyGreenMid
import com.example.ui.theme.CandyGreenTop
import com.example.ui.theme.CoinGold
import com.example.ui.theme.CoinGoldDark
import com.example.ui.theme.CoinGoldLight
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight

@Composable
fun GlossyGreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "btn_scale"
    )

    val shape = RoundedCornerShape(20.dp)
    val bgBrush = if (enabled) {
        Brush.verticalGradient(listOf(CandyGreenTop, CandyGreenMid, CandyGreenBottom))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
    }
    val borderColor = if (enabled) CandyGreenBorder else Color(0xFF64748B)

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .scale(scale)
            .heightIn(min = 52.dp)
            .minimumInteractiveComponentSize()
            .shadow(elevation = if (enabled) 6.dp else 1.dp, shape = shape)
            .clip(shape)
            .background(bgBrush)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glossy top specular shine
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (enabled) 0.48f else 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CoinBadge(
    coins: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("header_coin_badge"),
        shape = RoundedCornerShape(50),
        color = SkyBlueDeep.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CoinGoldLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CoinGoldLight, CoinGold, CoinGoldDark)
                    ),
                    radius = size.minDimension / 2f
                )
                drawCircle(
                    color = Color(0xFFFFF8E1),
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.42f, size.height * 0.40f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = CoinConversion.formatNumber(coins),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun BrightBlueHeader(
    coinBalance: Long,
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SkyBlueLight, SkyBlueBright, SkyBlueDeep)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("header_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    CoinBadge(coins = coinBalance)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (title != null) {
                    CoinBadge(coins = coinBalance)
                }
                if (onSettingsClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Pengaturan",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TreasureChestIndicator(
    chestCount: Int,
    modifier: Modifier = Modifier
) {
    val clamped = chestCount.coerceIn(0, 5)
    Surface(
        modifier = modifier.testTag("treasure_chest_indicator"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F4C81).copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (clamped >= 5) CoinGold else Color.White.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom drawn treasure chest icon
            Canvas(modifier = Modifier.size(28.dp)) {
                val w = size.width
                val h = size.height
                // Chest body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFD97706), Color(0xFF92400E))
                    ),
                    topLeft = Offset(w * 0.1f, h * 0.38f),
                    size = Size(w * 0.8f, h * 0.52f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Chest lid
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFBBF24), Color(0xFFB45309))
                    ),
                    topLeft = Offset(w * 0.08f, h * 0.16f),
                    size = Size(w * 0.84f, h * 0.28f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Gold lock clasp
                drawRoundRect(
                    color = CoinGoldLight,
                    topLeft = Offset(w * 0.42f, h * 0.38f),
                    size = Size(w * 0.16f, h * 0.22f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$clamped/5",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CoinGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(5) { idx ->
                        Box(
                            modifier = Modifier
                                .size(width = 12.dp, height = 5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (idx < clamped) CoinGold else Color.White.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoosterBar(
    boosters: BoosterCounts,
    activeBooster: BoosterType?,
    enabled: Boolean,
    onBoosterClick: (BoosterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BoosterType.entries.forEach { type ->
            val count = boosters.countOf(type)
            val isSelected = activeBooster == type
            val canUse = enabled && count > 0
            val icon = when (type) {
                BoosterType.HAMMER -> Icons.Filled.Construction
                BoosterType.BUCKET -> Icons.Filled.WaterDrop
                BoosterType.TRAIN -> Icons.Filled.Train
                BoosterType.BRUSH -> Icons.Filled.Brush
            }
            val bgColor = when {
                isSelected -> CandyGreenMid
                canUse -> Color(0xFF0F4C81).copy(alpha = 0.90f)
                else -> Color(0xFF334155).copy(alpha = 0.65f)
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 64.dp)
                    .testTag("booster_${type.name.lowercase()}")
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = canUse) { onBoosterClick(type) },
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) CoinGoldLight else Color.White.copy(alpha = 0.35f)
                )
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = type.title,
                            tint = if (canUse) Color.White else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (canUse) Color.White else Color.White.copy(alpha = 0.45f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }

                    // Count pill in top-right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (count > 0) CoinGold else Color(0xFF64748B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
