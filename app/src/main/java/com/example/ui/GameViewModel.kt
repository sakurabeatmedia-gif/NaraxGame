package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ads.RewardedAdManager
import com.example.data.GameRepository
import com.example.data.WithdrawRepository
import com.example.game.BoardEngine
import com.example.game.GameFeedbackPlayer
import com.example.game.GridPos
import com.example.model.BoosterCounts
import com.example.model.BoosterType
import com.example.model.CandyColor
import com.example.model.CandyTile
import com.example.model.LevelCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExplodingTile(
    val id: Long,
    val row: Int,
    val col: Int,
    val color: CandyColor
)

sealed interface GameDialog {
    data object OutOfMoves : GameDialog
    data class LevelUp(val nextLevel: Int) : GameDialog
    data object GameOver : GameDialog
    data object Settings : GameDialog
    data class AdUnavailable(val pendingEndCheck: Boolean = false) : GameDialog
}

data class GameUiState(
    val level: Int = 1,
    val targetColor: CandyColor = CandyColor.RED,
    val targetCount: Int = 12,
    val targetCollected: Int = 0,
    val movesLeft: Int = 18,
    val chestCount: Int = 0,
    val boosters: BoosterCounts = BoosterCounts(),
    val activeBooster: BoosterType? = null,
    val board: List<List<CandyTile>> = emptyList(),
    val isAnimating: Boolean = false,
    val invalidSwapPair: Pair<GridPos, GridPos>? = null,
    val explodingTiles: List<ExplodingTile> = emptyList(),
    val fallRowDeltas: Map<Long, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val activeDialog: GameDialog? = null,
    val hapticPulseTick: Int = 0,
    val shuffleNoticeTick: Int = 0
)

