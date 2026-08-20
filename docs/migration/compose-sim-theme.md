# SimTheme — Compose Theme Definition and Styling Guidance

Reference document for implementing the `SimTheme` Compose theme in `:infra:compose-common`.
This translates the existing `Theme.Simprints` (Material 2, defined in `infra/resources/`) to a
single light Material 3 `MaterialTheme` wrapper and defines the styling rules screens and components should follow.

---

## Styling principles

- **3-Tier Token Hierarchy**:
    1. *Primitive Tokens*: Raw design values (`Color(0xFF00B3D1)`, `8.dp`, `16.sp`). Never use directly in screen composables.
    2. *Semantic Tokens*: Theme-level roles (`MaterialTheme.colorScheme.primary`, `MaterialTheme.typography.bodyMedium`,
       `SimTheme.spacing.medium`).
    3. *Component Tokens / Styles*: Style objects or default parameter values that configure specific components (`SimButtonStyle`,
       `CardDefaults.cardColors()`).
- **Immutable & Stable Tokens**: Mark custom token classes and style wrappers with `@Immutable` or `@Stable` to allow Compose compiler smart
  optimizations and prevent unnecessary recompositions.
- **Custom Design Extensions**: Expose tokens outside standard M3 scales (e.g., spacing/padding, custom semantic status colors) using
  `staticCompositionLocalOf` and top-level accessor extensions on `MaterialTheme` or `SimTheme`.
- **Component Slot APIs**: Keep custom UI components thin wrappers around Material 3 components. Expose parameters (`modifier`, `colors`,
  `shape`, `contentPadding`, `textStyle`) instead of hardcoding styling choices.
- **Single Public Theme Entry Point**: Wrap top-level screens at the Fragment / `ComposeView` boundary using `SimTheme`.

---

## Future-proofing for a Compose Styles API

The Compose Styles API is not stable for production use. Do not add `Style`, `StyleScope`,
`Modifier.styleable()`, or related experimental APIs to this app.

Keep components ready for a future styles API by:

1. **Decouple Component Logic from Visual Styling**:
    - Design custom components (`SimButton`, `SimCard`, `SimTextField`) to accept component style objects (e.g., `SimButtonStyle`) or style
      parameters rather than querying `MaterialTheme` directly inside internal layout code.
2. **Encapsulate Style Definitions**:
    - Define component style classes marked with `@Immutable` that bundle background colors, content colors, typography, shapes, and
      paddings.
3. **Adapter Pattern in `SimTheme`**:
    - Treat `SimTheme` as the central style provider and adapter layer. Screen call sites consume semantic component styles or default theme
      values without knowing the underlying token implementation.
4. **Migration Strategy**:
    - When a stable API is available and approved, adapt the internal style implementation while
      preserving screen call sites and component public signatures.

---

## Color mapping — `Theme.Simprints` (M2) → `SimColorScheme` (M3)

Source files: `infra/resources/src/main/res/values/colors.xml` and `theme.xml`.

| M2 attribute                           | Color name              | Hex         | M3 role                                          |
|----------------------------------------|-------------------------|-------------|--------------------------------------------------|
| `colorPrimary`                         | `simprints_blue`        | `#00B3D1`   | `primary`                                        |
| `colorPrimaryVariant`                  | `simprints_blue_dark`   | `#009CB6`   | `primaryContainer`                               |
| `colorOnPrimary`                       | `simprints_text_white`  | `#DEFFFFFF` | `onPrimary` / `onPrimaryContainer`               |
| `colorSecondary` / buttons             | `simprints_orange`      | `#FF7C00`   | `secondary`                                      |
| `colorSecondaryVariant`                | `simprints_orange_dark` | `#CC6300`   | `secondaryContainer`                             |
| `colorOnSecondary`                     | `simprints_text_white`  | `#DEFFFFFF` | `onSecondary` / `onSecondaryContainer`           |
| `android:colorBackground`              | `simprints_white`       | `#FFFFFF`   | `background`                                     |
| `colorSurface`                         | `simprints_white`       | `#FFFFFF`   | `surface` / `surfaceContainer`                   |
| `colorOnBackground` / `colorOnSurface` | `simprints_text_black`  | `#DE000000` | `onBackground` / `onSurface`                     |
| `colorError`                           | `simprints_red_dark`    | `#B8443F`   | `error`                                          |
| `colorOnError`                         | `simprints_text_white`  | `#DEFFFFFF` | `onError`                                        |
| *(no M2 equivalent)*                   | `simprints_green`       | `#2B9962`   | `tertiary` (success states)                      |
| status bar                             | `simprints_blue_dark`   | `#009CB6`   | drives `SystemBarStyle` via `enableEdgeToEdge()` |

> **Company design policy exception:** This application ships exclusively with a single **Light** theme. Do **NOT** implement dark mode, and
> do **NOT** add `darkColorScheme()` or `isSystemInDarkTheme()` branching logic.

`Color.kt`:

