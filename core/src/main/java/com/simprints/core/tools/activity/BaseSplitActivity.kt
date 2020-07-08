package com.simprints.core.tools.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import com.simprints.core.tools.utils.LanguageHelper

/**
 * This base activity unifies calls to attachBaseContext to make sure the correct language is set for
 * the activity and we get instant access to the activity after installing the dynamic feature module as described in:
 * https://developer.android.com/guide/app-bundle/playcore#invoke_splitcompat_at_runtime
 */
abstract class BaseSplitActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
        SplitCompat.installActivity(this)
    }
}
