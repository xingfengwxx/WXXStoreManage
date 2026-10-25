package com.wangxingxing.wxxstoremanage.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = OnBrandBlueContainer,
    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateBlueContainerLight,
    onSecondaryContainer = OnSlateBlueContainerLight,
    tertiary = WarmTaupe,
    onTertiary = Color.White,
    tertiaryContainer = WarmTaupeContainerLight,
    onTertiaryContainer = OnWarmTaupeContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = BrandBlueOnDark,
    primaryContainer = BrandBlueContainerDark,
    onPrimaryContainer = OnBrandBlueContainerDark,
    secondary = SlateBlueLight,
    onSecondary = SlateBlueOnDark,
    secondaryContainer = SlateBlueContainerDark,
    onSecondaryContainer = OnSlateBlueContainerDark,
    tertiary = WarmTaupeLight,
    onTertiary = WarmTaupeOnDark,
    tertiaryContainer = WarmTaupeContainerDark,
    onTertiaryContainer = OnWarmTaupeContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

@Composable
fun WXXStoreManageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
