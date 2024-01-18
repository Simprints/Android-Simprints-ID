package com.simprints.infra.logging

import com.google.firebase.FirebaseNetworkException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLProtocolException

/**
 * A very lightweight wrapper around Timber in case we ever decide to use a different logging
 * library.
 * @see <a href="URL#https://github.com/JakeWharton/timber">Timber</a>
 */
object Simber {

    internal const val USER_PROPERTY_TAG = "zzUserPropertyTag"
    private const val FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH = 100
    private const val FIREBASE_ANALYTICS_MAX_TAG_LENGTH = 24
    private const val FIREBASE_ANALYTICS_MAX_USER_TAG_LENGTH = 40
    private val firebaseInvalidCharactersRegex = Regex("[^a-zA-Z0-9_]")

    /**
     * Use this when you want to go absolutely nuts with your logging. If for some reason you've
     * decided to log every little thing in a particular part of your app, use the Simber.v tag.
     * DEBUG: Is sent to Log.v
     * STAGING: Is sent to Log.v
     * RELEASE: Is ignored
     */
    fun v(t: Throwable) = Timber.v(t)

    fun v(message: String, vararg args: Any?) = Timber.v(message, *args)

    fun v(t: Throwable, message: String, args: Any? = null) = Timber.v(t, message, args)

    /**
     * Use this for debugging purposes. If you want to print out a bunch of messages so you can log
     * the exact flow of your program, use this. If you want to keep a log of variable values,
     * use this.
     * DEBUG: Is sent to Log.d
     * STAGING: Is sent to Log.d
     * RELEASE: Is ignored
     */
    @JvmStatic
    fun d(t: Throwable) = Timber.d(t)

    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)

    fun d(t: Throwable, message: String, args: Any? = null) = Timber.d(t, message, args)

    /**
     * Use this to post useful information to the log. For example: that you have successfully
     * connected to a server. Basically use it to report events that we need to be
     * aware of.
     * DEBUG: Is sent to Log.i
     * STAGING: Is sent to Log.i & sent to Firebase Analytics as an event, and Crashlytics as a breadcrumb
     * RELEASE: Is sent to Firebase Analytics as an event, and Crashlytics as a breadcrumb
     */
    fun i(t: Throwable) = Timber.i(t)

    fun i(message: String, vararg args: Any?) =
        Timber.i(limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), *args)

    fun i(t: Throwable, message: String, args: Any? = null) =
        Timber.i(t, limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), args)

    /**
     * Use this when you suspect something shady is going on. You may not be completely in full on
     * error mode, but maybe you recovered from some unexpected behavior. Basically, use this to log
     * stuff you didn't expect to happen but isn't necessarily an error. Kind of like a "hey, this
     * happened, and it's weird, we should look into it."
     *
     * NOTE: Set list of throwable classes are not reported to Crashlytics to reduce amount of
     * false-positive issues. Such exceptions are usually part of normal error flow,
     * e.g. connection timeout in bad network conditions.
     *
     * DEBUG: Is sent to Log.w
     * STAGING: Is sent to Log.w & sent to Firebase Crashlytics
     * RELEASE: Is sent to Firebase Crashlytics
     */
    fun w(t: Throwable) {
        if (shouldSkipThrowableReporting(t)) Timber.i(t)
        else Timber.w(t)
    }

    fun w(message: String, vararg args: Any?) =
        Timber.w(limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), *args)

    fun w(t: Throwable, message: String, args: Any? = null) {
        if (shouldSkipThrowableReporting(t))
            Timber.i(t, limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), args)
        else
            Timber.w(t, limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), args)
    }

    /**
     * This is for when bad stuff happens. You know that an error has occurred and therefore you're
     * logging an error. NOTE: There is an important difference between Exceptions and Errors.
     * Exceptions are used to pass information and handle logic, but this doesn't mean they are bad.
     * We only log Exceptions as errors when we know the product is not behaving and expected.
     *
     * NOTE: Set list of throwable classes are not reported to Crashlytics to reduce amount of
     * false-positive issues. Such exceptions are usually part of normal error flow,
     * e.g. connection timeout in bad network conditions.
     *
     * DEBUG: Is sent to Log.e
     * STAGING: Is sent to Log.e & sent to Firebase Crashlytics
     * RELEASE: Is sent to Firebase Crashlytics
     */
    fun e(t: Throwable) {
        if (shouldSkipThrowableReporting(t))
            Timber.i(t)
        else
            Timber.e(t)
    }

    fun e(message: String, vararg args: Any?) =
        Timber.e(limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), *args)

    fun e(t: Throwable, message: String, args: Any? = null) {
        if (shouldSkipThrowableReporting(t))
            Timber.i(t, limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), args)
        else
            Timber.e(t, limitLength(message, FIREBASE_ANALYTICS_MAX_MESSAGE_LENGTH), args)
    }

    /**
     * Adds a custom tag to the log.
     * @param tag Custom tag to add to log
     * @param isUserProperty Marks the tag as a user property. Default is false. If you set this
     * value to true your log will be added as a custom key to Crashlytics, and as a user property
     * to Firebase Analytics. The tag will be the Key and the message will be the Value. This param
     * respects the BuildType settings, so a user property set with Simber.d or Simber.v will not
     * be reported in production.
     *
     * @see <a href="URL#https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android">Crashlytics</a>
     * @see <a href="URL#https://firebase.google.com/docs/analytics/user-properties?platform=android">Firebase Analytics</a>
     *
     * NOTE!! "Crashlytics supports a maximum of 64 key/value pairs. After
     * you reach this threshold, additional values are not saved. Each key/value pair can be up to
     * 1 kB in size."
     */
    fun tag(tag: String, isUserProperty: Boolean = false): Simber {
        var conformingTag = ensureCharactersAreValid(tag)

        conformingTag = if (isUserProperty) {
            limitLength(USER_PROPERTY_TAG + conformingTag, FIREBASE_ANALYTICS_MAX_USER_TAG_LENGTH)
        } else {
            limitLength(conformingTag, FIREBASE_ANALYTICS_MAX_TAG_LENGTH)
        }

        Timber.tag(conformingTag)
        return Simber
    }

    /*
    * Check that tag complies with Firebase Analytics requirements:
    * Name must consist of letters, digits or _ (underscores).
     */
    private fun ensureCharactersAreValid(tag: String): String {
        if (tag.contains(firebaseInvalidCharactersRegex)) {
            // Throw an exception in debug but replace invalid characters in other modes
            if (BuildConfig.DEBUG) {
                throw IllegalArgumentException("Tag must consist of letters, digits or _ (underscores).")
            } else {
                return tag.replace(firebaseInvalidCharactersRegex, "_")
            }
        }
        return tag
    }

    private fun limitLength(message: String, max: Int): String {
        if (message.length > max) {
            // TODO revert comment
//            if (BuildConfig.DEBUG) {
//                throw IllegalArgumentException("String must be less than $max characters.")
//            }

            return message.substring(0, max)
        }
        return message
    }

    // Some exception do not provide any value when logged into crashlytics,
    // e.g. issues due to bad network
    private fun shouldSkipThrowableReporting(t: Throwable) =
        isSkippableException(t) || isSkippableException(t.cause)

    private fun isSkippableException(it: Throwable?) = it is SocketTimeoutException
        || it is UnknownHostException
        || it is SSLProtocolException
        || it is SSLHandshakeException
        || it is FirebaseNetworkException

}
