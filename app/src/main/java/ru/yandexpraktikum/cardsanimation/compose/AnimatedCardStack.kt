package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import ru.yandexpraktikum.cardsanimation.model.SwipeDirection

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var currentCards by remember { mutableStateOf(cards) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    val modifier = Modifier
        .fillMaxWidth()
        .pointerInput(Unit) {
            var totalDragOffset = Offset.Zero

            detectDragGestures(
                onDrag = { _, dragAmount ->
                    totalDragOffset += dragAmount
                },
                onDragEnd = {
                    if (animationState.isAnimating) {
                        return@detectDragGestures
                    }

                    SwipeDirection.fromOffset(offsetX = totalDragOffset.x, offsetY = totalDragOffset.y) { direction ->
                        when (direction) {
                            SwipeDirection.VERTICAL -> isRotated = totalDragOffset.y < 0
                            SwipeDirection.HORIZONTAL -> {
                                animationState = CardSwapAnimationState(true, AnimationStep.STEP_1)
                            }
                            else -> Unit
                        }
                    }
                    totalDragOffset = Offset.Zero
                }
            )
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = {
                        handleAnimationStepComplete(
                            step = animationState.animationStep,
                            cardIndex = i,
                            onStepChange = { nextStep ->
                                animationState = animationState.copy(animationStep = nextStep)

                                if (nextStep == AnimationStep.STEP_3) {
                                    currentCards = reorderCards(currentCards)
                                }
                            },
                            onAnimationComplete = {
                                animationState = CardSwapAnimationState(isAnimating = false)
                            }
                        )
                    }
                )
            }
        }
    }
}

private fun handleAnimationStepComplete(
    step: AnimationStep,
    cardIndex: Int,
    onStepChange: (AnimationStep) -> Unit,
    onAnimationComplete: () -> Unit
) {
    when (step) {
        AnimationStep.STEP_1 -> if (cardIndex == 0) onStepChange(AnimationStep.STEP_2)
        AnimationStep.STEP_2 -> if (cardIndex == 0) onStepChange(AnimationStep.STEP_3)
        AnimationStep.STEP_3 -> onAnimationComplete()
        else -> Unit
    }
}

// Простая функция перестановки карт
private fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}