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
    private fun localeFor(languageString: String): Locale =

        with(languageString) {
            if (contains("-r") && length > 2) {

                var indexOfFlag = -1
                for (i in 0 until length - 1) {
                    if (substring(i, i + 2) == "-r") {
                        indexOfFlag = i
                    }
                }

                val language = substring(0, indexOfFlag)
                val country = substring(indexOfFlag + 1, length)

                Locale(language, country)

            } else {
                Locale(languageString)
            }
        }

    fun contextWithSpecificLanguage(context: Context, languageString: String): Context =
        context.createConfigurationContext(configurationWithSpecificLocale(languageString))
}
