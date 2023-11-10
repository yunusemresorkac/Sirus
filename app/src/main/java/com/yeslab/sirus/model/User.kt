package com.yeslab.sirus.model

data class User(val username : String = "",
                val email : String = "",
                val balance : Int = 0,
                val ticket : Int = 0,
                val deviceId : String = "",
                val userId : String = "",
                val registerDate : Long = 0,
                val correctAnswer : Int = 0,
                val wrongAnswer : Int = 0,
                val accountType : Int = 0)
