package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.data.WithdrawRepository
import com.example.game.BoardEngine
import com.example.model.BoosterCounts
import com.example.model.CoinConversion
import com.example.model.PaymentMethod
import com.example.model.WithdrawCatalog
import com.example.model.WithdrawSubmissionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(WithdrawRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun appName_isNaraGame() {
        val appName = context.getString(R.string.app_name)
        assertEquals("NARA-Game", appName)
    }

    @Test
    fun withdrawRepository_persistsCoinsAndHistoryAndValidatesDana() {
        val repo1 = WithdrawRepository(context)
        repo1.addCoins(12_000L)
        assertEquals(12_000L, repo1.coinBalance.value)
        assertEquals(203L, repo1.getRupiahValue())

        // Invalid DANA number rejected
        val invalidResult = repo1.submitWithdrawal(
            nominal = WithdrawCatalog.nominals.first(), // Rp200 = 11.800 koin
            fullName = "Budi Santoso",
            danaNumber = "07123456789",
            method = PaymentMethod.DANA
        )
        assertTrue(invalidResult is WithdrawSubmissionResult.Error)
        assertEquals(12_000L, repo1.coinBalance.value)

        // Coming soon method rejected
        val ovoResult = repo1.submitWithdrawal(
            nominal = WithdrawCatalog.nominals.first(),
            fullName = "Budi Santoso",
            danaNumber = "081234567890",
            method = PaymentMethod.OVO
        )
        assertTrue(ovoResult is WithdrawSubmissionResult.Error)

        // Valid DANA withdrawal succeeds and persists across new repository instance
        val validResult = repo1.submitWithdrawal(
            nominal = WithdrawCatalog.nominals.first(),
            fullName = "Budi Santoso",
            danaNumber = "081234567890",
            method = PaymentMethod.DANA
        )
        assertTrue(validResult is WithdrawSubmissionResult.Success)
        assertEquals(200L, repo1.coinBalance.value)

        val repo2 = WithdrawRepository(context)
        assertEquals(200L, repo2.coinBalance.value)
        assertEquals(1, repo2.history.value.size)
        assertEquals("Budi Santoso", repo2.history.value.first().fullName)
        assertEquals("081234567890", repo2.history.value.first().danaNumber)
        assertEquals(WithdrawRepository.STATUS_PENDING, repo2.history.value.first().status)
    }

    @Test
    fun gameRepository_persistsLevelMovesChestBoostersAndBoard() {
        val engine = BoardEngine()
        val board = engine.createPlayableBoard()
        val repo1 = GameRepository(context)
        repo1.saveGameState(
            level = 4,
            movesLeft = 11,
            targetCollected = 7,
            chestCount = 3,
            boosters = BoosterCounts(hammer = 0, bucket = 1, train = 1, brush = 0),
            board = board
        )

        val repo2 = GameRepository(context)
        val restored = repo2.loadProgress()
        assertEquals(4, restored.level)
        assertEquals(11, restored.movesLeft)
        assertEquals(7, restored.targetCollected)
        assertEquals(3, restored.chestCount)
        assertEquals(0, restored.boosters.hammer)
        assertEquals(1, restored.boosters.bucket)
        assertEquals(7, restored.boardColors?.size)
        assertFalse(restored.boardColors.isNullOrEmpty())
    }

    @Test
    fun coinConversion_matchesExactSchedule() {
        assertEquals(590L, CoinConversion.rupiahToCoins(10))
        assertEquals(649L, CoinConversion.rupiahToCoins(11))
        assertEquals(708L, CoinConversion.rupiahToCoins(12))
        assertEquals(767L, CoinConversion.rupiahToCoins(13))
        assertEquals(826L, CoinConversion.rupiahToCoins(14))
        assertEquals(885L, CoinConversion.rupiahToCoins(15))
    }
}
