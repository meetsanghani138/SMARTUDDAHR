package com.marwadiuniversity.radhe.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.marwadiuniversity.radhe.repositories.SalesDataPoint
import com.marwadiuniversity.radhe.utils.CurrencyFormatter

class CustomBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<SalesDataPoint> = emptyList()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16834A")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#758078")
        textSize = 28f
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#17231D")
        textSize = 26f
        isFakeBoldText = true
    }

    fun setData(points: List<SalesDataPoint>) {
        dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val paddingBottom = 60f
        val paddingTop = 40f
        val chartHeight = height - paddingTop - paddingBottom

        val maxVal = dataPoints.maxOfOrNull { it.amountPaise }?.coerceAtLeast(100L) ?: 100L
        val barWidth = (width / (dataPoints.size * 2)).coerceAtMost(60f)
        val spacing = (width - (barWidth * dataPoints.size)) / (dataPoints.size + 1)

        var currentX = spacing

        for (point in dataPoints) {
            val barHeight = (point.amountPaise.toFloat() / maxVal.toFloat()) * chartHeight
            val top = height - paddingBottom - barHeight
            val bottom = height - paddingBottom

            val rect = RectF(currentX, top, currentX + barWidth, bottom)
            canvas.drawRoundRect(rect, 12f, 12f, barPaint)

            // Value text
            val valStr = CurrencyFormatter.formatPaiseToRupees(point.amountPaise)
            canvas.drawText(valStr, currentX, top - 10f, valuePaint)

            // Label text
            val dateLabel = point.label.takeLast(5)
            canvas.drawText(dateLabel, currentX, height - 15f, textPaint)

            currentX += barWidth + spacing
        }
    }
}
