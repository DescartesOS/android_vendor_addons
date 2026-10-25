/*
 * SPDX-FileCopyrightText: DescartesOS
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.crdroid.utils

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

class ShapeBackgroundDrawable(
    key: String?,
    private val fillColor: Int,
    private val strokeColor: Int = 0,
    private val strokeWidth: Float = 0f,
    private val gradientColors: IntArray? = null,
) : Drawable() {

    private val unitPath: Path = shapeFor(key).normalized().toPath()
    private val path = Path()
    private val matrix = Matrix()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = fillColor
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = strokeColor
        strokeWidth = this@ShapeBackgroundDrawable.strokeWidth
        strokeJoin = Paint.Join.ROUND
    }

    override fun onBoundsChange(bounds: Rect) {
        val inset = strokeWidth / 2f
        val w = bounds.width() - strokeWidth
        val h = bounds.height() - strokeWidth
        matrix.reset()
        matrix.setScale(w, h)
        matrix.postTranslate(bounds.left + inset, bounds.top + inset)
        unitPath.transform(matrix, path)
        gradientColors?.let {
            fillPaint.shader = LinearGradient(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.bottom.toFloat(),
                it, null, Shader.TileMode.CLAMP
            )
        }
    }

    override fun draw(canvas: Canvas) {
        if (gradientColors != null || fillPaint.alpha != 0) {
            canvas.drawPath(path, fillPaint)
        }
        if (strokeWidth > 0f) {
            canvas.drawPath(path, strokePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
        strokePaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        strokePaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    companion object {
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        private val SHAPES: List<RoundedPolygon> by lazy {
            listOf(
                MaterialShapes.Cookie4Sided,
                MaterialShapes.Cookie6Sided,
                MaterialShapes.Cookie7Sided,
                MaterialShapes.Clover4Leaf,
                MaterialShapes.Clover8Leaf,
                MaterialShapes.Sunny,
                MaterialShapes.SoftBurst,
                MaterialShapes.Cookie9Sided,
                MaterialShapes.Gem,
                MaterialShapes.Puffy,
            )
        }

        private fun shapeFor(key: String?): RoundedPolygon =
            SHAPES[Math.floorMod((key ?: "").hashCode(), SHAPES.size)]
    }
}
