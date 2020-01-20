package com.vido.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore.EXTRA_VIDEO_QUALITY
import android.text.Layout
import android.view.*
import android.widget.*
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.vido.MainActivity
import com.vido.R
import com.vido.VideoEditActivity
import com.vido.model.UploadVideo
import com.vido.model.VidoFile
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import com.vido.ui.dashboard.DranNDrop.SimpleItemTouchHelperCallback
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.vido.ui.dashboard.DranNDrop.ItemTouchHelperAdapter
import com.vido.ui.dashboard.DranNDrop.ItemTouchHelperViewHolder
import com.vido.ui.dashboard.DranNDrop.RecyclerViewAdapter
import com.vido.ui.dashboard.MyCamera.MyCameraActivity
import java.util.*


class DashboardFragment : Fragment() {

    val VIDEO_CAPTURE = 101
    private lateinit var plans: Plans
    private lateinit var myCustomAdapter: RecyclerViewAdapter
    lateinit var videoView: VideoView
    lateinit var thumbnailView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        VidoFile.internalFilesDir = context!!.filesDir

        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val listView: RecyclerView = root.findViewById(R.id.listView)
        videoView = root.findViewById(R.id.video_view)
        videoView.setVideoPath(VidoFile.finalVideoFile.toString())
        thumbnailView = root.findViewById(R.id.thumbnail_image_view)
        val fab: View = root.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            context.runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
                val intent = Intent(context, MyCameraActivity::class.java)
                startActivityForResult(intent, VIDEO_CAPTURE)
            }
        }
        plans = Plans(context!!)
        var mC = MediaController(context)
        videoView.setMediaController(mC)
        videoView.setOnPreparedListener { mp -> mp.setOnSeekCompleteListener { videoView.start() }}

        if (plans.size() != 0) playVideo(0)
        myCustomAdapter = RecyclerViewAdapter(context!!, plans, this)
        val callback = SimpleItemTouchHelperCallback(myCustomAdapter)
        val touchHelper = ItemTouchHelper(callback)
        listView.adapter = myCustomAdapter
        listView.setHasFixedSize(true);
        listView.setLayoutManager(LinearLayoutManager(activity));
        touchHelper.attachToRecyclerView(listView)
        updateThumbnail()
        myCustomAdapter.notifyDataSetChanged()

        setHasOptionsMenu(true)

        return root
    }

    fun updateThumbnail() {
        if (plans.size() == 0) {
            videoView.visibility = View.INVISIBLE
            thumbnailView.visibility = View.VISIBLE
        } else {
            videoView.visibility = View.VISIBLE
            thumbnailView.visibility = View.INVISIBLE
        }
    }

    fun playVideo(position: Int = 0) {
        if (position == 0) videoView.start()
        videoView.setVideoPath(VidoFile.finalVideoFile.toString())
        videoView.seekTo(plans.startTimeFor(position).roundToInt())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.video_top_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_plans && plans.size() != 0) {
            val myIntent = Intent(context, VideoEditActivity::class.java)
            startActivity(myIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {

                var videoPath = data!!.getStringExtra("file_path")
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)
                MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStorageDirectory().toString()), null, null)
                val duration =
                    (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toDouble() / 1000).roundToInt()
                val durationMS =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toDouble()
                retriever.release()
                var newPlan = Plan(videoPath, duration, durationMS, plans.size(), plans.size(), UUID.randomUUID().toString())
                plans.add(newPlan, {
                    myCustomAdapter.notifyDataSetChanged()
                    updateThumbnail()
                    playVideo()
                })

            }
        }
    }
}