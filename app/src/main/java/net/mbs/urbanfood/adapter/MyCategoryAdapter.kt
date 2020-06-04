package net.mbs.urbanfood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.mbs.urbanfood.R
import net.mbs.urbanfood.callBacks.IRecyclerClickListener
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.eventBus.CategoryClick
import net.mbs.urbanfood.models.CategoryModel
import org.greenrobot.eventbus.EventBus

class MyCategoryAdapter(
    internal var context: Context,
    internal var categoryModels: List<CategoryModel>
) :
    RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var img_category: ImageView? = null
        var txt_category_name: TextView? = null
        internal var listener:IRecyclerClickListener?=null

        fun setListener(listener: IRecyclerClickListener){
            this.listener =listener
        }

        init {
            img_category = itemView.findViewById(R.id.img_category)
            txt_category_name = itemView.findViewById(R.id.txt_category_name)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener?.onItemClickListener(v!!,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_category_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return categoryModels.size
    }


    override fun getItemViewType(position: Int): Int {
        return if (categoryModels.size == 1)
            Common.DEFAULT_COLUMN_COUNT
        else {
            if (categoryModels.size % 2 == 0)
                Common.DEFAULT_COLUMN_COUNT
            else
                if (position > 1 && position == categoryModels.size)
                    Common.FULL_WIDHT_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(categoryModels.get(position).image).into(holder.img_category!!)
        holder.txt_category_name?.text = categoryModels.get(position).name

        holder.setListener(object: IRecyclerClickListener{
            override fun onItemClickListener(view: View, position: Int) {
                Common.CATEGORY_SELECTED=categoryModels.get(position)
                EventBus.getDefault().postSticky(CategoryClick(true,categoryModels.get(position)))
            }

        })
    }


}