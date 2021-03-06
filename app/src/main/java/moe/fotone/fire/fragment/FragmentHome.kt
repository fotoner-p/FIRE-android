package moe.fotone.fire.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import moe.fotone.fire.adapter.ArticleRecyclerViewAdapter
import moe.fotone.fire.R
import moe.fotone.fire.WriteActivity


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
        val activity = requireActivity()

        homeListView.layoutManager = LinearLayoutManager(context)
        homeListView.adapter =
            ArticleRecyclerViewAdapter(activity, "main")
        articleSnapshot = (homeListView.adapter as ArticleRecyclerViewAdapter).articleSnapshot

        activity.mainProgressBar.visibility = View.INVISIBLE
        activity.toolbarTitleText.text = "Home"
        activity.toolbarTitleText.visibility = View.VISIBLE
        activity.toolbarBackImage.visibility = View.INVISIBLE
    }

    override fun onStop() {
        super.onStop()
        articleSnapshot.remove()
    }
}