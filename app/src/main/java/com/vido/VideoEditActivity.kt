package com.vido

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vido.model.Folder
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.provider.MediaStore
import com.vido.model.UploadVideo
import com.vido.ui.dashboard.Plans
import kotlin.concurrent.thread
import android.app.Dialog
import android.view.Window.FEATURE_NO_TITLE
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import android.view.Window
import android.widget.*
import com.vido.model.VideoDetails
import kotlinx.android.synthetic.main.activity_video_edit.*
import org.w3c.dom.Text
import kotlin.math.roundToInt


class VideoEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)

        val publishButton = findViewById<Button>(R.id.publish_video_button)
        val titleView = findViewById<TextView>(R.id.video_title_text_view)
        val descriptionView = findViewById<TextView>(R.id.video_description_text_view)
        val spinnerView = findViewById<Spinner>(R.id.video_folder_picker)
        val progressTextView = findViewById<TextView>(R.id.progress_text)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val folderTitleView = findViewById<TextView>(R.id.folder_title_view)
        val adp = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Folder.all)

        progressBar.setVisibility(View.GONE)
        progressTextView.setVisibility(View.GONE)

        spinnerView.adapter = adp
        progressBar.progress = 0
        publishButton.setOnClickListener { view ->
            progressBar.setVisibility(View.VISIBLE)
            progressTextView.setVisibility(View.VISIBLE)
            titleView.visibility = View.GONE
            publishButton.visibility = View.GONE
            spinnerView.visibility = View.GONE
            descriptionView.visibility = View.GONE
            folderTitleView.visibility = View.GONE

            var folder: String = spinnerView.selectedItem.toString()
            if (folder == "Racine") folder = "root"

//            thread(start = true) {
//                Upload.main(this)
//            }

            progressTextView.text = "Compression"
            thread(start = true) {
                Plans.instance.compress {
                    thread(start = true) {
                        UploadVideo.main(titleView.text.toString(), descriptionView.text.toString(), folder, { text, nb ->
                            runOnUiThread({
                                progressBar.progress = nb
                                progressTextView.text = text;
                            })
                        })
                        this.runOnUiThread( {
                            VideoDetails.fetch {
                                finish()
                            }
                        })
                    }
                }

            }
        }
    }

}
