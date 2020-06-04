package net.mbs.urbanfood.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.database.CartDataSource
import net.mbs.urbanfood.database.CartDatabase
import net.mbs.urbanfood.database.CartItem
import net.mbs.urbanfood.database.LocalCartDataSource

class CartViewModel : ViewModel() {
    private  var mutableLiveDataCartItems: MutableLiveData<List<CartItem>>?=null
    private lateinit var messageError:MutableLiveData<String>
    private var cartDataSource: CartDataSource?=null
    private var compositeDisposable:CompositeDisposable

    init {
        compositeDisposable= CompositeDisposable()
    }

    fun initCartDataSource(context: Context){
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }

    fun getMutableLiveDataCartItems():MutableLiveData<List<CartItem>>{
        if(mutableLiveDataCartItems == null)
            mutableLiveDataCartItems =  MutableLiveData()
        getCartItems()
        return mutableLiveDataCartItems!!
    }


    private fun getCartItems(){
        compositeDisposable.addAll(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({cartItems ->
                mutableLiveDataCartItems!!.value = cartItems
            },{
                t: Throwable? -> mutableLiveDataCartItems!!.value=null
            })
        )
    }
fun onStop(){
    compositeDisposable.clear()
}
}
