package com.simprints.core.tools.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_FILE_NAME
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_MODE
import java.util.*

object LanguageHelper {

    const val SHARED_PREFS_LANGUAGE_KEY = "SelectedLanguage"
    const val SHARED_PREFS_LANGUAGE_DEFAULT = "en"

    lateinit var prefs: SharedPreferences
    var language: String
        get() {
            return prefs.getString(SHARED_PREFS_LANGUAGE_KEY, SHARED_PREFS_LANGUAGE_DEFAULT)!!
        }
        set(value) {
            prefs.edit().putString(SHARED_PREFS_LANGUAGE_KEY, value).apply()
        }

    fun init(ctx: Context){
        prefs = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)
    }

    private fun configurationWithSpecificLocale(languageString: String): Configuration =
        Configuration().apply {
            val locale = localeFor(languageString)
            Locale.setDefault(locale)
            setLocale(locale)
        }


    /*
     * Languages are usually two  or three characters, except if they also contain a region.
     * If they contain a region they follow the format [language]-r[REGION] e.g. fa-rAF
     */
    private fun localeFor(languageString: String): Locale {
        val localeParts = languageString.split("-r")
        val language = localeParts[0]
        return if (localeParts.size > 1) {
            Locale.Builder()
                .setLanguage(language)
                .setRegion(localeParts[1]).build()

        } else {
            Locale(localeParts[0])
        }
    }

    fun getLanguageConfigurationContext(ctx: Context): Context {
        val conf = configurationWithSpecificLocale(language)
        return ctx.createConfigurationContext(conf)
    }
}
