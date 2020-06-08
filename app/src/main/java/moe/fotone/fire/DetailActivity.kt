package moe.fotone.fire

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_article_detail.*


class DetailActivity : AppCompatActivity() {
    val database: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)
        val intent = intent
        val aid=  intent.getStringExtra("aid")!!

        database.collection("article")
            .document(aid)
            .get()
            .addOnSuccessListener { document ->
                detailArticleNameText.text = document["name"].toString()
                detailArticleDateText.text = (document["timestamp"] as Timestamp).toDate().toString()
                detailArticleMainText.text =  document["main"].toString()
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error load Article", e) }
    }
}