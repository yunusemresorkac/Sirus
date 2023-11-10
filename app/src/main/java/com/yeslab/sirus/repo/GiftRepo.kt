package com.yeslab.sirus.repo

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeslab.sirus.model.Gift
import com.yeslab.sirus.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GiftRepo {

    private var mutableLiveData: MutableLiveData<List<Gift>?> = MutableLiveData()
    private var giftList: ArrayList<Gift>? = null

    fun getGifts(){
        CoroutineScope(Dispatchers.IO).launch {
            giftList = ArrayList()
            try {
                val querySnapshot = FirebaseFirestore.getInstance().collection("Gifts")
                    .orderBy("price", Query.Direction.ASCENDING)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documents = querySnapshot.documents

                    for (document in documents) {
                        val gift = document.toObject(Gift::class.java)
                        if (gift != null && gift.status == Constants.ACTIVE_GIFT_NUMBER) {
                            giftList?.add(gift)
                        }
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda işleme geçin
            }

            withContext(Dispatchers.Main) {
                mutableLiveData.postValue(giftList)
            }
        }

    }

    fun clearGifts() {
        giftList?.clear()
    }

    fun getAllGifts(): MutableLiveData<List<Gift>?> {
        return mutableLiveData
    }


}