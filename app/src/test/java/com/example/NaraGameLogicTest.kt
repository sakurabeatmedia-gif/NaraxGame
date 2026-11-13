package com.example

import com.example.ads.AdSdkBridge
import com.example.ads.RewardedAdManager
import com.example.game.BoardEngine
import com.example.game.GridPos
import com.example.model.BoosterType
import com.example.model.CandyColor
import com.example.model.CandyTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NaraGameLogicTest {

    @Test
    fun boardEngine_initialBoardIs7x7WithNoMatchesAndHasValidMove() {
        val engine = BoardEngine(Random(42))
        repeat(20) {
            val board = engine.createPlayableBoard()
            assertEquals(7, board.size)
            assertTrue(board.all { it.size == 7 })
            assertTrue(engine.findMatches(board).isEmpty())
            assertTrue(engine.hasValidMoves(board))
        }
    }

    @Test
    fun boardEngine_dropAndRefillMaintainsGridSizeAndMovesSurvivorsDown() {
        val engine = BoardEngine(Random(7))
        val board = engine.createPlayableBoard()
        val cleared = setOf(GridPos(6, 0), GridPos(5, 0), GridPos(4, 0))
        val topSurvivor = board[0][0]
        val drop = engine.dropAndRefill(board, cleared)

        assertEquals(7, drop.newBoard.size)
        assertTrue(drop.newBoard.all { it.size == 7 })
        // Row 0 in col 0 falls down 3 rows to row 3
        assertEquals(topSurvivor.id, drop.newBoard[3][0].id)
    }

    @Test
    fun boardEngine_boostersSelectCorrectTargets() {
        val engine = BoardEngine(Random(1))
        val board = List(7) { r ->
            List(7) { c ->
                CandyTile((r * 7 + c + 1).toLong(), CandyColor.entries[(r + c) % 6])
            }
        }
        assertEquals(setOf(GridPos(2, 3)), engine.getBoosterTargets(board, BoosterType.HAMMER, GridPos(2, 3)))
        assertEquals(7, engine.getBoosterTargets(board, BoosterType.TRAIN, GridPos(2, 3)).size)
        assertEquals(7, engine.getBoosterTargets(board, BoosterType.BRUSH, GridPos(2, 3)).size)
        assertTrue(engine.getBoosterTargets(board, BoosterType.BUCKET, GridPos(0, 0)).size >= 7)
    }

    @Test
    fun rewardedAdManager_grantsCoinsOnlyOnceOnVideoCompletedAndRespectsCooldown() {
        var nowMs = 10_000L
        var grantedCoins = 0L
        var grantCalls = 0
        val fakeBridge = FakeAdSdkBridge()

        val manager = RewardedAdManager(
            bridge = fakeBridge,
            onGrantCoins = { coins ->
                grantedCoins += coins
                grantCalls++
            },
            elapsedRealtimeMs = { nowMs },
            scheduleDelay = { _, r -> r.run() },
            random = Random(99)
        )

        // 1. Not ready -> unavailable, 0 coins
        var unavailableCalled = false
        fakeBridge.rewardedReady = false
        manager.showRewardedVideo(
            onCompletedWithReward = {},
            onClosedWithoutReward = {},
            onUnavailable = { unavailableCalled = true }
        )
        assertTrue(unavailableCalled)
        assertEquals(0L, grantedCoins)

        // 2. Ready, closed early without video completion -> 0 coins
        fakeBridge.rewardedReady = true
        var closedWithoutReward = false
        manager.showRewardedVideo(
            onCompletedWithReward = {},
            onClosedWithoutReward = { closedWithoutReward = true },
            onUnavailable = {}
        )
        fakeBridge.triggerRewardedDisplayed()
        fakeBridge.triggerRewardedHidden()
        assertTrue(closedWithoutReward)
        assertEquals(0L, grantedCoins)

        // 3. Cooldown blocks immediate retry (< 5s)
        var cooldownBlocked = false
        nowMs += 2_000L
        manager.showRewardedVideo(
            onCompletedWithReward = {},
            onClosedWithoutReward = {},
            onUnavailable = { cooldownBlocked = true }
        )
        assertTrue(cooldownBlocked)

        // 4. After 5s cooldown, video completes -> coins granted ONCE even if callback fires twice
        nowMs += 5_000L
        var completedCoins = 0L
        manager.showRewardedVideo(
            onCompletedWithReward = { completedCoins = it },
            onClosedWithoutReward = {},
            onUnavailable = {}
        )
        fakeBridge.triggerRewardedDisplayed()
        fakeBridge.triggerRewardedVideoCompleted()
        fakeBridge.triggerRewardedVideoCompleted() // duplicate callback attempt
        fakeBridge.triggerRewardedHidden()

        assertEquals(1, grantCalls)
        assertTrue(grantedCoins in 590L..885L)
        assertEquals(grantedCoins, completedCoins)

        // 5. Interstitial NEVER grants coins
        nowMs += 6_000L
        fakeBridge.interstitialReady = true
        var interstitialClosed = false
        manager.showInterstitial(onClosed = { interstitialClosed = true })
        fakeBridge.triggerInterstitialHidden()
        assertTrue(interstitialClosed)
        assertEquals(1, grantCalls)
    }

    private class FakeAdSdkBridge : AdSdkBridge {
        var rewardedReady = false
        var interstitialReady = false
        private var onVideoCompleted: (() -> Unit)? = null
        private var onRewardedDisplayed: (() -> Unit)? = null
        private var onRewardedHidden: (() -> Unit)? = null
        private var onInterstitialHidden: (() -> Unit)? = null

        override fun loadRewarded(onLoaded: () -> Unit, onFailed: (String?) -> Unit) {}
        override fun isRewardedReady(): Boolean = rewardedReady

        override fun showRewarded(
            onVideoCompleted: () -> Unit,
            onDisplayed: () -> Unit,
            onHidden: () -> Unit,
            onNotDisplayed: () -> Unit
        ): Boolean {
            this.onVideoCompleted = onVideoCompleted
            this.onRewardedDisplayed = onDisplayed
            this.onRewardedHidden = onHidden
            return true
        }

        override fun loadInterstitial(onLoaded: () -> Unit, onFailed: (String?) -> Unit) {}
        override fun isInterstitialReady(): Boolean = interstitialReady

        override fun showInterstitial(
            onDisplayed: () -> Unit,
            onHidden: () -> Unit,
            onNotDisplayed: () -> Unit
        ): Boolean {
            this.onInterstitialHidden = onHidden
            return true
        }

        fun triggerRewardedDisplayed() = onRewardedDisplayed?.invoke()
        fun triggerRewardedVideoCompleted() = onVideoCompleted?.invoke()
        fun triggerRewardedHidden() = onRewardedHidden?.invoke()
        fun triggerInterstitialHidden() = onInterstitialHidden?.invoke()
    }
}
