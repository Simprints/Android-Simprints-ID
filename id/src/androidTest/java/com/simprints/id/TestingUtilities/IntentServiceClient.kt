package com.simprints.id.TestingUtilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import java.util.concurrent.CountDownLatch

class IntentServiceClient {

    companion object {

        @JvmStatic
        fun callAsync(context: Context, intentAction: String, extras: Bundle?,
                      callback: (resultCode: Int, resultData: Bundle?) -> Unit) {
            val superExtras = Bundle(extras)
            superExtras.putParcelable(Intent.EXTRA_RESULT_RECEIVER, object : ResultReceiver(Handler()) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    super.onReceiveResult(resultCode, resultData)
                    callback(resultCode, resultData)
                }
            })

            val serviceIntent = Intent(intentAction)
            serviceIntent.putExtras(superExtras)
            context.startService(serviceIntent)
        }

        @JvmStatic
        fun callAsync(context: Context, intentAction: String,
                      callback: (resultCode: Int, resultData: Bundle?) -> Unit) {
            this.callAsync(context, intentAction, null, callback)
        }

        @JvmStatic
        fun callSync(context: Context, intentAction: String, extras: Bundle?): Pair<Int, Bundle?> {
            val done = CountDownLatch(1)
            var futureResultCode: Int = Activity.RESULT_CANCELED
            var futureResultData : Bundle? = null
            callAsync(context, intentAction, extras) { resultCode, resultData ->
                futureResultCode = resultCode
                futureResultData = resultData
                done.countDown()
            }
            done.await()
            return Pair(futureResultCode, futureResultData)
        }

        @JvmStatic
        fun callSync(context: Context, intentAction: String): Pair<Int, Bundle?> {
            return callSync(context, intentAction, null)
        }

    }

}