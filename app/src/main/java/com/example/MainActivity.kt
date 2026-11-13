package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.GameViewModel
import com.example.ui.WithdrawViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.NaraGameTheme

const val ROUTE_MENU = "menu"
const val ROUTE_GAME = "game"
const val ROUTE_WITHDRAW = "withdraw"
const val ROUTE_HISTORY = "history"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NaraGameApplication

        setContent {
            NaraGameTheme {
                NaraGameNavHost(app = app)
            }
        }
    }
}

@Composable
private fun NaraGameNavHost(app: NaraGameApplication) {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(
            gameRepository = app.gameRepository,
            withdrawRepository = app.withdrawRepository,
            adManager = app.rewardedAdManager
        )
    )
    val withdrawViewModel: WithdrawViewModel = viewModel(
        factory = WithdrawViewModel.Factory(
            withdrawRepository = app.withdrawRepository
        )
    )

    val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val coinBalance by gameViewModel.coinBalance.collectAsStateWithLifecycle()
    val withdrawState by withdrawViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = ROUTE_MENU
    ) {
        composable(ROUTE_MENU) {
            MainMenuScreen(
                gameState = gameState,
                coinBalance = coinBalance,
                onPlayClick = { navController.navigate(ROUTE_GAME) },
                onWithdrawClick = { navController.navigate(ROUTE_WITHDRAW) },
                onHistoryClick = { navController.navigate(ROUTE_HISTORY) },
                onOpenSettings = { gameViewModel.openSettings() },
                onDismissSettings = { gameViewModel.dismissSettings() },
                onToggleSound = { gameViewModel.toggleSound(it) },
                onToggleVibration = { gameViewModel.toggleVibration(it) },
                onResetToLevelOne = { gameViewModel.onResetToLevelOne() }
            )
        }

        composable(ROUTE_GAME) {
            GameScreen(
                uiState = gameState,
                coinBalance = coinBalance,
                onBackClick = { navController.popBackStack() },
                onSwipe = { from, to -> gameViewModel.onSwipe(from, to) },
                onBoosterClick = { type -> gameViewModel.selectBooster(type) },
                onBoosterTileTap = { pos -> gameViewModel.onTileTappedForBooster(pos) },
                onContinueWithAd = { gameViewModel.onContinueWithAdClicked() },
                onQuitOutOfMoves = { gameViewModel.onQuitOutOfMovesClicked() },
                onNextLevel = { gameViewModel.onAdvanceNextLevelClicked() },
                onRetryLevel = { gameViewModel.onRetryCurrentLevelClicked() },
                onBackToMenuFromGameOver = {
                    gameViewModel.onBackToMenuFromGameOver {
                        navController.popBackStack(ROUTE_MENU, inclusive = false)
                    }
                },
                onOpenSettings = { gameViewModel.openSettings() },
                onDismissSettings = { gameViewModel.dismissSettings() },
                onToggleSound = { gameViewModel.toggleSound(it) },
                onToggleVibration = { gameViewModel.toggleVibration(it) },
                onResetToLevelOne = { gameViewModel.onResetToLevelOne() },
                onDismissAdUnavailable = { gameViewModel.dismissAdUnavailable() }
            )
        }

        composable(ROUTE_WITHDRAW) {
            WithdrawScreen(
                uiState = withdrawState,
                onBackClick = { navController.popBackStack() },
                onSelectMethod = { withdrawViewModel.selectMethod(it) },
                onSelectNominal = { withdrawViewModel.selectNominal(it) },
                onFullNameChange = { withdrawViewModel.updateFullName(it) },
                onDanaNumberChange = { withdrawViewModel.updateDanaNumber(it) },
                onSubmitClick = { withdrawViewModel.submitWithdrawal() },
                onDismissSuccess = { withdrawViewModel.dismissSuccessDialog() }
            )
        }

        composable(ROUTE_HISTORY) {
            HistoryScreen(
                coinBalance = withdrawState.coinBalance,
                history = withdrawState.history,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
