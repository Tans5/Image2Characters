package com.tans.image2characters.converters

import android.graphics.*

interface CharactersConverter<DrawData> {

    val paint: Paint

    fun calculateOutputSize(originBitmap: Bitmap, drawData: DrawData): Pair<Int, Int>

    fun calculateDrawData(originBitmap: Bitmap): DrawData

    fun draw(drawData: DrawData, canvas: Canvas)

    fun convert(originBitmap: Bitmap): Bitmap {
        val drawData = calculateDrawData(originBitmap)
        val (outWidth, outHeight) = calculateOutputSize(originBitmap, drawData)
        val result = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(result)
        draw(drawData, canvas)
        return result
    }

    fun Paint.getCharSize(): Int {
        val fm = fontMetricsInt
        return fm.descent + fm.bottom + fm.leading - fm.ascent - fm.top
    }

    fun Paint.getBaseLineToTop(): Int {
        val fm = fontMetricsInt
        return - fm.ascent - fm.top
    }

    fun Paint.getBaseLineToTopWithOutSpace(): Int {
        val fm = fontMetricsInt
        return - fm.ascent
    }

    fun Paint.getCharSizeWithoutSpace(): Int {
        val fm = fontMetricsInt
        return fm.descent - fm.ascent
    }
}