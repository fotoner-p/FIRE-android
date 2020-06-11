package moe.fotone.fire.utils

import android.content.ContentValues.TAG
import android.util.Log
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
}