package com.simprints.core.tools.activity

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
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

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }
}
