package com.r0adkll.gossip.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <I> LiveData<I>.doOnNext(onNext: (I) -> Unit): LiveData<I> {
    return MediatorLiveData<I>().apply {
        addSource(this@doOnNext) {
            onNext(it)
        }
    }
}

fun <A, B, R> LiveData<A>.combineLatest(b: LiveData<B>, combiner: (A, B) -> R): LiveData<R> {
    return MediatorLiveData<R>().apply {
        var lastA: A? = null
        var lastB: B? = null

        addSource(this@combineLatest) {
            if (it == null && value != null) value = null
            lastA = it
            if (lastA != null && lastB != null) value = combiner(lastA!!, lastB!!)
        }

        addSource(b) {
            if (it == null && value != null) value = null
            lastB = it
            if (lastA != null && lastB != null) value = combiner(lastA!!, lastB!!)
        }
    }
}
