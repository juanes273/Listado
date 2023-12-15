package com.example.listado.model

data class User(
    val id: String?,
    val userId: String,
    val displayName: String,
    val prioridad: String
){
    fun toMap(): MutableMap<String,Any>{
        return mutableMapOf(
            "user_id" to this.userId,
            "display_name" to this.displayName,
            "prioridad" to this.prioridad
        )
    }
}
