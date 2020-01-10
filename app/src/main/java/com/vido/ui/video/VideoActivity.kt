package com.vido.ui.video

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubePlayerView
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.vido.R
import com.vido.model.VideoDetails
import com.vido.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.activity_main.*


class VideoActivity : YouTubeBaseActivity() {
    private lateinit var video: VideoDetails
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.vido.R.layout.activity_video)
        val youTubePlayerView = findViewById<View>(com.vido.R.id.player) as YouTubePlayerView
        video = intent.getSerializableExtra("video") as VideoDetails


        var titleView = findViewById<TextView>(R.id.titleView)
        var descriptionView = findViewById<TextView>(R.id.descriptionView)
        titleView.text = video.title
        descriptionView.text = video.description
        youTubePlayerView.initialize("AIzaSyC8flGtYu3UmrLPZhSE_N3dRaO4SA_axNg",
            object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(
                    provider: YouTubePlayer.Provider,
                    youTubePlayer: YouTubePlayer, b: Boolean
                ) {
                    youTubePlayer.cueVideo(video.youtube_id)

                }

                override fun onInitializationFailure(
                    provider: YouTubePlayer.Provider,
                    youTubeInitializationResult: YouTubeInitializationResult
                ) {

                }
            })
    }

}
