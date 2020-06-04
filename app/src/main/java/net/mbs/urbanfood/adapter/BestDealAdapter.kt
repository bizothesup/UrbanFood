package net.mbs.urbanfood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.bumptech.glide.Glide
import net.mbs.urbanfood.R
import net.mbs.urbanfood.eventBus.BestDealItemClick
import net.mbs.urbanfood.models.BestDealModel
import org.greenrobot.eventbus.EventBus

class BestDealAdapter(context: Context, itemList: List<BestDealModel>,isInfinitie:Boolean):
    LoopingPagerAdapter<BestDealModel>(context,itemList,isInfinitie) {
    var img_best_deal:ImageView?=null
    var txt_best_deal:TextView?=null
    override fun inflateView(viewType: Int, container: ViewGroup?, listPosition: Int): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_best_deals_item,container,false);
    }

    override fun bindView(convertView: View?, listPosition: Int, viewType: Int) {
        img_best_deal= convertView?.findViewById(R.id.img_best_deal) as ImageView
        txt_best_deal = convertView.findViewById(R.id.txt_best_deal) as TextView

        Glide.with(convertView).load(itemList.get(listPosition).image).into(img_best_deal!!);
        txt_best_deal!!.text =itemList.get(listPosition).name

        convertView.setOnClickListener({v ->
            EventBus.getDefault().postSticky( BestDealItemClick(itemList.get(listPosition)))
        })
    }
}