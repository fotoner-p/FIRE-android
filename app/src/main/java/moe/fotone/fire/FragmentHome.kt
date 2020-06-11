package moe.fotone.fire

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.article_item.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import moe.fotone.fire.utils.ArticleDTO
import java.util.*
import kotlin.collections.ArrayList

class FragmentHome: Fragment() {
    private lateinit var articleSnapshot: ListenerRegistration
    private val database by lazy { FirebaseFirestore.getInstance()}
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.newArticleBtn.setOnClickListener {
            startActivity(Intent(context, WriteActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        homeListView.layoutManager = LinearLayoutManager(context)
        homeListView.adapter = ArticleRecyclerViewAdapter()
    }

    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
    }

    inner class ArticleRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val articleDTOs: ArrayList<ArticleDTO>
        val articleUidList: ArrayList<String>

        init {
            articleDTOs = ArrayList()
            articleUidList = ArrayList()
            getCotents()
        }

        private fun getCotents() {
            articleSnapshot = database.collection("articles").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
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

                startActivity(intent)
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
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}