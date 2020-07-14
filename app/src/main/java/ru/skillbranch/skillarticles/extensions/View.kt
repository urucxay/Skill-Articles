package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

fun View.setMarginOptionally(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    (layoutParams as ViewGroup.MarginLayoutParams).setMargins(left, top, right, bottom)
}

fun View.setPaddingOptionally(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}


//лисенер для глобальной остановки всех кликов
//например, когда надо, чтобы нельзя было одновременно нажать две кнопки
private var lastClickTimestamp = 0L
fun View.setThrottledClickListener(delay: Long = 200L, clickListener: (View) -> Unit) {
    setOnClickListener {
        val currentTimestamp = System.currentTimeMillis()
        val delta = currentTimestamp - lastClickTimestamp
        if (delta !in 0L..delay) {
            lastClickTimestamp = currentTimestamp
            clickListener(this)
        }
    }
}
