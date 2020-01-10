package com.vido

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.vido.model.Folder
import android.widget.ArrayAdapter
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
import android.view.Window


class VideoEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)

        val mdialog = Dialog(this)

        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        mdialog.setContentView(R.layout.custom_progress_dialog)
        mdialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)


        mdialog.show()


        val publishButton = findViewById<Button>(R.id.publish_video_button)
        val titleView = findViewById<TextView>(R.id.video_title_text_view)
        val descriptionView = findViewById<TextView>(R.id.video_description_text_view)
        val spinnerView = findViewById<Spinner>(R.id.video_folder_picker)
        val adp = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Folder.all)
        spinnerView.adapter = adp

        publishButton.setOnClickListener { view ->
            Plans.instance.compress {
                thread(start = true) {
                    val folder: String = spinnerView.selectedItem.toString()
                    if (folder == "Racine") folder == "root"
                    UploadVideo.main(titleView.text.toString(), descriptionView.text.toString(), folder)
                }
            }

        }
    }
}
