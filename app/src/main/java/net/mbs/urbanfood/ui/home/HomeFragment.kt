package net.mbs.urbanfood.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager

import kotlinx.android.synthetic.main.fragment_home.*
import net.mbs.urbanfood.R
import net.mbs.urbanfood.adapter.BestDealAdapter
import net.mbs.urbanfood.adapter.MyPopularCategoriesAdapter
import net.mbs.urbanfood.eventBus.MenuItemBack
import org.greenrobot.eventbus.EventBus

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    var recycler_populaire:RecyclerView?=null
    var viewpage:LoopingViewPager?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        initView(root)

        homeViewModel.popularList.observe(viewLifecycleOwner, Observer {
            val listData = it
            val adapter= MyPopularCategoriesAdapter(requireContext(),listData)
            recycler_populaire?.adapter = adapter
        })

        homeViewModel.bestDealList.observe(viewLifecycleOwner, Observer {
            val listData = it
            val adapter = BestDealAdapter( requireContext(),listData,true)
            viewpage?.adapter = adapter
        })

        return root
    }

    private fun initView(root:View) {
        recycler_populaire= root.findViewById(R.id.recycler_populaire) as RecyclerView
        recycler_populaire?.setHasFixedSize(true)
        recycler_populaire?.layoutManager = LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)

        viewpage= root.findViewById(R.id.viewpager) as LoopingViewPager


    }
    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}
