package com.simprints.id.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import com.simprints.id.R;

import java.util.Locale;

public class Language {
    public static Configuration selectLanguage(Context context) {
        String languageToLoad = new SharedPrefHelper(context).getLanguageString();

        Locale locale = new Locale(languageToLoad);
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
