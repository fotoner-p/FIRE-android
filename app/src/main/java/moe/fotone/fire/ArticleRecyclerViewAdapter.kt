package moe.fotone.fire

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.article_item.view.*
import moe.fotone.fire.utils.ArticleDTO
import java.util.*
import kotlin.collections.ArrayList


class ArticleRecyclerViewAdapter(context: Context?, type: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var articleSnapshot: ListenerRegistration
    private val database by lazy { FirebaseFirestore.getInstance()}
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private val context = context

    val articleDTOs: ArrayList<ArticleDTO>
    val articleUidList: ArrayList<String>

    init {
        articleDTOs = ArrayList()
        articleUidList = ArrayList()
        getCotents(type)
    }

    private fun getCotents(type: String) {
        lateinit var query: Query
        if (type == "main")
            query = database.collection("articles").orderBy("timestamp", Query.Direction.DESCENDING)
        else
            query = database.collection("articles").whereEqualTo("uid", type).orderBy("timestamp", Query.Direction.DESCENDING)

        articleSnapshot = query.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            articleDTOs.clear()
            articleUidList.clear()

            if (querySnapshot == null) return@addSnapshotListener

            else for (document in querySnapshot.documents){
                val item = document.toObject(ArticleDTO::class.java)!!

                articleDTOs.add(item)
                articleUidList.add(document.id)
            }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)

        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = (holder as CustomViewHolder).itemView
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = articleDTOs[position].timestamp!!

        val dateText =
            calendar.get(Calendar.YEAR).toString() + '-' +
                    calendar.get(Calendar.MONTH).toString() + '-' +
                    calendar.get(Calendar.DAY_OF_MONTH).toString() + " · " +
                    calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                    calendar.get(Calendar.MINUTE).toString()

        viewHolder.articleNameText.text = articleDTOs[position].name
        viewHolder.articleMainText.text = articleDTOs[position].main
        viewHolder.articleDateText.text = dateText
        viewHolder.favoritText.text = articleDTOs[position].favoriteCount.toString()
        viewHolder.comentText.text = articleDTOs[position].commentCount.toString()

        if (articleDTOs[position].favorites.containsKey(auth.currentUser!!.uid)){
            viewHolder.favoritImage.setImageResource(R.drawable.ic_baseline_favorite_24)
        }
        else {
            viewHolder.favoritImage.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }

        viewHolder.favoritImage.setOnClickListener{
            favoriteEvent(position)
        }

        viewHolder.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)

            intent.putExtra("articleUid", articleUidList[position])
            intent.putExtra("writerUid", articleDTOs[position].uid)

            context?.startActivity(intent)
        }
    }
    private fun favoriteEvent(position: Int){
        val tsDoc = database.collection("articles").document(articleUidList[position])

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
    override fun getItemCount(): Int = articleDTOs.size

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}