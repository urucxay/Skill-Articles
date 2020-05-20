package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun View.setMarginOptionally(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {

    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.let { leftMargin = it }
        top?.let { topMargin = it }
        right?.let { rightMargin = it }
        bottom?.let { bottomMargin = it }
    }

}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
//    if (layoutParams is T) block(layoutParams as T)
    if (layoutParams is T) block.invoke(layoutParams as T)
}

fun <T> T.also(block: (T) -> Unit) {
    block(this)
}

fun <T> View.apply1(block: T.() -> Unit) {
    block(this as T)
}

fun <T> T.apply2(block: T.() -> Unit) {
      block()
}
