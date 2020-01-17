package com.vido.ui.video

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.icu.text.CaseMap
import android.os.Bundle
import android.util.AttributeSet
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubePlayerView
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.firebase.firestore.FirebaseFirestore
import com.vido.R
import com.vido.model.Folder
import com.vido.model.User
import com.vido.model.VideoDetails
import com.vido.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.activity_main.*


class VideoActivity : YouTubeBaseActivity() {
    private lateinit var video: VideoDetails
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.vido.R.layout.activity_video)
        val youTubePlayerView = findViewById<View>(com.vido.R.id.player) as YouTubePlayerView
        video = VideoDetails.all[intent.getSerializableExtra("video_index").toString().toInt()]
        val changeFolderButton = findViewById<Button>(R.id.change_folder_button)
        val deleteButton = findViewById<Button>(R.id.delete_video_button)

        deleteButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Etes vous sur?")
            builder.setPositiveButton("Oui", DialogInterface.OnClickListener({_ ,_ ->
                video.delete()
                VideoDetails.fetch { finish() }
            }))

            builder.create().show()
        }


        changeFolderButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choisir un dossier")

            builder.setItems(Folder.all.toArray(Folder.all.map { el -> el as CharSequence }.toTypedArray()), {_, which ->
                val selected = Folder.all[which]
                if (selected == "Racine") video.changeFolder("root")
                else video.changeFolder(selected)
            })
            builder.create().show()
        }

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
