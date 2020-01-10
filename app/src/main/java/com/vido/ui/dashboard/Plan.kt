package com.vido.ui.dashboard

import android.net.Uri
import com.google.gson.annotations.SerializedName


data class Plan(
    @SerializedName("video_path")
    val video_path: String,
    @SerializedName("duration")
    val duration: Int
)