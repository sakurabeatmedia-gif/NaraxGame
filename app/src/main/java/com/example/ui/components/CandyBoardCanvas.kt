package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.game.BoardEngine
import com.example.game.GridPos
import com.example.model.BoosterType
import com.example.model.CandyColor
import com.example.model.CandyTile
import com.example.ui.ExplodingTile
import com.example.ui.theme.BoardCellDark
import com.example.ui.theme.BoardCellLight
import com.example.ui.theme.BoardFrameBlue
import com.example.ui.theme.SkyBlueLight
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CandyBoard(
    board: List<List<CandyTile>>,
    invalidSwapPair: Pair<GridPos, GridPos>?,
    explodingTiles: List<ExplodingTile>,
    fallRowDeltas: Map<Long, Int>,
    activeBooster: BoosterType?,
    isInputEnabled: Boolean,
    onSwipe: (GridPos, GridPos) -> Unit,
    onBoosterTileTap: (GridPos) -> Unit,
    modifier: Modifier = Modifier
) {
    if (board.isEmpty()) return

    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(invalidSwapPair) {
        if (invalidSwapPair != null) {
            shakeAnim.snapTo(0f)
            shakeAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360, easing = LinearEasing)
            )
            shakeAnim.snapTo(0f)
        }
    }

    val explosionAnim = remember { Animatable(0f) }
    LaunchedEffect(explodingTiles) {
        if (explodingTiles.isNotEmpty()) {
            explosionAnim.snapTo(0f)
            explosionAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        } else {
            explosionAnim.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(10.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(BoardFrameBlue.copy(alpha = 0.92f))
                .border(3.dp, SkyBlueLight.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                .padding(8.dp)
                .testTag("candy_board")
        ) {
            val density = LocalDensity.current
            val boardPx = with(density) { maxWidth.toPx() }
            val cellSizePx = boardPx / BoardEngine.GRID_SIZE
            val cellSizeDp = maxWidth / BoardEngine.GRID_SIZE

            // Checkerboard cell background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / BoardEngine.GRID_SIZE
                val cellH = size.height / BoardEngine.GRID_SIZE
                for (r in 0 until BoardEngine.GRID_SIZE) {
                    for (c in 0 until BoardEngine.GRID_SIZE) {
                        val color = if ((r + c) % 2 == 0) BoardCellLight else BoardCellDark
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(c * cellW + 2f, r * cellH + 2f),
                            size = Size(cellW - 4f, cellH - 4f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }
                }
            }

            // Gesture layer: strictly SWIPE for moves, TAP only when a booster is active
            var dragHandled by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(board, isInputEnabled, activeBooster) {
                        if (!isInputEnabled) return@pointerInput
                        if (activeBooster != null) {
                            detectTapGestures { tapOffset ->
                                val col = (tapOffset.x / cellSizePx).toInt()
                                    .coerceIn(0, BoardEngine.GRID_SIZE - 1)
                                val row = (tapOffset.y / cellSizePx).toInt()
                                    .coerceIn(0, BoardEngine.GRID_SIZE - 1)
                                onBoosterTileTap(GridPos(row, col))
                            }
                        } else {
                            var startPos: GridPos? = null
                            var totalDx = 0f
                            var totalDy = 0f

                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    val col = (startOffset.x / cellSizePx).toInt()
                                        .coerceIn(0, BoardEngine.GRID_SIZE - 1)
                                    val row = (startOffset.y / cellSizePx).toInt()
                                        .coerceIn(0, BoardEngine.GRID_SIZE - 1)
                                    startPos = GridPos(row, col)
                                    totalDx = 0f
                                    totalDy = 0f
                                    dragHandled = false
                                },
                                onDragEnd = {
                                    startPos = null
                                    dragHandled = false
                                },
                                onDragCancel = {
                                    startPos = null
                                    dragHandled = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (dragHandled) return@detectDragGestures
                                    val origin = startPos ?: return@detectDragGestures

                                    totalDx += dragAmount.x
                                    totalDy += dragAmount.y
                                    val threshold = cellSizePx * 0.25f

                                    if (abs(totalDx) >= threshold || abs(totalDy) >= threshold) {
                                        val target = if (abs(totalDx) >= abs(totalDy)) {
                                            if (totalDx > 0) GridPos(origin.row, origin.col + 1)
                                            else GridPos(origin.row, origin.col - 1)
                                        } else {
                                            if (totalDy > 0) GridPos(origin.row + 1, origin.col)
                                            else GridPos(origin.row - 1, origin.col)
                                        }
                                        dragHandled = true
                                        if (target.row in 0 until BoardEngine.GRID_SIZE &&
                                            target.col in 0 until BoardEngine.GRID_SIZE
                                        ) {
                                            onSwipe(origin, target)
                                        }
                                    }
                                }
                            )
                        }
                    }
            ) {
                // Render each tile with fall animation and invalid-swap shake
                for (r in 0 until BoardEngine.GRID_SIZE) {
                    for (c in 0 until BoardEngine.GRID_SIZE) {
                        val tile = board[r][c]
                        val pos = GridPos(r, c)
                        val isExploding = explodingTiles.any { it.id == tile.id }
                        if (!isExploding) {
                            key(tile.id) {
                                val initialDelta = fallRowDeltas[tile.id] ?: 0
                                val fallOffsetY = remember(tile.id) {
                                    Animatable(initialDelta * cellSizePx)
                                }
                                LaunchedEffect(r, fallRowDeltas[tile.id]) {
                                    val delta = fallRowDeltas[tile.id]
                                    if (delta != null && delta != 0) {
                                        fallOffsetY.snapTo(delta * cellSizePx)
                                        fallOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.68f,
                                                stiffness = 380f
                                            )
                                        )
                                    } else if (fallOffsetY.value != 0f) {
                                        fallOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.72f,
                                                stiffness = 420f
                                            )
                                        )
                                    }
                                }

                                val isShaking = invalidSwapPair != null &&
                                    (invalidSwapPair.first == pos || invalidSwapPair.second == pos)
                                val shakeOffsetX = if (isShaking) {
                                    sin(shakeAnim.value * 4f * PI.toFloat()) * (cellSizePx * 0.16f)
                                } else {
                                    0f
                                }

                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                x = (c * cellSizePx + shakeOffsetX).roundToInt(),
                                                y = (r * cellSizePx + fallOffsetY.value).roundToInt()
                                            )
                                        }
                                        .size(cellSizeDp)
                                        .padding(3.dp)
                                ) {
                                    CandyTileCanvas(
                                        color = tile.color,
                                        highlightForBooster = activeBooster != null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                // Particle explosion overlay
                if (explodingTiles.isNotEmpty()) {
                    val progress = explosionAnim.value
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        explodingTiles.forEach { exploded ->
                            val cx = (exploded.col + 0.5f) * cellSizePx
                            val cy = (exploded.row + 0.5f) * cellSizePx
                            val alpha = (1f - progress).coerceIn(0f, 1f)

                            // Expanding shockwave ring
                            drawCircle(
                                color = exploded.color.lightColor.copy(alpha = alpha * 0.7f),
                                radius = cellSizePx * (0.25f + progress * 0.65f),
                                center = Offset(cx, cy)
                            )

                            // 8 flying candy shards
                            val particleCount = 8
                            for (i in 0 until particleCount) {
                                val angle = (2.0 * PI * i / particleCount) + 0.3
                                val dist = cellSizePx * (0.15f + progress * 0.85f)
                                val px = cx + (cos(angle) * dist).toFloat()
                                val py = cy + (sin(angle) * dist).toFloat()
                                val particleRadius = cellSizePx * 0.13f * (1f - progress * 0.5f)
                                drawCircle(
                                    color = if (i % 2 == 0) exploded.color.mainColor.copy(alpha = alpha)
                                    else Color.White.copy(alpha = alpha),
                                    radius = particleRadius,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandyTileCanvas(
    color: CandyColor,
    highlightForBooster: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val corner = w * 0.28f

        // Drop shadow base
        drawRoundRect(
            color = color.darkColor,
            topLeft = Offset(0f, h * 0.08f),
            size = Size(w, h * 0.92f),
            cornerRadius = CornerRadius(corner, corner)
        )

        // Main glossy candy body
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(color.lightColor, color.mainColor, color.darkColor)
            ),
            topLeft = Offset(0f, 0f),
            size = Size(w, h * 0.92f),
            cornerRadius = CornerRadius(corner, corner)
        )

        // Inner candy emblem for visual distinction
        drawCandyMotif(color)

        // Top glossy specular highlight
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.05f)
                )
            ),
            topLeft = Offset(w * 0.14f, h * 0.08f),
            size = Size(w * 0.72f, h * 0.28f),
            cornerRadius = CornerRadius(corner * 0.6f, corner * 0.6f)
        )

        // Specular sparkle dot
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.065f,
            center = Offset(w * 0.25f, h * 0.22f)
        )

        if (highlightForBooster) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(0f, 0f),
                size = Size(w, h * 0.92f),
                cornerRadius = CornerRadius(corner, corner),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
    }
}

