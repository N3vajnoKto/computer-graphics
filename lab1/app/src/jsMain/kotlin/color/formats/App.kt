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


import csstype.*
import dom.html.HTMLFormElement
import dom.html.HTMLInputElement
import emotion.react.*
import react.dom.events.ChangeEventHandler
import react.dom.events.FormEventHandler

fun Format(str: String): String = str.replace("^0+".toRegex(), "").ifEmpty { "0" }

data class Rgb(var r: Int = 0, var g: Int = 0, var b: Int = 0)
data class Cmyk(var c: Int, var m: Int, var y: Int, var k: Int)
data class Hsv(var h: Int, var s: Int, var v: Int)

data class ColorState(var rgb: Rgb) {
    var r: Int
        get() = rgb.r
        set(value) {
            rgb.r = value
        }

    var g: Int
        get() = rgb.g
        set(value) {
            rgb.g = value
        }

    var b: Int
        get() = rgb.b
        set(value) {
            rgb.b = value
        }
}

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

external interface ColorFieldProps : Props {
    var color: ColorState
    var callback: List<(Int) -> Unit>
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
            margin = 0.px
        }
        +"RGB"
    }
}

private val RgbField = FC<ColorFieldProps> { props ->
    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            alignContent = AlignContent.center
            gap = 10.px
        }
        name {
            name = "RGB"
        }

        div {
            css {
                display = Display.flex
                gap = 10.px
            }
            InputField {
                predicate = P255
                callback = props.callback[0]
                value = props.color.r
            }

            InputField {
                predicate = P255
                callback = props.callback[1]
                value = props.color.g
            }

            InputField {
                predicate = P255
                callback = props.callback[2]
                value = props.color.b
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
            width = 100.px
            height = 100.px
            backgroundColor = rgb(props.color.r, props.color.g, props.color.b)
        }
    }
}

private val App = FC<Props> {
    h1 {
        +"Experiment"
    }

    var cnt by useState(ColorState(Rgb(0, 0, 0)))
    Screen {
        color = cnt
    }

    RgbField {
        color = cnt
        callback = listOf({
            cnt = ColorState(Rgb(it, color.g, color.b))
        }, {
            cnt = ColorState(Rgb(color.r, it, color.b))
        }, {
            cnt = ColorState(Rgb(color.r, color.g, it))
        }
        )
    }

}


fun main() {
    val root = document.getElementById("root")?.let { createRoot(it) }
    root?.render(App.create())
}