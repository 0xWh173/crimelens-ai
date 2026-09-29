package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Pure Apple iOS 2026 Color System
val AppleBlue = Color(0xFF007AFF)      // iOS Primary Accent
val AppleRed = Color(0xFFFF3B30)       // iOS System Red (Scam / Danger)
val AppleGreen = Color(0xFF34C759)     // iOS System Green (Safe / Verified)
val AppleOrange = Color(0xFFFF9500)    // iOS System Orange (Warning)
val AppleGray = Color(0xFF8E8E93)      // iOS System Gray

// Light Theme (iOS Settings / Health #F5F5F7 Canvas)
val LightBackground = Color(0xFFF5F5F7)        // Pure Apple Light Background
val LightCardSurface = Color(0xFFFFFFFF)       // Crisp White iOS Card Surface
val LightSecondarySurface = Color(0xFFEFEFF4)  // Grouped Inset Surface
val LightCardBorder = Color(0xFFE5E5EA)        // Thin Separator
val LightSubtleSeparator = Color(0xFFE5E5EA)   // List Divider
val LightPrimaryText = Color(0xFF000000)       // High-contrast Black Text
val LightMutedText = Color(0xFF8E8E93)         // iOS Secondary Label Gray
val LightPrimaryBlue = AppleBlue
val LightAccentIndigo = AppleBlue

// Dark Theme (iOS True Pitch Black #000000 Canvas)
val DarkBackground = Color(0xFF000000)         // Pitch Black OLED Canvas
val DarkCardSurface = Color(0xFF1C1C1E)        // iOS Dark Elevated Card
val DarkSecondarySurface = Color(0xFF2C2C2E)   // Dark Grouped Container
val DarkCardBorder = Color(0xFF38383A)         // Dark Thin Separator
val DarkSubtleSeparator = Color(0xFF38383A)    // Dark List Divider
val DarkPrimaryText = Color(0xFFFFFFFF)        // Crisp White Text
val DarkMutedText = Color(0xFF8E8E93)          // iOS Secondary Label Gray
val DarkPrimaryBlue = AppleBlue
val DarkAccentIndigo = AppleBlue

// Compatibility references
val AppleBackground = DarkBackground
val AppleSecondarySurface = DarkSecondarySurface
val AppleCardSurface = DarkCardSurface
val AppleCardBorder = DarkCardBorder
val AppleSubtleSeparator = DarkSubtleSeparator
val AppleMutedGray = LightMutedText
val AppleSecondaryGray = Color(0xFF8E8E93)
val AppleLightText = LightPrimaryText

// Aliases for DAO / Repo / Components
val CyberCyan = AppleBlue
val CyberCyanVariant = AppleBlue
val CyberPurple = AppleBlue
val CyberPurpleVariant = AppleBlue
val CyberDarkBg = DarkBackground
val CyberDarkSurface = DarkSecondarySurface
val CyberDarkSurfaceVariant = DarkCardSurface
val CyberDarkCardBorder = DarkCardBorder

val ScamRiskHigh = AppleRed
val ScamRiskMedium = AppleOrange
val ScamRiskLow = AppleGreen

val TextPrimary = LightPrimaryText
val TextSecondary = LightMutedText
val TextMuted = LightMutedText


