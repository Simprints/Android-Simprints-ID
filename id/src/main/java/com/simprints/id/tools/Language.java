package com.simprints.id.tools;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class Language {
    public static Configuration selectLanguage(Context context) {
        String languageToLoad = new SharedPref(context).getLanguageString();

        Locale locale;

        /*
         * Languages are usually two characters, except if they also contain a region.
         * If they contain a region they follow the format [language]-r[REGION] e.g. fa-rAF
         */
        if (languageToLoad.contains("-r") && languageToLoad.length() > 2) {

            int indexOfFlag = -1;
            for (int i = 0; i < languageToLoad.length() - 1; i++) {
                if (languageToLoad.substring(i, i + 2).equals("-r")) {
                    indexOfFlag = i;
                }
            }

            String language = languageToLoad.substring(0, indexOfFlag);
            String country = languageToLoad.substring(indexOfFlag + 1, languageToLoad.length());

            locale = new Locale(language, country);

        } else {
            locale = new Locale(languageToLoad);
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        return config;
    }
}
