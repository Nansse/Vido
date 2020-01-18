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
import android.content.Intent
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.app.Activity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.camera.camera2.Camera2Config
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraView
import com.vido.MyApplication
import java.util.concurrent.Executor




@SuppressLint("RestrictedApi")
class MyCameraActivity : AppCompatActivity(){

    private lateinit var cameraView: CameraView
    private lateinit var captureButton: ImageButton
    private lateinit var file: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CameraX.isInitialized()) {
            CameraX.unbindAll()
            CameraX.shutdown()
        }
        CameraX.initialize(this, (application as MyApplication).cameraXConfig)
        ProcessCameraProvider.getInstance(this)
        setContentView(R.layout.activity_my_camera)
        cameraView = findViewById(R.id.view_finder)
        cameraView.captureMode = CameraView.CaptureMode.VIDEO
        cameraView.isPinchToZoomEnabled = true
        cameraView.bindToLifecycle(this)
        captureButton = findViewById(R.id.capture_button)
        file = File(filesDir, "${System.currentTimeMillis()}.mp4")

        captureButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                captureButton.setBackgroundColor(Color.GREEN)

                cameraView.startRecording(file, object: Executor {
                    override fun execute(command: Runnable) {
                        runOnUiThread({command.run()})
                    }
                },
                    object : VideoCapture.OnVideoSavedCallback {
                        override fun onVideoSaved(file: File) {
                            val intent = Intent()
                            intent.putExtra("file_path", file!!.path.toString())
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }

                        override fun onError(
                            videoCaptureError: Int,
                            message: String,
                            cause: Throwable?
                        ) {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    })

            } else if (event.action == MotionEvent.ACTION_UP) {
                captureButton.setBackgroundColor(Color.RED)
                cameraView.stopRecording()
            }
            false
        }

        val switchButton = findViewById<ImageButton>(R.id.switch_camera_button)
        switchButton.setOnClickListener {
            cameraView.toggleCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
