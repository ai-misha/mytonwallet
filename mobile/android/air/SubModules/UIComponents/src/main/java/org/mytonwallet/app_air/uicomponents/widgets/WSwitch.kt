package org.mytonwallet.app_air.uicomponents.widgets

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.StateListDrawable
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.mytonwallet.app_air.uicomponents.cat.CatSwitchThumbDrawable
import org.mytonwallet.app_air.walletbasecontext.theme.WColor
import org.mytonwallet.app_air.walletbasecontext.theme.color

class WSwitch(context: Context) :
    SwitchCompat(context),
    WThemedView {

    init {
        id = generateViewId()

        updateTheme()
    }

    override val isTinted = true
    override fun updateTheme() {
        // 🐱 The thumb is a cat face: asleep while off, wide awake while on
        val selectedThumb = CatSwitchThumbDrawable(
            backgroundColor = WColor.Background.color,
            strokeColor = WColor.Tint.color,
            isAwake = true
        )

        val defaultThumb = CatSwitchThumbDrawable(
            backgroundColor = WColor.Background.color,
            strokeColor = WColor.SecondaryText.color,
            isAwake = false
        )

        thumbDrawable = StateListDrawable().apply {
            addState(intArrayOf(R.attr.state_checked), selectedThumb)
            addState(intArrayOf(), defaultThumb)
        }

        DrawableCompat.setTintList(
            this.trackDrawable,
            ColorStateList(
                arrayOf(
                    intArrayOf(R.attr.state_checked),
                    intArrayOf()
                ),
                intArrayOf(
                    WColor.Tint.color,
                    WColor.SecondaryText.color
                )
            )
        )
        refreshDrawableState()
    }
}
