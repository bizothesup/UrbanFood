package net.mbs.urbanfood.database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
@Dao
interface CartDAO {
    @Query("SELECT * FROM Cart WHERE uid=:uid")
    fun getAllCart(uid:String): Flowable<List<CartItem>>

    @Query("SELECT SUM(foodQuantity) FROM Cart WHERE uid=:uid")
    fun countItemInCart(uid:String): Single<Int>

    @Query("SELECT SUM((foodPrice + foodExtraPrice) * foodQuantity) FROM Cart WHERE uid=:uid")
    fun  sumPriceInCart(uid:String):Single<Double>

    @Query("SELECT * FROM Cart WHERE foodId=:foodId and uid=:uid")
    fun  getItemInCart(foodId:String,uid:String):Single<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun  insertOrReplaceAll(vararg cartItems:CartItem): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun  updateCartItems(vararg cartItems:CartItem):Single<Int>

    @Delete
    fun  deleteCartItem(cartItem:CartItem ): Single<Int>

    @Query("DELETE FROM Cart WHERE uid=:uid")
    fun cleanCart(uid:String):Single<Int>

    @Query("SELECT * FROM Cart WHERE foodId=:foodId and uid=:uid and foodAddon=:foodAddon and foodSize=:foodSize")
    fun getItemWithAllOptionsInCart( uid:String,foodId:String,foodAddon:String,foodSize:String): Single<CartItem>
}