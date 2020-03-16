/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.testing.espresso.coroutines

import androidx.test.espresso.IdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.collections.WeakList
import si.inova.kotlinova.coroutines.TestableDispatchers

/**
 * Espresso idling resource and test watcher that injects
 * wrapped dispatchers into [TestableDispatchers] and monitors for any leftover jobs.
 */
class DispatchersIdlingResourceRule : TestWatcher(), IdlingResource {
    private var idlingCallback: IdlingResource.ResourceCallback? = null
    private val allJobs = WeakList<JobCheckingDispatcherWrapper>()

    override fun starting(description: Description?) {
        super.starting(description)

        TestableDispatchers.dispatcherOverride = {
            val wrappedDispatcher = JobCheckingDispatcherWrapper(it() as CoroutineDispatcher)
            registerNewDispatcher(wrappedDispatcher)

            wrappedDispatcher
        }
    }

    override fun finished(description: Description?) {
        super.finished(description)

        TestableDispatchers.dispatcherOverride = { it() }
    }

    private fun registerNewDispatcher(dispatcherWrapper: JobCheckingDispatcherWrapper) {
        dispatcherWrapper.completionEvent = this::onJobStopped

        allJobs.add(dispatcherWrapper)
    }

    private fun onJobStopped() {
        if (isIdleNow) {
            idlingCallback?.onTransitionToIdle()
        }
    }

    override fun isIdleNow(): Boolean {
        var idle = true

        allJobs.forEach {
            if (it.isAnyJobRunning) {
                idle = false
            }
        }

        return idle
    }

    override fun getName(): String {
        return "DispatchersIdlingResourceRule"
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.idlingCallback = callback
    }
}