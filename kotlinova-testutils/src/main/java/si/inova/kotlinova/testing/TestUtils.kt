package si.inova.kotlinova.testing

/**
 * @author Matej Drobnic
 */

import android.arch.lifecycle.LiveData
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.createInstance
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.first
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.robolectric.shadows.ShadowSystemClock
import si.inova.kotlinova.coroutines.UI
import si.inova.kotlinova.coroutines.toChannel

infix fun <T> Comparable<T>.isGreaterThan(other: T) {
    assertTrue("$this is greater than $other", this > other)
}

infix fun <T> Comparable<T>.isEqualTo(other: T) {
    assertTrue("$this is equal than $other", this.compareTo(other) == 0)
}

fun advanceTime(ms: Int) {
    advanceTime(ms.toLong())
}

fun advanceTime(ms: Long) {
    ShadowSystemClock.sleep(ms)
}

/**
 * Variant of Mockito's [argThat] that provides better error printing for easier debugging.
 *
 * @param block Code where you specify pair of objects (*modified argument to check*) to check for inequality.
 */
inline fun <reified T : Any> paramEquals(crossinline block: (T) -> Pair<T?, T?>): T {
    val matcher = ArgumentMatcher<T> { argument ->
        val pair = block(argument)
        Assert.assertEquals(pair.second, pair.first)
        true
    }

    return Mockito.argThat(matcher) ?: createInstance(T::class)
}

suspend fun <T> LiveData<T>.waitUntil(predicate: (T?) -> Boolean): Deferred<T?> {
    return async(UI) { toChannel().first(predicate) }
}

fun <T, R : Any> assertIs(test: R?, expectedClass: Class<T>) {
    assertTrue(
            "Object should be ${expectedClass.name}, but is ${test?.javaClass?.name}",
            test != null && expectedClass.isAssignableFrom(test.javaClass)
    )
}