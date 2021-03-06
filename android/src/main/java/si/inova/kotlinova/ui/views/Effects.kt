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

package si.inova.kotlinova.ui.views

import android.annotation.TargetApi
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.StateSet
import androidx.annotation.ColorInt

/**
 * @author Matej Drobnic
 */

/**
 * @return ripple that ripples in provided color
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun createRipple(@ColorInt color: Int): RippleDrawable {
    val colorState = ColorStateList(
        arrayOf(intArrayOf()),
        intArrayOf(color)
    )

    return RippleDrawable(colorState, null, ColorDrawable(Color.BLACK))
}

/**
 * Method to create legacy (color switching) press effect for pre-Lollipop devices.
 *
 * @return Drawable that is normally transparent but changes to provided color when pressed.
 */
private fun createLegacyPressEffect(@ColorInt color: Int): StateListDrawable {
    val normalDrawable = ColorDrawable(Color.TRANSPARENT)
    val pressedDrawable = ColorDrawable(color)

    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        addState(StateSet.WILD_CARD, normalDrawable)
    }
}

/**
 * @return Press effect with specific color.
 * Depending on the platform it can be ripple or standard color changing drawable.
 */
fun createPressEffect(@ColorInt color: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        createRipple(color)
    } else {
        createLegacyPressEffect(color)
    }
}