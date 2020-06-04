package net.mbs.urbanfood.models



class CommentModel {
var ratingValue:Float?=0.toFloat()
var comment:String?=null
var name:String?=null
var uid:String?=null
var commentTimeStamp:Map<String, Any>?=null

    constructor()
    constructor(
        ratingValue: Float?,
        comment: String?,
        name: String?,
        uid: String?,
        commentTimeStamp: Map<String, Any>?
    ) {
        this.ratingValue = ratingValue
        this.comment = comment
        this.name = name
        this.uid = uid
        this.commentTimeStamp = commentTimeStamp
    }

}