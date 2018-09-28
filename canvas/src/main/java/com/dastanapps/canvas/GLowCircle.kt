package com.dastanapps.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * TODO: document your custom view class.
 */
class GLowCircle : View {
    private var _paintSimple = Paint()
    private var _paintBlur = Paint()

    private var radius = 100f

    private var blurColor = Color.parseColor("#aa0000")

    private var blurRadius = 50f

    private var colorArray = intArrayOf(Color.parseColor("#ff0000"),
            Color.parseColor("#a80000"))

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GLowCircle, defStyle, 0)
        radius = typedArray.getDimension(R.styleable.GLowCircle_radius, radius)
        blurColor = typedArray.getColor(R.styleable.GLowCircle_blurColor, blurColor)
        val textArray = typedArray.getTextArray(R.styleable.GLowCircle_colors)
        textArray?.let {
            prepareColorArray(it)
        }
        blurRadius = radius / 2f
        typedArray.recycle()
        setup()
    }

    private fun prepareColorArray(textArray: Array<out CharSequence>) {
        if (textArray.isEmpty()) return
        colorArray = IntArray(textArray.size) { 0 }
        textArray.forEachIndexed { index, charSequence ->
            run {
                colorArray[index] = Color.parseColor(charSequence.toString())
            }
        }

    }


    fun setup() {
        if (width == 0 && height == 0) return
        _paintSimple.isAntiAlias = true
        _paintSimple.isDither = true
        //setup()
        _paintSimple.strokeJoin = Paint.Join.ROUND
        _paintSimple.strokeCap = Paint.Cap.ROUND

        _paintBlur.color = blurColor
        _paintBlur.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER)

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val radialGradient = RadialGradient(width / 2f, width / 2f, 100f,
                colorArray, null, Shader.TileMode.MIRROR)
        _paintSimple.shader = radialGradient
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, width / 2f, radius, _paintBlur)
        canvas.drawCircle(width / 2f, width / 2f, radius, _paintSimple);
    }
}
