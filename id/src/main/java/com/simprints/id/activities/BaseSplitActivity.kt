package com.simprints.id.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import com.simprints.id.tools.LanguageHelper
import org.intellij.lang.annotations.Language

abstract class BaseSplitActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
        SplitCompat.installActivity(this)
    }
}
