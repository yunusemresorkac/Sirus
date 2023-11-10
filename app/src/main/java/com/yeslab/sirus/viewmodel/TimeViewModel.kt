package com.yeslab.sirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yeslab.sirus.repo.TimeRepo

class TimeViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : TimeRepo = TimeRepo()

    fun getNowFromApi() : Long  {
         return repo.getNowFromApi()
    }


}