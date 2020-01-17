
package com.vido

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.vido.model.Folder
import com.vido.model.User
import com.vido.model.VideoDetails
import com.vido.ui.dashboard.MyCamera.MyCameraActivity
import com.vido.ui.video.VideoActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val button = findViewById<Button>(R.id.login_button)
        val userView = findViewById<TextView>(R.id.code_view)
        userView.visibility = View.INVISIBLE
        button.visibility = View.INVISIBLE

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            login()
        } else {
            userView.visibility = View.VISIBLE
            button.visibility = View.VISIBLE
        }

        button.setOnClickListener { view ->
            val user = userView.text.toString()
            val password = "testouille"
            auth.signInWithEmailAndPassword(user + "@ma.com", password).addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    login()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), task.exception!!.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun login() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { it ->
            User.company = it.getDocumentReference("company")
            User.name = it.getString("name")
            db.document(User.company!!.path).get().addOnSuccessListener { it ->
                User.refreshToken = it.getString("refresh_token")
                User.youtubeId = it.getString("youtube_id")
                User.youtubeSecret = it.getString("youtube_secret")
                Folder.fetch {
                    VideoDetails.fetch {
                        runOnUiThread(java.lang.Runnable {
                            val myIntent = Intent(this, MainActivity::class.java)
                            startActivity(myIntent)
                        })
                    }
                }
            }
        }
    }

}
