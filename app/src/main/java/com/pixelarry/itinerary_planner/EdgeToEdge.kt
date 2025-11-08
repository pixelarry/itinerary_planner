package com.pixelarry.itinerary_planner

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.enableEdgeToEdge(
    lightStatusBar: Boolean = true,
    lightNavigationBar: Boolean = true,
) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
        controller.isAppearanceLightStatusBars = lightStatusBar
        controller.isAppearanceLightNavigationBars = lightNavigationBar
    }
}

private data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

private data class InitialMargins(val left: Int, val top: Int, val right: Int, val bottom: Int)

fun View.applySystemBarsPadding(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false,
) {
    val initialPadding = InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            initialPadding.left + if (applyLeft) systemBars.left else 0,
            initialPadding.top + if (applyTop) systemBars.top else 0,
            initialPadding.right + if (applyRight) systemBars.right else 0,
            initialPadding.bottom + if (applyBottom) systemBars.bottom else 0,
        )
        insets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applySystemBarsMargins(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false,
) {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val initialMargins = InitialMargins(
        layoutParams.leftMargin,
        layoutParams.topMargin,
        layoutParams.rightMargin,
        layoutParams.bottomMargin,
    )

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null) {
            params.leftMargin = initialMargins.left + if (applyLeft) systemBars.left else 0
            params.topMargin = initialMargins.top + if (applyTop) systemBars.top else 0
            params.rightMargin = initialMargins.right + if (applyRight) systemBars.right else 0
            params.bottomMargin = initialMargins.bottom + if (applyBottom) systemBars.bottom else 0
            view.layoutParams = params
        }
        insets
    }
    requestApplyInsetsWhenAttached()
}

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

