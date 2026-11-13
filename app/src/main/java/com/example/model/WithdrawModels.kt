package com.example.model

import java.text.NumberFormat
import java.util.Locale

object CoinConversion {
    const val COINS_PER_RUPIAH = 59L
    val REWARD_NOMINALS_RUPIAH = listOf(10, 11, 12, 13, 14, 15)

    fun rupiahToCoins(rupiah: Int): Long = rupiah.toLong() * COINS_PER_RUPIAH

    fun coinsToRupiah(coins: Long): Long = if (coins <= 0L) 0L else coins / COINS_PER_RUPIAH

    fun formatNumber(value: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        return formatter.format(value)
    }
}

enum class PaymentMethod(val label: String, val isAvailable: Boolean) {
    DANA("DANA", true),
    OVO("OVO", false),
    GOPAY("GoPay", false),
    SHOPEEPAY("ShopeePay", false)
}

data class WithdrawNominal(
    val rupiah: Long,
    val coinsRequired: Long
)

object WithdrawCatalog {
    val nominals: List<WithdrawNominal> = listOf(
        WithdrawNominal(rupiah = 200L, coinsRequired = 11_800L),
        WithdrawNominal(rupiah = 500L, coinsRequired = 29_500L),
        WithdrawNominal(rupiah = 1_000L, coinsRequired = 59_000L),
        WithdrawNominal(rupiah = 5_000L, coinsRequired = 295_000L),
        WithdrawNominal(rupiah = 10_000L, coinsRequired = 590_000L),
        WithdrawNominal(rupiah = 50_000L, coinsRequired = 2_950_000L),
        WithdrawNominal(rupiah = 100_000L, coinsRequired = 5_900_000L)
    )
}

data class WithdrawRecord(
    val id: String,
    val timestampMillis: Long,
    val dateFormatted: String,
    val rupiahAmount: Long,
    val coinAmount: Long,
    val fullName: String,
    val danaNumber: String,
    val method: String,
    val status: String
)

sealed class WithdrawSubmissionResult {
    data class Success(val record: WithdrawRecord) : WithdrawSubmissionResult()
    data class Error(val message: String) : WithdrawSubmissionResult()
}
