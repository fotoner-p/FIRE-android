package moe.fotone.fire.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Article(var aid:String? = null, var uid:String? = null, var name:String? = null, var timestamp: Timestamp? = null, var main:String? = null){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "timestamp" to timestamp,
            "main" to main
        )
    }
}