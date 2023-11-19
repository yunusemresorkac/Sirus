package com.prizma.app.repo

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs
import com.prizma.app.controller.DummyMethods
import com.prizma.app.model.Question
import com.prizma.app.model.User
import com.prizma.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToastStyle

class TestRepo {

    private val firestore = FirebaseFirestore.getInstance()

    fun addBalanceForWinners(userId: String, context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = firestore.collection("Users").document(userId)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        val map : HashMap<String,Any> = HashMap()
                        map["balance"] = user!!.balance + Constants.BIG_PRIZE
                        firestore.collection("Users").document(userId)
                            .update(map).addOnSuccessListener {
                                createNewUserObject(userId,context)
                            }

                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }

    fun addAnsweredQuestions(questionId : String,userId : String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["answeredQuestionId"] = questionId
            map["userId"] = userId
            firestore.collection("MyQuestions").document(userId)
                .collection("MyQuestions").document(questionId)
                .set(map)
        }

    }


    fun lostQuizRight(userId: String, context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = firestore.collection("Users").document(userId)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!!.ticket > 0 ){
                            val map : HashMap<String,Any> = HashMap()
                            map["ticket"] = user.ticket - 1
                            firestore.collection("Users").document(userId)
                                .update(map).addOnSuccessListener {
                                    createNewUserObject(userId,context)
                                }
                        }

                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }


    fun reportQuestion(question: Question,context: Context, userId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val id = System.currentTimeMillis().toString()
            val map : HashMap<String,Any> = HashMap()
            val questionId = question.questionId
            map["questionId"] = questionId
            map["question"] = question.question
            map["id"] = id
            map["userId"] = userId
            firestore.collection("Reports").document(questionId)
                .collection("Reports").document(id)
                .set(map).addOnSuccessListener {
                    DummyMethods.showMotionToast(context,"Geri Bildiriminiz İçin Teşekkürler","Soruyu Kısa Sürede Tekrardan İnceleyeceğiz",MotionToastStyle.INFO)

                }
        }

    }

    fun addCorrectAnswer(userId: String, context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = firestore.collection("Users").document(userId)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!!.ticket > 0 ){
                            val map : HashMap<String,Any> = HashMap()
                            map["correctAnswer"] = user.correctAnswer + 1
                            firestore.collection("Users").document(userId)
                                .update(map).addOnSuccessListener {
                                }
                        }

                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }

    fun addWrongAnswer(userId: String, context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = firestore.collection("Users").document(userId)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!!.ticket > 0 ){
                            val map : HashMap<String,Any> = HashMap()
                            map["wrongAnswer"] = user.wrongAnswer + 1
                            firestore.collection("Users").document(userId)
                                .update(map).addOnSuccessListener {
                                }
                        }

                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }



    private fun createNewUserObject(userId: String,context: Context)= CoroutineScope(Dispatchers.IO).launch {
        firestore.collection("Users").document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        val prefs = FastPrefs(context)
                        prefs.set("user", user)
                    }
                }
            }

    }



}