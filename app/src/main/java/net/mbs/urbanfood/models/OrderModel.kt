package net.mbs.urbanfood.models

import net.mbs.urbanfood.database.CartItem

class OrderModel {
    var userId: String? = null
    var userName: String? = null
    var userPhone: String? = null
    var shippingAdress: String? = null
    var comment: String? = null
    var transactionId: String? = null
    var lat: Double? = 0.toDouble()
    var lng: Double? = 0.toDouble()
    var discount: Double? = 0.toDouble()
    var totalPayement: Double? = 0.toDouble()
    var finalPayement: Double? = 0.toDouble()
    var isCod: Boolean? = null
    var cartItemList: List<CartItem>? = null
    var createDate: Long? = 0.toLong()
    var key: String? = null
    var status: Int? = null


    constructor()
    constructor(
        userId: String?,
        userName: String?,
        userPhone: String?,
        shippingAdress: String?,
        comment: String?,
        transactionId: String?,
        lat: Double?,
        lng: Double?,
        totalPayement: Double?,
        finalPayement: Double?,
        isCod: Boolean?,
        cartItemList: List<CartItem>?,
        createDate: Long?,
        key: String?,
        status: Int?
    ) {
        this.userId = userId
        this.userName = userName
        this.userPhone = userPhone
        this.shippingAdress = shippingAdress
        this.comment = comment
        this.transactionId = transactionId
        this.lat = lat
        this.lng = lng
        this.totalPayement = totalPayement
        this.finalPayement = finalPayement
        this.isCod = isCod
        this.cartItemList = cartItemList
        this.createDate = createDate
        this.key = key
        this.status = status
    }

}