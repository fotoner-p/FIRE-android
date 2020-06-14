package moe.fotone.fire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*


class FragmentDashboard: Fragment() {
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

        requireActivity().mainProgressBar.visibility = View.INVISIBLE
        requireActivity().toolbarTitleText.text = "Dashboard"
        requireActivity().toolbarTitleText.visibility = View.VISIBLE
    }

}