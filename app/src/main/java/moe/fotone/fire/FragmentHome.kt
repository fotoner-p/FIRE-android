package moe.fotone.fire

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import moe.fotone.fire.utils.Article
import moe.fotone.fire.utils.FirebaseHelper


class FragmentHome: Fragment() {
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lists: ArrayList<Article> = ArrayList()

        articleAdapter = ArticleAdapter(context, lists)
        FirebaseHelper().loadArticle(lists, articleAdapter)
        homeListView.adapter = articleAdapter

        newArticleBtn.setOnClickListener {
            startActivity(Intent(context, WriteActivity::class.java))
        }
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
}