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

    // This method is overridden in the SettingsActivity. Why?
    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }

// Are these delegate calls really necessary considering that the base Activity class already has a  delegate initialized lazily?
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        delegate.installViewFactory()
//        delegate.onCreate(savedInstanceState)
//    }

//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//        delegate.onPostCreate(savedInstanceState)
//    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        delegate.setSupportActionBar(toolbar)
        delegate.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

//    override fun getMenuInflater(): MenuInflater {
//        return delegate.menuInflater
//    }

//    override fun setContentView(@LayoutRes layoutResID: Int) {
//        delegate.setContentView(layoutResID)
//    }

//    override fun setContentView(view: View) {
//        delegate.setContentView(view)
//    }

//    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
//        delegate.setContentView(view, params)
//    }

//    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
//        delegate.addContentView(view, params)
//    }

//    override fun onPostResume() {
//        super.onPostResume()
//        delegate.onPostResume()
//    }

//    override fun onTitleChanged(title: CharSequence, color: Int) {
//        super.onTitleChanged(title, color)
//        delegate.setTitle(title)
//    }

//    override fun onStop() {
//        super.onStop()
//        delegate.onStop()
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        delegate.onDestroy()
//    }

//    private val delegate: AppCompatDelegate by lazy {
//        AppCompatDelegate.create(this, null)
//    }
}
