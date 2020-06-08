package moe.fotone.fire.utils

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import moe.fotone.fire.FragmentHome

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
                    "timestamp" to FieldValue.serverTimestamp(),
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

    fun loadArticle(list: ArrayList<Article>, articleAdapter: FragmentHome.ArticleAdapter) {
        database.collection("article")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener {documents ->
                for (document in documents){
                    val uid = document.data["uid"].toString()
                    val name = document.data["name"].toString()
                    val date = (document.data["timestamp"] as Timestamp).toDate().toString()
                    val main = document.data["main"].toString()

                    list.add(Article(uid, name, date, main))
                    articleAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener{
                    e -> Log.w(TAG, "Error load Article", e)
            }
    }
}