import react.dom.*
import react.dom.client.*
import kotlinx.browser.document
import react.*
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.h3
import react.dom.html.*

import kotlin.math.*

import csstype.*
import dom.html.HTMLFormElement
import dom.html.HTMLInputElement
import emotion.react.*
import react.dom.events.ChangeEventHandler
import react.dom.events.FormEventHandler

fun Format(str: String): String = str.replace("^0+".toRegex(), "").ifEmpty { "0" }

data class Rgb(var r: Int = 0, var g: Int = 0, var b: Int = 0)
data class Cmyk(var c: Int = 0, var m: Int = 0, var y: Int = 0, var k: Int)
data class Hsv(var h: Int = 0, var s: Int = 0, var v: Int = 0)

fun CMYK2RGB(c: Int, m: Int, y: Int, k: Int): Rgb = Rgb(
    (255 * (100 - c) / 100.0 * (100 - k) / 100.0).toInt(),
    (255 * (100 - m) / 100.0 * (100 - k) / 100.0).toInt(),
    (255 * (100 - y) / 100.0 * (100 - k) / 100.0).toInt()
)

fun RGB2CMYK(r: Int, g: Int, b: Int): Cmyk {
    val k = (255 - max(max(r, g), b)) / 255.0 * 100
    val c = (100 - r / 255.0 * 100 - k) / (100.0 - k) * 100
    val m = (100 - g / 255.0 * 100 - k) / (100.0 - k) * 100
    val y = (100 - b / 255.0 * 100 - k) / (100.0 - k) * 100
    return Cmyk(round(c).toInt(), round(m).toInt(), round(y).toInt(), round(k).toInt())
}

fun HSV2RGB(h: Int, s: Int, v: Int): Rgb {
    // Normalize the inputs
    val normalizedH = h % 360
    val normalizedS = s / 100.0
    val normalizedV = v / 100.0

    // If saturation is 0, the color is a shade of gray
    if (normalizedS == 0.0) {
        val grayValue = (normalizedV * 255).toInt()
        return Rgb(grayValue, grayValue, grayValue)
    }

    val C = normalizedV * normalizedS
    val X = C * (1 - abs((normalizedH / 60.0) % 2 - 1))
    val m = normalizedV - C

    val (r, g, b) = when {
        normalizedH < 60 -> Triple(C, X, 0.0)
        normalizedH < 120 -> Triple(X, C, 0.0)
        normalizedH < 180 -> Triple(0.0, C, X)
        normalizedH < 240 -> Triple(0.0, X, C)
        normalizedH < 300 -> Triple(X, 0.0, C)
        else -> Triple(C, 0.0, X)
    }
    return Rgb(((r + m) * 255).toInt(), ((g + m) * 255).toInt(), ((b + m) * 255).toInt())
}

fun RGB2HSV(r: Int, g: Int, b: Int): Hsv {
    // Normalize RGB values to the range [0, 1]
    val rPrime = r / 255.0
    val gPrime = g / 255.0
    val bPrime = b / 255.0

    val v = maxOf(rPrime, gPrime, bPrime) // Value
    val min = minOf(rPrime, gPrime, bPrime)
    val delta = v - min

    val s = if (v == 0.0) 0.0 else delta / v // Saturation

    // Calculate Hue
    val h = when {
        delta == 0.0 -> 0.0 // Undefined hue
        v == rPrime -> 60 * ((gPrime - bPrime) / delta % 6)
        v == gPrime -> 60 * ((bPrime - rPrime) / delta + 2)
        else -> 60 * ((rPrime - gPrime) / delta + 4)
    }

    return Hsv(round(h.takeIf { it >= 0 } ?: (h + 360)).toInt(), round(s * 100).toInt(), round(v * 100).toInt())
}

fun CMYK2HSV(c: Int, m: Int, y: Int, k: Int): Hsv {
    val rgb = CMYK2RGB(c, m, y, k)
    return RGB2HSV(rgb.r, rgb.g, rgb.b)
}

fun HSV2CMYK(h: Int, s: Int, v: Int): Cmyk {
    val rgb = HSV2RGB(h, s, v)
    return RGB2CMYK(rgb.r, rgb.g, rgb.b)
}

data class ColorState(val rgb: Rgb, val cmyk: Cmyk, val hsv: Hsv)

fun FromRgb(rgb: Rgb): ColorState = ColorState(rgb, RGB2CMYK(rgb.r, rgb.g, rgb.b), RGB2HSV(rgb.r, rgb.g, rgb.b))
fun FromCmyk(cmyk: Cmyk): ColorState =
    ColorState(CMYK2RGB(cmyk.c, cmyk.m, cmyk.y, cmyk.k), cmyk, CMYK2HSV(cmyk.c, cmyk.m, cmyk.y, cmyk.k))

fun FromHsv(hsv: Hsv): ColorState = ColorState(HSV2RGB(hsv.h, hsv.s, hsv.v), HSV2CMYK(hsv.h, hsv.s, hsv.v), hsv)

