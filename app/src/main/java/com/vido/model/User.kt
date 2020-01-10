package com.vido.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class User() {
    companion object {
        var company: DocumentReference? = null
        var name: String? = null
        var youtubeId: String? = null
        var youtubeSecret: String? = null
        var refreshToken: String? = null
    }
}