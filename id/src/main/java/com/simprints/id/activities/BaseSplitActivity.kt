package com.simprints.id.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import com.simprints.core.tools.utils.LanguageHelper

abstract class BaseSplitActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
        SplitCompat.installActivity(this)
    }
}