external interface InputNumberProps : Props {
    var predicate: (Int) -> Boolean
    var callback: (Int) -> Unit
    var value: Int
}

private val InputField = FC<InputNumberProps>("InputField") { props ->
    div {
        css {
            borderRadius = 25.px
            backgroundColor = NamedColor.white
            padding = 5.px
            display = Display.inlineBlock
            boxShadow = BoxShadow(0.px, 0.px, 10.px, 5.px, Color("#dadada"))
        }

        input {
            css {
                position = Position.relative
                width = 70.px
                fontSize = 22.px
                borderStyle = LineStyle.hidden
                outline = 0.px
                alignContent = AlignContent.center
                background = NamedColor.transparent
            }
            value = props.value.toString()
            type = InputType.text
            onChange = {
                val res = Format(it.target.value)
                if (res.matches(Regex("\\d{0,3}")) && props.predicate(res.toInt())) {
                    props.callback(res.toInt())
                }
            }
        }
    }
}

var P255 = { it: Int -> it <= 255 }
var P360 = { it: Int -> it <= 360 }
var P100 = { it: Int -> it <= 100 }

external interface NameProps : Props {
    var name: String
}

private var name = FC<NameProps> { props ->
    p {
        css {
            fontSize = 30.px
            alignItems = AlignItems.center
            justifyItems = JustifyItems.center
            textAlign = TextAlign.center
            margin = 0.px
        }
        +props.name
    }
}

external interface ColorFieldProps : Props {
    var name: String
    var color: ColorState
    var callback: List<(Int) -> Unit>
    var predicates: List<(Int) -> Boolean>
    var properties: List<Int>
}

private val ColorField = FC<ColorFieldProps> { props ->
    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            alignContent = AlignContent.center
            justifyItems = JustifyItems.center
            gap = 10.px
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignContent = AlignContent.center
                justifyItems = JustifyItems.center
            }
            name {
                name = props.name
            }

            div {
                css {
                    display = Display.flex
                    gap = 10.px
                }
                for (i in 0..<props.properties.size) {
                    InputField {
                        predicate = props.predicates[i]
                        callback = props.callback[i]
                        value = props.properties[i]
                    }
                }
            }
        }
    }
}

external interface ScreenProps : Props {
    var color: ColorState
}

private val Screen = FC<ScreenProps> { props ->
    div {
        css {
            width = 300.px
            height = 100.px
            borderRadius = 25.px
            backgroundColor = rgb(props.color.rgb.r, props.color.rgb.g, props.color.rgb.b)
        }
    }
}

private val App = FC<Props> { props ->
    div {
        css {
            width = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.column
            alignItems = AlignItems.center
            justifyItems = JustifyItems.center
        }

        h1 {
            +"Color formats"
        }

        var cnt by useState(FromRgb(Rgb(0, 0, 0)))
        Screen {
            color = cnt
        }

        ColorField {
            name = "CMYK"
            color = cnt
            callback = listOf({
                cnt = FromCmyk(Cmyk(it, cnt.cmyk.m, cnt.cmyk.y, cnt.cmyk.k))
            }, {
                cnt = FromCmyk(Cmyk(cnt.cmyk.c, it, cnt.cmyk.y, cnt.cmyk.k))
            }, {
                cnt = FromCmyk(Cmyk(cnt.cmyk.c, cnt.cmyk.m, it, cnt.cmyk.k))
            }, {
                cnt = FromCmyk(Cmyk(cnt.cmyk.c, cnt.cmyk.m, cnt.cmyk.y, it))
            }
            )
            predicates = listOf(P100, P100, P100, P100)
            properties = listOf(cnt.cmyk.c, cnt.cmyk.m, cnt.cmyk.y, cnt.cmyk.k)
        }

        ColorField {
            name = "RGB"
            color = cnt
            callback = listOf({
                cnt = FromRgb(Rgb(it, cnt.rgb.g, cnt.rgb.b))
            }, {
                cnt = FromRgb(Rgb(cnt.rgb.r, it, cnt.rgb.b))
            }, {
                cnt = FromRgb(Rgb(cnt.rgb.r, cnt.rgb.g, it))
            }
            )
            predicates = listOf(P255, P255, P255)
            properties = listOf(cnt.rgb.r, cnt.rgb.g, cnt.rgb.b)
        }

        ColorField {
            name = "HSV"
            color = cnt
            callback = listOf({
                cnt = FromHsv(Hsv(it, cnt.hsv.s, cnt.hsv.v))
            }, {
                cnt = FromHsv(Hsv(cnt.hsv.h, it, cnt.hsv.v))
            }, {
                cnt = FromHsv(Hsv(cnt.hsv.h, cnt.hsv.s, it))
            }
            )
            predicates = listOf(P360, P100, P100)
            properties = listOf(cnt.hsv.h, cnt.hsv.s, cnt.hsv.v)
        }


    }
}


fun main() {
    val root = document.getElementById("root")?.let { createRoot(it) }
    root?.render(App.create())
}