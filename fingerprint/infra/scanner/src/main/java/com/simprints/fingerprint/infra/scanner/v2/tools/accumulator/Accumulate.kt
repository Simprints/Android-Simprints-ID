package com.simprints.fingerprint.infra.scanner.v2.tools.accumulator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.scan

/**
 * Transforms a stream of [Fragment]s into [Element]s.
 *
 * See [Accumulator] for details on behaviour and usage.
 *
 * @param accumulator the entity that scans the stream for [Fragment]s, stores them in a [FragmentCollection], and produces [Element]s.
 * The same instance of the Accumulator is used for the lifetime of the stream.
 */
fun <Fragment, FragmentCollection, Element> Flow<Fragment>.accumulateAndTakeElements(
    accumulator: Accumulator<Fragment, FragmentCollection, Element>,
): Flow<Element> = this.accumulate(accumulator).takeElements()

private fun <Fragment, FragmentCollection, Element> Flow<Fragment>.accumulate(
    accumulator: Accumulator<Fragment, FragmentCollection, Element>,
): Flow<Accumulator<Fragment, FragmentCollection, Element>> =
    scan(accumulator) { acc: Accumulator<Fragment, FragmentCollection, Element>, newFragment: Fragment ->
        acc.apply { updateWithNewFragment(newFragment) }
    }

private fun <Fragment, FragmentCollection, Element> Flow<Accumulator<Fragment, FragmentCollection, Element>>.takeElements(): Flow<Element> =
    flatMapConcat {
        it.takeElements()
    }
