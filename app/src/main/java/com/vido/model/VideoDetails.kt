package com.vido.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    var folder_name: String? = null,
    val thumbnail_url: String? = null,
    val title: String? = null,
    val youtube_id: String? = null,
    var reference: DocumentReference? = null
): Serializable  {

    fun changeFolder(new: String){
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf("folder_name" to new)
        this.folder_name = new
        db.document(reference!!.path).set(data, SetOptions.merge())
    }
    fun delete(){
        val db = FirebaseFirestore.getInstance()
        db.document(reference!!.path).delete()
        UploadVideo.deleteVideo(this)
        all.remove(this)

    }


    companion object {
        var all = arrayListOf<VideoDetails>()
        private var didFetch = false
        fun fetch(after: () -> Unit) {
            if (didFetch) {
                after()
            }
            var eph = arrayListOf<VideoDetails>()
            val db = FirebaseFirestore.getInstance()
            db.document(User.company!!.path).collection("videos")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        document.reference
                        var vd = VideoDetails(
                            title = document.getString("title"),
                            youtube_id = document.getString("youtube_id"),
                            description = document.getString("description"),
                            thumbnail_url = document.getString("thumbnail_url"),
                            folder_name = document.getString("folder_name"),
                            reference = document.reference
                        )
                        eph.add(vd)
                    }
                    all = eph
                    after()
                    didFetch = true
                }
                .addOnFailureListener { exception ->
                    print("hey")
                }
        }
    }
}