//Use code from https://www.geeksforgeeks.org/how-to-create-custom-loading-button-by-extending-viewclass-in-android/

package com.udacity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

private const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bgColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK // default color

    private var widthSize = 0
    private var heightSize = 0

    private var currentDegree = 0f
    private val r = Rect()
    val rectF = RectF()

    // tells the compiler that the value of a variable
    // must never be cached as its value may change outside
    @Volatile
    private var progress: Double = 0.0
    private var valueAnimator: ValueAnimator
    private var circleAnimator: ValueAnimator

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->

        when(new) {
            ButtonState.Loading -> {
                Log.d(TAG, "Button State Loading")
            }
            ButtonState.Completed -> {
                Log.d(TAG, "Button State Complete")
            }
            ButtonState.Clicked -> {
                Log.d(TAG, "Button State Clicked")
            }
        }
    }

    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        progress = (it.animatedValue as Float).toDouble()
        invalidate() //redraw the screen
        requestLayout() //when rectangular progress dimension changes
    }

    private val updateCircleListener = ValueAnimator.AnimatorUpdateListener {
        currentDegree = it.animatedValue as Float
        invalidate()
    }

    // call after downloading is completed
    fun hasCompletedDownload() {
        // cancel the animation when file is downloaded
        valueAnimator.cancel()
        buttonState = ButtonState.Completed
        invalidate()
        requestLayout()
    }

    // initialize
    init {
        isClickable = true
        valueAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.loading_animator) as ValueAnimator
        valueAnimator.addUpdateListener(updateListener)
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.RESTART

        circleAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.circle_animator) as ValueAnimator
        circleAnimator.addUpdateListener(updateCircleListener)
        circleAnimator.repeatCount = ValueAnimator.INFINITE
        circleAnimator.repeatMode = ValueAnimator.RESTART

        // initialize custom attributes of the button
        val attr = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        )
        try {

            //button background color
            bgColor = attr.getColor(R.styleable.LoadingButton_bgColor, ContextCompat.getColor(context, R.color.colorPrimaryDark))

            // button text color
            textColor = attr.getColor(R.styleable.LoadingButton_textColor, ContextCompat.getColor(context, R.color.white))

        } finally {
            // clearing all the data associated with attributes
            attr.recycle()
        }
    }

    // set attributes of paint
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER // button text alignment
        textSize = 55.0f // button text size
        typeface = Typeface.create("", Typeface.BOLD) // button text's font style
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Loading
        animation()

        return true
    }

    // start the animation when button is clicked
    private fun animation() {
        valueAnimator.start()
        circleAnimator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.strokeWidth = 0f
        paint.color = bgColor
        // draw custom button
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // to show rectangular progress on custom button while file is downloading
        if (buttonState == ButtonState.Loading) {
            paint.color = ContextCompat.getColor(context, R.color.colorAccent)
            canvas?.drawRect(0f, 0f, (width * (progress / 100)).toFloat(), height.toFloat(), paint)
        }

        // check the button state
        val buttonText = if (buttonState == ButtonState.Loading)
            resources.getString(R.string.button_loading)
        else resources.getString(R.string.button_name)

        // write the text on custom button
        paint.color = textColor
        canvas?.drawText(buttonText, (width /2).toFloat(), ((height + 30) / 2).toFloat(), paint)

        canvas?.getClipBounds(r)
        val cHeight = r.height()
        val cWidth = r.width()

        paint.getTextBounds(buttonText, 0, buttonText.length, r)


        rectF.set(
            (cWidth / 2f + r.width() * 0.65f) - 25f,
            (cHeight / 2) - 25f,
            (cWidth / 2f + r.width() * 0.65f) + 25f,
            (cHeight / 2) + 25f
        )

        paint.color = Color.WHITE

        //Log.d(TAG, "currentDegree : $currentDegree")

        canvas?.drawArc(
            rectF,
            0f,
            currentDegree,
            true,
            paint
        )

//        canvas?.drawArc(
//            width.toFloat()  * 0.8F - 25F,
//            height.toFloat() / 2 - 25F,
//            width.toFloat()  * 0.8F + 25F,
//            height.toFloat() / 2 + 25F,
//            0.0f,
//            currentDegree,
//            true,
//            paint)


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}