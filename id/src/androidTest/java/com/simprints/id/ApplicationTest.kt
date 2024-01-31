package com.simprints.id

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.infra.logging.Simber
import com.simprints.testtools.android.waitOnSystem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationTest {

    @Test
    fun rxJavaUndeliverableExceptionHappens_shouldBeHandled() {

        val observable1 = Observable.create<Int> {
            Thread.sleep(100)
            it.onError(Exception("Ops1"))
        }

        val observable2 = Observable.create<Int> {
            Thread.sleep(300)

            //It will throw an UndeliverableException
            it.onError(Exception("UndeliverableException exception"))
        }

        Observables.zip(observable1, observable2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {},
                onError = { it.message?.let { Simber.d(it) } },
                onNext = {}
            )

        waitOnSystem(1000)
    }
}