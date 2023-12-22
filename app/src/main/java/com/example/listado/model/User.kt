package com.example.listado.model

import android.provider.ContactsContract.CommonDataKinds.Email

data class User(
    val id: String?,
    val userId: String,
    val displayName: String,
    val residencia: String,
    val email: String
){
    fun toMap(): MutableMap<String,Any>{
        return mutableMapOf(
            "user_id" to this.userId,
            "display_name" to this.displayName,
            "residencia" to this.residencia,
            "email" to this.email
        )
    }
}
