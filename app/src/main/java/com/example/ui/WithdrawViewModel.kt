package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.WithdrawRepository
import com.example.model.CoinConversion
import com.example.model.PaymentMethod
import com.example.model.WithdrawCatalog
import com.example.model.WithdrawNominal
import com.example.model.WithdrawRecord
import com.example.model.WithdrawSubmissionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class WithdrawUiState(
    val coinBalance: Long = 0L,
    val rupiahValue: Long = 0L,
    val selectedMethod: PaymentMethod = PaymentMethod.DANA,
    val selectedNominal: WithdrawNominal = WithdrawCatalog.nominals.first(),
    val fullName: String = "",
    val danaNumber: String = "",
    val errorMessage: String? = null,
    val successRecord: WithdrawRecord? = null,
    val history: List<WithdrawRecord> = emptyList()
)

class WithdrawViewModel(
    private val withdrawRepository: WithdrawRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(WithdrawUiState())

    val uiState: StateFlow<WithdrawUiState> = combine(
        withdrawRepository.coinBalance,
        withdrawRepository.history,
        _formState
    ) { coins, history, form ->
        form.copy(
            coinBalance = coins,
            rupiahValue = CoinConversion.coinsToRupiah(coins),
            history = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WithdrawUiState(
            coinBalance = withdrawRepository.coinBalance.value,
            rupiahValue = withdrawRepository.getRupiahValue(),
            history = withdrawRepository.history.value
        )
    )

    fun selectMethod(method: PaymentMethod) {
        if (!method.isAvailable) return
        _formState.update { it.copy(selectedMethod = method, errorMessage = null) }
    }

    fun selectNominal(nominal: WithdrawNominal) {
        _formState.update { it.copy(selectedNominal = nominal, errorMessage = null) }
    }

    fun updateFullName(name: String) {
        _formState.update { it.copy(fullName = name, errorMessage = null) }
    }

    fun updateDanaNumber(number: String) {
        val digitsOnly = number.filter { it.isDigit() }.take(13)
        _formState.update { it.copy(danaNumber = digitsOnly, errorMessage = null) }
    }

    fun submitWithdrawal() {
        val current = uiState.value
        when (val result = withdrawRepository.submitWithdrawal(
            nominal = current.selectedNominal,
            fullName = current.fullName,
            danaNumber = current.danaNumber,
            method = current.selectedMethod
        )) {
            is WithdrawSubmissionResult.Success -> {
                _formState.update {
                    it.copy(
                        fullName = "",
                        danaNumber = "",
                        errorMessage = null,
                        successRecord = result.record
                    )
                }
            }
            is WithdrawSubmissionResult.Error -> {
                _formState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun dismissSuccessDialog() {
        _formState.update { it.copy(successRecord = null) }
    }

    class Factory(
        private val withdrawRepository: WithdrawRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WithdrawViewModel(withdrawRepository) as T
        }
    }
}
