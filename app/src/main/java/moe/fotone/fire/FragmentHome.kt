package moe.fotone.fire

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_home.*
import moe.fotone.fire.utils.Article

class FragmentHome: Fragment() {
    private val database by lazy { FirebaseFirestore.getInstance()}
    private lateinit var articleAdapter: FirestoreRecyclerAdapter<Article, ArticleHolder>
    private lateinit var firestoreListener: ListenerRegistration
    private lateinit var lists: MutableList<Article>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lm = LinearLayoutManager(context)

        homeListView.layoutManager = lm
        homeListView.setHasFixedSize(true)
        homeListView.addItemDecoration(DividerItemDecoration(homeListView.context, DividerItemDecoration.VERTICAL))

        loadData()

        firestoreListener = database.collection("article")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener {documentSnapshots, e ->
                if (e != null) return@addSnapshotListener

                if (documentSnapshots != null) {
                    lists = mutableListOf()

                    for (document in documentSnapshots){
                        val aid = document.id
                        val uid = document.data["uid"].toString()
                        val name = document.data["name"].toString()
                        val timestamp = document.data["timestamp"] as? Timestamp
                        val main = document.data["main"].toString()

                        val article = Article(aid, uid, name, timestamp, main)

                        lists.add(article)
                    }
                }
                articleAdapter.notifyDataSetChanged()
                homeListView.adapter = articleAdapter
            }

        newArticleBtn.setOnClickListener {
            startActivity(Intent(context, WriteActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        articleAdapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()

        firestoreListener.remove()
    }

    override fun onStop() {
        super.onStop()

        articleAdapter.stopListening()
    }

    private fun loadData(){
        val query = database.collection("article").orderBy("timestamp", Query.Direction.DESCENDING)

        val response = FirestoreRecyclerOptions
            .Builder<Article>()
            .setQuery(query, Article::class.java)
            .build()

        articleAdapter = object: FirestoreRecyclerAdapter<Article, ArticleHolder>(response){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleHolder {
                val view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false)

                return ArticleHolder(view)
            }

            override fun onBindViewHolder(holder: ArticleHolder, position: Int, article: Article) {
                holder.bind(lists[position])

                holder.layout.setOnClickListener {
                    val intent = Intent(context, DetailActivity::class.java)
                    val aid = lists[position].aid

                    intent.putExtra("aid", aid)
                    context?.startActivity(intent)
                }
            }
        }

        articleAdapter.notifyDataSetChanged()
        homeListView.adapter = articleAdapter
    }
}