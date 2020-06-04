package net.mbs.urbanfood.Remote

import io.reactivex.Observable
import net.mbs.urbanfood.models.BraintreeToken
import net.mbs.urbanfood.models.BraintreeTransaction
import okhttp3.ResponseBody
import retrofit2.http.*
import java.util.*

interface ICloudFunctions {
    @GET("token")
    fun getToken(@HeaderMap headers: Map<String,String>): Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun  submitPayement(@HeaderMap headers: Map<String,String>,
                        @Field("amount") amount:Double,
                        @Field("payment_method_nonce") nonce:String): Observable<BraintreeTransaction>
}