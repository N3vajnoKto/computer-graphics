import canvas.CanvasRenderingContext2D
import react.dom.*
import react.dom.client.*
import kotlinx.browser.document
import kotlinx.browser.window
import react.*
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.img
import react.dom.html.*

import kotlin.math.*

import csstype.*
import dom.html.*
import dom.html.Image
import emotion.react.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.set
import org.w3c.dom.ImageData
import org.w3c.files.get
import react.dom.events.ChangeEvent
import react.dom.events.MouseEventHandler
import web.file.FileReader

operator fun Uint8ClampedArray.set(i: Int, v: Int) {
    asDynamic()[i] = v
}

operator fun Uint8ClampedArray.get(i: Int): Int = asDynamic()[i] as Int


fun applyLaplass(context: CanvasRenderingContext2D?, width: Double, height: Double) {
    val imageData = context?.getImageData(0.0, 0.0, width, height)
    val data = imageData?.data ?: return

    val copy = Uint8ClampedArray(data.length)

    val sharpenMatrix = arrayOf(
        -1, -1, -1,
        -1, 9, -1,
        -1, -1, -1
    )

    for (y in 0 until imageData.height) {
        for (x in 0 until imageData.width) {
            var r = 0
            var g = 0
            var b = 0

            for (dy in -1..1) {
                for (dx in -1..1) {
                    val weight = sharpenMatrix[(dy + 1) * 3 + (dx + 1)]
                    val pixelIndex = ((y + dy) * imageData.width + (x + dx)) * 4
                    if (pixelIndex < 0 || pixelIndex >= data.length) {
                        continue
                    }
                    r += (data[pixelIndex] * weight)
                    g += (data[pixelIndex + 1] * weight)
                    b += (data[pixelIndex + 2] * weight)
                }
            }

            // Ограничиваем значения цветов
            val index = (y * imageData.width + x) * 4

            copy[index] = r.coerceIn(0, 255)
            copy[index + 1] = g.coerceIn(0, 255)
            copy[index + 2] = b.coerceIn(0, 255)
            copy[index + 3] = data[index + 3]
        }
    }

    for (i in 0 until data.length) {
        data[i] = copy[i]
    }

    // Обновляем данные изображения на канвасе
    context.putImageData(imageData, 0.0, 0.0)
}

fun applyLog(context: CanvasRenderingContext2D?, width: Double, height: Double) {
    val imageData = context?.getImageData(0.0, 0.0, width, height)
    val data = imageData?.data ?: return

    val copy = Uint8ClampedArray(data.length)

    val sharpenMatrix = arrayOf(
        0, 0, -1, 0, 0,
        0, -1, -2, -1, 0,
        -1, -2, 17, -2, -1,
        0, -1, -2, -1, 0,
        0, 0, -1, 0, 0
    )

    for (y in 0 until imageData.height) {
        for (x in 0 until imageData.width) {
            var r = 0
            var g = 0
            var b = 0

            for (dy in -2..2) {
                for (dx in -2..2) {
                    val weight = sharpenMatrix[(dy + 2) * 5 + (dx + 2)]
                    val pixelIndex = ((y + dy) * imageData.width + (x + dx)) * 4
                    if (pixelIndex < 0 || pixelIndex >= data.length) {
                        continue
                    }
                    r += (data[pixelIndex] * weight)
                    g += (data[pixelIndex + 1] * weight)
                    b += (data[pixelIndex + 2] * weight)
                }
            }

            // Ограничиваем значения цветов
            val index = (y * imageData.width + x) * 4

            copy[index] = r.coerceIn(0, 255)
            copy[index + 1] = g.coerceIn(0, 255)
            copy[index + 2] = b.coerceIn(0, 255)
            copy[index + 3] = data[index + 3]
        }
    }

    for (i in 0 until data.length) {
        data[i] = copy[i]
    }

    // Обновляем данные изображения на канвасе
    context.putImageData(imageData, 0.0, 0.0)
}


fun applyGlobal(context: CanvasRenderingContext2D?, width: Double, height: Double) {
    val imageData = context?.getImageData(0.0, 0.0, width, height)
    val data = imageData?.data ?: return

    val T = 128

    for (i in 0 until data.length step 4) {
        val sum = max(data[i], max(data[i + 1], data[i + 2]))
        val res = when {
            sum <= T -> 0
            else -> 255
        }

        data[i] = res;
        data[i + 1] = res;
        data[i + 2] = res;
    }

    // Обновляем данные изображения на канвасе
    context.putImageData(imageData, 0.0, 0.0)
}

fun applyOtsu(context: CanvasRenderingContext2D?, width: Double, height: Double) {
    val imageData = context?.getImageData(0.0, 0.0, width, height)
    val data = imageData?.data ?: return

    var dsp = 0.0
    var argmax = 127

    for (T in 0..255) {
        var p1 = 0
        var p2 = 0
        var mean1 = 0.0
        var mean2 = 0.0

        for (i in 0 until data.length step 4) {
            val vl = max(data[i], max(data[i + 1], data[i + 2]))

            if (vl <= T) {
                p1 += 1
                mean1 += vl
            } else {
                p2 += 1
                mean2 += vl
            }
        }

        var disp =
            p1 / (data.length / 4) * (p1 / (data.length / 4)) * (mean1 / p1 - mean2 / p2) * (mean1 / p1 - mean2 / p2)
        if (disp > dsp) {
            dsp = disp
            argmax = T
        }
    }

    for (i in 0 until data.length step 4) {
        val vl = max(data[i], max(data[i + 1], data[i + 2]))
        var res = when {
            vl <= argmax -> 0
            else -> 255
        }

        data[i] = res
        data[i + 1] = res
        data[i + 2] = res
    }

    context.putImageData(imageData, 0.0, 0.0)
}

