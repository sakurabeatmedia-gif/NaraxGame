package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CoinConversion
import com.example.model.PaymentMethod
import com.example.model.WithdrawNominal
import com.example.model.WithdrawRecord
import com.example.model.WithdrawSubmissionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WithdrawRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        const val PREFS_NAME = "nara_game_prefs"
        private const val KEY_COIN_BALANCE = "coin_balance"
        private const val KEY_WITHDRAW_HISTORY = "withdraw_history_json"
        const val STATUS_PENDING = "Menunggu / Diproses"
        private val DANA_REGEX = Regex("^08[0-9]{8,11}$")
    }

    private val lock = Any()

    private val _coinBalance = MutableStateFlow(prefs.getLong(KEY_COIN_BALANCE, 0L))
    val coinBalance: StateFlow<Long> = _coinBalance.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<WithdrawRecord>> = _history.asStateFlow()

    fun addCoins(amount: Long) {
        if (amount <= 0L) return
        synchronized(lock) {
            val updated = _coinBalance.value + amount
            prefs.edit().putLong(KEY_COIN_BALANCE, updated).commit()
            _coinBalance.value = updated
        }
    }

    fun getRupiahValue(): Long {
        return CoinConversion.coinsToRupiah(_coinBalance.value)
    }

    fun isValidDanaNumber(number: String): Boolean {
        return DANA_REGEX.matches(number.trim())
    }

    fun submitWithdrawal(
        nominal: WithdrawNominal,
        fullName: String,
        danaNumber: String,
        method: PaymentMethod,
        nowMillis: Long = System.currentTimeMillis()
    ): WithdrawSubmissionResult {
        val cleanName = fullName.trim()
        val cleanNumber = danaNumber.trim()

        if (!method.isAvailable) {
            return WithdrawSubmissionResult.Error("Metode ${method.label} segera hadir.")
        }
        if (cleanName.isEmpty()) {
            return WithdrawSubmissionResult.Error("Nama lengkap wajib diisi.")
        }
        if (!isValidDanaNumber(cleanNumber)) {
            return WithdrawSubmissionResult.Error(
                "Nomor DANA harus diawali 08, terdiri dari 10-13 digit angka."
            )
        }

        synchronized(lock) {
            val currentBalance = _coinBalance.value
            if (currentBalance < nominal.coinsRequired) {
                return WithdrawSubmissionResult.Error("Saldo koin tidak mencukupi.")
            }

            val updatedBalance = currentBalance - nominal.coinsRequired
            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
                .format(Date(nowMillis))
            val record = WithdrawRecord(
                id = UUID.randomUUID().toString(),
                timestampMillis = nowMillis,
                dateFormatted = dateStr,
                rupiahAmount = nominal.rupiah,
                coinAmount = nominal.coinsRequired,
                fullName = cleanName,
                danaNumber = cleanNumber,
                method = method.label,
                status = STATUS_PENDING
            )

            val updatedHistory = listOf(record) + _history.value
            prefs.edit()
                .putLong(KEY_COIN_BALANCE, updatedBalance)
                .putString(KEY_WITHDRAW_HISTORY, serializeHistory(updatedHistory))
                .commit()

            _coinBalance.value = updatedBalance
            _history.value = updatedHistory
            return WithdrawSubmissionResult.Success(record)
        }
    }

    private fun loadHistory(): List<WithdrawRecord> {
        val raw = prefs.getString(KEY_WITHDRAW_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        WithdrawRecord(
                            id = obj.getString("id"),
                            timestampMillis = obj.getLong("timestampMillis"),
                            dateFormatted = obj.getString("dateFormatted"),
                            rupiahAmount = obj.getLong("rupiahAmount"),
                            coinAmount = obj.getLong("coinAmount"),
                            fullName = obj.getString("fullName"),
                            danaNumber = obj.getString("danaNumber"),
                            method = obj.getString("method"),
                            status = obj.getString("status")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeHistory(records: List<WithdrawRecord>): String {
        val array = JSONArray()
        records.forEach { record ->
            val obj = JSONObject().apply {
                put("id", record.id)
                put("timestampMillis", record.timestampMillis)
                put("dateFormatted", record.dateFormatted)
                put("rupiahAmount", record.rupiahAmount)
                put("coinAmount", record.coinAmount)
                put("fullName", record.fullName)
                put("danaNumber", record.danaNumber)
                put("method", record.method)
                put("status", record.status)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
