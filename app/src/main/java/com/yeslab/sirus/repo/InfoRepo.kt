package com.yeslab.sirus.repo

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.yeslab.sirus.util.Constants

class InfoRepo {


    fun getTestStatus(context: Context, callback: (Boolean) -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        remoteConfig.fetch().addOnCompleteListener { fetchTask ->
            if (fetchTask.isSuccessful) {
                remoteConfig.activate().addOnCompleteListener { activationTask ->
                    if (activationTask.isSuccessful) {
                        val withdraw = remoteConfig.getBoolean(Constants.IS_TEST_ACTIVE_KEY)
                        callback(withdraw)
                    } else {
                        callback(false)
                    }
                }
                remoteConfig.reset()
            } else {
                callback(false)

            }
        }
    }

}