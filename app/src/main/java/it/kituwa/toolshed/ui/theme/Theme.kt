package it.kituwa.toolshed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandAmber = Color(0xFFC9832F)
private val BrandAmberDark = Color(0xFFF0A93C)
private val BrandGreen = Color(0xFF2E7D52)
private val BrandGreenDark = Color(0xFF6FD3A0)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A5A22),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7E0C4),
    onPrimaryContainer = Color(0xFF2C1806),

    inversePrimary = BrandAmber,

    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEEDA),
    onSecondaryContainer = Color(0xFF072015),

    tertiary = Color(0xFF1B5E96),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD2E4F7),
    onTertiaryContainer = Color(0xFF0A1E2E),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = Color(0xFFF7F5F2),
    onBackground = Color(0xFF1C1A17),
    onSurface = Color(0xFF1C1A17),
    onSurfaceVariant = Color(0xFF4C463E),
    surfaceTint = Color(0xFF8A5A22),

    surface = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE3DED8),
    surfaceBright = Color(0xFFF7F5F2),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF7F4),
    surfaceContainer = Color(0xFFF2EEE9),
    surfaceContainerHigh = Color(0xFFECE7E1),
    surfaceContainerHighest = Color(0xFFE6E1DA),
    surfaceVariant = Color(0xFFEDE7DF),
    inverseSurface = Color(0xFF312E2A),
    inverseOnSurface = Color(0xFFF4EFE9),

    outline = Color(0xFF82796E),
    outlineVariant = Color(0xFFD3C9BE),
    scrim = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    primary = BrandAmberDark,
    onPrimary = Color(0xFF452A05),
    primaryContainer = Color(0xFF663F0F),
    onPrimaryContainer = Color(0xFFFFDDB0),

    inversePrimary = Color(0xFF8A5A22),

    secondary = BrandGreenDark,
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF005235),
    onSecondaryContainer = Color(0xFF8DF8C4),

    tertiary = Color(0xFF4FA3E3),
    onTertiary = Color(0xFF00334F),
    tertiaryContainer = Color(0xFF004B77),
    onTertiaryContainer = Color(0xFFCBE5FF),

    error = Color(0xFFFF8A80),
    onError = Color(0xFF3A0906),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    background = Color(0xFF17150F),
    onBackground = Color(0xFFEDE6DC),
    onSurface = Color(0xFFEDE6DC),
    onSurfaceVariant = Color(0xFFCEC5B9),
    surfaceTint = BrandAmberDark,

    surface = Color(0xFF17150F),
    surfaceDim = Color(0xFF17150F),
    surfaceBright = Color(0xFF3E3A32),
    surfaceContainerLowest = Color(0xFF120F0A),
    surfaceContainerLow = Color(0xFF1F1C15),
    surfaceContainer = Color(0xFF242019),
    surfaceContainerHigh = Color(0xFF2F2A22),
    surfaceContainerHighest = Color(0xFF3A342B),
    surfaceVariant = Color(0xFF2A251E),
    inverseSurface = Color(0xFFEDE6DC),
    inverseOnSurface = Color(0xFF2A251E),

    outline = Color(0xFF574F45),
    outlineVariant = Color(0xFF3B342B),
    scrim = Color(0xFF000000),
)

@Composable
fun ToolshedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
