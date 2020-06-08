package moe.fotone.fire

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import moe.fotone.fire.utils.Article

class ArticleHolder(v: View): RecyclerView.ViewHolder(v) {
    val articleName = v.findViewById<TextView>(R.id.articleNameText)
    val articleDate = v.findViewById<TextView>(R.id.articleDateText)
    val articleMain = v.findViewById<TextView>(R.id.articleMainText)
    val layout = v.findViewById<ConstraintLayout>(R.id.itemLayout)

    fun bind(article: Article){
        articleName.text = article.name
        articleDate.text = article.timestamp?.toDate().toString()
        articleMain.text = article.main
    }
}