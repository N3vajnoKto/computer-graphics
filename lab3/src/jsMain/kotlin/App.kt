import canvas.CanvasRenderingContext2D
import react.dom.*
import react.dom.client.*
import kotlinx.browser.document
import kotlinx.browser.window
import react.*
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.dom.html.*


import mui.material.Slider
import kotlin.math.*

import csstype.*
import dom.html.*
import dom.html.Image
import emotion.react.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.set
import react.dom.events.ChangeEvent
import react.dom.events.MouseEventHandler
import web.performance.performance

operator fun Uint8ClampedArray.set(i: Int, v: Int) {
    asDynamic()[i] = v
}

operator fun Uint8ClampedArray.get(i: Int): Int = asDynamic()[i] as Int

// base parameters of a grid (for scale 1)
val cellSide = 25
val gridHeight = 20
val gridWidth = 40

//enum of algorithms
enum class Algorithm {
    Step,
    Brezenhem,
    DDA,
    BrezenhemCircle
}

// function that draws grid on canvas
// it takes canvas, scale, start and end points, tag for an algorithm to apply
fun DrawGrid(
    canvas: HTMLCanvasElement, scale: Double = 1.0, start: Pair<Int, Int>? = null,
    end: Pair<Int, Int>? = null, func: Algorithm? = null
) {
    val side = (cellSide * scale).toInt()
    val cellvcnt = (cellSide * gridHeight / side) + 1
    val cellhcnt = (cellSide * gridWidth / side) + 1

    //correct canvas sizes
    canvas.apply {
        width = cellhcnt * side
        height = cellvcnt * side
    }

    val w = cellhcnt * side
    val h = cellvcnt * side

    val context = canvas.getContext(RenderingContextId.canvas)
    if (context != null) {
        // draw canvas
        context.clearRect(0.0, 0.0, w.toDouble(), h.toDouble())

        context.strokeStyle = "lightgray"
        context.lineWidth = 0.5

        context.font = "10px Arial"
        context.fillStyle = "gray"

        // vertical lines
        for (x in 0..w step side) {
            context.beginPath()
            context.moveTo(x.toDouble(), 0.0)
            context.fillText((x / side).toString(), x.toDouble(), 10.0)
            context.lineTo(x.toDouble(), h.toDouble())
            context.stroke()
        }

        // horizontal lines
        for (y in 0..h step side) {
            context.beginPath()
            context.moveTo(0.0, y.toDouble())
            if (y != 0) {
                context.fillText((y / side).toString(), 0.0, y.toDouble() + 10)
            }
            context.lineTo(w.toDouble(), y.toDouble())
            context.stroke()
        }

        //draw selected start point if needed
        if (start != null) {
            val x = start.first * side
            val y = start.second * side
            if (x < w && y < h) {
                context.fillStyle = "rgba(0, 255, 0, 0.5)"
                context.fillRect(x.toDouble(), y.toDouble(), side.toDouble(), side.toDouble())
            }
        }

        //draw selected end point if needed
        if (end != null) {
            val x = end.first * side
            val y = end.second * side
            if (x < w && y < h) {
                context.fillStyle = "rgba(255, 0, 0, 0.5)"
                context.fillRect(x.toDouble(), y.toDouble(), side.toDouble(), side.toDouble())
            }
        }

        //gets the list of points computed by selected algorithm and draws them
        if (func != null && start != null && end != null) {
            val points = when (func) {
                Algorithm.Step -> StepAlgorythm(
                    start.first, start.second, end.first, end.second
                )

                Algorithm.Brezenhem -> Brezenhem(
                    start.first, start.second, end.first, end.second
                )

                Algorithm.DDA -> DDA(
                    start.first, start.second, end.first, end.second
                )

                Algorithm.BrezenhemCircle -> BrezenhemCircle(
                    start.first, start.second, end.first, end.second
                )
            }

            context.fillStyle = "black"

            for ((x, y) in points) {
                context.fillRect((x * side).toDouble(), (y * side).toDouble(), side.toDouble(), side.toDouble())
            }
        }
    }
}


