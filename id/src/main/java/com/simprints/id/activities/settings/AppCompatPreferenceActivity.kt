package com.simprints.id.activities.settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.simprints.core.tools.utils.LanguageHelper

/**
 * An [AppCompatActivity] which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
abstract class AppCompatPreferenceActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        delegate.setSupportActionBar(toolbar)
        delegate.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
