
package com.vido.model

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import com.google.android.youtube.player.internal.i





object VidoFile {
    lateinit var internalFilesDir: File // /data/data/com.vido.blabla/files
    val finalVideoFile: File
        get() = File(internalFilesDir.toString() + "/MOV_output.mp4")
    val compressedFinalVideoFile: File
        get() = File(internalFilesDir.toString() + "/COMPRESSED_MOV_output.mp4")

    fun copyToInternalStorage(src: String): String {
        val dst = internalFilesDir.toString() + "/" + src.toString().split("/").last()
        FileInputStream(src).use({ input ->
            FileOutputStream(dst).use({ out ->
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len= input.read(buf)
                while (len > 0) {
                    out.write(buf, 0, len)
                    len = input.read(buf)
                }
            })
        })
        File(src).delete()
        return dst
    }

}