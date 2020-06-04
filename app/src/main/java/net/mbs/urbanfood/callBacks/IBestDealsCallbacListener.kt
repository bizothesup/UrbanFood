package net.mbs.urbanfood.callBacks

import net.mbs.urbanfood.models.BestDealModel

interface IBestDealsCallbacListener {
    fun onBestDealsLoadSuccess(bestDeals: List<BestDealModel>);
    fun onBestDealsLoadFailed(message:String);
}