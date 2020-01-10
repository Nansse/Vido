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
                return
            }
            didFetch = true

            root = TreeNode<Folder>(Folder("root", parent = ""))
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
            all.add("Racine")
            db.document(User.company!!.path).collection("folders")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var vd = Folder(document.getString("name")!!,document.getString("parent")!!)
                        all.add(document.getString("name")!!)
                        list.add(vd)
                    }
                    addChildrensTo(root)
                    print(root)
                    after()
                }
                .addOnFailureListener { exception ->
                    print("hey")
                }


        }

    }
}