package moe.fotone.fire

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.Exclude
import kotlinx.android.synthetic.main.activity_add.*

class AddActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    @IgnoreExtraProperties
    data class Article(
        var title: String? = "",
        var url: String? = ""
    )
    {

        @Exclude
        fun toMap(): Map<String, Any?> {
            return mapOf("title" to title, "url" to url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        database = FirebaseDatabase.getInstance().reference

        add_saveBtn.setOnClickListener {
            val article = Article(add_title.text.toString(), add_url.text.toString())
            val articleValue = article.toMap()

            val key = database.child("article").push().key
            val childUpdates = HashMap<String, Any>()

            childUpdates["/article/$key"] = articleValue

            database.updateChildren(childUpdates)
        }
    }
}