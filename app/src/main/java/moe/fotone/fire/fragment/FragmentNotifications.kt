package moe.fotone.fire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.comment_item.view.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*
import moe.fotone.fire.R
import moe.fotone.fire.utils.NotificationDTO
import java.util.*
import kotlin.collections.ArrayList


class FragmentNotifications: Fragment() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        view.notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        view.notificationRecyclerView.adapter = NotificationRecyclerViewAdapter()

        return view
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity()

        activity.mainProgressBar.visibility = View.INVISIBLE
        activity.toolbarTitleText.text = "Notification"
        activity.toolbarTitleText.visibility = View.VISIBLE
        activity.toolbarBackImage.visibility = View.INVISIBLE
    }

    inner class NotificationRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val notificationDTOList = ArrayList<NotificationDTO>()

        init {
            database
                .collection("notification")
                .whereEqualTo("targetUID", auth.currentUser!!.uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    notificationDTOList.clear()
                    if(querySnapshot == null)return@addSnapshotListener
                    for (snapshot in querySnapshot?.documents!!) {
                        notificationDTOList.add(snapshot.toObject(NotificationDTO::class.java)!!)
                    }
                    notificationDTOList.sortByDescending { it.timestamp }
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

            calendar.timeInMillis = notificationDTOList[position].timestamp!!

            val dateText =
                calendar.get(Calendar.YEAR).toString() + '-' +
                        calendar.get(Calendar.MONTH).toString() + '-' +
                        calendar.get(Calendar.DAY_OF_MONTH).toString() + " · " +
                        calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                        calendar.get(Calendar.MINUTE).toString()

            viewHolder.commentNameText.text = notificationDTOList[position].startName
            viewHolder.commentMainText.text = notificationDTOList[position].message
            viewHolder.commentDateText.text = dateText

            database
                .collection("profileImages")
                .document(notificationDTOList[position].startUID!!)
                .get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful && task.result!!["image"] != null){
                        val url = task.result!!["image"].toString()

                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(viewHolder.commentUserImage)
                    }
                }

            when (notificationDTOList[position].kind) {
                0 -> {
                    val str_0 = notificationDTOList[position].startName + "님이 좋아요를 눌렀습니다."
                    viewHolder.commentMainText.text = str_0
                }

                1 -> {
                    val str_1 = notificationDTOList[position].startName + "님이 \"" + notificationDTOList[position].message + "\" 메시지를 남겼습니다."
                    viewHolder.commentMainText.text = str_1
                }

                2 -> {
                    val str_2 = notificationDTOList[position].startName + "님이 팔로우 했습니다."
                    viewHolder.commentMainText.text = str_2
                }
            }
        }

        override fun getItemCount(): Int {

            return notificationDTOList.size
        }
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    }
}