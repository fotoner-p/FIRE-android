package moe.fotone.fire

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_article_detail.*
import moe.fotone.fire.utils.ArticleDTO
import java.util.*


class DetailActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private val database: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)
        val intent = intent
        val articleUid=  intent.getStringExtra("articleUid")!!
        val writerUid=  intent.getStringExtra("writerUid")!!

        database.collection("user")
            .document(writerUid)
            .get()
            .addOnSuccessListener  { document ->
                detailEmailText.text = document["email"].toString()
            }

        database.collection("articles")
            .document(articleUid)
            .addSnapshotListener { document, e ->
                val article = document!!.toObject(ArticleDTO::class.java)!!
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = article.timestamp!!

                val dateText =
                    calendar.get(Calendar.YEAR).toString() + '-' +
                    calendar.get(Calendar.MONTH).toString() + '-' +
                    calendar.get(Calendar.DAY_OF_MONTH).toString() + " · " +
                    calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                    calendar.get(Calendar.MINUTE).toString()

                detailArticleNameText.text = article.name.toString()
                detailArticleDateText.text = dateText
                detailArticleMainText.text = article.main.toString()
                detailFavoriteText.text = article.favoriteCount.toString()
                detailCommentText.text = article.commentCount.toString()

                if (article.favorites.containsKey(auth.currentUser!!.uid)){
                    detailFavoriteImage.setImageResource(R.drawable.ic_baseline_favorite_24)
                }
                else {
                    detailFavoriteImage.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }

                detailFavoriteImage.setOnClickListener{
                    favoriteEvent(articleUid)
                }
            }
    }

    private fun favoriteEvent(articleUid: String){
        val tsDoc = database.collection("articles").document(articleUid)

        database.runTransaction { transaction ->
            val uid = auth.currentUser!!.uid
            val articleDTO = transaction.get(tsDoc).toObject(ArticleDTO::class.java)

            if(articleDTO!!.favorites.containsKey(uid)){
                articleDTO.favoriteCount = articleDTO.favoriteCount - 1
                articleDTO.favorites.remove(uid)
            } else {
                articleDTO.favoriteCount = articleDTO.favoriteCount + 1
                articleDTO.favorites[uid] = true
            }
            transaction.set(tsDoc, articleDTO)
        }
    }
}