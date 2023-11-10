package com.yeslab.sirus.repo

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.sirus.model.User

class UserRepo {

    private val db = FirebaseFirestore.getInstance()

    fun getAccountType(context: Context, userId: String, callback: (Int) -> Unit){

        val docRef = db.collection("Users").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    callback(user!!.accountType)


                } else {
                    callback(0)
                }
            }
            .addOnFailureListener { exception ->
                callback(0)
            }


    }

}