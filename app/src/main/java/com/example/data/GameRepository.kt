package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.game.BoardEngine
import com.example.model.BoosterCounts
import com.example.model.CandyColor
import com.example.model.CandyTile
import com.example.model.LevelCatalog

data class SavedGameProgress(
    val level: Int,
    val movesLeft: Int,
    val targetCollected: Int,
    val chestCount: Int,
    val boosters: BoosterCounts,
    val boardColors: List<List<CandyColor>>?,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean
)

class GameRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(WithdrawRepository.PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        private const val KEY_LEVEL = "game_level"
        private const val KEY_MOVES_LEFT = "game_moves_left"
        private const val KEY_TARGET_COLLECTED = "game_target_collected"
        private const val KEY_CHEST_COUNT = "game_chest_count"
        private const val KEY_BOOSTER_HAMMER = "booster_hammer"
        private const val KEY_BOOSTER_BUCKET = "booster_bucket"
        private const val KEY_BOOSTER_TRAIN = "booster_train"
        private const val KEY_BOOSTER_BRUSH = "booster_brush"
        private const val KEY_BOARD_STATE = "game_board_state"
        private const val KEY_SOUND_ENABLED = "settings_sound"
        private const val KEY_VIBRATION_ENABLED = "settings_vibration"
    }

    fun loadProgress(): SavedGameProgress {
        val level = prefs.getInt(KEY_LEVEL, 1).coerceAtLeast(1)
        val config = LevelCatalog.getConfig(level)
        val movesLeft = if (prefs.contains(KEY_MOVES_LEFT)) {
            prefs.getInt(KEY_MOVES_LEFT, config.maxMoves).coerceAtLeast(0)
        } else {
            config.maxMoves
        }
        val targetCollected = prefs.getInt(KEY_TARGET_COLLECTED, 0).coerceAtLeast(0)
        val chestCount = prefs.getInt(KEY_CHEST_COUNT, 0).coerceIn(0, 5)

        val boosters = BoosterCounts(
            hammer = prefs.getInt(KEY_BOOSTER_HAMMER, 1).coerceAtLeast(0),
            bucket = prefs.getInt(KEY_BOOSTER_BUCKET, 1).coerceAtLeast(0),
            train = prefs.getInt(KEY_BOOSTER_TRAIN, 1).coerceAtLeast(0),
            brush = prefs.getInt(KEY_BOOSTER_BRUSH, 1).coerceAtLeast(0)
        )

        val boardColors = deserializeBoard(prefs.getString(KEY_BOARD_STATE, null))
        val soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)

        return SavedGameProgress(
            level = level,
            movesLeft = movesLeft,
            targetCollected = targetCollected,
            chestCount = chestCount,
            boosters = boosters,
            boardColors = boardColors,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled
        )
    }

    fun saveGameState(
        level: Int,
        movesLeft: Int,
        targetCollected: Int,
        chestCount: Int,
        boosters: BoosterCounts,
        board: List<List<CandyTile>>
    ) {
        prefs.edit()
            .putInt(KEY_LEVEL, level)
            .putInt(KEY_MOVES_LEFT, movesLeft)
            .putInt(KEY_TARGET_COLLECTED, targetCollected)
            .putInt(KEY_CHEST_COUNT, chestCount.coerceIn(0, 5))
            .putInt(KEY_BOOSTER_HAMMER, boosters.hammer)
            .putInt(KEY_BOOSTER_BUCKET, boosters.bucket)
            .putInt(KEY_BOOSTER_TRAIN, boosters.train)
            .putInt(KEY_BOOSTER_BRUSH, boosters.brush)
            .putString(KEY_BOARD_STATE, serializeBoard(board))
            .apply()
    }

    fun saveSettings(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_SOUND_ENABLED, soundEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, vibrationEnabled)
            .apply()
    }

    private fun serializeBoard(board: List<List<CandyTile>>): String {
        return board.joinToString(";") { row ->
            row.joinToString(",") { it.color.ordinal.toString() }
        }
    }

    private fun deserializeBoard(raw: String?): List<List<CandyColor>>? {
        if (raw.isNullOrBlank()) return null
        return try {
            val rows = raw.split(";")
            if (rows.size != BoardEngine.GRID_SIZE) return null
            val colors = CandyColor.entries
            rows.map { rowStr ->
                val cols = rowStr.split(",")
                if (cols.size != BoardEngine.GRID_SIZE) return null
                cols.map { idxStr -> colors[idxStr.toInt()] }
            }
        } catch (_: Exception) {
            null
        }
    }
}