// step algorithm impl
fun StepAlgorythm(xs: Int, ys: Int, xe: Int, ye: Int): List<Pair<Int, Int>> {
    val res = mutableListOf(Pair(xs, ys))

    val st = performance.now()

    if (xs == xe) {
        val b = if (ye > ys) 1 else -1
        var y = ys;
        while (y != ye) {
            y += b
            res.add(Pair(xs, y))
        }
    } else {
        println("Step algorithm")
        println("from $xs, $ys to $xe, $ye")
        if (abs(xe - xs) >= abs(ye - ys)) {
            val k = (ye - ys).toDouble() / (xe - xs).toDouble()
            val b = ys.toDouble()

            println("k = $k, b = $b")

            var x = xs
            val sx = if (xe > xs) 1 else -1
            while (x != xe) {
                x += sx
                println("$x, ${k * (x - xs) + b} -> $x, ${round(k * (x - xs) + b).toInt()}")
                res.add(Pair(x, round(k * (x - xs) + b).toInt()))
            }
        } else {
            val k = (xe - xs).toDouble() / (ye - ys).toDouble()
            val b = xs.toDouble()

            println("k = $k, b = $b")

            var y = ys
            val sy = if (ye > ys) 1 else -1
            while (y != ye) {
                y += sy
                println("${k * (y - ys) + b}, $y -> ${round(k * (y - ys) + b).toInt()}, $y")
                res.add(Pair(round(k * (y - ys) + b).toInt(), y))
            }
        }
    }

    println("total time: ${performance.now() - st}")
    return res
}


// Brezenhem algorithm impl
fun Brezenhem(x0: Int, y0: Int, x1: Int, y1: Int): List<Pair<Int, Int>> {
    val st = performance.now()
    val points = mutableListOf<Pair<Int, Int>>()

    val dx = x1 - x0
    val dy = y1 - y0
    val absDx = abs(dx)
    val absDy = abs(dy)

    val sx = if (dx >= 0) 1 else -1
    val sy = if (dy >= 0) 1 else -1

    if (absDx > absDy) {
        var err = absDx / 2
        var y = y0

        var x = x0
        while (x != x1) {
            points.add(Pair(x, y))
            err -= absDy
            if (err < 0) {
                y += sy
                err += absDx
            }

            x += sx
        }
    } else {
        var err = absDy / 2
        var x = x0

        var y = y0
        while (y != y1) {
            points.add(Pair(x, y))
            err -= absDx
            if (err < 0) {
                x += sx
                err += absDy
            }

            y += sy
        }
    }

    points.add(Pair(x1, y1))

    println("total time: ${performance.now() - st}")
    return points
}

// DDA algorithm impl
fun DDA(x0: Int, y0: Int, x1: Int, y1: Int): List<Pair<Int, Int>> {
    val st = performance.now()
    val points = mutableListOf<Pair<Int, Int>>()

    val dx = x1 - x0
    val dy = y1 - y0
    val steps = max(abs(dx), abs(dy))

    val xIncrement = dx.toDouble() / steps
    val yIncrement = dy.toDouble() / steps

    var x = x0.toDouble()
    var y = y0.toDouble()

    for (i in 0..steps) {
        points.add(Pair(x.roundToInt(), y.roundToInt()))
        x += xIncrement
        y += yIncrement
    }

    println("total time: ${performance.now() - st}")
    return points
}

// DDA algorithm impl
// Brezenhem algorithm for drawing circle impl
fun drawCircle(xCenter: Int, yCenter: Int, radius: Int): List<Pair<Int, Int>> {
    val st = performance.now()
    val points = mutableListOf<Pair<Int, Int>>()

    var x = 0
    var y = radius
    var d = 3 - 2 * radius

    while (x <= y) {
        points.add(Pair(xCenter + x, yCenter + y))
        points.add(Pair(xCenter - x, yCenter + y))
        points.add(Pair(xCenter + x, yCenter - y))
        points.add(Pair(xCenter - x, yCenter - y))
        points.add(Pair(xCenter + y, yCenter + x))
        points.add(Pair(xCenter - y, yCenter + x))
        points.add(Pair(xCenter + y, yCenter - x))
        points.add(Pair(xCenter - y, yCenter - x))

        if (d < 0) {
            d = d + 4 * x + 6
        } else {
            d = d + 4 * (x - y) + 10
            y--
        }
        x++
    }

    println("total time: ${performance.now() - st}")
    return points
}

