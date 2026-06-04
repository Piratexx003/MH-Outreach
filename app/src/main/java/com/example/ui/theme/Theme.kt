package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    secondary = SlateTeal80,
    tertiary = WarmAmber80,
    background = AppBackgroundDark,
    surface = CardBackgroundDark,
    primaryContainer = Color(0xFF312E81),
    secondaryContainer = Color(0xFF115E59),
    onPrimary = AppBackgroundDark,
    onSecondary = AppBackgroundDark,
    onTertiary = AppBackgroundDark,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    secondary = SlateTeal,
    tertiary = WarmAmber,
    background = AppBackgroundLight,
    surface = CardBackgroundLight,
    primaryContainer = LightPrimaryContainer,
    secondaryContainer = LightSecondaryContainer,
    tertiaryContainer = LightTertiaryContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onPrimaryContainer = Color(0xFF1E1B4B),
    onSecondaryContainer = Color(0xFF0F766E),
    onTertiaryContainer = Color(0xFF78350F),
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our custom clean theme for better specific styling
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
