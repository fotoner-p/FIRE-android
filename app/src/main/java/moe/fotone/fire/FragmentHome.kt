package moe.fotone.fire

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*


class FragmentHome: Fragment() {
    private lateinit var articleSnapshot: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.newArticleBtn.setOnClickListener {
            startActivity(Intent(context, WriteActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        homeListView.layoutManager = LinearLayoutManager(context)
        homeListView.adapter = ArticleRecyclerViewAdapter(requireActivity(), "main")
        articleSnapshot = (homeListView.adapter as ArticleRecyclerViewAdapter).articleSnapshot
    }

    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
    }
}