// Brezenhem algorithm for drawing circle closure
fun BrezenhemCircle(x0: Int, y0: Int, x1: Int, y1: Int): List<Pair<Int, Int>> {
    val d = round(sqrt(((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0)).toDouble())).toInt()
    return drawCircle(x0, y0, d)
}

// main box
val App = FC<Props> { props ->
    div {
        css {
            display = Display.flex
            width = 100.pct
            height = 100.vh

            margin = 0.px
            padding = 0.px

            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
        }
        // states and their effect
        val canvasRef = useRef<HTMLCanvasElement>(null)
        var scale by useState(1.0)

        var start by useState<Pair<Int, Int>?>(null)
        var end by useState<Pair<Int, Int>?>(null)

        var func by useState<Algorithm?>(null)

        useEffect(canvasRef, scale, start, end, func) {
            val canv = canvasRef.current
            if (canv != null) {
                DrawGrid(canv, scale, start, end, func)
            }
        }

        // ui
        // vertical box
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = AlignItems.center
                gap = 10.px
            }

            //nav section (scale slider and buttons)
            div {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    alignItems = AlignItems.center
                    justifyContent = JustifyContent.center
                }

                //scale slider
                +"Scale:"
                div {
                    css {
                        marginLeft = 10.px
                        marginRight = 10.px
                        width = 100.px
                    }
                    Slider {
                        value = scale
                        max = 2
                        min = 0.5
                        step = 0.05
                        onChange = { _, v, _ ->
                            scale = (v as Double)
                        }
                    }

                }

                //step algorithm button
                div {
                    css {
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        alignContent = AlignContent.center
                        width = 100.px
                        borderRadius = 10.px

                        hover {
                            background = rgb(240, 240, 240)
                        }
                    }
                    +"Step"
                    onClick = {
                        func = Algorithm.Step
                    }
                }

                //Brezenhem algorithm button
                div {
                    css {
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        alignContent = AlignContent.center
                        width = 100.px
                        borderRadius = 10.px

                        hover {
                            background = rgb(240, 240, 240)
                        }
                    }
                    +"Brezenhem"
                    onClick = {
                        func = Algorithm.Brezenhem
                    }
                }

                //DDA algorithm button
                div {
                    css {
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        alignContent = AlignContent.center
                        width = 100.px
                        borderRadius = 10.px

                        hover {
                            background = rgb(240, 240, 240)
                        }
                    }
                    +"DDA"
                    onClick = {
                        func = Algorithm.DDA
                    }
                }

                //circle algorithm button
                div {
                    css {
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        alignContent = AlignContent.center
                        width = 150.px
                        borderRadius = 10.px

                        hover {
                            background = rgb(240, 240, 240)
                        }
                    }
                    +"Brezenhem circle"
                    onClick = {
                        func = Algorithm.BrezenhemCircle
                    }
                }
            }

            //grid
            canvas {
                ref = canvasRef
                css {
                    cursor = Cursor.crosshair
                    display = Display.inlineBlock
                    background = rgb(250, 250, 250)
                }

                // map coords on click
                onClick = { e ->
                    run {
                        func = null
                        val rect = canvasRef.current?.getBoundingClientRect()
                        if (rect != null) {
                            val x = (e.clientX - rect.x).toInt() / (cellSide * scale).toInt()
                            val y = (e.clientY - rect.y).toInt() / (cellSide * scale).toInt()

                            if (e.ctrlKey || start == null) {
                                start = Pair(x, y)
                            } else {
                                end = Pair(x, y)
                            }
                        }
                    }
                }
            }

            //description
            div {
                css {
                    fontSize = 14.px
                    width = (gridWidth * cellSide).px
                }
                h3 {
                    +"Описание"
                }
                +"Выберите на сетке две клетки (зелёную и красную), для этого щёлкните по полю. Когда первая(зелёная) клетка поставлена, следующие щелчки будут менять положение второй(красной) клетки. Для изменения первой(зелёной) клетки, щёлкните по полю с зажатой клавишей ctrl."
                +"Чтобы отрисовать линии, нажмите на название соответствующего алгоритма."
                +"Для изменения масштаба используйте слайдер."
                +"Координаты точки указвывают в центр клетки."
            }
        }

    }
}

fun main() {
    val root = document.getElementById("root")?.let { createRoot(it) }
    root?.render(App.create())
}