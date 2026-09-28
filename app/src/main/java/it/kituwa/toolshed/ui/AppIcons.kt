package it.kituwa.toolshed.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import it.kituwa.toolshed.R

/**
 * The handful of icons the app uses, as local vector drawables.
 *
 * These were originally taken from material-icons-extended, which ships
 * around two thousand icons as generated Kotlin. With minification off, all of
 * that code ended up in the APK: 42 MB of dex for an app whose entire content
 * is a few hundred kilobytes of text. Keeping the six we actually use costs
 * about six kilobytes.
 */
object AppIcons {
    val Calculate: Painter
        @Composable get() = painterResource(R.drawable.ic_calculate)

    val ContentCopy: Painter
        @Composable get() = painterResource(R.drawable.ic_content_copy)

    val Layers: Painter
        @Composable get() = painterResource(R.drawable.ic_layers)

    val Memory: Painter
        @Composable get() = painterResource(R.drawable.ic_memory)

    val Storage: Painter
        @Composable get() = painterResource(R.drawable.ic_storage)

    val OpenInNew: Painter
        @Composable get() = painterResource(R.drawable.ic_open_in_new)
}
