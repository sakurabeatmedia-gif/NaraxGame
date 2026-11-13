package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CandyBlueDark
import com.example.ui.theme.CandyBlueLight
import com.example.ui.theme.CandyBlueMain
import com.example.ui.theme.CandyGreenDark
import com.example.ui.theme.CandyGreenLight
import com.example.ui.theme.CandyGreenMain
import com.example.ui.theme.CandyOrangeDark
import com.example.ui.theme.CandyOrangeLight
import com.example.ui.theme.CandyOrangeMain
import com.example.ui.theme.CandyPurpleDark
import com.example.ui.theme.CandyPurpleLight
import com.example.ui.theme.CandyPurpleMain
import com.example.ui.theme.CandyRedDark
import com.example.ui.theme.CandyRedLight
import com.example.ui.theme.CandyRedMain
import com.example.ui.theme.CandyYellowDark
import com.example.ui.theme.CandyYellowLight
import com.example.ui.theme.CandyYellowMain

enum class CandyColor(
    val displayName: String,
    val lightColor: Color,
    val mainColor: Color,
    val darkColor: Color
) {
    RED("Merah", CandyRedLight, CandyRedMain, CandyRedDark),
    YELLOW("Kuning", CandyYellowLight, CandyYellowMain, CandyYellowDark),
    GREEN("Hijau", CandyGreenLight, CandyGreenMain, CandyGreenDark),
    BLUE("Biru", CandyBlueLight, CandyBlueMain, CandyBlueDark),
    PURPLE("Ungu", CandyPurpleLight, CandyPurpleMain, CandyPurpleDark),
    ORANGE("Oranye", CandyOrangeLight, CandyOrangeMain, CandyOrangeDark)
}

data class CandyTile(
    val id: Long,
    val color: CandyColor
)

enum class BoosterType(val title: String, val hint: String) {
    HAMMER("Palu", "Ketuk 1 balok untuk menghancurkannya"),
    BUCKET("Ember", "Ketuk 1 balok untuk menghapus semua warna sama"),
    TRAIN("Kereta", "Ketuk baris untuk menyapu bersih 1 baris"),
    BRUSH("Kuas", "Ketuk kolom untuk menyapu bersih 1 kolom")
}

data class BoosterCounts(
    val hammer: Int = 1,
    val bucket: Int = 1,
    val train: Int = 1,
    val brush: Int = 1
) {
    fun countOf(type: BoosterType): Int = when (type) {
        BoosterType.HAMMER -> hammer
        BoosterType.BUCKET -> bucket
        BoosterType.TRAIN -> train
        BoosterType.BRUSH -> brush
    }

    fun decrement(type: BoosterType): BoosterCounts = when (type) {
        BoosterType.HAMMER -> copy(hammer = (hammer - 1).coerceAtLeast(0))
        BoosterType.BUCKET -> copy(bucket = (bucket - 1).coerceAtLeast(0))
        BoosterType.TRAIN -> copy(train = (train - 1).coerceAtLeast(0))
        BoosterType.BRUSH -> copy(brush = (brush - 1).coerceAtLeast(0))
    }
}

data class LevelConfig(
    val level: Int,
    val targetColor: CandyColor,
    val targetCount: Int,
    val maxMoves: Int
)

object LevelCatalog {
    fun getConfig(level: Int): LevelConfig {
        val safeLevel = level.coerceAtLeast(1)
        val colors = CandyColor.entries
        val targetColor = colors[(safeLevel - 1) % colors.size]
        val targetCount = (12 + (safeLevel - 1) * 3).coerceAtMost(42)
        val maxMoves = (18 + ((safeLevel - 1) / 3) * 2).coerceAtMost(28)
        return LevelConfig(
            level = safeLevel,
            targetColor = targetColor,
            targetCount = targetCount,
            maxMoves = maxMoves
        )
    }
}
