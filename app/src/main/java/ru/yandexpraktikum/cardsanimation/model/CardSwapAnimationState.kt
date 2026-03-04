package ru.yandexpraktikum.cardsanimation.model

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: AnimationStep = AnimationStep.NONE
)