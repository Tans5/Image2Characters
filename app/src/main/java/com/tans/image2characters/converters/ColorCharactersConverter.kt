package com.tans.image2characters.converters

import android.graphics.*
import androidx.annotation.ColorInt
import androidx.core.graphics.get
import kotlin.random.Random

class ColorCharactersConverter(
    @ColorInt val backgroundColor: Int = Color.BLACK,
    val textSize: Float = 10f,
    val grayChars: String = ClassicCharactersConverter.ASCII_GRAY_CHARS,
    val colorType: ColorType = ColorType.ImageColor
) : CharactersConverter<List<List<Pair<Char, Int>>>> {

    override val paint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = this@ColorCharactersConverter.textSize
        }
    }

    override fun calculateDrawData(originBitmap: Bitmap): List<List<Pair<Char, Int>>> {
        val width = originBitmap.width
        val height = originBitmap.height
        return List(height) { y ->
            List<Pair<Char, Int>>(width) { x ->
                val color = originBitmap[x, y]
                val a = (color shr 24 and 0x000000FF)
                val r = (color shr 16 and 0x000000FF)
                val g = (color shr 8 and 0x000000FF)
                val b = color and 0x000000FF
                if (a <= 0) {
                    ' ' to Color.WHITE
                } else {
                    val gray = ((0.2126 * r + 0.7152 * g + 0.0722 * b) * a / 255)
                    val i = ((1f - (gray / 255)) * (grayChars.length - 1)).toInt()
                    grayChars[i] to when (colorType) {
                        ColorType.ImageColor -> color
                        is ColorType.StaticColor -> colorType.color
                    }
                }
            }
        }
    }


    override fun draw(drawData: List<List<Pair<Char, Int>>>, canvas: Canvas) {
        paint.color = backgroundColor
        canvas.drawRect(Rect(0, 0, canvas.width, canvas.height), paint)
        val baseLineTop = paint.getBaseLineToTop()
        val charSize = paint.getCharSizeWithoutSpace()
        for ((line, s) in drawData.withIndex()) {
            var lineWidth = 0f
            for ((c, color) in s) {
                paint.color = color
                val xOffset = (charSize - paint.measureText(c.toString())) / 2
                canvas.drawText(
                    c.toString(),
                    lineWidth + xOffset,
                    (baseLineTop + line * charSize).toFloat(),
                    paint
                )
                lineWidth += charSize
            }
        }
    }

    companion object {
        sealed class ColorType {
            object ImageColor : ColorType()
            class StaticColor(@ColorInt val color: Int = Color.BLACK) : ColorType()
        }
    }

}