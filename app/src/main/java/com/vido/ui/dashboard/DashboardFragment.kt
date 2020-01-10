package com.vido.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Environment
import android.view.*
import android.widget.*
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.vido.MainActivity
import com.vido.R
import com.vido.VideoEditActivity
import com.vido.model.UploadVideo
import com.vido.model.VidoFile
import kotlin.concurrent.thread


class DashboardFragment : Fragment() {

    val VIDEO_CAPTURE = 101
    private lateinit var plans: Plans
    private lateinit var myCustomAdapter: MyCustomAdapter
    lateinit var videoView: VideoView
    lateinit var thumbnailView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        VidoFile.internalFilesDir = context!!.filesDir

        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val listView: ListView = root.findViewById(R.id.listView)
        videoView = root.findViewById(R.id.video_view)
        thumbnailView = root.findViewById(R.id.thumbnail_image_view)

        val fab: View = root.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivityForResult(intent, VIDEO_CAPTURE)
        }
        plans = Plans(context!!)
        if (plans.size() != 0) playVideo(0)
        myCustomAdapter = MyCustomAdapter(context!!, plans, this)
        listView.adapter = myCustomAdapter
        updateThumbnail()

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
        plans.merge {
            videoView.setVideoPath(VidoFile.finalVideoFile.toString())
            videoView.start()
        }
        var mC = MediaController(context)
        videoView.setMediaController(mC)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.video_top_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_plans) {
            val myIntent = Intent(context, VideoEditActivity::class.java)
            startActivity(myIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                var videoPath = String()
                val uri = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context!!.getContentResolver().query(uri!!, filePathColumn, null, null, null)
                if (cursor!!.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    videoPath = cursor!!.getString(columnIndex)
                }
                cursor.close()
                videoPath = VidoFile.copyToInternalStorage(videoPath)


                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)
                MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStorageDirectory().toString()), null, null)
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt() / 1000
                retriever.release()
                var newPlan = Plan(videoPath, duration)
                plans.add(newPlan)
                myCustomAdapter.notifyDataSetChanged()
                updateThumbnail()
                playVideo()
            }
        }
    }
}


private class MyCustomAdapter(context: Context, plans: Plans, fragment: DashboardFragment): BaseAdapter() {

    private val mContext: Context
    private val mPlans: Plans
    private val mFragment: DashboardFragment

    init {
        mContext = context
        mPlans = plans
        mFragment = fragment
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val row_plans = layoutInflater.inflate(R.layout.row_plan, parent, false)
        row_plans.findViewById<TextView>(R.id.plan_number_view).text = "Plan ${position + 1}"
        row_plans.findViewById<TextView>(R.id.timeView).text = "${mPlans.at(position).duration}s"
        row_plans.findViewById<ImageButton>(R.id.delete_button).tag = position
        row_plans.findViewById<ImageButton>(R.id.delete_button).setOnClickListener { view -> deleteVideo(view.tag.toString().toInt()) }
        row_plans.findViewById<ImageButton>(R.id.play_button).tag = position
        row_plans.findViewById<ImageButton>(R.id.play_button).setOnClickListener { view -> playVideo(view.tag.toString().toInt()) }
        return row_plans
    }
    private fun playVideo(position: Int) {
        mFragment.playVideo(position)
    }
    private fun deleteVideo(position: Int) = mContext.runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        mPlans.removeAt(position)
        mFragment.updateThumbnail()
        notifyDataSetChanged()
    }
    override fun getItem(position: Int): Any {
        return "Hello"
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mPlans.size()
    }
}