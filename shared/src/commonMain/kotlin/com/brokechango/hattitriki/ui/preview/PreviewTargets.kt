package com.brokechango.hattitriki.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brokechango.hattitriki.core.design.HattitrikiTheme

/** Renders each preview at the responsive breakpoints used by the app. */
@Preview(
    name = "Móvil · 360 × 800",
    group = "Targets",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF07111F
)
@Preview(
    name = "Tablet · 1024 × 800",
    group = "Targets",
    widthDp = 1024,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF07111F
)
@Preview(
    name = "Web · 1440 × 900",
    group = "Targets",
    widthDp = 1440,
    heightDp = 900,
    showBackground = true,
    backgroundColor = 0xFF07111F
)
annotation class PreviewTargets

@Composable
fun HattitrikiPreview(content: @Composable () -> Unit) {
    HattitrikiTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
