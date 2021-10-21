package com.simprints.logging

import com.simprints.logging.trees.PerformanceMonitoringTree.getTrace
import com.simprints.logging.trees.Trace
import timber.log.Timber

/**
 * A very lightweight wrapper around Timber in case we ever decide to use a different logging
 * library.
 * @see <a href="URL#https://github.com/JakeWharton/timber">Timber</a>
 */
object Simber {

    internal const val USER_PROPERTY_TAG = "zzUserPropertyTag"
    internal const val PERFORMANCE_FLAG = "zzPerformanceFlag"

    /**
     * Use this when you want to go absolutely nuts with your logging. If for some reason you've
     * decided to log every little thing in a particular part of your app, use the Simber.v tag.
     * DEBUG: Is sent to Log.v
     * STAGING: Is sent to Log.v
     * RELEASE: Is ignored
     */
    fun v(t: Throwable) = Timber.v(t)
    fun v(message: String, args: Any? = null) = Timber.v(message, args)
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
    fun d(message: String, args: Any? = null) = Timber.d(message, args)
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
    fun i(message: String, args: Any? = null) = Timber.i(message, args)
    fun i(t: Throwable, message: String, args: Any? = null) = Timber.i(t, message, args)

    /**
     * Use this when you suspect something shady is going on. You may not be completely in full on
     * error mode, but maybe you recovered from some unexpected behavior. Basically, use this to log
     * stuff you didn't expect to happen but isn't necessarily an error. Kind of like a "hey, this
     * happened, and it's weird, we should look into it."
     * DEBUG: Is sent to Log.w
     * STAGING: Is sent to Log.w & sent to Firebase Crashlytics
     * RELEASE: Is sent to Firebase Crashlytics
     */
    fun w(t: Throwable) = Timber.w(t)
    fun w(message: String, args: Any? = null) = Timber.w(message, args)
    fun w(t: Throwable, message: String, args: Any? = null) = Timber.w(t, message, args)

    /**
     * This is for when bad stuff happens. You know that an error has occurred and therefore you're
     * logging an error. NOTE: There is an important difference between Exceptions and Errors.
     * Exceptions are used to pass information and handle logic, but this doesn't mean they are bad.
     * We only log Exceptions as errors when we know the product is not behaving and expected.
     * DEBUG: Is sent to Log.e
     * STAGING: Is sent to Log.e & sent to Firebase Crashlytics
     * RELEASE: Is sent to Firebase Crashlytics
     */
    fun e(t: Throwable) = Timber.e(t)
    fun e(message: String, args: Any? = null) = Timber.e(message, args)
    fun e(t: Throwable, message: String, args: Any? = null) = Timber.e(t, message, args)

    /**
     * Used to track the performance of something inside of the code. Call [trace] to get a [Trace]
     * instance. Call [Trace.start] to begin the trace and [Trace.stop] to end it.
     * DEBUG: Is sent to Log.i
     * STAGING: Is sent to Log.i & sent to Firebase Performance Monitoring
     * RELEASE: Is sent to Firebase Performance Monitoring
     */
    fun trace(traceName: String): Trace = getTrace(traceName, this)

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
        if (isUserProperty)
            Timber.tag(USER_PROPERTY_TAG + tag)
        else
            Timber.tag(tag)
        return Simber
    }

}
