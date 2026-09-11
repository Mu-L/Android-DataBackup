package com.xayah.databackup.ui.component

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

fun forwardNavigationTransition(): ContentTransform = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
) togetherWith slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
)

fun backwardNavigationTransition(): ContentTransform = slideInHorizontally(
    initialOffsetX = { -it },
    animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
) togetherWith slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
)