external interface FileProps : Props {
    var file: String?
}

val chooseFile = FC<FileProps> { props ->
    span {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.center
        }
        +(props.file ?: "choose file")
    }
}

external interface FilterButtonProps : Props {
    var name: String
    var onClick: MouseEventHandler<HTMLDivElement>?
}

val filterButton = FC<FilterButtonProps> { props ->
    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.center
            paddingLeft = 5.px
            paddingRight = 5.px
            background = NamedColor.white
            color = NamedColor.black
            borderRadius = 5.px

            hover {
                background = rgb(240, 240, 240)
            }
        }

        +props.name

        onClick = props.onClick

    }
}


external interface NavProps : Props {
    var file: String?
    var callback: (ChangeEvent<HTMLInputElement>) -> Unit
    var onButton: (Int) -> Unit
}

val nav = FC<NavProps> { props ->
    div {
        css {
            position = Position.fixed
            display = Display.flex
            flexDirection = FlexDirection.row
            gap = 30.px
            justifyContent = JustifyContent.center
            margin = 0.px;
            padding = 5.px;
            height = 20.px
            width = 100.pct
            left = 0.px;
            top = 0.px
        }

        div {
            css {
                display = Display.flex
                paddingLeft = 5.px
                paddingRight = 5.px
                background = NamedColor.white
                color = NamedColor.black
                borderRadius = 5.px
                outline = 0.px

                hover {
                    background = rgb(240, 240, 240)
                }
            }

            val fileInputRef = useRef<HTMLInputElement>(null)

            onClick = {
                fileInputRef.current?.click()
            }

            chooseFile {
                file = props.file
            }

            input {
                css {
                    position = Position.fixed
                    left = -1000.px
                    top = -1000.px
                }
                type = InputType.file
                accept = "*/*"
                ref = fileInputRef
                onChange = { e ->
                    run {
                        props.callback(e)
                    }
                }
            }
        }

        filterButton {
            name = "Laplass"
            onClick = { props.onButton(0) }
        }

        filterButton {
            name = "LOG"
            onClick = { props.onButton(1) }
        }

        filterButton {
            name = "Bin"
            onClick = { props.onButton(2) }
        }

        filterButton {
            name = "Otsu"
            onClick = { props.onButton(3) }
        }
    }
}

external interface BodyProps : Props {
    var file: String?
    var src: String?
    var filter: Int
}

val body = FC<BodyProps> { props ->
    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            margin = 30.px
            width = 100.pct - 60.px
            height = 100.vh - 60.px
        }

        // original
        div {
            css {
                display = Display.flex
                alignContent = AlignContent.center
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                width = 50.pct - 30.px
                height = 100.pct
            }

            if (props.file != null) {
                img {
                    css {
                        maxWidth = 100.pct - 20.px;
                        maxHeight = 100.pct - 20.px;
                    }
                    src = props.src
                }
            }
        }

        // processed
        div {
            css {
                display = Display.flex
                alignContent = AlignContent.center
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                width = 50.pct - 30.px
                height = 100.pct
            }

            val canvasRef = useRef<HTMLCanvasElement>(null)

            useEffect(props.src, props.filter) {
                val imgSrc = props.src
                if (imgSrc != null) {
                    val canvas = canvasRef.current
                    val context = canvas?.getContext(RenderingContextId.canvas)
                    val img = Image().apply { this.src = imgSrc }

                    img.onload = {
                        canvas?.width = img.width
                        canvas?.height = img.height

                        context?.drawImage(img, 0.0, 0.0)
                        when (props.filter) {
                            0 -> applyLaplass(context, img.width.toDouble(), img.height.toDouble())
                            1 -> applyLog(context, img.width.toDouble(), img.height.toDouble())
                            2 -> applyGlobal(context, img.width.toDouble(), img.height.toDouble())
                            3 -> applyOtsu(context, img.width.toDouble(), img.height.toDouble())
                            else -> {}
                        }
                    }
                }
            }

            if (props.file != null) {

                canvas {
                    css {
                        maxWidth = 100.pct - 20.px;
                        maxHeight = 100.pct - 20.px;
                    }
                    ref = canvasRef
                }
            }

        }
    }
}

val App = FC<Props> { props ->
    div {
        var filename: String? by useState(null)
        var imgSrc: String? by useState(null)
        var filterSrc by useState(0)
        nav {
            file = filename
            callback = { e ->
                run {
                    val file = e.target.files?.get(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = { e ->
                            run {
                                imgSrc = (reader.result as String?)
                            }
                        }
                        reader.readAsDataURL(file)
                    }

                    filename = file?.name
                }
            }
            onButton = {
                filterSrc = it
            }
        }
        body {
            file = filename
            src = imgSrc
            filter = filterSrc
        }
    }
}

fun main() {
    val root = document.getElementById("root")?.let { createRoot(it) }
    root?.render(App.create())
}