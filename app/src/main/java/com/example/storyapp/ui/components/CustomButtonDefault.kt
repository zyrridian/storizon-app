package com.example.storyapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.storyapp.R

class CustomButtonDefault @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatButton(context, attrs), View.OnTouchListener {

    init {
        // Set custom attributes for the button
        setBackgroundResource(R.drawable.bg_button_default_normal)
        setTextColor(ContextCompat.getColor(context, R.color.white))
        textSize = 16f
//        iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_cloud_upload)

        // Set padding and icon position
        setPadding(50, 20, 50, 20)
//        iconDrawable?.setBounds(0, 0, 60, 60) // Adjust size as needed


        // Remove default shadow by disabling stateListAnimator and setting elevation to 0
        stateListAnimator = null
        elevation = 0f

        setOnTouchListener(this)
    }


//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)

        // Draw the icon if it exists
//        iconDrawable?.let {
//            val iconLeft = (width - it.intrinsicWidth) / 2
//            val iconTop = (height - it.intrinsicHeight) / 2
//            canvas.save()
//            canvas.translate(iconLeft.toFloat(), iconTop.toFloat())
//            it.draw(canvas)
//            canvas.restore()
//        }
//    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                alpha = 0.7f // Dim button on press
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                alpha = 1.0f // Reset alpha on release
                performClick()
                return true
            }
        }
        return false
    }

    // Override performClick for accessibility support
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
