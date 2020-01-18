
package com.vido.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

data class Folder(
    val name: String,
    val parent: String,
    val reference: DocumentReference? = null
) {

    fun delete(): Boolean {
        if (list.count { it.parent == name } != 0 && VideoDetails.all.count{it.folder_name == name} != 0) return false
        val db = FirebaseFirestore.getInstance()
        db.document(reference!!.path).delete()
        list.remove(this)
        all.remove(this.name)
        return true
    }

    companion object {
        var all = arrayListOf<String>()
        var root: TreeNode<Folder> = TreeNode<Folder>(Folder("root", parent = ""))
        var didFetch = false
        var list = arrayListOf<Folder>()

        fun fetch(after: () -> Unit) {
            if (didFetch) {
                after()
            }
            didFetch = true
            var new_root = TreeNode<Folder>(Folder("root", parent = ""))
            var new_all = arrayListOf<String>()
            val db = FirebaseFirestore.getInstance()
            var newList = arrayListOf<Folder>()

            fun addChildrensTo(parent: TreeNode<Folder>) {
                for (el in newList) {
                    if(el.parent == parent.value.name) {
                        val children = TreeNode(el)
                        parent.addChild(children)
                        addChildrensTo(children)
                    }
                }
            }
            new_all.add("Racine")
            db.document(User.company!!.path).collection("folders")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var vd = Folder(document.getString("name")!!,document.getString("parent")!!, document.reference)
                        new_all.add(document.getString("name")!!)
                        newList.add(vd)
                    }
                    addChildrensTo(new_root)
                    root = new_root
                    all = new_all
                    list = newList
                    after()
                }
                .addOnFailureListener { exception ->
                    print("hey")
                }


        }

    }
}