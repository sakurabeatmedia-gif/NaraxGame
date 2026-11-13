package com.example.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.example.model.CoinConversion
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

/**
 * Abstraction over the underlying Start.io ad SDK so RewardedAdManager
 * can be verified cleanly in JVM unit tests while delegating directly
 * to StartAppAd at runtime.
 */
interface AdSdkBridge {
    fun loadRewarded(
        onLoaded: () -> Unit,
        onFailed: (String?) -> Unit
    )

    fun isRewardedReady(): Boolean

    fun showRewarded(
        onVideoCompleted: () -> Unit,
        onDisplayed: () -> Unit,
        onHidden: () -> Unit,
        onNotDisplayed: () -> Unit
    ): Boolean

    fun loadInterstitial(
        onLoaded: () -> Unit,
        onFailed: (String?) -> Unit
    )

    fun isInterstitialReady(): Boolean

    fun showInterstitial(
        onDisplayed: () -> Unit,
        onHidden: () -> Unit,
        onNotDisplayed: () -> Unit
    ): Boolean
}

class StartIoAdBridge(
    private val context: Context
) : AdSdkBridge {
    private val rewardedAd = StartAppAd(context.applicationContext)
    private val interstitialAd = StartAppAd(context.applicationContext)

    override fun loadRewarded(onLoaded: () -> Unit, onFailed: (String?) -> Unit) {
        rewardedAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                onLoaded()
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                onFailed(ad?.errorMessage)
            }
        })
    }

    override fun isRewardedReady(): Boolean = rewardedAd.isReady

    override fun showRewarded(
        onVideoCompleted: () -> Unit,
        onDisplayed: () -> Unit,
        onHidden: () -> Unit,
        onNotDisplayed: () -> Unit
    ): Boolean {
        rewardedAd.setVideoListener {
            onVideoCompleted()
        }
        return rewardedAd.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                onHidden()
            }

            override fun adDisplayed(ad: Ad) {
                onDisplayed()
            }

            override fun adClicked(ad: Ad) = Unit

            override fun adNotDisplayed(ad: Ad) {
                onNotDisplayed()
            }
        })
    }

    override fun loadInterstitial(onLoaded: () -> Unit, onFailed: (String?) -> Unit) {
        interstitialAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                onLoaded()
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                onFailed(ad?.errorMessage)
            }
        })
    }

    override fun isInterstitialReady(): Boolean = interstitialAd.isReady

    override fun showInterstitial(
        onDisplayed: () -> Unit,
        onHidden: () -> Unit,
        onNotDisplayed: () -> Unit
    ): Boolean {
        return interstitialAd.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                onHidden()
            }

            override fun adDisplayed(ad: Ad) {
                onDisplayed()
            }

            override fun adClicked(ad: Ad) = Unit

            override fun adNotDisplayed(ad: Ad) {
                onNotDisplayed()
            }
        })
    }
}

