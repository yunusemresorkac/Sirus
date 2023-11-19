package com.prizma.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.prizma.app.model.Question
import com.prizma.app.repo.TestRepo

class TestViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : TestRepo = TestRepo()

    fun addAnsweredQuestions(questionId : String,userId : String){
        repo.addAnsweredQuestions(questionId, userId)
    }

    fun lostQuizRight(userId: String, context : Context){
        repo.lostQuizRight(userId, context)
    }

    fun reportQuestion(question: Question, context: Context, userId: String){
        repo.reportQuestion(question, context, userId)
    }

    fun addBalanceForWinners(userId: String, context: Context){
        repo.addBalanceForWinners(userId, context)
    }

    fun addCorrectAnswer(userId: String, context: Context){
        repo.addCorrectAnswer(userId, context)
    }

    fun addWrongAnswer(userId: String, context: Context){
        repo.addWrongAnswer(userId, context)
    }


}