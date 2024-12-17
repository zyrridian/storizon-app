package com.example.storyapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.storyapp.R

class CustomEmailEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private var clearButtonImage: Drawable
    private var errorStateChangedListener: ((Boolean) -> Unit)? = null // Callback for error state

    init {

        hint = resources.getString(R.string.hint_email_edit_text)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_clear) as Drawable
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        setOnTouchListener(this)

        // Apply custom text attributes
        textSize = 16f
        setTextColor(ContextCompat.getColor(context, R.color.black))
        setBackgroundResource(R.drawable.bg_edit_text)
        setPaddingRelative(50, 40, 50, 40)

        // Monitor changes in the text
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) showClearButton() else hideClearButton()
                validateEmail(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    private fun showClearButton() {
        setButtonDrawables(endOfTheText = clearButtonImage)
    }

    private fun hideClearButton() {
        setButtonDrawables()
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    private fun validateEmail(s: CharSequence?) {
        val isErrorPresent = if (s != null && !isValidEmail(s)) {
            error = "Invalid email format."
            true
        } else {
            error = null
            false
        }
        errorStateChangedListener?.invoke(isErrorPresent) // Trigger callback on error change
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            val clearButtonStart: Float
            val clearButtonEnd: Float
            val isClearButtonClicked: Boolean

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                clearButtonEnd = (clearButtonImage.intrinsicWidth + paddingStart).toFloat()
                isClearButtonClicked = event.x < clearButtonEnd
            } else {
                clearButtonStart = (width - paddingEnd - clearButtonImage.intrinsicWidth).toFloat()
                isClearButtonClicked = event.x > clearButtonStart
            }

            if (isClearButtonClicked) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clearButtonImage = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_clear
                        ) as Drawable
                        showClearButton()
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        clearButtonImage = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_clear
                        ) as Drawable
                        text?.clear()
                        hideClearButton()
                        return true
                    }

                    else -> return false
                }
            }
        }
        return false
    }
}