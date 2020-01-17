package com.vido.model

import com.google.firebase.firestore.FirebaseFirestore

data class Folder(
    val name: String,
    val parent: String
) {

    companion object {
        var all = arrayListOf<String>()
        var root: TreeNode<Folder> = TreeNode<Folder>(Folder("root", parent = ""))
        var didFetch = false
        fun fetch(after: () -> Unit) {
            if (didFetch) {
                after()
            }
            didFetch = true
            var new_root = TreeNode<Folder>(Folder("root", parent = ""))
            var new_all = arrayListOf<String>()
            val db = FirebaseFirestore.getInstance()
            var list = arrayListOf<Folder>()

            fun addChildrensTo(parent: TreeNode<Folder>) {
                for (el in list) {
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
                        var vd = Folder(document.getString("name")!!,document.getString("parent")!!)
                        new_all.add(document.getString("name")!!)
                        list.add(vd)
                    }
                    addChildrensTo(new_root)
                    root = new_root
                    all = new_all
                    after()
                }
                .addOnFailureListener { exception ->
                    print("hey")
                }


        }

    }
}