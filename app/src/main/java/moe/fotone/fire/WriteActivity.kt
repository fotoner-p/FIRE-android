package moe.fotone.fire

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_write.*
import moe.fotone.fire.utils.ArticleDTO


class WriteActivity : AppCompatActivity() {
    private val database by lazy { FirebaseFirestore.getInstance()}
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)


        sansBtn.setOnClickListener{
            articleUpload()
            finish()
        }
    }

    fun articleUpload(){
        val userRef = database.collection("user").document(auth.currentUser!!.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                val article = ArticleDTO()
                article.uid = auth.currentUser!!.uid
                article.name = document["name"].toString()
                article.main = writeTextMain.text.toString()
                article.timestamp = System.currentTimeMillis()

                database.collection("articles").document().set(article)

                setResult(Activity.RESULT_OK)
                finish()
            }
    }
}