package com.simprints.id.tools

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl.Companion.LANGUAGE_DEFAULT
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl.Companion.LANGUAGE_KEY
import timber.log.Timber
import java.util.*

object LanguageHelper {

    lateinit var prefs: SharedPreferences
    var language: String
        get() {
            return prefs.getString(LANGUAGE_KEY, LANGUAGE_DEFAULT)!!
        }
        set(value) {
            prefs.edit().putString(LANGUAGE_KEY, value).apply()
        }

    fun init(ctx: Context){
        prefs = ctx.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, Context.MODE_PRIVATE)
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

    fun contextWithSpecificLanguage(context: Context, languageString: String): Context =
        context.createConfigurationContext(configurationWithSpecificLocale(languageString))

    fun getLanguageConfigurationContext(ctx: Context): Context {
        println("Testing language: $language")
        val conf = configurationWithSpecificLocale(language)
        return ctx.createConfigurationContext(conf)
    }
}
