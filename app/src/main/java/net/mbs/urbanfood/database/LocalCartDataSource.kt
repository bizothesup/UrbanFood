package net.mbs.urbanfood.database

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalCartDataSource(private val cartDAO: CartDAO):CartDataSource {
    override fun getAllCart(uid: String): Flowable<List<CartItem>> {
        return cartDAO.getAllCart(uid)
    }

    override fun countItemInCart(uid: String): Single<Int> {
        return cartDAO.countItemInCart(uid)
    }

    override fun sumPriceInCart(uid: String): Single<Double> {
        return cartDAO.sumPriceInCart(uid)
    }

    override fun getItemInCart(foodId: String, uid: String): Single<CartItem> {
        return cartDAO.getItemInCart(foodId,uid)
    }

    override fun insertOrReplaceAll(vararg cartItems: CartItem): Completable {
        return cartDAO.insertOrReplaceAll(*cartItems)
    }

    override fun updateCartItems(vararg cartItems: CartItem): Single<Int> {
        return cartDAO.updateCartItems(*cartItems)
    }

    override fun deleteCartItem(cartItem: CartItem): Single<Int> {
        return cartDAO.deleteCartItem(cartItem)
    }

    override fun cleanCart(uid: String): Single<Int> {
        return cartDAO.cleanCart(uid)
    }

    override fun getItemWithAllOptionsInCart(
        uid: String,
        foodId: String,
        foodAddon: String,
        foodSize: String
    ): Single<CartItem> {
        return cartDAO.getItemWithAllOptionsInCart(uid,foodId,foodAddon,foodSize)
    }

}