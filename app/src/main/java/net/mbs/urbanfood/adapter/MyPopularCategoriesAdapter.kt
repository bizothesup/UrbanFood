package net.mbs.urbanfood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_popular_categories_item.view.*
import net.mbs.urbanfood.R
import net.mbs.urbanfood.callBacks.IRecyclerClickListener
import net.mbs.urbanfood.eventBus.PopularCategoryClick
import net.mbs.urbanfood.models.PopularCategoryModel
import org.greenrobot.eventbus.EventBus

class MyPopularCategoriesAdapter (internal  var context: Context,
                                    internal var popularCategoryModels: List<PopularCategoryModel>) :
    RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViewHolder>() {


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) ,View.OnClickListener {

    var category_name:TextView?=null
    var categorie_image:CircleImageView?=null
        internal var listener: IRecyclerClickListener?=null

        fun setListener(listener: IRecyclerClickListener){
            this.listener =listener
        }
        init {
          category_name = itemView.findViewById(R.id.txt_category_name) as TextView
            categorie_image= itemView.findViewById(R.id.categorie_image) as CircleImageView
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener?.onItemClickListener(v!!,adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return  MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_popular_categories_item,parent,false))
    }

    override fun getItemCount(): Int {
       return popularCategoryModels.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(popularCategoryModels.get(position).image)
            .into(holder.categorie_image!!)
        holder.category_name?.setText(popularCategoryModels.get(position).name!!)

        holder.listener= object :IRecyclerClickListener{
            override fun onItemClickListener(view: View, position: Int) {
                EventBus.getDefault().postSticky(PopularCategoryClick(popularCategoryModels.get(position)))
            }

        }
    }
}