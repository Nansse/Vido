package com.vido.ui.home

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.vido.R
import com.vido.model.VideoDetails
import com.vido.ui.video.VideoActivity
import android.widget.AdapterView.OnItemClickListener
import android.view.*
import android.widget.SearchView
import android.widget.*
import com.vido.MainActivity
import com.vido.model.Folder
import com.vido.model.TreeNode


class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var listView: ListView
    private val apiKey = "AIzaSyC8flGtYu3UmrLPZhSE_N3dRaO4SA_axNg"
    private var folder: TreeNode<Folder> = Folder.root
    private var myCustomAdapter: MyCustomAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).homeFragment = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        listView = root.findViewById(R.id.listView)

        displayVideos()


        listView.setOnItemClickListener(OnItemClickListener { arg0, arg1, arg2, arg3 ->
            // TODO Auto-generated method stub
            if (arg2 < folder.children.size) {
                setFolder(folder.children[arg2])
            } else {
                val myIntent = Intent(context, VideoActivity::class.java)
                myIntent.putExtra("video", myCustomAdapter!!.mvideos[arg2 - folder.children.size])
                startActivity(myIntent)
            }

        })

        setHasOptionsMenu(true)

        return root
    }


    private fun setFolder(folder: TreeNode<Folder>) {
        myCustomAdapter!!.mfolders = folder
        myCustomAdapter!!.mvideos = ArrayList(VideoDetails.all.filter { arg0 -> arg0.folder_name == folder.value.name })
        this.folder = folder
        myCustomAdapter!!.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_top_menu, menu)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var test = menu.findItem(R.id.action_search).actionView
        (menu.findItem(R.id.action_search).actionView as androidx.appcompat.widget.SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
        }
    }

    fun backPressed(): Boolean {
        if(folder.value.name != "root") {
            setFolder(folder.parent!!)
            return true
        }
        return false
    }

    private fun displayVideos() {
        myCustomAdapter = MyCustomAdapter(context!!)
        listView.adapter = myCustomAdapter
        Folder.fetch {
            VideoDetails.fetch {
                activity!!.runOnUiThread(java.lang.Runnable {
                    setFolder(folder)
                    myCustomAdapter!!.notifyDataSetChanged()
                })
            }
        }


/*        val requestQueu = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(
            Request.Method.GET, "https://www.googleapis.com/youtube/v3/search?part=snippet" +
                    "&q=YouTube+Data&type=video&videoCaption=closedcaption" +
                    "&key=AIzaSyC8flGtYu3UmrLPZhSE_N3dRaO4SA_axNg",
            Response.Listener<String> { response ->
                print(response)
                val jsonObject = JSONObject(response)
                val jsonArray = jsonObject.getJSONArray("items")
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val videoId = item.getJSONObject("id").getString("videoId")
                    val videoTitel = item.getJSONObject("snippet").getString("title")
                    val videoUrl = item.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url")

                    videosDetailsList.add(VideoDetails(videoId, videoTitel, videoUrl))
                }
                val myCustomAdapter = MyCustomAdapter(context!!, videosDetailsList)
                listView.adapter = myCustomAdapter
                myCustomAdapter.notifyDataSetChanged()
            },
            Response.ErrorListener { error ->
                print("error")
            })
        requestQueu.add(stringRequest)*/
    }

    private class MyCustomAdapter(context: Context): BaseAdapter() {

        private val mContext: Context
        var mvideos: ArrayList<VideoDetails> = ArrayList<VideoDetails>()
        var mfolders = Folder.root

        init {
            mContext = context
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (position < mfolders.children.size) {
                val layoutInflater = LayoutInflater.from(mContext)
                val row_folder = layoutInflater.inflate(R.layout.row_folder, parent, false)
                row_folder.findViewById<TextView>(R.id.folder_title).text = mfolders.children[position].value.name
                return row_folder

            }
            val newPosition = position - mfolders.children.size
            val layoutInflater = LayoutInflater.from(mContext)
            val row_video = layoutInflater.inflate(R.layout.row_video, parent, false)
            Glide.with(mContext).load(mvideos[newPosition].thumbnail_url).into(row_video.findViewById<ImageView>(R.id.imageView))
            row_video.findViewById<TextView>(R.id.titleView).text = mvideos[newPosition].title
            row_video.findViewById<TextView>(R.id.descriptionView).text = mvideos[newPosition].description
            return row_video
        }

        override fun getItem(position: Int): Any {
            return "Hello"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mvideos.size + mfolders.children.size
        }
    }
}
