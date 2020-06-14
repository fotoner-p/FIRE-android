package moe.fotone.fire

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import moe.fotone.fire.utils.FollowDTO
import moe.fotone.fire.utils.NotificationDTO


class FragmentUser: Fragment() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance()}

    private lateinit var articleSnapshot: ListenerRegistration
    private lateinit var imageProfileListenerRegistration: ListenerRegistration
    private lateinit var followingListenerRegistration: ListenerRegistration
    private lateinit var followListenerRegistration: ListenerRegistration

    private lateinit var uid: String

    private val PICK_PROFILE_FROM_ALBUM = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        view.userArticleRelativeLayout.isNestedScrollingEnabled = false

        uid = arguments?.getString("destinationUid").toString()

        if (uid == auth.currentUser!!.uid) {
            view.userFollowBtn.text = "Logout"

            view.userFollowBtn.setOnClickListener {
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
                auth.signOut()
            }

            view.userImage.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    var photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    requireActivity().startActivityForResult(
                        photoPickerIntent,
                        PICK_PROFILE_FROM_ALBUM
                    )
                }
            }
        }
        else{
            view.userFollowBtn.setOnClickListener {
                requestFollow()
            }
        }

        database.collection("user")
            .document(uid)
            .get()
            .addOnSuccessListener  { document ->
                view.userNameText.text = document["name"].toString()
                view.userEmailText.text = document["email"].toString()
            }

        database.collection("articles").whereEqualTo("uid", uid).get().addOnSuccessListener {
            view.userAritcleCountText.text = it.size().toString()
        }
        getFollowInfo()

        return view
    }

    override fun onResume() {
        super.onResume()
        userListview.layoutManager = LinearLayoutManager(context)
        userListview.adapter = ArticleRecyclerViewAdapter(requireActivity(), uid)
        articleSnapshot = (userListview.adapter as ArticleRecyclerViewAdapter).articleSnapshot
        getProfileImage()
    }

    private fun getProfileImage() {
        imageProfileListenerRegistration = database.collection("profileImages").document(uid)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot?.data != null) {
                    val url = documentSnapshot.data!!["image"]
                    Glide.with(requireActivity())
                        .load(url)
                        .apply(RequestOptions().circleCrop()).into(userImage)
                }
            }
    }

    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
        imageProfileListenerRegistration.remove()
        followingListenerRegistration.remove()
        followListenerRegistration.remove()
    }

    private fun getFollowInfo(){
        val query = database.collection("followInfo").document(uid)

        followingListenerRegistration = query.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot != null && documentSnapshot.exists()){
                val followDTO = documentSnapshot.toObject(FollowDTO::class.java)

                userFollowingCountText.setText(followDTO?.followingCount.toString())
            }
            else
                return@addSnapshotListener
        }

        followListenerRegistration = query.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot != null && documentSnapshot.exists()){
                val followDTO = documentSnapshot.toObject(FollowDTO::class.java)

                userFollowerCountText.setText(followDTO?.followerCount.toString())
                if (followDTO!!.followers.containsKey(auth.currentUser!!.uid)) {
                    userFollowBtn.text = "unfollow"
                }
            }
            else{
                return@addSnapshotListener
            }
        }
    }


    private fun requestFollow(){
        val followingRef = database.collection("followInfo").document(auth.currentUser!!.uid)

        database.collection("user")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener  { document ->
                val userName= document["name"].toString()

                database
                    .collection("followInfo")
                    .document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener {
                        if(it != null && it.exists()){
                            val followDTO = it.toObject(FollowDTO::class.java)!!

                            if (followDTO.followings.containsKey(uid)){
                                followDTO.followingCount = followDTO.followingCount - 1
                                followDTO.followings.remove(uid)
                                userFollowBtn.text = "follow"
                            } else {
                                followDTO.followingCount = followDTO.followingCount + 1
                                followDTO.followings[uid] = true
                                followerNotification(uid, userName)
                            }
                            followingRef.set(followDTO)
                        }
                        else{
                            val followDTO = FollowDTO()
                            followDTO.followingCount = 1
                            followDTO.followings[uid] = true
                            followerNotification(uid, userName)

                            followingRef.set(followDTO)
                        }
                    }
            }

        val followerRef = database.collection("followInfo").document(uid)

        database
            .collection("followInfo")
            .document(uid)
            .get()
            .addOnSuccessListener {
                if(it != null && it.exists()) {
                    val followDTO = it.toObject(FollowDTO::class.java)!!

                    if (followDTO.followers.containsKey(auth.currentUser!!.uid)){
                        followDTO.followerCount = followDTO.followerCount - 1
                        followDTO.followers.remove(auth.currentUser!!.uid)
                    } else {
                        followDTO.followerCount = followDTO.followerCount + 1
                        followDTO.followers[auth.currentUser!!.uid] = true
                    }
                    followerRef.set(followDTO)
                }
                else{
                    val followDTO = FollowDTO()
                    followDTO.followerCount = 1
                    followDTO.followers[auth.currentUser!!.uid] = true
                    followerRef.set(followDTO)
                }
            }
    }

    private fun followerNotification(destinationUid: String, userName:String) {
        val notificationDTO = NotificationDTO()
        notificationDTO.targetUID = destinationUid
        notificationDTO.startName = userName
        notificationDTO.startUID = auth.currentUser!!.uid
        notificationDTO.kind = 2
        notificationDTO.timestamp = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("notification").document().set(notificationDTO)
    }

}