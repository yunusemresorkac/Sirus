package com.prizma.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.prizma.app.repo.TimeRepo

class TimeViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : TimeRepo = TimeRepo()

    fun getNowFromApi() : Long  {
         return repo.getNowFromApi()
    }


}