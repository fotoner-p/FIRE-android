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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.article_item.view.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import moe.fotone.fire.R
import moe.fotone.fire.utils.ArticleDTO
import moe.fotone.fire.utils.FcmPush
import moe.fotone.fire.utils.FollowDTO
import moe.fotone.fire.utils.NotificationDTO
import java.util.*
import kotlin.collections.ArrayList

class FragmentDashboard: Fragment() {
    private lateinit var articleSnapshot: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        return view
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity()

        dashboardRecyclerView.layoutManager = LinearLayoutManager(context)
        dashboardRecyclerView.adapter = DashboardRecyclerViewAdapter()

        activity.mainProgressBar.visibility = View.INVISIBLE
        activity.toolbarTitleText.text = "Dashboard"
        activity.toolbarTitleText.visibility = View.VISIBLE
        activity.toolbarBackImage.visibility = View.INVISIBLE
    }
    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
    }

    inner class DashboardRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val database by lazy { FirebaseFirestore.getInstance()}
        private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
        private val fcmpush by lazy { FcmPush() }

        val articleDTOs: ArrayList<ArticleDTO>
        val articleUidList: ArrayList<String>

        init {

            articleDTOs = ArrayList()
            articleUidList = ArrayList()
            val uid = auth.currentUser!!.uid
            val query = database.collection("followInfo").document(uid)
            query
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful ) {
                        val userDTO: FollowDTO? = task.result?.toObject(FollowDTO::class.java)

                        if (userDTO?.followings != null) {
                            getCotents(userDTO.followings)
                        }
                    }
                }
        }

        private fun getCotents(followers: MutableMap<String, Boolean>?) {
            val query = database.collection("articles").orderBy("timestamp", Query.Direction.DESCENDING)

            articleSnapshot = query.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                articleDTOs.clear()
                articleUidList.clear()

                if (querySnapshot == null) return@addSnapshotListener

                else for (document in querySnapshot.documents){
                    val item = document.toObject(ArticleDTO::class.java)!!

                    if (followers?.keys?.contains(item.uid)!!) {
                        articleDTOs.add(item)
                        articleUidList.add(document.id)
                    }
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

            if(articleDTOs[position].imageUrl != null){
                Glide.with(holder.itemView.context)
                    .load(articleDTOs[position].imageUrl)
                    .into(viewHolder.articleMainIamge)

                viewHolder.articleMainIamge.visibility = View.VISIBLE
            }

            database
                .collection("profileImages")
                .document(articleDTOs[position].uid!!)
                .get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful && task.result!!["image"] != null){
                        val url = task.result!!["image"].toString()

                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(viewHolder.articleUserImage)
                    }
                }

            if (articleDTOs[position].favorites.containsKey(auth.currentUser!!.uid)){
                viewHolder.favoritImage.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
            else {
                viewHolder.favoritImage.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }

            viewHolder.articleUserImage.setOnClickListener {
                val fragment = FragmentUser()
                val bundle = Bundle()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()

                bundle.putString("destinationUid", articleDTOs[position].uid)
                fragment.arguments = bundle
                transaction.replace(R.id.content, fragment).addToBackStack(null).replace(
                    R.id.content, fragment).commit()
            }


            viewHolder.favoritImage.setOnClickListener{
                favoriteEvent(position)
            }

            viewHolder.setOnClickListener {
                val fragment = FragmentDetail()
                val bundle = Bundle()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()

                bundle.putString("writerUid", articleDTOs[position].uid)
                bundle.putString("articleUid", articleUidList[position])

                fragment.arguments = bundle
                transaction.replace(R.id.content, fragment).addToBackStack(null).replace(
                    R.id.content, fragment).commit()
            }
        }
        private fun favoriteEvent(position: Int){
            val docRef = database.collection("articles").document(articleUidList[position])
            val userRef = database.collection("user").document(auth.currentUser!!.uid)

            database.runTransaction { transaction ->
                val uid = auth.currentUser!!.uid
                val articleDTO = transaction.get(docRef).toObject(ArticleDTO::class.java)
                val userName = transaction.get(userRef)["name"].toString()
                if(articleDTO!!.favorites.containsKey(uid)){
                    articleDTO.favoriteCount -= 1
                    articleDTO.favorites.remove(uid)
                } else {
                    articleDTO.favoriteCount += 1
                    articleDTO.favorites[uid] = true
                    favoriteNotification(userName, articleDTOs[position].uid!!)
                }
                transaction.set(docRef, articleDTO)
            }
        }

        private fun favoriteNotification(name:String, writerUid:String){
            if(writerUid == auth.currentUser!!.uid)
                return

            val notificationDTO = NotificationDTO()
            notificationDTO.targetUID = writerUid
            notificationDTO.startName = name
            notificationDTO.startUID = auth.currentUser!!.uid
            notificationDTO.kind = 0
            notificationDTO.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("notification").document().set(notificationDTO)

            val message = name + "님이 좋아요를 눌렀습니다"
            fcmpush.sendMessage(writerUid, "알림 메세지 입니다.", message)
        }

        override fun getItemCount(): Int = articleDTOs.size

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}