package moe.fotone.fire.utils

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.Exclude

class FirebaseHelper {
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun createUserInfo(uid: String, name: String, email: String){
        @IgnoreExtraProperties
        data class User(
            var name: String? = "",
            var email: String? =""
        )
        {
            @Exclude
            fun toMap(): Map<String, Any?> {
                return mapOf("name" to name, "email" to email)
            }
        }
        val user = User(name, email)
        val userValue = user.toMap()
        val childUpdates = HashMap<String, Any>()

        childUpdates["/users/$uid"] = userValue

        database.updateChildren(childUpdates)
    }

    fun createArticle(uid:String, name:String, date:String, main:String ){
        @IgnoreExtraProperties
        data class Article(
            var uid: String? = "",
            var name: String? = "",
            var date: String? = "",
            var main: String? = ""
        )
        {
            @Exclude
            fun toMap(): Map<String, Any?> {
                return mapOf(
                    "uid" to uid,
                    "name" to name,
                    "date" to date,
                    "main" to main
                )
            }
        }

        val article = Article(uid, name, date, main)
        val articleValue = article.toMap()

        val key = database.child("article").push().key
        val childUpdates = HashMap<String, Any>()

        childUpdates["/article/$key"] = articleValue

        database.updateChildren(childUpdates)
    }
}