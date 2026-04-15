package com.simprints.core.tools.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.broadcasts.InternalBroadcaster
import com.simprints.core.tools.utils.LanguageHelper

/**
 * This base activity unifies calls to attachBaseContext to make sure the correct language is set for
 * the activity
 */
@ExcludedFromGeneratedTestCoverageReports("Abstract base class")
abstract class BaseActivity : AppCompatActivity {
    constructor() : super()
    constructor(
        @LayoutRes contentLayoutId: Int,
    ) : super(contentLayoutId)

    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            if (InternalBroadcaster.LOGOUT_ACTION != intent.action) return
            val isProjectEnded = intent.getBooleanExtra(InternalBroadcaster.LOGOUT_EXTRA_IS_PROJECT_ENDED, false)

            onLogout(isProjectEnded)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            logoutReceiver,
            IntentFilter(InternalBroadcaster.LOGOUT_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(logoutReceiver)
    }

    abstract fun onLogout(isProjectEnded: Boolean)
}
