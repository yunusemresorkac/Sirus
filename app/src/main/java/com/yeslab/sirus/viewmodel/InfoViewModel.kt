package com.yeslab.sirus.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.yeslab.sirus.repo.InfoRepo

class InfoViewModel  (application: Application) : AndroidViewModel(application) {

    private val repo : InfoRepo = InfoRepo()


    fun getTestStatus(context: Context, callback: (Boolean) -> Unit){
        repo.getTestStatus(context, callback)
    }
}