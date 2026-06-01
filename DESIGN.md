# Simprints UI Design Contract (Material Design)

This document is the implementation contract for creating new screens.

Use it for:

- generating XML screens in Google Stitch
- enforcing consistent design language across feature modules
- implementing Jetpack Compose theme using Material Design 3 tokens
- guiding future migration from View-based XML to Compose

Do not treat this as a file inventory. Treat it as build rules.

**Note:** This document uses Material Design 3 (M3) token naming. For legacy Material Components 2 (M2) XML references and historical
context, see [Appendix A: Legacy Material Components 2 Naming](#appendix-a-legacy-material-components-2-naming).

## 1) Core Principles

1. Use tokenized styles first, inline values last.
2. Prefer Material Components primitives with Simprints styles.
3. Keep behaviorally equivalent XML and Compose tokens.
4. Blue workflow surfaces are primary operational context; white/off-white surfaces are settings/content context.
5. Screen structure must be reusable: header/content/actions and explicit loading/error states.

## 2) Theme and Design Tokens

### 2.1 Theme Baseline

- App theme family: `Theme.Simprints` (Material Design 3)
- Base framework: Jetpack Compose with Material 3, View-based XML using Material Components 3 library
- Core token layers:
    - **Semantic color roles:** primary, secondary, tertiary, error, surface, outline, etc.
    - **Typography scale:** displayLarge/Medium/Small, headlineLarge/Medium/Small, titleLarge/Medium/Small, bodyLarge/Medium/Small,
      labelLarge/Medium/Small
    - **Shape tokens:** small (4dp), medium (8dp), large (12dp)
    - **Spacing scale:** 4dp, 8dp, 16dp, 24dp, 32dp
    - **Component defaults:** buttons, cards, app bars, text inputs, progress, radio/checkbox

**Supporting M2 XML Layer:** Current View-based screens use Material Components 2 with Simprints custom styles.
See [Appendix A](#appendix-a-legacy-material-components-2-naming) for M2 style references.

### 2.2 Color Tokens

Simprints color palette (immutable brand colors):

- **Primary action:** `simprints_blue` (#00B3D1), `simprints_blue_dark` (#009CB6)
- **Secondary action:** `simprints_orange` (#FF7C00), `simprints_orange_dark` (#CC6300)
- **Status colors:**
    - success: `simprints_green_dark` (#347046)
    - error: `simprints_red_dark` (#B8433F)
    - warning: `simprints_red` (#E74C3C), `simprints_yellow` (#FFC107)
- **Neutrals:** `simprints_white`, `simprints_off_white`, `simprints_black`, `simprints_grey`, `simprints_grey_light`,
  `simprints_grey_disabled`
- **Text overlays:** `simprints_text_white`, `simprints_text_black`, `simprints_text_grey`, `simprints_text_grey_light`,
  `simprints_text_blue`, `simprints_text_orange`

M3 semantic role mapping:

- `primary` → `simprints_blue` (primary brand action)
- `onPrimary` → `simprints_text_white` (text/icons on primary surfaces)
- `secondary` → `simprints_orange` (secondary/important action)
- `onSecondary` → `simprints_text_white` (text/icons on secondary surfaces)
- `tertiary` → `simprints_orange_dark` (accent action)
- `onTertiary` → `simprints_text_white`
- `surface` → `simprints_white` or `simprints_off_white` (card and container backgrounds)
- `onSurface` → `simprints_text_black` (primary text on surfaces)
- `error` → `simprints_red_dark` (error states)
- `onError` → `simprints_text_white` (text on error surfaces)
- `outline` → `simprints_grey` (borders and dividers)
- `background` → context-dependent: `simprints_blue` (workflow), `simprints_white` (settings)
- `onBackground` → `simprints_text_white` (workflow), `simprints_text_black` (settings)

Semantic usage patterns:

- **Workflow screens:** blue background + white text overlays + white card containers
- **Settings/content screens:** white/off-white background + black text + white cards
- **Critical messaging:** red dark surface + white text
- **Primary CTA:** orange-filled button on blue or white background
- **Secondary CTA:** white-filled button on blue background

### 2.3 Typography Tokens

Font family (primary): `muli`

M3 Type scale (use these in Compose and future XML):

| Role             | Size | Usage                              |
|------------------|------|------------------------------------|
| `displayLarge`   | 57sp | Extra-large headings (rare)        |
| `displayMedium`  | 45sp | Large screen titles                |
| `displaySmall`   | 36sp | Major section titles               |
| `headlineLarge`  | 32sp | Primary screen headings            |
| `headlineMedium` | 28sp | Secondary headings                 |
| `headlineSmall`  | 24sp | Tertiary headings, section headers |
| `titleLarge`     | 22sp | Card/dialog titles                 |
| `titleMedium`    | 16sp | Subsection titles                  |
| `titleSmall`     | 14sp | Small titles, list item headers    |
| `bodyLarge`      | 16sp | Primary body text                  |
| `bodyMedium`     | 14sp | Standard body text                 |
| `bodySmall`      | 12sp | Secondary body text, captions      |
| `labelLarge`     | 14sp | Button text, strong labels         |
| `labelMedium`    | 12sp | Standard labels                    |
| `labelSmall`     | 11sp | Small labels, badges               |

Usage rules (current View-based XML):

- Use style references from `TextAppearance.Simprints.*` (see [Appendix A](#appendix-a-legacy-material-components-2-naming) for M2 mapping)
- Use `Text.*` style variants on `TextView`
- Avoid inline `textSize` and inline `textColor` unless no token exists

Current M2 XML layer maintains: `Headline1-6`, `Subtitle1-2`, `Body1-2`, `Caption`, `Button`, `Overline` styles.

### 2.4 Shape Tokens

- Small component radius: 4dp
- Medium component radius: 8dp
- Large component radius: 12dp

### 2.5 Spacing Tokens

Canonical spacing scale:

- `4dp` micro spacing (icon/text separation)
- `8dp` compact spacing (`margin_default`)
- `16dp` standard spacing (`margin_large`, `padding_default`)
- `24dp` large spacing (`padding_large`)
- `32dp` section spacing (`margin_huge`)

Layout edges:

- default activity horizontal/vertical edge spacing: 16dp

### 2.6 M3 Design System Tokens (Primary Implementation Target)

Jetpack Compose implementation using Material 3:

**Color Scheme:**

```kotlin
ColorScheme(
    primary = simprints_blue,
    onPrimary = simprints_text_white,
    secondary = simprints_orange,
    onSecondary = simprints_text_white,
    tertiary = simprints_orange_dark,
    onTertiary = simprints_text_white,
    error = simprints_red_dark,
    onError = simprints_text_white,
    background = simprints_white,  // context-override: simprints_blue for workflow
    onBackground = simprints_text_black,  // context-override: simprints_text_white for workflow
    surface = simprints_white,
    onSurface = simprints_text_black,
    outline = simprints_grey,
    outlineVariant = simprints_grey_light
)
```

**Typography:**

- Font family: `FontFamily.Serif` → use custom Muli font provider
- Scale: `Typography` with all roles mapped per section 2.3 (displayLarge through labelSmall)

**Shapes:**

```kotlin
Shapes(
    small = RoundedCornerShape(4.dp),     // buttons, small chips
    medium = RoundedCornerShape(8.dp),    // cards, dialogs, medium components
    large = RoundedCornerShape(12.dp)     // large containers, sheets
)
```

**Spacing (custom CompositionLocal):**

```kotlin
data class SimprintsSpacing(
    val micro: Dp = 4.dp,      // icon/text separation
    val compact: Dp = 8.dp,    // close element spacing
    val default: Dp = 16.dp,   // standard spacing
    val large: Dp = 24.dp,     // section spacing
    val huge: Dp = 32.dp       // major section spacing
)
```

**Current View-based XML layer** uses Material Components 3 with Simprints overrides.
See [Appendix A](#appendix-a-legacy-material-components-2-naming) for M2 XML style migration path.

## 3) Component Contracts

### 3.1 Buttons

Default button contract:

- minimum height: 56dp
- shape: small component (4dp)
- text style: `TextAppearance.Simprints.Button`

Variants and intent:

- `Widget.Simprints.Button` -> primary CTA (orange)
- `Widget.Simprints.Button.White` -> secondary CTA on blue surfaces
- `Widget.Simprints.Button.Blue` -> contextual blue-filled action
- `Widget.Simprints.Button.Red` -> destructive action
- `Widget.Simprints.Button.TextButton.*` -> tertiary/link-like actions

State behavior:

- disabled state uses gray-disabled token by selector/theming
- do not manually reduce alpha unless component has no disabled style

### 3.2 Text Inputs

- Use `TextInputLayout` + `TextInputEditText`
- Use filled style (`Widget.Simprints.FilledTextInputLayout`)
- Hint, error, focus colors come from theme overlay
- Respect helper/error text pattern instead of ad-hoc labels

### 3.3 Cards

- Preferred card primitive: `MaterialCardView`
- White card on blue surfaces for contrast
- Standard corner: medium (8dp) unless visual requirement differs
- Use card blocks as semantic sections (preview, progress, result, grouped settings)

### 3.4 App Bars and Tabs

- Use `AppBarLayout` + `MaterialToolbar`
- Optional `TabLayout` under toolbar for multi-pane content
- Body scrolls under app bar with clear top constraint

### 3.5 Selection Controls

- Radio: `MaterialRadioButton` with `Widget.Simprints.RadioButton` (white variant on dark/blue)
- Checkbox: use for explicit confirmation gates before enabling submit
- Chips: use for module/tag selection and quick filtering

### 3.6 Progress and Busy States

- Circular progress for indefinite wait
- Linear progress for measurable progression
- Use large blue/white variants for workflow-heavy progress tracks
- Busy overlays should block input with explicit visibility state

## 4) Reusable Layout Blueprints

These are explicit patterns for Stitch to generate new screens.

### 4.1 Blueprint A: Workflow Full Screen (Blue)

Use for capture, selection, and critical operation flows.

Structure:

1. Root `ConstraintLayout` with blue background
2. Top title block centered, white headline text
3. Middle content region (card/list/preview)
4. Bottom action region with 1-2 buttons

Sizing and spacing:

- outer padding: 16dp
- title top/side spacing: 16dp
- content-to-actions separation: 16dp+
- two-button rows: equal width with 16dp gap

### 4.2 Blueprint B: Toolbar + Scroll Content (Light)

Use for settings, troubleshooting, and informational pages.

Structure:

1. Root layout with white/off-white background
2. App bar (`MaterialToolbar`)
3. Scrollable body (`ScrollView`/`NestedScrollView`)
4. Body width constrained and centered when needed

Width policy:

- max readable width targets: 500dp (general), 600dp (long text)

### 4.3 Blueprint C: Form + Validation

Use for auth, input, and optional details entry.

Structure:

1. Vertical group of `TextInputLayout` fields
2. Optional inline error card or helper text block
3. Primary submit action below fields
4. Secondary text button beneath primary action when needed

Rules:

- 8dp vertical spacing between fields
- primary button full width where possible
- validation state drives button enabled state

### 4.4 Blueprint D: Scan/Capture Overlay

Use for camera-based flows.

Structure:

1. Fullscreen preview view
2. Mask and target frame overlay
3. Top or near-target instruction text
4. Optional bottom action button / permission card

Rules:

- target frame centered
- instruction text high contrast against camera preview
- permission fallback replaces or overlays scanner controls

### 4.5 Blueprint E: Result/Decision Screen

Use for confirmation, match, and summary outcomes.

Structure:

1. Preview/info card
2. Progress/result card (mutually exclusive states)
3. Bottom action bar (`recapture` + `confirm` or `cancel` + `continue`)

Rules:

- white cards on blue background
- clear visual distinction between searching/loading and result

## 5) State Model Contract (for Stitch and UI Implementation)

Every new screen should support these states explicitly:

- `Loading`: show spinner/progress, disable actions
- `Content`: normal interactive state
- `Error`: show error card/message with recovery action
- `Empty`: show informative empty hint and optional action
- `BusyOverlay`: transient blocking overlay over existing content
- `Dialog`: confirmation or additional decision context

Action enablement rules:

- destructive/submit actions stay disabled until prerequisites are valid
- confirmation checkbox/radio selection can gate final action
- recoverable failures provide explicit retry path

## 6) Responsive and Orientation Rules

1. Keep critical actions reachable on small-height devices.
2. For long content use scrolling containers, not clipped fixed layouts.
3. Constrain readable width on tablets/landscape.
4. Provide `layout-land` / `layout-sw600dp-land` when visual hierarchy changes materially (not just spacing).

## 7) Accessibility and Internationalization Rules

1. All visible text must use string resources.
2. Interactive icons require accessible semantics (content description or intentionally decorative setting).
3. Keep text scalable; avoid hardcoded `textSize` unless absolutely necessary.
4. Avoid truncation for action labels when possible.
5. Preserve contrast on blue background with white text variants.

## 8) Stitch Output Rules (Must Follow)

When generating XML in Stitch:

1. Start from one of the blueprints in section 4.
2. Use existing style tokens before creating new styles.
3. Prefer `MaterialButton`, `MaterialCardView`, Material text input components.
4. Use `@dimen/*` and tokenized spacing; avoid raw dp where token exists.
5. Keep all labels in `strings.xml`.
6. Keep all colors in `colors.xml` or style overlays.
7. Explicitly encode loading/error/content states in layout structure and IDs.
8. Keep IDs semantic and feature-scoped (`<feature><Element><Purpose>` style).

## 9) View-to-Compose Migration Contract (M3 Foundation)

For each View-based XML component, the Compose equivalent maintains semantic and visual equivalence:

**Buttons:**

- M2 `Widget.Simprints.Button` → M3 `Button` with `primary` color role, 56dp min height
- M2 `Widget.Simprints.Button.White` → M3 `OutlinedButton` with `onPrimary` color
- M2 `Widget.Simprints.Button.Red` → M3 `Button` with `error` color role
- M2 `Widget.Simprints.TextButton.*` → M3 `TextButton` with appropriate color roles

**Text:**

- M2 `Text.*` → M3 `Text()` with `style = MaterialTheme.typography.headlineLarge` (or appropriate role)
- M2 `TextAppearance.Simprints.Headline1-6` → M3 `displayLarge/Medium/Small, headlineLarge/Medium/Small`
- M2 `TextAppearance.Simprints.Body1-2` → M3 `bodyLarge/Medium`
- Color: use `color = MaterialTheme.colorScheme.onSurface` (or appropriate semantic role)

**Cards:**

- M2 `MaterialCardView` → M3 `Card` with `containerColor = MaterialTheme.colorScheme.surface`
- Shape: `shape = MaterialTheme.shapes.medium`

**Text Inputs:**

- M2 `Widget.Simprints.FilledTextInputLayout + TextInputEditText` → M3 `TextField` with `colors = TextFieldDefaults.colors()`
- Supporting text and error text map 1:1 (use M3 `supportingText` parameter)

**Progress:**

- M2 `CircularProgressIndicator` → M3 `CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)`
- M2 `LinearProgressIndicator` → M3 `LinearProgressIndicator(color = MaterialTheme.colorScheme.primary)`

**App Bar:**

- M2 `AppBarLayout + MaterialToolbar` → M3 `TopAppBar(colors = TopAppBarDefaults.topAppBarColors(...))`
- Headline text style: `headlineLarge` (M3)

**Spacing and Shapes:**

- Use custom `SimprintsSpacing` composition local instead of hardcoded dp values
- Use `MaterialTheme.shapes.small/medium/large` instead of hardcoded corner radii

**Accessibility & Semantics:**

- M2 `contentDescription` (View) → M3 `contentDescription` (Modifier or Semantics)
- Maintain all existing accessibility patterns from XML layer

## 10) Quick Screen Recipe (Copy for Stitch Prompt)

"Create a [workflow|settings|form|scanner|result] screen using Simprints design contract:

- use Material Components 3 and Simprints M3 token mappings
- apply spacing scale 4/8/16/24/32dp (SimprintsSpacing in Compose)
- use blue workflow background with white text for operational screens, white/off-white for settings
- use 56dp min-height buttons with correct primary/secondary/error variants
- include explicit loading/content/error states
- localize all text via string resources
- constrain content width on large screens (max 500-600dp)
- structure IDs syntactically for ViewBinding and testability"

---

## Appendix A: Legacy Material Components 2 Naming

This section documents the Material Components 2 (M2) naming and implementation that remains in the current View-based XML layer. This is
for reference during XML maintenance and is not used in new Jetpack Compose work.

### A.1 Legacy Typography Naming (M2)

The following named text styles existed in Material Components 2:

| M2 Style  | Size | M3 Equivalent | Usage                           | XML Scope                                            |
|-----------|------|---------------|---------------------------------|------------------------------------------------------|
| Headline1 | 96sp | displayLarge  | Rare: extra-large headings      | `TextAppearance.Simprints.Headline1`                 |
| Headline2 | 60sp | displayMedium | Large screen titles             | `TextAppearance.Simprints.Headline2`                 |
| Headline3 | 48sp | displaySmall  | Major section titles            | `TextAppearance.Simprints.Headline3`                 |
| Headline4 | 34sp | headlineLarge | Primary screen headings         | `TextAppearance.Simprints.Headline4`                 |
| Headline5 | 24sp | headlineSmall | Tertiary headings               | `TextAppearance.Simprints.Headline5`                 |
| Headline6 | 20sp | titleLarge    | Card titles                     | `TextAppearance.Simprints.Headline6`                 |
| Subtitle1 | 16sp | titleMedium   | Subsection titles               | `TextAppearance.Simprints.Subtitle1`                 |
| Subtitle2 | 14sp | titleSmall    | Small titles, list item headers | `TextAppearance.Simprints.Subtitle2`                 |
| Body1     | 16sp | bodyLarge     | Primary body text               | `TextAppearance.Simprints.Body1` or `Text.Body1`     |
| Body2     | 14sp | bodyMedium    | Standard body text              | `TextAppearance.Simprints.Body2` or `Text.Body2`     |
| Caption   | 12sp | bodySmall     | Secondary text, captions        | `TextAppearance.Simprints.Caption` or `Text.Caption` |
| Button    | 14sp | labelLarge    | Button text, strong labels      | `TextAppearance.Simprints.Button`                    |
| Overline  | 10sp | labelSmall    | Small labels, badges            | `TextAppearance.Simprints.Overline`                  |

**Usage in M2 XML:**

- `android:textAppearance="@style/TextAppearance.Simprints.Headline5"` on `<TextView>`
- `style="@style/Text.Body1"` on `<TextView>` for inline styles
- Style inheritance: `<TextView style="@style/Text.Body2" />`

### A.2 Legacy Color Attributes (M2)

Material Components 2 exposed color roles as theme attributes:

| M2 Theme Attribute         | Simprints Mapping                          | M3 Equivalent                                  |
|----------------------------|--------------------------------------------|------------------------------------------------|
| `?attr/colorPrimary`       | `simprints_blue`                           | `MaterialTheme.colorScheme.primary`            |
| `?attr/colorPrimaryDark`   | `simprints_blue_dark`                      | `MaterialTheme.colorScheme.primaryContainer`   |
| `?attr/colorSecondary`     | `simprints_orange`                         | `MaterialTheme.colorScheme.secondary`          |
| `?attr/colorSecondaryDark` | `simprints_orange_dark`                    | `MaterialTheme.colorScheme.secondaryContainer` |
| `?attr/colorError`         | `simprints_red_dark`                       | `MaterialTheme.colorScheme.error`              |
| `?attr/colorSurface`       | `simprints_white` or `simprints_off_white` | `MaterialTheme.colorScheme.surface`            |

**Usage in M2 XML:**

```xml

<View android:background="?attr/colorPrimary" /><TextView android:textColor="?attr/colorError" />
```

### A.3 Legacy Button Styles (M2)

Material Components 2 button variants:

| M2 Style                        | Color                            | Usage              | M3 Equivalent                                                                                      |
|---------------------------------|----------------------------------|--------------------|----------------------------------------------------------------------------------------------------|
| `Widget.Simprints.Button`       | Orange (#FF7C00)                 | Primary CTA        | `Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Orange))`                      |
| `Widget.Simprints.Button.White` | White (#FFFFFF) with blue border | Secondary on blue  | `OutlinedButton(colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = Color.White))` |
| `Widget.Simprints.Button.Blue`  | Blue (#00B3D1)                   | Contextual action  | `Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0x00B3D1)))`                   |
| `Widget.Simprints.Button.Red`   | Red dark (#B8433F)               | Destructive action | `Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error))`   |
| `Widget.Simprints.TextButton.*` | Transparent with text color      | Tertiary/link      | `TextButton(...)`                                                                                  |

**Common M2 Button Properties:**

- `minHeight = 56dp` (maintain in M3)
- `textAppearance = TextAppearance.Simprints.Button` (14sp, bold)
- `cornerRadius = 4dp` (apply `shape = MaterialTheme.shapes.small` in M3)

### A.4 Legacy Widget Styles (M2)

Material Components 2 custom widget styles:

**TextInputLayout:**

- `Widget.Simprints.FilledTextInputLayout`: Filled input style, white background, blue focus state

**MaterialCardView:**

- Default corner radius: 8dp (`MaterialTheme.shapes.medium` in M3)
- Background: white (`MaterialTheme.colorScheme.surface` in M3)
- Elevation: 2dp or 4dp (use M3 `Card` shadow elevation parameter)

**AppBarLayout + MaterialToolbar:**

- Background: blue (`simprints_blue`)
- Title text: white (Headline5 or 24sp)

**RadioButton:**

- `Widget.Simprints.RadioButton`: White variant for dark/blue backgrounds
- Checked color: blue (`simprints_blue`)

### A.5 Legacy Theme Attribute References

Common theme attribute lookups in M2 XML that should be replaced with M3 direct color/shape/typography:

```text
M2: ?attr/colorPrimary           → M3: MaterialTheme.colorScheme.primary
M2: ?attr/colorSurface           → M3: MaterialTheme.colorScheme.surface
M2: ?attr/textAppearanceHeadline5 → M3: style = MaterialTheme.typography.headlineSmall
M2: ?attr/shapeAppearanceSmall   → M3: shape = MaterialTheme.shapes.small
```

### A.6 Rationale for M2 Naming

Material Components 2 used numbered heading styles (Headline1–6) and paired body/caption styles inherited from Material Design 2010–2018
typographic conventions. These did not map cleanly to functional roles across design systems at scale. M3 introduced semantic role naming (
displayLarge, headlineLarge, bodyLarge, labelLarge) to improve consistency, clarity, and composability across platforms (Web, iOS, Android,
Flutter).

This codebase initially adopted M2 to align with Material Components library defaults. As Compose adoption grows, M3 semantics provide
better long-term clarity and consistency.
