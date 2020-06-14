package moe.fotone.fire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_write.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import moe.fotone.fire.utils.ArticleDTO
import moe.fotone.fire.utils.FcmPush
import moe.fotone.fire.utils.NotificationDTO
import java.util.*

class FragmentDetail: Fragment() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private val database: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val fcmpush by lazy { FcmPush() }

    private lateinit var articleSnapshot: ListenerRegistration
    private lateinit var imageProfileListenerRegistration: ListenerRegistration
    private lateinit var articleUid:String
    private lateinit var writerUid:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        articleUid = arguments?.getString("articleUid").toString()
        writerUid = arguments?.getString("writerUid").toString()

        view.detailUserImage.setOnClickListener {
            val fragment = FragmentUser()
            val bundle = Bundle()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            bundle.putString("destinationUid", writerUid)
            fragment.arguments = bundle
            transaction.replace(R.id.content, fragment).addToBackStack(null).replace(R.id.content, fragment).commit()
        }

        view.commentSendBtn.setOnClickListener {
            sendComment()
        }

        database.collection("user")
            .document(writerUid)
            .get()
            .addOnSuccessListener  { document ->
                view.detailEmailText.text = document["email"].toString()
            }

        database.collection("articles")
            .document(articleUid)
            .addSnapshotListener { document, e ->
                val article = document!!.toObject(ArticleDTO::class.java)!!
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = article.timestamp!!

                val dateText =
                    calendar.get(Calendar.YEAR).toString() + '-' +
                            calendar.get(Calendar.MONTH).toString() + '-' +
                            calendar.get(Calendar.DAY_OF_MONTH).toString() + " · " +
                            calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                            calendar.get(Calendar.MINUTE).toString()

                view.detailArticleNameText.text = article.name.toString()
                view.detailArticleDateText.text = dateText
                view.detailArticleMainText.text = article.main.toString()
                view.detailFavoriteText.text = article.favoriteCount.toString()
                view.detailCommentText.text = article.commentCount.toString()

                if (article.imageUrl != null){
                    Glide.with(requireActivity())
                        .load(article.imageUrl)
                        .into(view.detailArticleImage)

                    view.detailArticleImage.visibility = View.VISIBLE
                }

                if (article.favorites.containsKey(auth.currentUser!!.uid)){
                    view.detailFavoriteImage.setImageResource(R.drawable.ic_baseline_favorite_24)
                }
                else {
                    view.detailFavoriteImage.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }

                view.detailFavoriteImage.setOnClickListener{
                    favoriteEvent(articleUid)
                }
            }
        return view
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity()
        detailComentListView.layoutManager = LinearLayoutManager(context)
        detailComentListView.adapter = CommentRecyclerViewAdapter(activity, articleUid)

        activity.mainProgressBar.visibility = View.INVISIBLE
        activity.toolbarTitleText.text = "Article"
        activity.toolbarTitleText.visibility = View.VISIBLE
        activity.toolbarBackImage.setOnClickListener {
            activity.supportFragmentManager.popBackStackImmediate()
        }
        activity.toolbarBackImage.visibility = View.VISIBLE

        articleSnapshot = (detailComentListView.adapter as CommentRecyclerViewAdapter).articleSnapshot
        getProfileImage()
    }

    private fun getProfileImage() {
        imageProfileListenerRegistration = database.collection("profileImages").document(writerUid)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot?.data != null) {
                    val url = documentSnapshot.data!!["image"]
                    Glide.with(requireActivity())
                        .load(url)
                        .apply(RequestOptions().circleCrop()).into(detailUserImage)
                }
            }
    }

    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
        imageProfileListenerRegistration.remove()
    }

    private fun sendComment(){
        if (commentText.text.toString().length == 0){
            Toast.makeText(requireActivity(),"코멘트를 작성해주세요" , Toast.LENGTH_SHORT).show()
            return
        }else if(commentText.text.toString().length > 150) {
            Toast.makeText(requireActivity(),"150자 이내로 작성해주세요" , Toast.LENGTH_SHORT).show()
            return
        }

        val docRef = database.collection("articles").document(articleUid)
        val userRef = database.collection("user").document(auth.currentUser!!.uid)
        database.runTransaction{transaction ->
            val comment = ArticleDTO.Comment()
            val articleDTO = transaction.get(docRef).toObject(ArticleDTO::class.java)
            val name = transaction.get(userRef)["name"].toString()

            comment.aid = articleUid
            comment.uid = auth.currentUser!!.uid
            comment.name = name
            comment.main = commentText.text.toString()
            comment.timestamp = System.currentTimeMillis()

            articleDTO?.commentCount = articleDTO?.commentCount?.plus(1)!!

            transaction.set(docRef, articleDTO)
            transaction.set(docRef.collection("comments").document(), comment)

            commentNotification(name, commentText.text.toString())
            commentText.setText("")
        }
    }

    private fun favoriteEvent(articleUid: String){
        val articleRef = database.collection("articles").document(articleUid)
        val userRef = database.collection("user").document(auth.currentUser!!.uid)

        database.runTransaction { transaction ->
            val uid = auth.currentUser!!.uid
            val articleDTO = transaction.get(articleRef).toObject(ArticleDTO::class.java)
            val userName = transaction.get(userRef)["name"].toString()

            if(articleDTO!!.favorites.containsKey(uid)){
                articleDTO.favoriteCount -= 1
                articleDTO.favorites.remove(uid)
            } else {
                articleDTO.favoriteCount += 1
                articleDTO.favorites[uid] = true
                favoriteNotification(userName)
            }
            transaction.set(articleRef, articleDTO)
        }
    }

    private fun favoriteNotification(name:String){
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
    private fun commentNotification(name:String, message:String){
        if(writerUid == auth.currentUser!!.uid)
            return

        val notificationDTO = NotificationDTO()
        notificationDTO.targetUID = writerUid
        notificationDTO.startName = name
        notificationDTO.startUID = auth.currentUser!!.uid
        notificationDTO.kind = 1
        notificationDTO.message = message
        notificationDTO.timestamp = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("notification").document().set(notificationDTO)

        val send_message = name + "님이 \"" + message + "\" 메세지를 남겼습니다"
        fcmpush.sendMessage(writerUid, "알림 메세지 입니다.", send_message)
    }
}