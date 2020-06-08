package moe.fotone.fire.utils

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelper {
    private val database by lazy {FirebaseFirestore.getInstance()}

    fun createUserInfo(uid: String, name: String, email: String){
        val user = hashMapOf(
            "name" to name,
            "email" to email
        )

        database.collection("user")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener {
                e -> Log.w(TAG, "Error writing document", e)
            }
    }

    fun createArticle(uid:String, main:String ){
        val userRef = database.collection("user").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                val article = hashMapOf(
                    "uid" to uid,
                    "name" to document["name"].toString(),
                    "timestamp" to  FieldValue.serverTimestamp(),
                    "main" to main
                )
                database.collection("article")
                    .add(article)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                    }
                    .addOnFailureListener{
                            e -> Log.w(TAG, "Error writing document", e)
                    }
            }
            .addOnFailureListener {
                    e -> Log.w(TAG, "Error read user info  document", e)
            }
    }
}