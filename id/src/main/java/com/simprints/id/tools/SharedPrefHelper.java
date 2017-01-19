package com.simprints.id.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.simprints.id.R;


public class SharedPrefHelper {
    private Context context;
    private SharedPreferences sharedPref;

    public SharedPrefHelper(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
    }

    /**
     * Nudge mode. This determines if the UI should automatically slide forward
     */
    public Boolean getNudgeModeBool() {
        return sharedPref.getBoolean(context.getString(R.string.pref_nudge_mode_bool), true);
    }

    public void setNudgeModeBool(Boolean nudgeModeBool) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.pref_nudge_mode_bool), nudgeModeBool);
        editor.apply();
    }

    /**
     * Consent. Has the CHW given consent to use Simprints ID
     */
    public Boolean getConsentBool() {
        return sharedPref.getBoolean(context.getString(R.string.pref_consent_bool), true);
    }

    public void setConsentBool(Boolean consentBool) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.pref_consent_bool), consentBool);
        editor.apply();
    }

    /**
     * Quality threshold. This determines the UI feedback for the fingerprint quality
     */
    public int getQualityThresholdInt() {
        return sharedPref.getInt(context.getString(R.string.pref_quality_theshold), 60);
    }

    public void setQualityThresholdInt(int qualityThreshold) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.pref_quality_theshold), qualityThreshold);
        editor.apply();
    }

    /**
     * The number of UIDs to be returned to the calling app
     */
    public int getReturnIdCountInt() {
        return sharedPref.getInt(context.getString(R.string.pref_nb_of_ids), 10);
    }

    public void setReturnIdCountInt(int returnIdCount) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.pref_nb_of_ids), returnIdCount);
        editor.apply();
    }

    /**
     * The selected language
     */
    public String getLanguageString() {
        return sharedPref.getString(context.getString(R.string.pref_language), "");
    }

    public void setLanguageString(String languageString) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.pref_language), languageString);
        editor.apply();
    }

    /**
     * The active language position to be displayed in the list
     */
    public int getLanguagePositionInt() {
        return sharedPref.getInt(context.getString(R.string.pref_language_position), 0);
    }

    public void setLanguagePositionInt(int languagePositionInt) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.pref_language_position), languagePositionInt);
        editor.apply();
    }

    /**
     * The current database version of LibData
     */
    public int getDbVersionInt() {
        return sharedPref.getInt(context.getString(R.string.db_version_int), 0);
    }

    public void setDbVersionInt(int dbVersionInt) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.db_version_int), dbVersionInt);
        editor.apply();
    }

    /**
     * Matcher type
     */
    public int getMatcherTypeInt() {
        return sharedPref.getInt(context.getString(R.string.pref_matcher_type), 0);
    }

    public void setMatcherTypeInt(int matcherTypeInt) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.pref_matcher_type), matcherTypeInt);
        editor.apply();
    }

}
