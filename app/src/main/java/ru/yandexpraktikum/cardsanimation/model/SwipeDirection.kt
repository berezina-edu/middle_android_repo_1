package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs

const val SWIPE_THRESHOLD: Float = 100f

enum class SwipeDirection {
    VERTICAL,
    HORIZONTAL;

    companion object {
        fun fromOffset(
            offsetX: Float,
            offsetY: Float,
            threshold: Float = SWIPE_THRESHOLD,
            onComplete: (SwipeDirection?) -> Unit
        ) {
            val isVerticalDominant = abs(offsetY) > abs(offsetX)
            val direction =  when {
                isVerticalDominant && abs(offsetY) > threshold -> VERTICAL
                !isVerticalDominant && abs(offsetX) > threshold -> HORIZONTAL
                else -> null
            }
            onComplete(direction)
        }
    }
}