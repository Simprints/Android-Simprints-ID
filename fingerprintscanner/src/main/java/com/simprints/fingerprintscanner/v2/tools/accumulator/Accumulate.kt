package com.simprints.fingerprintscanner.v2.tools.accumulator

import io.reactivex.Flowable

/**
 * Transforms a stream of [Fragment]s into [Element]s.
 *
 * See [Accumulator] for details on behaviour and usage.
 *
 * @param accumulator the entity that scans the stream for [Fragment]s, stores them in a [FragmentCollection], and produces [Element]s.
 * The same instance of the Accumulator is used for the lifetime of the stream.
 */
fun <Fragment, FragmentCollection, Element> Flowable<out Fragment>.accumulateAndTakeElements(
    accumulator: Accumulator<Fragment, FragmentCollection, Element>
): Flowable<Element> = this
    .accumulate(accumulator)
    .takeElements()

private fun <Fragment, FragmentCollection, Element> Flowable<out Fragment>.accumulate(
    accumulator: Accumulator<Fragment, FragmentCollection, Element>
): Flowable<Accumulator<Fragment, FragmentCollection, Element>> =
    scan(accumulator) { acc: Accumulator<Fragment, FragmentCollection, Element>, newFragment: Fragment ->
        acc.apply { updateWithNewFragment(newFragment) }
    }

private fun <Fragment, FragmentCollection, Element> Flowable<out Accumulator<Fragment, FragmentCollection, Element>>.takeElements(): Flowable<Element> =
    concatMap {
        it.takeElements()
    }
