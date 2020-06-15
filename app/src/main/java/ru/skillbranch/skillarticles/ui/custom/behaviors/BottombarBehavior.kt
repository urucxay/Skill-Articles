package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import kotlin.math.max
import kotlin.math.min

class BottombarBehavior : CoordinatorLayout.Behavior<Bottombar>() {

    @ViewCompat.NestedScrollType
    private var lastStartedType: Int = 0
    private var offsetAnimator: ValueAnimator? = null
    private var isSnappingEnabled = false

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: Bottombar,
        dependency: View
    ): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
//            updateSnackbar(child, dependency)
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Bottombar,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        if (axes != ViewCompat.SCROLL_AXIS_VERTICAL)
            return false

        lastStartedType = type
        offsetAnimator?.cancel()

        return true
    }

//    override fun onStopNestedScroll(
//        coordinatorLayout: CoordinatorLayout,
//        child: Bottombar,
//        target: View,
//        type: Int
//    ) {
//
//        if (!isSnappingEnabled)
//            return
//
//        if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
//            val currTranslation = child.translationY
//            val childHalfHeight = child.height * 0.5f
//
//            if (currTranslation >= childHalfHeight) {
//                animateBarVisibility(child, isVisible = false)
//            } else {
//                animateBarVisibility(child, isVisible = true)
//            }
//        }
//    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Bottombar,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
//        if (!child.isSearchMode) {
//            val offset = MathUtils.clamp(child.translationY + dy, 0f, child.minHeight.toFloat())
//            if (offset != child.translationY) child.translationY = offset
//        }
//        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (!child.isSearchMode) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            child.translationY = max(0f, min(child.height.toFloat(), child.translationY + dy))
        }
    }

    private fun updateSnackbar(child: View, snackbarLayout: Snackbar.SnackbarLayout) {
        if (snackbarLayout.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = snackbarLayout.layoutParams as CoordinatorLayout.LayoutParams

            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            snackbarLayout.layoutParams = params
        }
    }

    private fun animateBarVisibility(child: View, isVisible: Boolean) {
        if (offsetAnimator == null) {
            offsetAnimator = ValueAnimator().apply {
                interpolator = DecelerateInterpolator()
                duration = 200L
            }

            offsetAnimator?.addUpdateListener {
                child.translationY = it.animatedValue as Float
            }
        } else {
            offsetAnimator?.cancel()
        }

        val targetTranslation = if (isVisible) 0f else child.height.toFloat()
        offsetAnimator?.setFloatValues(child.translationY, targetTranslation)
        offsetAnimator?.start()
    }
}