package com.simprints.core.tools

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LanguageHelper {

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
}
