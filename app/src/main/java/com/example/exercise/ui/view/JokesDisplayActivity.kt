package com.example.exercise.ui.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.exercise.R
import com.example.exercise.data.api.RandomJokeWebService
import com.example.exercise.data.model.JokeCache
import com.example.exercise.data.network.RandomJokeRepository
import com.example.exercise.data.network.RetrofitRandomJoke
import com.example.exercise.data.util.JokeResponse
import com.example.exercise.databinding.ActivityJokesDisplayBinding
import com.example.exercise.ui.util.AnimationUtil
import com.example.exercise.ui.util.CommonUtil
import com.example.exercise.ui.viewModel.JokeViewModel
import com.example.exercise.ui.viewModel.JokeViewModelFactory

class JokesDisplayActivity : AppCompatActivity() {
    private val TAG: String = "JokesDisplayActivity"
    private var threeHundredDpAsPixels: Int = 300
    private var previousCardView: ViewGroup? = null
    private var currentCardView: ViewGroup? = null
    private var currentTextView: TextView? = null
    private var jokeViewModel: JokeViewModel? = null
    private var binding: ActivityJokesDisplayBinding? = null
    private var currentPage: Int? = 0
    private var animationType = -1 ;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJokesDisplayBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        threeHundredDpAsPixels = (300.0f * resources.displayMetrics.density + 0.5f).toInt()

        val firstName = intent.getStringExtra(CommonUtil.FIRST_NAME)
        val lastName = intent.getStringExtra(CommonUtil.LAST_NAME)

        val randomJokeWebService = RetrofitRandomJoke().getApi(RandomJokeWebService::class.java)
        val jokeCache: JokeCache? = JokeCache.instance

        val randomJokeRepository = RandomJokeRepository(randomJokeWebService,jokeCache)
        val factory = JokeViewModelFactory(randomJokeRepository, firstName!!,lastName!!,currentPage!!)

        enableProgressBar()
        jokeViewModel = ViewModelProvider(this, factory).get(JokeViewModel::class.java)
        setObserverOnJokeViewModel(jokeViewModel?.jokeResponse)

        setTouchListenerOnRootView()    // sets gesture on linear layout for handling page swipes

        addCardViewToRootView(createCardView())
    }

    /*
             Handles the gestures events and loads jokes based on the swipe action of the user
     */
    fun setTouchListenerOnRootView() {
        binding?.linearLayoutRoot?.setOnTouchListener(object :
                OnSwipeTouchListener(this@JokesDisplayActivity) {
                override fun onSwipeLeft() {
                Log.d(TAG, "onSwipeLeft is called")
                    currentPage = jokeViewModel?.pageIndex  // gets the current page index from model 
                    if (currentPage == 0) {
                        return;
                    }
                    animationType = AnimationUtil.LEFT_IN_ANIMATION
                    currentPage = currentPage!! - 1
                    getJokeFromJokeViewModel(currentPage!!)
            }

            override fun onSwipeRight() {
                Log.d(TAG, "onSwipeRight is called")
                    animationType = AnimationUtil.RIGHT_IN_ANIMATION
                    currentPage = jokeViewModel?.pageIndex // get the current pageIndex from model
                    currentPage = currentPage!! + 1
                    getJokeFromJokeViewModel(currentPage!!) 
            }
        })
    }

    fun createCardView(): ViewGroup {
        val card_view = CardView(this@JokesDisplayActivity)

        val layoutParams = LinearLayout.LayoutParams(
                this@JokesDisplayActivity.threeHundredDpAsPixels, // CardView width
                this@JokesDisplayActivity.threeHundredDpAsPixels // CardView height
        )
        layoutParams.gravity = Gravity.CENTER
        card_view.layoutParams = layoutParams //  Set the card view layout params
        card_view.radius = 4F    // Set the card view corner radius

        val textView = createTextView(this@JokesDisplayActivity)
        card_view.addView(textView)
        return card_view
    }

    fun createTextView(context: Context): View {
        val textView = TextView(this)
        val textViewLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        textView.gravity = Gravity.CENTER
        textView.layoutParams = textViewLayoutParams
        return textView
    }


    fun addCardViewToRootView(newCardView: ViewGroup) {
        previousCardView = currentCardView;
        currentCardView = newCardView;
        
        currentTextView = currentCardView?.get(0) as TextView
        previousCardView?.animation = null; // remove animation for outgoing view
        binding?.linearLayoutRoot!!.addView(currentCardView)
        binding?.linearLayoutRoot!!.removeView(previousCardView)
    }

    fun setObserverOnJokeViewModel(jokeResponse: LiveData<JokeResponse>?) {
        jokeResponse?.observe(this@JokesDisplayActivity, Observer { response ->
                if (response.result == CommonUtil.RESULT_SUCCESS) {
                    if (animationType != -1) {
                        addCardView(animationType)
                    }
                    currentTextView?.text = response.joke
                    Log.d(TAG, "onSuccess, joke = " + response.joke)
                } else {
                    if(currentPage!! > 0) {
                        // decrement page index to avoid showing of empty pages when swiping backwards
                        jokeViewModel?.decrementPageIndex()
                    }
                    if (response.isNetworkError) {
                        CommonUtil.showInternetConnectionError(this@JokesDisplayActivity)
                    } else {
                        val title: String = this@JokesDisplayActivity.getString(R.string.error)
                        CommonUtil.showDialog(
                            this@JokesDisplayActivity,
                            title,
                            response.errorMessage
                        )
                    }
                }
                binding?.progressBar?.visibility = View.GONE
        })
    }
 
    fun getJokeFromJokeViewModel(pageIndex: Int) {
        enableProgressBar()
        jokeViewModel?.getJoke(pageIndex)
    }

    fun enableProgressBar() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    open class OnSwipeTouchListener(ctx: Context?) : View.OnTouchListener {
        private val gestureDetector: GestureDetector

        companion object {
            private val SWIPE_THRESHOLD = 20
            private val SWIPE_VELOCITY_THRESHOLD = 50
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
            ): Boolean {
                var result = false
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > Companion.SWIPE_THRESHOLD && Math.abs(velocityX) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                            if (e1.x > e2.x) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                            result = true
                        }
                    } else if (Math.abs(diffY) > Companion.SWIPE_THRESHOLD && Math.abs(velocityY) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom()
                        } else {
                            onSwipeTop()
                        }
                        result = true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return result
            }
        }

        open fun onSwipeRight() {}
        open fun onSwipeLeft() {}
        open fun onSwipeTop() {}
        open fun onSwipeBottom() {}

        init {
            gestureDetector = GestureDetector(ctx, GestureListener())
        }
    }

    fun addCardView(animationType: Int) {
        val cardView: ViewGroup = createCardView()
        cardView.animation = AnimationUtil.getAnimation(applicationContext, animationType)
        addCardViewToRootView(cardView)
    }
 
    // This function wil be used to handle to navigate to previous page , since it is not mentioned in the requirement, 
    // going to previous page is not handled.
    override fun onBackPressed() {
        super.onBackPressed()
    }
}