```kotlin
package com.simprints.infra.composecommon.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Primitive Brand Palette
val SimprintsBlue = Color(0xFF00B3D1)
val SimprintsBlueDark = Color(0xFF009CB6)
val SimprintsOrange = Color(0xFFFF7C00)
val SimprintsOrangeDark = Color(0xFFCC6300)
val SimprintsGreen = Color(0xFF2B9962)
val SimprintsRed = Color(0xFFB8443F)
val SimprintsWhite = Color(0xFFFFFFFF)
val SimprintsTextBlack = Color(0xDE000000)
val SimprintsTextWhite = Color(0xDEFFFFFF)
val SimprintsOutlineGray = Color(0x1F000000)

val SimLightColorScheme = lightColorScheme(
    primary = SimprintsBlue,
    onPrimary = SimprintsTextWhite,
    primaryContainer = SimprintsBlueDark,
    onPrimaryContainer = SimprintsTextWhite,
    secondary = SimprintsOrange,
    onSecondary = SimprintsTextWhite,
    secondaryContainer = SimprintsOrangeDark,
    onSecondaryContainer = SimprintsTextWhite,
    tertiary = SimprintsGreen,
    onTertiary = SimprintsTextWhite,
    error = SimprintsRed,
    onError = SimprintsTextWhite,
    background = SimprintsWhite,
    onBackground = SimprintsTextBlack,
    surface = SimprintsWhite,
    onSurface = SimprintsTextBlack,
    surfaceContainer = SimprintsWhite,
    outline = SimprintsOutlineGray,
)

/**
 * Extended semantic colors for domain-specific states not covered by standard Material 3 roles.
 */
@Immutable
data class SimExtendedColors(
    val success: Color = SimprintsGreen,
    val onSuccess: Color = SimprintsTextWhite,
    val warning: Color = SimprintsOrange,
    val onWarning: Color = SimprintsTextWhite,
)

val LocalSimExtendedColors = staticCompositionLocalOf { SimExtendedColors() }
```

---

## Typography mapping — `styles-text.xml` → `SimTypography`

Source file: `infra/resources/src/main/res/values/styles-text.xml`.  
Font: **Muli**, loaded from `infra/resources/src/main/res/font/muli.xml` and `muli_semibold.xml`.

Model the app's actual text usage using full `TextStyle` definitions with explicit font family, font weight, size, line height, and letter
spacing.

| XML `TextAppearance` | Size | M3 `Typography` slot |
|----------------------|------|----------------------|
| `Headline1`          | 96sp | `displayLarge`       |
| `Headline2`          | 60sp | `displayMedium`      |
| `Headline3`          | 48sp | `displaySmall`       |
| `Headline4`          | 34sp | `headlineLarge`      |
| `Headline5`          | 24sp | `headlineMedium`     |
| `Headline6`          | 20sp | `headlineSmall`      |
| `Subtitle1`          | 16sp | `titleLarge`         |
| `Subtitle2`          | 14sp | `titleMedium`        |
| `Body1`              | 16sp | `bodyLarge`          |
| `Body2`              | 14sp | `bodyMedium`         |
| `Button`             | 14sp | `labelLarge`         |
| `Caption`            | 12sp | `bodySmall`          |
| `Overline`           | 10sp | `labelSmall`         |

`Typography.kt`:

```kotlin
package com.simprints.infra.composecommon.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MuliFamily = FontFamily(
    Font(com.simprints.infra.resources.R.font.muli, FontWeight.Normal),
    Font(com.simprints.infra.resources.R.font.muli_semibold, FontWeight.SemiBold),
)

val SimTypography = Typography(
    displayLarge = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 96.sp, lineHeight = 112.sp),
    displayMedium = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 60.sp, lineHeight = 72.sp),
    displaySmall = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 48.sp, lineHeight = 56.sp),
    headlineLarge = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = MuliFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp),
)
```

---

## Shapes & Spacing mapping

### Shapes (`Shape.kt`)

Source file: `infra/resources/src/main/res/values/styles-shape.xml`.

```kotlin
package com.simprints.infra.composecommon.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SimShapes = Shapes(
    // SmallComponent — buttons, chips, text fields
    small = RoundedCornerShape(4.dp),
    // MediumComponent — cards, dialogs
    medium = RoundedCornerShape(8.dp),
    // LargeComponent — bottom sheets, nav drawers
    large = RoundedCornerShape(10.dp),
)
```

### Spacing & Padding (`Spacing.kt`)

Provide layout dimension tokens via a custom `@Immutable` class and `staticCompositionLocalOf`.

```kotlin
package com.simprints.infra.composecommon.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class SimSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
)

val LocalSimSpacing = staticCompositionLocalOf { SimSpacing() }
```

---

## Theme Wrapper & Accessors — `SimTheme.kt`

`SimTheme` wraps Material 3 and injects custom CompositionLocals for spacing and extended colors.

```kotlin
package com.simprints.infra.composecommon.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun SimTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSimSpacing provides SimSpacing(),
        LocalSimExtendedColors provides SimExtendedColors(),
    ) {
        MaterialTheme(
            colorScheme = SimLightColorScheme,
            typography = SimTypography,
            shapes = SimShapes,
            content = content,
        )
    }
}

/**
 * Direct accessors for custom SimTheme extensions.
 */
object SimTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: SimSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSimSpacing.current

    val extendedColors: SimExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSimExtendedColors.current

}
```

Usage at Fragment interop boundary:

```kotlin
setContent {
    SimTheme {
        MyScreen(viewModel = hiltViewModel())
    }
}
```

---

## Edge-to-Edge & System Bar Styling

Configure system bars at the Activity level using `enableEdgeToEdge()` with `SystemBarStyle.light()` or `SystemBarStyle.dark()` to match
`#009CB6` (`SimprintsBlueDark`). In Compose UI, rely on `WindowInsets.safeDrawing` or `WindowInsets.statusBars` rather than imperatively
mutating Activity window flags in composables.

```kotlin
// Activity setup:
enableEdgeToEdge(
    statusBarStyle = SystemBarStyle.dark(
        android.graphics.Color.parseColor("#009CB6")
    )
)
```

---

## Custom Component Definition Patterns

Build components as thin wrappers around stable Material 3 components. Use immutable parameter or
style data classes for reusable visual variants, and expose `modifier`, `colors`, `shape`,
`contentPadding`, `textStyle`, and `enabled` where appropriate. Do not use experimental Compose
Styles APIs until they are stable and explicitly adopted.
