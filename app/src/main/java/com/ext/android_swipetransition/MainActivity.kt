package com.ext.android_swipetransition

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ext.android_swipe_transition.SwipeTransitionView

class MainActivity : AppCompatActivity() {

    private lateinit var swipeView: SwipeTransitionView
    private lateinit var counterText: TextView
    private lateinit var instructionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeView = findViewById(R.id.swipeView)
        counterText = findViewById(R.id.counterText)
        instructionText = findViewById(R.id.instructionText)

        val imageList = listOf(
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3,
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
        )

        swipeView.setImages(imageList)
        updateCounter()

        swipeView.onSwipeListener = object : SwipeTransitionView.OnSwipeListener {

            override fun onSwipeStart(currentIndex: Int) {
                instructionText.alpha = 0.3f
            }

            override fun onSwipeProgress(progress: Float, currentIndex: Int) {
                // No UI update needed
            }

            override fun onSwipeComplete(
                direction: String,
                oldIndex: Int,
                newIndex: Int
            ) {
                updateCounter()
                instructionText.alpha = 1f
            }

            override fun onSwipeCancel(currentIndex: Int) {
                instructionText.alpha = 1f
            }

            override fun onLastImage() {
                // 🔁 LOOP BACK TO FIRST IMAGE
                swipeView.reset()
                updateCounter()

                instructionText.text = "Looping back to first image"
                instructionText.alpha = 1f
            }
        }
    }

    private fun updateCounter() {
        val current = swipeView.getCurrentIndex() + 1
        val total = swipeView.getImageCount()
        counterText.text = "$current / $total"
    }
}
