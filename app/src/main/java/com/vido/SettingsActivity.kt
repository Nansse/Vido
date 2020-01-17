package com.vido

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vido.model.Folder
import com.vido.model.User

import kotlinx.android.synthetic.main.activity_settings.*
import java.sql.Timestamp

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        val nameView = findViewById<TextView>(R.id.new_folder_name_text)
        val spinnerView = findViewById<Spinner>(R.id.new_folder_parent_picker)
        val adp = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Folder.all)
        spinnerView.adapter = adp
        val create_folder_button = findViewById<Button>(R.id.create_folder_button)
        create_folder_button.setOnClickListener { view ->
            var parent: String = spinnerView.selectedItem.toString()
            if (parent == "Racine") parent = "root"
            val db = FirebaseFirestore.getInstance()
            val folder = hashMapOf(
                "parent" to parent,
                "name" to nameView.text.toString()
            )
            if (Folder.all.count { e -> e == nameView.text.toString()} == 0) {
                db.document(User.company!!.path).collection("folders").add(folder).addOnSuccessListener { _ ->
                    Folder.fetch { finish() }
                }
            }
        }

        val spinnerDeleteFolder = findViewById<Spinner>(R.id.delete_folder_picker)
        val adp2 = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Folder.all.filter{e -> e != "Racine"})
        spinnerDeleteFolder.adapter = adp2

        val deleteFolderButton = findViewById<Button>(R.id.delete_folder_button)
        deleteFolderButton.setOnClickListener { view ->
            // TODO
        }

        val deconnexion_button = findViewById<Button>(R.id.deconnexion_button)
        deconnexion_button.setOnClickListener { view ->
            FirebaseAuth.getInstance().signOut()
            val myIntent = Intent(this, LoginActivity::class.java)
            startActivity(myIntent)
        }

    }

}
