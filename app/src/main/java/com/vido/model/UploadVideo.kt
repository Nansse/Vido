package com.vido.model

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.fasterxml.jackson.core.JsonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.InputStreamContent
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoSnippet
import com.google.api.services.youtube.model.VideoStatus
import com.google.common.collect.Lists


import java.io.IOException
import java.util.ArrayList
import java.util.Calendar
import com.google.api.client.http.javanet.NetHttpTransport
import com.vido.ui.dashboard.Plans
import java.net.URL
import kotlin.concurrent.thread
import com.google.android.youtube.player.internal.v
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp
import kotlin.math.roundToInt


/**
 * Upload a video to the authenticated user's channel. Use OAuth 2.0 to
 * authorize the request. Note that you must add your video files to the
 * project folder to upload them with this application.
 *
 * @author Jeremy Walker
 */
object UploadVideo {

    // Authentificate with OAuth 2.0 Playground

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private var youtube: YouTube? = null

    /**
     * Define a global variable that specifies the MIME type of the video
     * being uploaded.
     */
    private val VIDEO_FILE_FORMAT = "video/mp4"


    fun deleteVideo(video: VideoDetails) {
        thread(start = true) {
            val credential = GoogleCredential().setAccessToken(accessToken())
            youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory(), credential)
                .setApplicationName(
                    "youtube-cmdline-uploadvideo-sample"
                ).build()
            val deleter = youtube!!.videos().delete(video.youtube_id)
            deleter.execute()
        }
    }


    fun main(title: String, description: String, folder: String, progress: (String, Int) -> Unit) {
        val videoPath = VidoFile.finalVideoFile.toString()

        try {
            // Authorize the request.
            //val credential = Auth.authorize(scopes, "uploadvideo")
            //val credential = GoogleCredential().setAccessToken("ya29.Il-1B0oeao3dBC6iDUAyIBNTbAHmNXcL6L-lPVACb9qeqjKqthuWvjWtHSXJjXgp2KLKAbNYkMyqA522tIoE7LNFy7ixMPLr2jZM2R5g6_vA-pJZEBg212yUW8TobBvS3w")
            val credential = GoogleCredential().setAccessToken(accessToken())
            //GoogleCredential().setRefreshToken("") // Get it from playground
            // This object is used to make YouTube Data API requests.

            youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory(), credential).setApplicationName(
                "youtube-cmdline-uploadvideo-sample").build()

            println("Uploading: ${videoPath}")

            // Add extra information to the video before uploading.
            val videoObjectDefiningMetadata = Video()

            // Set the video to be publicly visible. This is the default
            // setting. Other supporting settings are "unlisted" and "private."
            val status = VideoStatus()
            status.privacyStatus = "unlisted"
            videoObjectDefiningMetadata.status = status

            // Most of the video's metadata is set on the VideoSnippet object.
            val snippet = VideoSnippet()

            // This code uses a Calendar instance to create a unique name and
            // description for test purposes so that you can easily upload
            // multiple files. You should remove this code from your project
            // and use your own standard names instead.
            val cal = Calendar.getInstance()
            snippet.title = title
            snippet.description = description

            // Add the completed snippet object to the video resource.
            videoObjectDefiningMetadata.snippet = snippet
            print(videoPath)

            val mediaContent = InputStreamContent(VIDEO_FILE_FORMAT,
                VidoFile.finalVideoFile.inputStream())
            mediaContent.setLength(VidoFile.finalVideoFile.length())

            // Insert the video. The command sends three arguments. The first
            // specifies which information the API request is setting and which
            // information the API response should return. The second argument
            // is the video resource that contains metadata about the new video.
            // The third argument is the actual video content.
            val videoInsert = youtube!!.videos()
                .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent)

            // Set the upload type and add an event listener.
            val uploader = videoInsert.mediaHttpUploader

            // Indicate whether direct media upload is enabled. A value of
            // "True" indicates that direct media upload is enabled and that
            // the entire media content will be uploaded in a single request.
            // A value of "False," which is the default, indicates that the
            // request will use the resumable media upload protocol, which
            // supports the ability to resume an upload operation after a
            // network interruption or other transmission failure, saving
            // time and bandwidth in the event of network failures.
            uploader.isDirectUploadEnabled = false

            val progressListener = MediaHttpUploaderProgressListener { uploader ->
                uploader.progress
                when (uploader.uploadState) {
                    MediaHttpUploader.UploadState.INITIATION_STARTED -> progress("Initialisation...", (uploader.progress*100).roundToInt())
                    MediaHttpUploader.UploadState.INITIATION_COMPLETE -> progress("Initialisation terminé", (uploader.progress*100).roundToInt())
                    MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> { progress("Téléversement: " + (uploader.progress*100).roundToInt().toString(), (uploader.progress*100).roundToInt())}
                }
            }
            uploader.progressListener = progressListener

            // Call the API and upload the video.
            val returnedVideo = videoInsert.execute()

            // Print data about the newly inserted video from the API response.
            println("\n================== Returned Video ==================\n")
            println("  - Id: " + returnedVideo.id)
            println("  - Title: " + returnedVideo.snippet.title)
            println("  - Tags: " + returnedVideo.snippet.tags)
            println("  - Privacy Status: " + returnedVideo.status.privacyStatus)
            println("  - Video Count: " + returnedVideo.statistics.viewCount)
            val thumbnail = returnedVideo.snippet.thumbnails.default.url


            val video = hashMapOf(
                "date_publication" to Timestamp(System.currentTimeMillis()),
                "deleted" to false,
                "description" to description,
                "folder_name" to folder,
                "thumbnail_url" to thumbnail,
                "title" to title,
                "youtube_id" to returnedVideo.id
            )
            val db = FirebaseFirestore.getInstance()
            db.document(User.company!!.path).collection("videos").add(video).addOnFailureListener{ it ->
                print(it.toString())
            }



        } catch (e: GoogleJsonResponseException) {
            System.err.println("GoogleJsonResponseException code: " + e.details.code + " : "
                    + e.details.message)
            e.printStackTrace()
        } catch (e: IOException) {
            System.err.println("IOException: " + e.message)
            e.printStackTrace()
        } catch (t: Throwable) {
            System.err.println("Throwable: " + t.message)
            t.printStackTrace()
        }

    }

    private fun accessToken(): String {
        var refreshToken = User.refreshToken!!
        var clienId = User.youtubeId!!
        var clientSecret = User.youtubeSecret!!

        var response = GoogleRefreshTokenRequest(NetHttpTransport(), JacksonFactory(), refreshToken, clienId, clientSecret).execute()
        return response.accessToken
    }
}