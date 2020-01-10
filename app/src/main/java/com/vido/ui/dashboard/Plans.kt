package com.vido.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vido.model.VidoFile
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import java.io.File


class Plans(context: Context) {

    companion object {
        lateinit var instance: Plans
    }

    private var list: ArrayList<Plan>
    private var mPrefs = context.getSharedPreferences("vidoPlans", Context.MODE_PRIVATE)
    private val gson: Gson
    private val mContext: Context

    init {
        mContext = context
        val builder = GsonBuilder()
        builder.registerTypeAdapter(Uri::class.java, UriAdapter())
        gson = builder.create()
        FFmpeg.getInstance(mContext).isSupported()

        list = arrayListOf<Plan>()
        //clear()
        val size = mPrefs.getInt("size", 0)
        for(i in 0 until size) {
            val json = mPrefs.getString("${i}", "")
            val obj = gson.fromJson(json, Plan::class.java)
            list.add(obj)
        }
        Plans.instance = this
    }

    fun at(index: Int): Plan = list[index]

    fun size(): Int = list.size

    fun add(plan: Plan) {
        val prefsEditor = mPrefs.edit()
        prefsEditor.putString("${list.size}", gson.toJson(plan))
        prefsEditor.putInt("size", list.size+1)
        prefsEditor.commit()
        list.add(plan)
        storeStream(plan, size()-1)
        merge { }
    }

    fun removeAt(at: Int) {
        val prefsEditor = mPrefs.edit()
        prefsEditor.remove("${at}")
        deleteStream(at)
        for(i in at+1 until list.size) {
            val json = mPrefs.getString("${i}", "")
            prefsEditor.putString("${i-1}", json)
            val plan = gson.fromJson(json, Plan::class.java)
            prefsEditor.remove("${i}")
            deleteStream(i)
            storeStream(plan, i-1)
        }
        prefsEditor.putInt("size", list.size-1)
        prefsEditor.commit()
        File(list[at].video_path).delete()
        list.removeAt(at)
        if(size() == 0) {
            VidoFile.finalVideoFile.delete()
        } else {
            merge {}
        }
    }
    private fun clear() {
        val editor: SharedPreferences.Editor = mPrefs.edit()
        editor.clear()
        editor.commit()
    }
    private fun deleteStream(at: Int) {
        val file = File(VidoFile.internalFilesDir.toString() + "/intermediate${at}.ts")
        file.delete()
    }
    private fun storeStream(plan: Plan, at: Int) {
        var ffmpeg = FFmpeg.getInstance(mContext)
        ffmpeg.execute(("-y -i ${plan.video_path} -c copy -bsf:v h264_mp4toannexb -f mpegts " +
                VidoFile.internalFilesDir.toString() + "/intermediate${at}.ts")
            .split(" ")
            .toTypedArray(), object: ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {

            }
            override fun onFailure(message: String?){
                print(message)
            }
        })
    }

    fun merge(after: () -> Unit) {
        var ffmpeg = FFmpeg.getInstance(mContext)
        var executable = "-y -i concat:"
        for (i in 0 until size()) {
            executable += VidoFile.internalFilesDir.toString() + "/intermediate${i}.ts"
            if(i != size()-1) {
                executable += "|"
            }
        }
        executable += " -c copy -bsf:a aac_adtstoasc " + VidoFile.finalVideoFile.toString()
        ffmpeg.execute(executable.split(" ").toTypedArray(), object: ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                System.out.println("SUCCESFULLY STORED AT " + VidoFile.finalVideoFile.toString())
                after()
            }
            override fun onFailure(message: String?){
                print(message)
            }
        })
    }

    fun compress(after: () -> Unit) {
        var ffmpeg = FFmpeg.getInstance(mContext)
        System.out.println("video size: " + VidoFile.finalVideoFile.length())
        var executable = "-y -i "
        executable += VidoFile.finalVideoFile.toString() + " -vcodec h264 -b:v 1000k -acodec mp2 "
        executable += VidoFile.compressedFinalVideoFile.toString()
        ffmpeg.execute(executable.split(" ").toTypedArray(), object: ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                super.onSuccess(message)
                System.out.println("SUCCESFULLY STORED AT " + VidoFile.compressedFinalVideoFile.toString())
                System.out.println("Compressed size: " + VidoFile.compressedFinalVideoFile.length())
                after()
            }
            override fun onFailure(message: String?){
                super.onSuccess(message)
                print(message)
            }

            override fun onProgress(message: String?) {
                super.onProgress(message)

            }
        })
    }

    internal class UriAdapter : JsonSerializer<Uri>, JsonDeserializer<Uri> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Uri {
            return Uri.parse(json.asString)
        }

        override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }
    }
}