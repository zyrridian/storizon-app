package com.example.storyapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.storyapp.R

class CustomButtonOutlined @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatButton(context, attrs), View.OnTouchListener {

    init {
        setBackgroundResource(R.drawable.bg_button_outlined_normal)
        setTextColor(ContextCompat.getColor(context, R.color.black))
        setPadding(50, 20, 50, 20)
        textSize = 16f
        stateListAnimator = null
        elevation = 0f
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                alpha = 0.7f
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                alpha = 1.0f
                performClick()
                return true
            }
        }
        return false
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}