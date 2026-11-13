package com.example

import android.app.Application
import com.example.ads.RewardedAdManager
import com.example.data.GameRepository
import com.example.data.WithdrawRepository
import com.startapp.sdk.adsbase.StartAppSDK

class NaraGameApplication : Application() {
    lateinit var withdrawRepository: WithdrawRepository
        private set
    lateinit var gameRepository: GameRepository
        private set
    lateinit var rewardedAdManager: RewardedAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        StartAppSDK.init(this, STARTIO_APP_ID, true)

        withdrawRepository = WithdrawRepository(this)
        gameRepository = GameRepository(this)
        rewardedAdManager = RewardedAdManager(
            context = this,
            onGrantCoins = { coins ->
                withdrawRepository.addCoins(coins)
            }
        )
        rewardedAdManager.preloadAll()
    }

    companion object {
        const val STARTIO_APP_ID = "210355804"
    }
}
