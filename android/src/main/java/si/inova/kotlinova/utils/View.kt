@file:JvmName("ViewUtils")

package si.inova.kotlinova.utils

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.SwitchCompat
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

/**
 * @author Matej Drobnic
 */

/**
 * Variant of [isVisible] that fades view in/out instead of just making it appear
 */
var View.isVisibleWithFade: Boolean
    get() = isVisible
    set(value) {
        val targetAlpha = if (value) 1f else 0f

        if (!isVisible && value && alpha == 1f) {
            alpha = 0f
        }

        isVisible = true

        animate()
            .alpha(targetAlpha)
            .apply {
                if (!value) {
                    withEndAction { isVisible = false }
                }
            }
            .setDuration(250)
            .start()
    }

/**
 * Helper method to inflate given layout asynchronously
 */
fun Activity.inflateAsync(
    savedInstanceState: Bundle?,
    @LayoutRes layout: Int,
    callback: AsyncLayoutInflater.OnInflateFinishedListener
) {
    val root = findViewById<ViewGroup>(android.R.id.content)

    if (savedInstanceState == null) {
        AsyncLayoutInflater(this).inflate(layout, root, callback)
    } else {
        // If we are restoring state, we cannot inflate asynchronously as it messes with fragment lifecycle
        val view = layoutInflater.inflate(layout, root, false)
        callback.onInflateFinished(view, layout, root)
    }
}

/**
 * Helper method to inflate given layout asynchronously
 */
fun Fragment.inflateAsync(
    savedInstanceState: Bundle?,
    @LayoutRes layout: Int,
    callback: AsyncLayoutInflater.OnInflateFinishedListener
) {
    val root = view as ViewGroup

    if (savedInstanceState == null) {
        AsyncLayoutInflater(requireContext()).inflate(layout, root, callback)
    } else {
        // If we are restoring state, we cannot inflate asynchronously as it messes with fragment lifecycle
        val view = layoutInflater.inflate(layout, root, false)
        callback.onInflateFinished(view, layout, root)
    }
}

/**
 * Close soft keyboard
 */
fun View.closeKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
        windowToken,
        InputMethodManager.HIDE_NOT_ALWAYS
    )
}

/**
 * Open soft keyboard
 */
fun View.openKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.showSoftInput(
        this,
        InputMethodManager.SHOW_IMPLICIT
    )
}

/**
 * It makes view non-editable
 */
fun View.makeNonEditable() {
    isClickable = false
    isEnabled = false
}

/**
 * Set view visibility without triggering any layout animations
 * (for example animations that get triggered when animateLayoutChanges flag is enabled)
 */
fun View.setVisibilityWithoutLayoutAnimations(visibility: Int) {
    val transition = (parent as? ViewGroup)?.layoutTransition

    val appearingEnabled =
        transition?.isTransitionTypeEnabled(LayoutTransition.APPEARING) ?: false
    val disappearingEnabled =
        transition?.isTransitionTypeEnabled(LayoutTransition.DISAPPEARING) ?: false

    transition?.disableTransitionType(LayoutTransition.APPEARING)
    transition?.disableTransitionType(LayoutTransition.DISAPPEARING)

    this.visibility = visibility

    if (appearingEnabled) {
        transition!!.enableTransitionType(LayoutTransition.APPEARING)
    }
    if (disappearingEnabled) {
        transition!!.enableTransitionType(LayoutTransition.DISAPPEARING)
    }
}

fun SwitchCompat.setIsCheckedWithoutTriggeringListener(
    state: Boolean,
    listener: CompoundButton.OnCheckedChangeListener
) {
    setOnCheckedChangeListener(null)
    isChecked = state
    setOnCheckedChangeListener(listener)
}