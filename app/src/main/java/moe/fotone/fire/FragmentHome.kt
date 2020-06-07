package moe.fotone.fire

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import moe.fotone.fire.utils.Article as Article

class FragmentHome: Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var articleAdapter: ArticleAdapter
    private var currentUser: FirebaseUser? = null
    private var lists: ArrayList<Article> = ArrayList<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadData()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val listView = view.findViewById<ListView>(R.id.homeListView)
        val writeBtn = view.findViewById<FloatingActionButton>(R.id.newArticleBtn)
        lists.clear()

        articleAdapter = ArticleAdapter(context, lists)
        listView.adapter = articleAdapter

        writeBtn.setOnClickListener {
            startActivity(Intent(context, WriteActivity::class.java))
        }
        return view
    }


    class ArticleAdapter(val context: Context?, val articleList: ArrayList<Article>): BaseAdapter(){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = LayoutInflater.from(context).inflate(R.layout.article_item,null)

            val articleName = view.findViewById<TextView>(R.id.articleNameText)
            val articleDate = view.findViewById<TextView>(R.id.articleDateText)
            val articleMain = view.findViewById<TextView>(R.id.articleMainText)

            val article = articleList[articleList.size - (position + 1)]
            articleName.text = article.name
            articleDate.text= article.date
            articleMain.text = article.main

            return view
        }

        override fun getItem(position: Int): Any {
            return articleList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return articleList.size
        }
    }


    fun loadData(){
        database.getReference("/article/").addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val uid = dataSnapshot.child("uid").value.toString()
                val name = dataSnapshot.child("name").value.toString()
                val date = dataSnapshot.child("date").value.toString()
                val main = dataSnapshot.child("main").value.toString()

                lists.add(Article(uid, name, date, main))

                articleAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }
}