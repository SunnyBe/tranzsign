package com.sunday.tranzsign.ui.component


import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

/**
 * Applies a visual blur effect, indicating the content is inactive.
 * @param active When true, the blur is applied.
 */
fun Modifier.blurScrim(active: Boolean): Modifier = this.then(
    if (active) this.blur(radius = 8.dp) else this
)