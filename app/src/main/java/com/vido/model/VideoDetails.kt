package com.vido.model

import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.io.Serializable

/*
public class VideoDetails {
    var videoId: String
    var title: String
    var url: String

    constructor(videoId: String, title: String, url: String) {
        this.videoId = videoId
        this.title = title
        this.url = url
    }
}*/
data class VideoDetails(
    val date_publication: String? = null,
    val deleted: Boolean? = null,
    val description: String? = null,
    val folder_name: String? = null,
    val thumbnail_url: String? = null,
    val title: String? = null,
    val youtube_id: String? = null
): Serializable  {
    companion object {
        val all = arrayListOf<VideoDetails>()
        private var didFetch = false
        fun fetch(after: () -> Unit) {
            if (didFetch) {
                after()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.document(User.company!!.path).collection("videos")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var vd = VideoDetails(
                            title = document.getString("title"),
                            youtube_id = document.getString("youtube_id"),
                            description = document.getString("description"),
                            thumbnail_url = document.getString("thumbnail_url"),
                            folder_name = document.getString("folder_name")
                        )
                        all.add(vd)
                        all.add(vd)
                        all.add(vd)
                        all.add(vd)
                        all.add(vd)
                        all.add(vd)
                        all.add(vd)
                    }
                    after()
                    didFetch = true
                }
                .addOnFailureListener { exception ->
                    print("hey")
                }
        }
    }
}