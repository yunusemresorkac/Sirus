package com.prizma.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.prizma.app.repo.InfoRepo

class InfoViewModel  (application: Application) : AndroidViewModel(application) {

    private val repo : InfoRepo = InfoRepo()


    fun getTestStatus(context: Context, callback: (Boolean) -> Unit){
        repo.getTestStatus(context, callback)
    }
}