package net.mbs.urbanfood.callBacks

import net.mbs.urbanfood.models.CommentModel

interface ICommentCallbackListener {
    fun onCommentLoadSuccess(commentModels:List<CommentModel>)
    fun onCommentLoadFailed(message:String)
}