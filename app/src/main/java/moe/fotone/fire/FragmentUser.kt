package moe.fotone.fire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_user.*


class FragmentUser: Fragment() {
    private lateinit var articleSnapshot: ListenerRegistration
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        return view
    }

    override fun onResume() {
        super.onResume()
        userListview.layoutManager = LinearLayoutManager(context)
        userListview.adapter = ArticleRecyclerViewAdapter(context, auth.currentUser!!.uid)
        articleSnapshot = (userListview.adapter as ArticleRecyclerViewAdapter).articleSnapshot
    }

    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
    }
}