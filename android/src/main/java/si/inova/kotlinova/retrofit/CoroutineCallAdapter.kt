package si.inova.kotlinova.retrofit

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A [CallAdapter.Factory] for use with Kotlin coroutines.
 *
 * Adding this class to [Retrofit] allows you to return [Deferred] from
 * service methods.
 *
 * In addition to original CoroutineCallAdapterFactory class,
 * this class also supports optional [responseParser] constructor parameter that can provide
 * custom error message parsing.
 *
 * @author original CoroutineCallAdapterFactory by Jake Wharton, adapted by Matej Drobnic
 */
class CoroutineCallAdapterFactory constructor(private val responseParser: ResponseParser? = null) :
    CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "Deferred return type must be parameterized " +
                    "as Deferred<Foo> or Deferred<out Foo>"
            )
        }
        val responseType = getParameterUpperBound(0, returnType)

        return BodyCallAdapter<Any>(responseType)
    }

    private inner class BodyCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Deferred<T>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Deferred<T> {
            val deferred = CompletableDeferred<T>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    val cause = t.cause
                    if (t is IOException && cause != null) {
                        deferred.completeExceptionally(cause)
                    } else {
                        deferred.completeExceptionally(t)
                    }
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (responseParser == null) {
                        if (response.isSuccessful) {
                            deferred.complete(response.body()!!)
                        } else {
                            deferred.completeExceptionally(HttpException(response))
                        }
                    } else {
                        try {
                            deferred.complete(responseParser.parseResponse(response))
                        } catch (e: Exception) {
                            deferred.completeExceptionally(e)
                        }
                    }
                }
            })

            return deferred
        }
    }

    interface ResponseParser {
        fun <T> parseResponse(response: Response<T>): T
    }
}