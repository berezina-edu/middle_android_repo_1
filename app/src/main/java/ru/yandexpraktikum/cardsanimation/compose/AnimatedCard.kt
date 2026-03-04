package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    isAnimating: Boolean,
    animationStep: AnimationStep,
    onAnimationStepComplete: ((AnimationStep) -> Unit)? = null
) {
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = if (animationStep == AnimationStep.STEP_3) 300 else 800),
        finishedListener = {
            if (isAnimating && isLastStep(animationStep, cardIndex)) onAnimationStepComplete?.invoke(AnimationStep.STEP_3)
        },
        label = "rotation"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            isAnimating && isFirstStep(animationStep, cardIndex) -> computeTargetX(
                rotation = targetRotation,
                density = LocalDensity.current
            )

            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isAnimating) {
                when (animationStep) {
                    AnimationStep.STEP_1 -> onAnimationStepComplete?.invoke(AnimationStep.STEP_1)
                    AnimationStep.STEP_2 -> onAnimationStepComplete?.invoke(AnimationStep.STEP_2)
                    else -> Unit
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            isAnimating && isFirstStep(animationStep, cardIndex) -> computeTargetY(
                rotation = targetRotation,
                density = LocalDensity.current
            )

            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY"
    )

    val shouldBringToFront = isAnimating && isFrontCardForStep(animationStep, cardIndex)

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                translationX = if (isAnimating) animatedTranslationX else 0f
                translationY = if (isAnimating) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }
            .let { modifier ->
                if (shouldBringToFront) {
                    modifier.zIndex(1000f)
                } else {
                    modifier
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

private fun computeTargetX(rotation: Float, density: Density): Float {
    val moveDistance = with(density) { 50.dp.toPx() }
    val rotationRad = Math.toRadians(rotation.toDouble())
    return moveDistance * cos(rotationRad).toFloat()
}

private fun computeTargetY(rotation: Float, density: Density): Float {
    val moveDistance = with(density) { 50.dp.toPx() }
    val rotationRad = Math.toRadians(rotation.toDouble())
    return moveDistance * sin(rotationRad).toFloat()
}

private fun isFrontCardForStep(step: AnimationStep, index: Int): Boolean = when (step) {
    AnimationStep.STEP_2 -> index == 0
    AnimationStep.STEP_3 -> index == 3
    else -> false
}

private fun isFirstStep(step: AnimationStep, cardIndex: Int) =
    step == AnimationStep.STEP_1 && cardIndex == 0

private fun isLastStep(step: AnimationStep, cardIndex: Int) =
    step == AnimationStep.STEP_3 && cardIndex == 3