class RewardedAdManager(
    private val bridge: AdSdkBridge,
    private val onGrantCoins: (Long) -> Unit,
    private val elapsedRealtimeMs: () -> Long = { SystemClock.elapsedRealtime() },
    private val scheduleDelay: (Long, Runnable) -> Unit = { delayMs, runnable ->
        Handler(Looper.getMainLooper()).postDelayed(runnable, delayMs)
    },
    private val random: Random = Random.Default
) {
    constructor(
        context: Context,
        onGrantCoins: (Long) -> Unit
    ) : this(
        bridge = StartIoAdBridge(context),
        onGrantCoins = onGrantCoins
    )

    companion object {
        const val COOLDOWN_MS = 5_000L
        private const val MAX_RETRIES = 3
        private const val BASE_RETRY_DELAY_MS = 3_000L
    }

    private var isLoadingRewarded = false
    private var rewardedRetryAttempt = 0

    private var isLoadingInterstitial = false
    private var interstitialRetryAttempt = 0

    private val isShowingAd = AtomicBoolean(false)
    private val sessionSequence = AtomicLong(0L)
    private var activeRewardSessionId = -1L
    private val rewardClaimedForSession = AtomicBoolean(false)
    private var rewardedCoinsEarnedInSession = 0L

    private var lastAdCompletedOrTriggeredMs = -COOLDOWN_MS

    fun preloadAll() {
        loadRewarded()
        loadInterstitial()
    }

    fun isRewardedReady(): Boolean = bridge.isRewardedReady()

    fun isInterstitialReady(): Boolean = bridge.isInterstitialReady()

    fun isInCooldown(): Boolean {
        val elapsed = elapsedRealtimeMs() - lastAdCompletedOrTriggeredMs
        return elapsed < COOLDOWN_MS
    }

    fun loadRewarded() {
        if (isLoadingRewarded || bridge.isRewardedReady()) return
        isLoadingRewarded = true
        bridge.loadRewarded(
            onLoaded = {
                isLoadingRewarded = false
                rewardedRetryAttempt = 0
            },
            onFailed = {
                isLoadingRewarded = false
                if (rewardedRetryAttempt < MAX_RETRIES) {
                    val delay = BASE_RETRY_DELAY_MS * (1L shl rewardedRetryAttempt)
                    rewardedRetryAttempt++
                    scheduleDelay(delay) { loadRewarded() }
                }
            }
        )
    }

    fun loadInterstitial() {
        if (isLoadingInterstitial || bridge.isInterstitialReady()) return
        isLoadingInterstitial = true
        bridge.loadInterstitial(
            onLoaded = {
                isLoadingInterstitial = false
                interstitialRetryAttempt = 0
            },
            onFailed = {
                isLoadingInterstitial = false
                if (interstitialRetryAttempt < MAX_RETRIES) {
                    val delay = BASE_RETRY_DELAY_MS * (1L shl interstitialRetryAttempt)
                    interstitialRetryAttempt++
                    scheduleDelay(delay) { loadInterstitial() }
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video ad.
     * Coins are ONLY granted when Start.io VideoListener.onVideoCompleted fires
     * for the active session, with strict anti-double-claim and 5-second cooldown.
     */
    fun showRewardedVideo(
        onCompletedWithReward: (coinsAwarded: Long) -> Unit,
        onClosedWithoutReward: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        if (isShowingAd.get() || isInCooldown()) {
            onUnavailable()
            return
        }

        if (!bridge.isRewardedReady()) {
            loadRewarded()
            onUnavailable()
            return
        }

        if (!isShowingAd.compareAndSet(false, true)) {
            onUnavailable()
            return
        }

        val currentSession = sessionSequence.incrementAndGet()
        activeRewardSessionId = currentSession
        rewardClaimedForSession.set(false)
        rewardedCoinsEarnedInSession = 0L
        lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()

        val started = bridge.showRewarded(
            onVideoCompleted = {
                if (activeRewardSessionId == currentSession &&
                    rewardClaimedForSession.compareAndSet(false, true)
                ) {
                    val nominalRupiah = CoinConversion.REWARD_NOMINALS_RUPIAH.random(random)
                    val coins = CoinConversion.rupiahToCoins(nominalRupiah)
                    rewardedCoinsEarnedInSession = coins
                    onGrantCoins(coins)
                }
            },
            onDisplayed = {
                lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()
            },
            onHidden = {
                if (activeRewardSessionId == currentSession) {
                    val earned = if (rewardClaimedForSession.get()) rewardedCoinsEarnedInSession else 0L
                    activeRewardSessionId = -1L
                    isShowingAd.set(false)
                    lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()
                    loadRewarded()
                    if (earned > 0L) {
                        onCompletedWithReward(earned)
                    } else {
                        onClosedWithoutReward()
                    }
                }
            },
            onNotDisplayed = {
                if (activeRewardSessionId == currentSession) {
                    val alreadyEarned = rewardClaimedForSession.get()
                    activeRewardSessionId = -1L
                    isShowingAd.set(false)
                    lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()
                    loadRewarded()
                    if (!alreadyEarned) {
                        onUnavailable()
                    } else {
                        onCompletedWithReward(rewardedCoinsEarnedInSession)
                    }
                }
            }
        )

        if (!started) {
            activeRewardSessionId = -1L
            isShowingAd.set(false)
            loadRewarded()
            onUnavailable()
        }
    }

    /**
     * Shows a skippable Interstitial ad.
     * NEVER grants coins, moves, or any reward.
     */
    fun showInterstitial(
        onClosed: () -> Unit
    ) {
        if (isShowingAd.get() || isInCooldown() || !bridge.isInterstitialReady()) {
            loadInterstitial()
            onClosed()
            return
        }

        if (!isShowingAd.compareAndSet(false, true)) {
            onClosed()
            return
        }

        val currentSession = sessionSequence.incrementAndGet()
        lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()

        val started = bridge.showInterstitial(
            onDisplayed = {
                lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()
            },
            onHidden = {
                if (isShowingAd.compareAndSet(true, false)) {
                    lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()
                    loadInterstitial()
                    onClosed()
                }
            },
            onNotDisplayed = {
                if (isShowingAd.compareAndSet(true, false)) {
                    lastAdCompletedOrTriggeredMs = elapsedRealtimeMs()
                    loadInterstitial()
                    onClosed()
                }
            }
        )

        if (!started && sessionSequence.get() == currentSession) {
            isShowingAd.set(false)
            loadInterstitial()
            onClosed()
        }
    }
}
