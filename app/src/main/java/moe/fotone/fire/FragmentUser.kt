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

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*


class FragmentUser: Fragment() {
    private val database by lazy { FirebaseFirestore.getInstance()}
    private lateinit var articleSnapshot: ListenerRegistration
    private lateinit var imageprofileListenerRegistration: ListenerRegistration
    private lateinit var uid: String

    private val PICK_PROFILE_FROM_ALBUM = 10


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        view.userArticleRelativeLayout.isNestedScrollingEnabled = false

        view.userImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                requireActivity().startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
            }
        }
        uid = arguments?.getString("destinationUid").toString()

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
        imageprofileListenerRegistration = database.collection("profileImages").document(uid)
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
    }
}