package net.mbs.urbanfood.models

class FoodModel {
    var key:String?=null
    var id:String?=null
    var name:String?=null
    var image:String?=null
    var description:String?=null
    var price:Long?=0
    var addon:List<AddonModel>?=ArrayList<AddonModel>()
    var size:List<SizesModel>?=ArrayList<SizesModel>()
    var ratingValue:Double?=0.0
    var ratingCount:Long?= 0L
    var userSelectedSize:SizesModel?=null
    var userSelectedAddon:MutableList<AddonModel>?=null

    constructor()
    constructor(
        key: String?,
        id: String?,
        name: String?,
        image: String?,
        description: String?,
        price: Long?,
        addon: List<AddonModel>?,
        size: List<SizesModel>?,
        ratingValue: Double?,
        ratingCount: Long?,
        userSelectedSize: SizesModel?,
        userSelectedAddon: MutableList<AddonModel>?
    ) {
        this.key = key
        this.id = id
        this.name = name
        this.image = image
        this.description = description
        this.price = price
        this.addon = addon
        this.size = size
        this.ratingValue = ratingValue
        this.ratingCount = ratingCount
        this.userSelectedSize = userSelectedSize
        this.userSelectedAddon = userSelectedAddon
    }


}
