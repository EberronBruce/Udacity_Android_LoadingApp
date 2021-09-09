//Use code from https://www.geeksforgeeks.org/how-to-create-custom-loading-button-by-extending-viewclass-in-android/

package com.udacity

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

private const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bgColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK // default color
    private var loadColor: Int = Color.BLACK
    private var circleColor: Int = Color.WHITE

    private var widthSize = 0
    private var heightSize = 0

    private var currentDegree = 0f
    private val r = Rect()
    private val rectF = RectF()

    // tells the compiler that the value of a variable
    // must never be cached as its value may change outside
    @Volatile
    private var progress: Double = 0.0
    private lateinit var valueAnimator: ValueAnimator
    private lateinit var circleAnimator: ValueAnimator

    private val animationSet = AnimatorSet().apply {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                Log.d(TAG, "Animation has Started")
                this@LoadingButton.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                Log.d(TAG, "Animation has Stopped")
                this@LoadingButton.isEnabled = true
            }
        })
    }

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->

        when(new) {
            ButtonState.Loading -> {
                Log.d(TAG, "Button State Loading")
                animationSet.playTogether(valueAnimator, circleAnimator)
                animationSet.start()
            }
            ButtonState.Completed -> {
                Log.d(TAG, "Button State Complete")
                animationSet.cancel()
                currentDegree = 0.0f
                progress = 0.0

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
        requestLayout()
    }

    // call after downloading is completed
//    fun hasCompletedDownload() {
//        // cancel the animation when file is downloaded
//        changeButtonState(ButtonState.Completed)
//        invalidate()
//        requestLayout()
//    }

    // initialize
    init {
        isClickable = true
        setupBarAnimator()
        setupCircleAnimator()
        setupColorsFromAttributes(attrs)
    }

    private fun setupBarAnimator() {
        valueAnimator = AnimatorInflater.loadAnimator(context, R.animator.loading_animator) as ValueAnimator
        valueAnimator.addUpdateListener(updateListener)
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.RESTART
    }

    private fun setupCircleAnimator() {
        circleAnimator = AnimatorInflater.loadAnimator(context, R.animator.circle_animator) as ValueAnimator
        circleAnimator.addUpdateListener(updateCircleListener)
        circleAnimator.repeatCount = ValueAnimator.INFINITE
        circleAnimator.repeatMode = ValueAnimator.RESTART
    }

    private fun setupColorsFromAttributes(attrs: AttributeSet?) {
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
            // load color
            loadColor = attr.getColor(R.styleable.LoadingButton_loadColor, ContextCompat.getColor(context, R.color.colorAccent))
            // circle colorP
            circleColor = attr.getColor(R.styleable.LoadingButton_circleColor, ContextCompat.getColor(context, R.color.white))
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
        Log.d(TAG, "Perform Click")
        super.performClick()
        changeButtonState(ButtonState.Loading)
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val buttonText = if (buttonState == ButtonState.Loading)
            resources.getString(R.string.button_loading)
        else resources.getString(R.string.button_name)

        canvas?.apply {
            drawBackground()
            drawLoadingBar()
            drawText(buttonText)
            drawCircle(buttonText)
        }
    }


    private fun Canvas.drawBackground() {
        paint.strokeWidth = 0f
        paint.color = bgColor
        // draw custom button
        drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun Canvas.drawLoadingBar() {
        paint.color = loadColor
        drawRect(0f, 0f, (width * (progress / 100)).toFloat(), height.toFloat(), paint)
    }

    private fun Canvas.drawText(buttonText: String)  {
        // write the text on custom button
        paint.color = textColor
        drawText(buttonText, (width /2).toFloat(), ((height + 30) / 2).toFloat(), paint)
    }

    private fun Canvas.drawCircle(buttonText: String) {
        getClipBounds(r)
        circleFrame(buttonText)
        paint.color = circleColor
        drawArc(
            rectF,
            0f,
            currentDegree,
            true,
            paint
        )
    }

    private fun circleFrame(buttonText: String) {
        val cHeight = r.height()
        val cWidth = r.width()
        paint.getTextBounds(buttonText, 0, buttonText.length, r)
        rectF.set(
            (cWidth / 2f + r.width() * 0.65f) - 25f,
            (cHeight / 2) - 25f,
            (cWidth / 2f + r.width() * 0.65f) + 25f,
            (cHeight / 2) + 25f
        )
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

    fun changeButtonState(state: ButtonState) {
        if (buttonState != state) {
            buttonState = state
            invalidate()
            requestLayout()
        }
    }

}