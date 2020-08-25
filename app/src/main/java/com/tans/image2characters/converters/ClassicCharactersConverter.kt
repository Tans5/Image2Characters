package com.tans.image2characters.converters

import android.graphics.*
import androidx.annotation.ColorInt
import androidx.core.graphics.get

class ClassicCharactersConverter(
    val textSize: Float = 10f,
    @ColorInt val textColor: Int = Color.BLACK,
    @ColorInt val backGroundColor: Int = Color.WHITE,
    val grayChars: String = ASCII_GRAY_CHARS
) : CharactersConverter<List<List<Char>>> {

    override val paint: Paint by lazy {
        Paint().apply {
            textSize = this@ClassicCharactersConverter.textSize
            isAntiAlias = true
        }
    }

    override fun calculateDrawData(originBitmap: Bitmap): List<List<Char>> {

        return List<List<Char>>(originBitmap.height) { y ->
            List<Char>(originBitmap.width) { x ->
                val color = originBitmap[x, y]
                val a = (color shr 24 and 0x000000FF)
                val r = (color shr 16 and 0x000000FF)
                val g = (color shr 8 and 0x000000FF)
                val b = color and 0x000000FF
                if (a <= 0) {
                    ' '
                } else {
                    val gray = ((0.2126 * r + 0.7152 * g + 0.0722 * b) * a / 255)
                    val i = ((1f - (gray / 255)) * (grayChars.length - 1)).toInt()
                    grayChars[i]
                }

            }
        }
    }

    override fun draw(drawData: List<List<Char>>, canvas: Canvas) {
        paint.color = backGroundColor
        canvas.drawRect(Rect(0, 0, canvas.width, canvas.height), paint)
        paint.color = textColor
        val baseLineTop = paint.getBaseLineToTop()
        val charSize = paint.getCharSizeWithoutSpace()
        for ((line, s) in drawData.withIndex()) {
            var lineWidth = 0f
            for (c in s) {
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
        const val ASCII_GRAY_CHARS = "\$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\\\"^`'."
    }

}