package moe.fotone.fire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.comment_item.view.*
import moe.fotone.fire.utils.ArticleDTO
import java.util.*
import kotlin.collections.ArrayList


class CommentRecyclerViewAdapter(private val activity: FragmentActivity, private val aid: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var articleSnapshot: ListenerRegistration
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val commentDTOs: ArrayList<ArticleDTO.Comment> = ArrayList()

    init {
        getCotents()
    }

    private fun getCotents() {
        val query = database.collection("articles").document(aid).collection("comments").orderBy("timestamp", Query.Direction.DESCENDING)

        articleSnapshot = query.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            commentDTOs.clear()
            notifyDataSetChanged()

            if (querySnapshot == null) return@addSnapshotListener

            else for (document in querySnapshot.documents){
                val item = document.toObject(ArticleDTO.Comment::class.java)!!
                commentDTOs.add(item)
            }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)

        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = (holder as CustomViewHolder).itemView
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = commentDTOs[position].timestamp!!

        val dateText =
            calendar.get(Calendar.YEAR).toString() + '-' +
                    calendar.get(Calendar.MONTH).toString() + '-' +
                    calendar.get(Calendar.DAY_OF_MONTH).toString() + " · " +
                    calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                    calendar.get(Calendar.MINUTE).toString()

        viewHolder.commentNameText.text = commentDTOs[position].name
        viewHolder.commentMainText.text = commentDTOs[position].main
        viewHolder.commentDateText.text = dateText

        database
            .collection("profileImages")
            .document(commentDTOs[position].uid!!)
            .get()
            .addOnCompleteListener {task ->
                if (task.isSuccessful && task.result!!["image"] != null){
                    val url = task.result!!["image"].toString()

                    Glide.with(holder.itemView.context)
                        .load(url)
                        .apply(RequestOptions().circleCrop()).into(viewHolder.commentUserImage)
                }
            }

        viewHolder.commentUserImage.setOnClickListener {
            val fragment = FragmentUser()
            val bundle = Bundle()
            val transaction = activity.supportFragmentManager.beginTransaction()

            bundle.putString("destinationUid", commentDTOs[position].uid)
            fragment.arguments = bundle
            transaction.replace(R.id.content, fragment).addToBackStack(null).replace(R.id.content, fragment).commit()
        }
    }

    override fun getItemCount(): Int = commentDTOs.size

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}