private fun DrawScope.drawCandyMotif(color: CandyColor) {
    val cx = size.width / 2f
    val cy = size.height * 0.48f
    val r = size.minDimension * 0.20f
    val tint = Color.White.copy(alpha = 0.35f)

    when (color) {
        CandyColor.RED -> {
            // Diamond gem
            val path = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r, cy)
                lineTo(cx, cy + r)
                lineTo(cx - r, cy)
                close()
            }
            drawPath(path, tint)
        }
        CandyColor.YELLOW -> {
            // 4-point star
            val path = Path().apply {
                moveTo(cx, cy - r * 1.1f)
                lineTo(cx + r * 0.35f, cy - r * 0.35f)
                lineTo(cx + r * 1.1f, cy)
                lineTo(cx + r * 0.35f, cy + r * 0.35f)
                lineTo(cx, cy + r * 1.1f)
                lineTo(cx - r * 0.35f, cy + r * 0.35f)
                lineTo(cx - r * 1.1f, cy)
                lineTo(cx - r * 0.35f, cy - r * 0.35f)
                close()
            }
            drawPath(path, tint)
        }
        CandyColor.GREEN -> {
            // Rounded leaf / clover circle pair
            drawCircle(tint, radius = r * 0.65f, center = Offset(cx - r * 0.3f, cy))
            drawCircle(tint, radius = r * 0.65f, center = Offset(cx + r * 0.3f, cy))
        }
        CandyColor.BLUE -> {
            // Droplet
            val path = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r * 0.85f, cy + r * 0.25f)
                lineTo(cx, cy + r)
                lineTo(cx - r * 0.85f, cy + r * 0.25f)
                close()
            }
            drawPath(path, tint)
        }
        CandyColor.PURPLE -> {
            // Ring / candy drop
            drawCircle(tint, radius = r * 0.85f, center = Offset(cx, cy))
            drawCircle(color.mainColor, radius = r * 0.42f, center = Offset(cx, cy))
        }
        CandyColor.ORANGE -> {
            // Citrus slice triangle trio
            drawCircle(tint, radius = r * 0.85f, center = Offset(cx, cy))
        }
    }
}
