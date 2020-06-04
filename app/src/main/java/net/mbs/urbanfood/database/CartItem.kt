package net.mbs.urbanfood.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "Cart",primaryKeys = ["uid", "foodId", "foodAddon", "foodSize"])
class CartItem {
    @NonNull
    @ColumnInfo(name = "foodId")
    var foodId:String?=""
    @ColumnInfo(name = "foodName")
    var foodName:String?=""
    @ColumnInfo(name = "foodImage")
     var foodImage:String?=""
    @ColumnInfo(name = "foodPrice")
    var foodPrice:Double?=0.toDouble()
    @ColumnInfo(name = "foodQuantity")
     var foodQuantity:Int?=0
    @ColumnInfo(name = "userPhone")
    var userPhone:String?=""
    @ColumnInfo(name = "foodExtraPrice")
     var foodExtraPrice:Double?=0.toDouble()
    @NonNull
    @ColumnInfo(name = "foodAddon")
   var foodAddon:String?=""
    @NonNull
    @ColumnInfo(name = "foodSize")
   var foodSize:String?=""
    @NonNull
    @ColumnInfo(name = "uid")
    var uid:String?=""


    constructor()

    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if(other !is CartItem) return false

        val cartItem = other as CartItem
        return cartItem.foodId == this.foodId
                && cartItem.foodAddon == this.foodAddon
                && cartItem.foodSize ==this.foodSize
    }
}