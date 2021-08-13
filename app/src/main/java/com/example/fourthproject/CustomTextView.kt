package com.example.fourthproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.example.fourthproject.R

class StrokedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    androidx.appcompat.widget.AppCompatButton(context, attrs, defStyle) {
    // fields
    private var _strokeColor = 0
    private var _strokeWidth: Float

    // getters + setters
    fun setStrokeColor(color: Int) {
        _strokeColor = color
    }

    fun setStrokeWidth(width: Int) {
        _strokeWidth = width.toFloat()
    }

    // overridden methods
    override fun onDraw(canvas: Canvas) {
        if (_strokeWidth > 0) {
            //set paint to fill mode
            val p: Paint = paint
            p.style = Paint.Style.FILL
            //draw the fill part of text
            super.onDraw(canvas)
            //save the text color
            val currentTextColor = currentTextColor
            //set paint to stroke mode and specify
            //stroke color and width
            p.style = Paint.Style.STROKE
            p.strokeWidth = _strokeWidth
            setTextColor(_strokeColor)
            //draw text stroke
            super.onDraw(canvas)
            //revert the color back to the one
            //initially specified
            setTextColor(currentTextColor)
        } else {
            super.onDraw(canvas)
        }
    }

    companion object {
        private const val DEFAULT_STROKE_WIDTH = 0

        fun dpToPx(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }

    // constructors
    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.StrokedTextAttrs)
            _strokeColor = a.getColor(
                R.styleable.StrokedTextAttrs_textStrokeColor,
                currentTextColor
            )
            _strokeWidth = a.getFloat(
                R.styleable.StrokedTextAttrs_textStrokeWidth,
                DEFAULT_STROKE_WIDTH.toFloat()
            )
            a.recycle()
        } else {
            _strokeColor = currentTextColor
            _strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
        }
        //convert values specified in dp in XML layout to
        //px, otherwise stroke width would appear different
        //on different screens
        _strokeWidth = dpToPx(context, _strokeWidth).toFloat()
    }
}