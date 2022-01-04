package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {
    private const val RESOURCE = "GLOBAL"

    @JvmField
    // this countingIdlingResource allows to increment and decrement a counter
    // such that if counter >0 app is working, <= 0 app is idle, work has been done
    // in prod code, we will need to increment the counter when the work started
    // and decrement it once it finishes
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
    // Espresso does not work well with coroutines yet. See
    // https://github.com/Kotlin/kotlinx.coroutines/issues/982
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        function()
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}
