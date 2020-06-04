package net.mbs.urbanfood.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mbs.urbanfood.R
import net.mbs.urbanfood.models.CommentModel

class MyCommentAdapter(
    internal var context: Context,
    internal var commentModels: List<CommentModel>
) :RecyclerView.Adapter<MyCommentAdapter.MyViewHolder>(){

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_comment_date:TextView?=null
        var txt_comment:TextView?=null
        var txt_comment_name:TextView?=null
        var ratingBar: RatingBar?=null
        init {
            txt_comment_date = itemView.findViewById(R.id.txt_comment_date) as TextView
            txt_comment = itemView.findViewById(R.id.txt_comment) as TextView
            txt_comment_name = itemView.findViewById(R.id.txt_comment_name) as TextView
            ratingBar = itemView.findViewById(R.id.ratingBar) as RatingBar
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return  MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_comment_item,parent,false));
    }

    override fun getItemCount(): Int {
       return commentModels.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_comment_name?.text =commentModels.get(position).name
        holder.txt_comment?.text =commentModels.get(position).comment
        holder.ratingBar?.rating =commentModels.get(position).ratingValue!!

        val timeStamp =commentModels.get(position).commentTimeStamp?.get("timeStamp").toString().toLong()
        holder.txt_comment_date?.text = DateUtils.getRelativeTimeSpanString(timeStamp)
    }
}