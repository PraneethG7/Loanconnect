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
    primary = LoanPrimary,
    onPrimary = LoanOnPrimary,
    primaryContainer = LoanPrimaryContainer,
    onPrimaryContainer = LoanOnPrimaryContainer,
    secondary = LoanSecondary,
    onSecondary = LoanOnSecondary,
    secondaryContainer = LoanSecondaryContainer,
    onSecondaryContainer = LoanOnSecondaryContainer,
    tertiary = LoanTertiary,
    onTertiary = LoanOnTertiary,
    tertiaryContainer = LoanTertiaryContainer,
    onTertiaryContainer = LoanOnTertiaryContainer,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = OverdueRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008764),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F5DD),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBAEAFF),
    onSecondaryContainer = Color(0xFF001F2B),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDFA0),
    onTertiaryContainer = Color(0xFF281800),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = OverdueRed,
    onError = Color.White
)

@Composable
fun LoanConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct fintech palette
    content: @Composable () -> Unit
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LoanConnectTheme(darkTheme, dynamicColor, content)
}