class GameViewModel(
    private val gameRepository: GameRepository,
    withdrawRepository: WithdrawRepository,
    private val adManager: RewardedAdManager,
    private val boardEngine: BoardEngine = BoardEngine(),
    private val feedbackPlayer: GameFeedbackPlayer = GameFeedbackPlayer()
) : ViewModel() {

    val coinBalance: StateFlow<Long> = withdrawRepository.coinBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), withdrawRepository.coinBalance.value)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        restoreSavedGame()
    }

    private fun restoreSavedGame() {
        val saved = gameRepository.loadProgress()
        val config = LevelCatalog.getConfig(saved.level)
        var restoredBoard = saved.boardColors?.let { boardEngine.boardFromColors(it) }
            ?: boardEngine.createPlayableBoard()
        if (boardEngine.findMatches(restoredBoard).isNotEmpty() || !boardEngine.hasValidMoves(restoredBoard)) {
            restoredBoard = boardEngine.shuffleBoard(restoredBoard)
        }

        val initialDialog: GameDialog? = when {
            saved.targetCollected >= config.targetCount -> GameDialog.LevelUp(config.level + 1)
            saved.movesLeft <= 0 -> GameDialog.OutOfMoves
            else -> null
        }

        _uiState.value = GameUiState(
            level = config.level,
            targetColor = config.targetColor,
            targetCount = config.targetCount,
            targetCollected = saved.targetCollected,
            movesLeft = saved.movesLeft,
            chestCount = saved.chestCount,
            boosters = saved.boosters,
            board = restoredBoard,
            soundEnabled = saved.soundEnabled,
            vibrationEnabled = saved.vibrationEnabled,
            activeDialog = initialDialog
        )
        persistCurrentState()
    }

    fun onSwipe(from: GridPos, to: GridPos) {
        val state = _uiState.value
        if (state.isAnimating || state.activeDialog != null) return
        if (state.movesLeft <= 0 || state.targetCollected >= state.targetCount) return
        if (from.row !in 0 until BoardEngine.GRID_SIZE || from.col !in 0 until BoardEngine.GRID_SIZE) return
        if (to.row !in 0 until BoardEngine.GRID_SIZE || to.col !in 0 until BoardEngine.GRID_SIZE) return

        val manhattan = kotlin.math.abs(from.row - to.row) + kotlin.math.abs(from.col - to.col)
        if (manhattan != 1) return

        val swappedBoard = boardEngine.swapTiles(state.board, from, to)
        val initialMatches = boardEngine.findMatches(swappedBoard)

        if (initialMatches.isEmpty()) {
            // Invalid swipe: shake animation and stay in original position
            viewModelScope.launch {
                feedbackPlayer.playInvalidShake(_uiState.value.soundEnabled)
                _uiState.update {
                    it.copy(
                        isAnimating = true,
                        invalidSwapPair = from to to,
                        activeBooster = null,
                        hapticPulseTick = if (it.vibrationEnabled) it.hapticPulseTick + 1 else it.hapticPulseTick
                    )
                }
                delay(380L)
                _uiState.update {
                    it.copy(
                        isAnimating = false,
                        invalidSwapPair = null
                    )
                }
            }
            return
        }

        // Valid swipe: consume 1 move, increment treasure chest (0..5)
        viewModelScope.launch {
            val newMoves = (state.movesLeft - 1).coerceAtLeast(0)
            val newChest = (state.chestCount + 1).coerceAtMost(5)
            _uiState.update {
                it.copy(
                    isAnimating = true,
                    activeBooster = null,
                    board = swappedBoard,
                    movesLeft = newMoves,
                    chestCount = newChest
                )
            }
            persistCurrentState()
            resolveBoardMatches(swappedBoard, initialMatches, checkChest = true)
        }
    }

    fun selectBooster(type: BoosterType) {
        val state = _uiState.value
        if (state.isAnimating || state.activeDialog != null) return
        if (state.boosters.countOf(type) <= 0) return

        _uiState.update {
            it.copy(activeBooster = if (it.activeBooster == type) null else type)
        }
    }

    fun onTileTappedForBooster(pos: GridPos) {
        val state = _uiState.value
        val active = state.activeBooster ?: return
        if (state.isAnimating || state.activeDialog != null) return
        if (state.boosters.countOf(active) <= 0) {
            _uiState.update { it.copy(activeBooster = null) }
            return
        }

        val targets = boardEngine.getBoosterTargets(state.board, active, pos)
        if (targets.isEmpty()) return

        viewModelScope.launch {
            val updatedBoosters = state.boosters.decrement(active)
            _uiState.update {
                it.copy(
                    isAnimating = true,
                    activeBooster = null,
                    boosters = updatedBoosters
                )
            }
            persistCurrentState()
            // Booster does not increment chest counter
            resolveBoardMatches(state.board, targets, checkChest = false)
        }
    }

    private suspend fun resolveBoardMatches(
        startingBoard: List<List<CandyTile>>,
        firstMatches: Set<GridPos>,
        checkChest: Boolean
    ) {
        var currentBoard = startingBoard
        var matches = firstMatches

        while (matches.isNotEmpty()) {
            val currentState = _uiState.value
            val targetMatches = matches.count { pos ->
                currentBoard[pos.row][pos.col].color == currentState.targetColor
            }
            val updatedCollected = (currentState.targetCollected + targetMatches)
                .coerceAtMost(currentState.targetCount)

            val explosions = matches.map { pos ->
                val tile = currentBoard[pos.row][pos.col]
                ExplodingTile(tile.id, pos.row, pos.col, tile.color)
            }

            feedbackPlayer.playMatchPop(currentState.soundEnabled)
            _uiState.update {
                it.copy(
                    targetCollected = updatedCollected,
                    explodingTiles = explosions,
                    hapticPulseTick = if (it.vibrationEnabled) it.hapticPulseTick + 1 else it.hapticPulseTick
                )
            }
            delay(260L)

            val dropResult = boardEngine.dropAndRefill(currentBoard, matches)
            val fallMap = dropResult.fallAnimations.associate { info ->
                info.tile.id to (info.fromRow - info.toRow)
            }
            currentBoard = dropResult.newBoard

            _uiState.update {
                it.copy(
                    board = currentBoard,
                    explodingTiles = emptyList(),
                    fallRowDeltas = fallMap
                )
            }
            delay(310L)

            _uiState.update { it.copy(fallRowDeltas = emptyMap()) }
            matches = boardEngine.findMatches(currentBoard)
        }

        if (!boardEngine.hasValidMoves(currentBoard)) {
            currentBoard = boardEngine.shuffleBoard(currentBoard)
            _uiState.update {
                it.copy(
                    board = currentBoard,
                    shuffleNoticeTick = it.shuffleNoticeTick + 1
                )
            }
        }

        _uiState.update { it.copy(isAnimating = false) }
        persistCurrentState()

        val afterState = _uiState.value
        if (checkChest && afterState.chestCount >= 5) {
            triggerChestRewardedVideo()
        } else {
            evaluateEndOfTurn()
        }
    }

    private fun triggerChestRewardedVideo() {
        adManager.showRewardedVideo(
            onCompletedWithReward = { _ ->
                resetChestAndEvaluate()
            },
            onClosedWithoutReward = {
                resetChestAndEvaluate()
            },
            onUnavailable = {
                _uiState.update { it.copy(chestCount = 0) }
                persistCurrentState()
                _uiState.update {
                    it.copy(activeDialog = GameDialog.AdUnavailable(pendingEndCheck = true))
                }
            }
        )
    }

    private fun resetChestAndEvaluate() {
        _uiState.update { it.copy(chestCount = 0) }
        persistCurrentState()
        evaluateEndOfTurn()
    }

    private fun evaluateEndOfTurn() {
        val state = _uiState.value
        when {
            state.targetCollected >= state.targetCount -> {
                feedbackPlayer.playLevelUp(state.soundEnabled)
                _uiState.update { it.copy(activeDialog = GameDialog.LevelUp(state.level + 1)) }
            }
            state.movesLeft <= 0 -> {
                _uiState.update { it.copy(activeDialog = GameDialog.OutOfMoves) }
            }
        }
    }

    fun onContinueWithAdClicked() {
        adManager.showRewardedVideo(
            onCompletedWithReward = { _ ->
                _uiState.update {
                    it.copy(
                        movesLeft = it.movesLeft + 5,
                        activeDialog = null
                    )
                }
                persistCurrentState()
            },
            onClosedWithoutReward = {
                // Closed early: no extra moves, no coins
                _uiState.update { it.copy(activeDialog = GameDialog.OutOfMoves) }
            },
            onUnavailable = {
                // Ad unavailable: no extra moves, no coins
                _uiState.update {
                    it.copy(activeDialog = GameDialog.AdUnavailable(pendingEndCheck = true))
                }
            }
        )
    }

    fun onQuitOutOfMovesClicked() {
        adManager.showInterstitial(
            onClosed = {
                _uiState.update { it.copy(activeDialog = GameDialog.GameOver) }
            }
        )
    }

    fun onAdvanceNextLevelClicked() {
        val nextLevel = _uiState.value.level + 1
        adManager.showInterstitial(
            onClosed = {
                startFreshLevel(nextLevel)
            }
        )
    }

    fun onRetryCurrentLevelClicked() {
        startFreshLevel(_uiState.value.level)
    }

    fun onBackToMenuFromGameOver(onNavigateMenu: () -> Unit) {
        startFreshLevel(_uiState.value.level)
        onNavigateMenu()
    }

    fun onResetToLevelOne() {
        startFreshLevel(1)
    }

    private fun startFreshLevel(targetLevel: Int) {
        val config = LevelCatalog.getConfig(targetLevel)
        val freshBoard = boardEngine.createPlayableBoard()
        _uiState.update {
            it.copy(
                level = config.level,
                targetColor = config.targetColor,
                targetCount = config.targetCount,
                targetCollected = 0,
                movesLeft = config.maxMoves,
                activeBooster = null,
                board = freshBoard,
                isAnimating = false,
                invalidSwapPair = null,
                explodingTiles = emptyList(),
                fallRowDeltas = emptyMap(),
                activeDialog = null
            )
        }
        persistCurrentState()
    }

    fun openSettings() {
        if (_uiState.value.isAnimating) return
        _uiState.update { it.copy(activeDialog = GameDialog.Settings) }
    }

    fun dismissSettings() {
        if (_uiState.value.activeDialog is GameDialog.Settings) {
            _uiState.update { it.copy(activeDialog = null) }
        }
    }

    fun dismissAdUnavailable() {
        val dialog = _uiState.value.activeDialog as? GameDialog.AdUnavailable ?: return
        _uiState.update { it.copy(activeDialog = null) }
        if (dialog.pendingEndCheck) {
            evaluateEndOfTurn()
        }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
        gameRepository.saveSettings(enabled, _uiState.value.vibrationEnabled)
    }

    fun toggleVibration(enabled: Boolean) {
        _uiState.update { it.copy(vibrationEnabled = enabled) }
        gameRepository.saveSettings(_uiState.value.soundEnabled, enabled)
    }

    private fun persistCurrentState() {
        val state = _uiState.value
        if (state.board.isEmpty()) return
        gameRepository.saveGameState(
            level = state.level,
            movesLeft = state.movesLeft,
            targetCollected = state.targetCollected,
            chestCount = state.chestCount,
            boosters = state.boosters,
            board = state.board
        )
    }

    override fun onCleared() {
        super.onCleared()
        feedbackPlayer.release()
    }

    class Factory(
        private val gameRepository: GameRepository,
        private val withdrawRepository: WithdrawRepository,
        private val adManager: RewardedAdManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(gameRepository, withdrawRepository, adManager) as T
        }
    }
}
