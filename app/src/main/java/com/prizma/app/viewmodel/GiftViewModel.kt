package com.prizma.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.prizma.app.model.Gift
import com.prizma.app.repo.GiftRepo

class GiftViewModel (application: Application) : AndroidViewModel(application) {

    private val repo: GiftRepo = GiftRepo()
    private val liveData: MutableLiveData<List<Gift>?> = repo.getAllGifts()


    fun getGifts(){
        repo.getGifts()
    }

    fun clearGifts() {
        repo.clearGifts()
    }

    fun getAllGifts():MutableLiveData<List<Gift>?>{
        return liveData
    }


}