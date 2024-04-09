package com.android.example

import android.content.Context
import android.os.Handler
import android.text.InputFilter
import android.text.Spanned
import android.text.method.NumberKeyListener
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.dastanapps.appwidget.R
import java.util.Formatter
import java.util.Locale

/* loaded from: classes.dex */
class NumberPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = DEFAULT_MIN
) : LinearLayout(context, attrs), View.OnClickListener, OnFocusChangeListener, OnLongClickListener {
    protected var mCurrent: Int = 0
    private var mDecrement = false
    private val mDecrementButton: NumberPickerButton
    private var mDisplayedValues: Array<String>? = null
    protected var mEnd: Int
    private var mFormatter: Formatter? = null
    private var mHandler: Handler
    private var mIncrement = false
    private val mIncrementButton: NumberPickerButton
    private var mListener: OnChangedListener? = null
    private val mNumberInputFilter: InputFilter
    protected var mPrevious: Int = 0
    private val mRunnable: Runnable
    private var mSpeed = 300L
    protected var mStart: Int
    private val mText: EditText

    /* loaded from: classes.dex */
    interface Formatter {
        fun toString(i: Int): String
    }

    /* loaded from: classes.dex */
    interface OnChangedListener {
        fun onChanged(numberPicker: NumberPicker?, i: Int, i2: Int)
    }

    init {
        this.mHandler = Handler()
        this.mRunnable = object : Runnable {
            // from class: com.android.example.NumberPicker.2
            // java.lang.Runnable
            override fun run() {
                if (this@NumberPicker.mIncrement) {
                    this@NumberPicker.changeCurrent(this@NumberPicker.mCurrent + 1)
                    mHandler.postDelayed(this, this@NumberPicker.mSpeed)
                } else if (this@NumberPicker.mDecrement) {
                    this@NumberPicker.changeCurrent(this@NumberPicker.mCurrent - 1)
                    mHandler.postDelayed(this, this@NumberPicker.mSpeed)
                }
            }
        }
        orientation = VERTICAL
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.number_picker, this as ViewGroup, true)

        val inputFilter: InputFilter = NumberPickerInputFilter(this, null)
        this.mNumberInputFilter = NumberRangeKeyListener(this, null)
        this.mIncrementButton = findViewById<View>(R.id.increment) as NumberPickerButton
        mIncrementButton.setOnClickListener(this)
        mIncrementButton.setOnLongClickListener(this)
        mIncrementButton.setNumberPicker(this)
        this.mDecrementButton = findViewById<View>(R.id.decrement) as NumberPickerButton
        mDecrementButton.setOnClickListener(this)
        mDecrementButton.setOnLongClickListener(this)
        mDecrementButton.setNumberPicker(this)
        this.mText = findViewById<View>(R.id.timepicker_input) as EditText
        mText.onFocusChangeListener = this
        mText.filters = arrayOf(inputFilter)
        mText.setRawInputType(2)
        if (!isEnabled) {
            isEnabled = false
        }
        this.mStart = DEFAULT_MIN
        this.mEnd = DEFAULT_MAX
    }

    // android.view.View
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mIncrementButton.isEnabled = enabled
        mDecrementButton.isEnabled = enabled
        mText.isEnabled = enabled
    }

    fun setOnChangeListener(listener: OnChangedListener?) {
        this.mListener = listener
    }

    fun setFormatter(formatter: Formatter?) {
        this.mFormatter = formatter
    }

    fun setRange(start: Int, end: Int) {
        this.mStart = start
        this.mEnd = end
        this.mCurrent = start
        updateView()
    }

    fun setRange(start: Int, end: Int, displayedValues: Array<String>?) {
        this.mDisplayedValues = displayedValues
        this.mStart = start
        this.mEnd = end
        this.mCurrent = start
        updateView()
    }

    fun setSpeed(speed: Long) {
        this.mSpeed = speed
    }

    // android.view.View.OnClickListener
    override fun onClick(v: View) {
        validateInput(this.mText)
        if (!mText.hasFocus()) {
            mText.requestFocus()
        }
        if (R.id.increment === v.id) {
            changeCurrent(this.mCurrent + 1)
        } else if (R.id.decrement === v.id) {
            changeCurrent(this.mCurrent - 1)
        }
    }

    private fun formatNumber(value: Int): String {
        if (this.mFormatter != null) {
            return mFormatter!!.toString(value)
        }
        return value.toString()
    }

    protected fun changeCurrent(current: Int) {
        var current = current
        if (current > this.mEnd) {
            current = this.mStart
        } else if (current < this.mStart) {
            current = this.mEnd
        }
        this.mPrevious = this.mCurrent
        this.mCurrent = current
        notifyChange()
        updateView()
    }

    protected fun notifyChange() {
        if (this.mListener != null) {
            mListener!!.onChanged(this, this.mPrevious, this.mCurrent)
        }
    }

    protected fun updateView() {
        if (this.mDisplayedValues == null) {
            mText.setText(formatNumber(this.mCurrent))
        } else {
            mText.setText(mDisplayedValues!![mCurrent - this.mStart])
        }
        mText.setSelection(mText.text.length)
    }

    private fun validateCurrentView(str: CharSequence) {
        val `val` = getSelectedPos(str.toString())
        if ((`val` >= this.mStart && `val` <= mEnd) && this.mCurrent != `val`) {
            this.mPrevious = this.mCurrent
            this.mCurrent = `val`
            notifyChange()
        }
        updateView()
    }

    // android.view.View.OnFocusChangeListener
    override fun onFocusChange(v: View, hasFocus: Boolean) {
        mText.setSelection(
            DEFAULT_MIN,
            mText.length()
        )
        if (!hasFocus) {
            validateInput(v)
        }
    }

    private fun validateInput(v: View) {
        val str = (v as TextView).text.toString()
        if ("" == str) {
            updateView()
        } else {
            validateCurrentView(str)
        }
    }

    // android.view.View.OnLongClickListener
    override fun onLongClick(v: View): Boolean {
        mText.clearFocus()
        if (R.id.increment === v.id) {
            this.mIncrement = true
            mHandler.post(this.mRunnable)
        } else if (R.id.decrement === v.id) {
            this.mDecrement = true
            mHandler.post(this.mRunnable)
        }
        return true
    }

    fun cancelIncrement() {
        this.mIncrement = false
    }

    fun cancelDecrement() {
        this.mDecrement = false
    }

    /* loaded from: classes.dex */
    private inner class NumberPickerInputFilter private constructor() : InputFilter {
        /* synthetic */
        constructor(
            numberPicker: NumberPicker?,
            numberPickerInputFilter: NumberPickerInputFilter?
        ) : this()

        // android.text.InputFilter
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence {
            if (this@NumberPicker.mDisplayedValues == null) {
                return mNumberInputFilter.filter(source, start, end, dest, dstart, dend)
            }
            val filtered: CharSequence = source.subSequence(start, end).toString()
            val result = dest.subSequence(DEFAULT_MIN, dstart).toString()
                .toString() + (filtered as Any) + (dest.subSequence(dend, dest.length) as Any)
            val str = result.toString().lowercase(Locale.getDefault())
            val strArr = this@NumberPicker.mDisplayedValues
            val length = strArr!!.size
            for (i in DEFAULT_MIN until length) {
                val `val` = strArr[i]
                if (`val`.lowercase(Locale.getDefault()).startsWith(str)) {
                    return filtered
                }
            }
            return ""
        }
    }

    /* loaded from: classes.dex */
    private inner class NumberRangeKeyListener private constructor() : NumberKeyListener() {
        /* synthetic */
        constructor(
            numberPicker: NumberPicker?,
            numberRangeKeyListener: NumberRangeKeyListener?
        ) : this()

        // android.text.method.KeyListener
        override fun getInputType(): Int {
            return 2
        }

        // android.text.method.NumberKeyListener
        override fun getAcceptedChars(): CharArray {
            return DIGIT_CHARACTERS
        }

        // android.text.method.NumberKeyListener, android.text.InputFilter
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence {
            var filtered = super.filter(source, start, end, dest, dstart, dend)
            if (filtered == null) {
                filtered = source.subSequence(start, end)
            }
            val result = dest.subSequence(DEFAULT_MIN, dstart).toString()
                .toString() + (filtered as Any) + (dest.subSequence(dend, dest.length) as Any)
            if ("" == result) {
                return result
            }
            val `val` = this@NumberPicker.getSelectedPos(result)
            return if (`val` > this@NumberPicker.mEnd) "" else filtered
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun getSelectedPos(str: String): Int {
        var str = str
        if (this.mDisplayedValues == null) {
            return str.toInt()
        }
        for (i in DEFAULT_MIN until mDisplayedValues!!.size) {
            str = str.lowercase(Locale.getDefault())
            if (mDisplayedValues!![i].lowercase(Locale.getDefault()).startsWith(str)) {
                return this.mStart + i
            }
        }
        return try {
            str.toInt()
        } catch (e: NumberFormatException) {
            mStart
        }
    }

    var current: Int
        get() {
            validateInput(this.mText)
            return this.mCurrent
        }
        set(current) {
            this.mCurrent = current
            updateView()
        }

    companion object {
        private const val DEFAULT_MAX = 200
        private const val DEFAULT_MIN = 0
        private const val TAG = "NumberPicker"
        @JvmField
        val TWO_DIGIT_FORMATTER: Formatter = object : Formatter {
            // from class: com.android.example.NumberPicker.1
            val mBuilder: StringBuilder = StringBuilder()
            val mFmt: java.util.Formatter = Formatter(this.mBuilder)
            val mArgs: Array<Any?> = arrayOfNulls(1)

            // com.android.example.NumberPicker.Formatter
            override fun toString(value: Int): String {
                mArgs[DEFAULT_MIN] = value
                mBuilder.delete(
                    DEFAULT_MIN,
                    mBuilder.length
                )
                mFmt.format("%02d", *this.mArgs)
                return mFmt.toString()
            }
        }
        private val DIGIT_CHARACTERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    }
}
