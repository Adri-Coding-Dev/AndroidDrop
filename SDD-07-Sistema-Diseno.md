## 7. Sistema de Diseno

### 7.1 Filosofia Visual

**"Energia contenida, liberada con intencion"**

Todo en la UI debe sentirse como si estuviera hecho de materia energetica:
- Suave pero con proposito
- Brillante pero no cegador
- Fluido pero con peso

### 7.2 Paleta de Colores

```kotlin
object AndroidDropColors {
    val Primary = Color(0xFF7C4DFF)
    val PrimaryVariant = Color(0xFF651FFF)
    val PrimaryDark = Color(0xFF4A148C)

    val Secondary = Color(0xFF00E5FF)
    val SecondaryVariant = Color(0xFF00B8D4)

    val Tertiary = Color(0xFFFF6EC7)
    val TertiaryVariant = Color(0xFFFF4081)

    val SurfaceDark = Color(0xFF0D0D1A)
    val SurfaceLight = Color(0xFFF5F0FF)
    val SurfaceVariant = Color(0xFF1A1A2E)

    val GlassLight = Color(0x33FFFFFF)
    val GlassMedium = Color(0x66FFFFFF)
    val GlassHeavy = Color(0x99FFFFFF)

    val EnergyLow = Color(0xFF1A237E)
    val EnergyMedium = Color(0xFF7C4DFF)
    val EnergyHigh = Color(0xFFFF6EC7)

    val Success = Color(0xFF00E676)
    val Error = Color(0xFFFF1744)
    val Warning = Color(0xFFFFEA00)
}
```

### 7.3 Tipografia

```kotlin
object AndroidDropTypography {
    val DisplayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp
    )
    val DisplayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 45.sp, lineHeight = 52.sp
    )
    val DisplaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp, lineHeight = 44.sp
    )
    val HeadlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 40.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    )
    val LabelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    )
}
```

### 7.4 Espaciados

```kotlin
object AndroidDropSpacing {
    val xxs = 2.dp; val xs = 4.dp; val sm = 8.dp
    val md = 16.dp; val lg = 24.dp; val xl = 32.dp
    val xxl = 48.dp; val xxxl = 64.dp
}
```

### 7.5 Elevacion y Sombras

- Sombras suaves, multiples capas de sombra para profundidad
- Color de sombra basado en color primario (no negro)
- Modo claro: sombras negras con 10-20% opacidad
- Modo oscuro: sombras del color primario con 30-40% opacidad

### 7.6 Iconografia

- Iconos lineales, delgados (stroke: 1.5dp - 2dp)
- Esquinas redondeadas
- Animacion sutil en transiciones
- Baseline: Lucide Icons / Phosphor Icons

### 7.7 Modos Claro/Oscuro

Soporte completo para ambos modos con transiciones animadas. El tema oscuro es el predeterminado (default-dark) para enfatizar los efectos de luz/energia.

### 7.8 Animaciones del Sistema de Diseno

| Elemento | Duracion | Curva | Descripcion |
|---|---|---|---|
| Boton press | 100ms | FastOutLinearIn | Contraer |
| Boton release | 200ms | LinearOutSlowIn | Expandir |
| Transicion pantalla | 350ms | FastOutSlowIn | Fade + slide |
| Card elevation | 200ms | StandardDecelerate | Elevacion suave |
| Feedback tactil | 50-100ms | - | Vibracion corta |
