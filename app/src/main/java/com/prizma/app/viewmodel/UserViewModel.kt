package com.prizma.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.prizma.app.repo.UserRepo

class UserViewModel  (application: Application) : AndroidViewModel(application) {

    private val repo : UserRepo = UserRepo()

    fun getAccountType(context: Context, userId: String, callback: (Int) -> Unit){

        repo.getAccountType(context, userId, callback)
    }

}
