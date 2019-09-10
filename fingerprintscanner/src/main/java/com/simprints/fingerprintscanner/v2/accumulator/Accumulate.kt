package com.simprints.fingerprintscanner.v2.accumulator

import io.reactivex.Flowable

fun <Fragment, FragmentCollection, Element> Flowable<out Fragment>.accumulateAndTakeElements(
    accumulator: Accumulator<Fragment, FragmentCollection, Element>
): Flowable<Element> = this
    .accumulate(accumulator)
    .takeElements()

fun <Fragment, FragmentCollection, Element> Flowable<out Fragment>.accumulate(
    accumulator: Accumulator<Fragment, FragmentCollection, Element>
): Flowable<Accumulator<Fragment, FragmentCollection, Element>> =
    scan(accumulator) { acc: Accumulator<Fragment, FragmentCollection, Element>, newFragment: Fragment ->
        acc.apply { updateWithNewFragment(newFragment) }
    }

fun <Fragment, FragmentCollection, Element> Flowable<out Accumulator<Fragment, FragmentCollection, Element>>.takeElements(): Flowable<Element> =
    concatMap {
        it.takeElements()
    }
