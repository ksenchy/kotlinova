package si.inova.kotlinova.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.support.annotation.LayoutRes
import android.support.v4.view.AsyncLayoutInflater
import android.view.View
import androidx.core.view.toBitmap
import kotlinx.coroutines.experimental.withContext
import si.inova.kotlinova.coroutines.UI
import si.inova.kotlinova.coroutines.inflateAndAwait
import javax.inject.Inject

/**
 * Helper class for generating consistent
 */
class ViewImageGenerator @Inject constructor(private val context: Context) {
    /**
     * Inflate view, resize it to [width] pixels wide times *wrap content* tall
     * and take a screenshot of the whole view with specificied display [density].
     **
     * Optionally, you can also define lambda as last [viewAction] parameter
     * to perform any action on the view before screenshot is taken (for example fill in the data)
     */
    suspend fun generateViewImageWrapHeight(
        @LayoutRes layout: Int,
        width: Int,
        density: Int,
        viewAction: (View.() -> Unit)? = null
    ): Bitmap {
        return generateViewImage(
            layout,
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            density,
            viewAction
        )
    }

    /**
     * Inflate view and take a screenshot of the whole view.
     *
     * You need to specify view's width and height in [MeasureSpec][View.MeasureSpec] format
     * and simulated display's density (higher density means items will be larger in the image).
     *
     * Optionally, you can also define lambda as last [viewAction] parameter
     * to perform any action on the view before screenshot is taken (for example fill in the data)
     */
    suspend fun generateViewImage(
        @LayoutRes layout: Int,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        density: Int,
        viewAction: (View.() -> Unit)? = null
    ): Bitmap {
        val config = Configuration(context.resources.configuration)
        config.densityDpi = density
        config.fontScale = 1f

        val densityContext = context.createConfigurationContext(config)

        val inflater = withContext(UI) {
            AsyncLayoutInflater(densityContext)
        }
        val view = inflater.inflateAndAwait(layout, null)
        viewAction?.invoke(view)

        view.measure(widthMeasureSpec, heightMeasureSpec)

        val width = view.measuredWidth
        val height = view.measuredHeight

        view.layout(0, 0, width, height)
        return view.toBitmap()
    }
}