package com.vido.ui.dashboard.MyCamera

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.MotionEvent
import android.view.TextureView
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import com.vido.R
import java.io.File

@SuppressLint("RestrictedApi")
class MyCameraActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var viewFinder: TextureView
    private lateinit var captureButton: ImageButton
    private lateinit var videoCapture: VideoCapture
    private lateinit var file: File


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_camera)
        viewFinder = findViewById(R.id.view_finder)
        captureButton = findViewById(R.id.capture_button)
        file = File(filesDir, "${System.currentTimeMillis()}.mp4")

        startCamera()
        captureButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                captureButton.setBackgroundColor(Color.GREEN)
                videoCapture.startRecording(file, object: VideoCapture.OnVideoSavedListener{
                    override fun onVideoSaved(file: File?) {
                        Log.i("", "Video File : $file")
                    }
                    override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
                        Log.i("", "Video Error: $message")
                    }
                })

            } else if (event.action == MotionEvent.ACTION_UP) {
                captureButton.setBackgroundColor(Color.RED)
                videoCapture.stopRecording()
                Log.i("", "Video File stopped")
            }
            false
        }
    }
    private fun startCamera() {
        CameraX.unbindAll()
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1,1))
            //setTargetResolution(Size(640,640))
        }.build()
        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            if (viewFinder.display != null) setTargetRotation(viewFinder.display.rotation)
        }.build()
        videoCapture = VideoCapture(videoCaptureConfig)

        preview.setOnPreviewOutputUpdateListener {
            viewFinder.surfaceTexture = it.surfaceTexture
        }
        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, preview, videoCapture)

    }
}
