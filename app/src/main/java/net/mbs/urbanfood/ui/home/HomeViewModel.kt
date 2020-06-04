package net.mbs.urbanfood.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import net.mbs.urbanfood.callBacks.IBestDealsCallbacListener
import net.mbs.urbanfood.callBacks.IPopularCategoryCallbackListener
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.BestDealModel
import net.mbs.urbanfood.models.PopularCategoryModel

class HomeViewModel : ViewModel(), IPopularCategoryCallbackListener, IBestDealsCallbacListener {
    private  var popularMutableList:MutableLiveData<List<PopularCategoryModel>>?=null
    private lateinit var messageError:MutableLiveData<String>
    private  var popularCategoryCallbackListener: IPopularCategoryCallbackListener?=null

    private var bestDealLiveData:MutableLiveData<List<BestDealModel>>?=null
    private var besIBestDealsCallbacListener:IBestDealsCallbacListener?=null

    val popularList:LiveData<List<PopularCategoryModel>>
        get() {
            if(popularMutableList == null)
            {
                popularMutableList = MutableLiveData()
                messageError= MutableLiveData()
                loadPopularList()
            }
            return popularMutableList!!
        }
    val bestDealList:LiveData<List<BestDealModel>>
    get() {
        if(bestDealLiveData == null){
            bestDealLiveData = MutableLiveData()
            messageError = MutableLiveData()
            loadBeastDeal()
        }
        return  bestDealLiveData!!
    }

    private fun loadBeastDeal() {
        val tempdir =ArrayList<BestDealModel>()
        val bestDealReference = FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REFERENCE)
        bestDealReference.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                besIBestDealsCallbacListener?.onBestDealsLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
               for (itemSnapshot in p0!!.children)
               {
                   val model = itemSnapshot.getValue(BestDealModel::class.java)
                   tempdir.add(model!!)
               }
                besIBestDealsCallbacListener?.onBestDealsLoadSuccess(tempdir)
            }

        })
    }


    private fun loadPopularList() {
        val tempdir =ArrayList<PopularCategoryModel>()
        val popularReference = FirebaseDatabase.getInstance().getReference(Common.POPULAR_CATEGORY_REFERENCE)
        popularReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                popularCategoryCallbackListener?.onPopularLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0!!.children)
                {
                    val model = itemSnapshot.getValue(PopularCategoryModel::class.java)
                    tempdir.add(model!!)
                }
                popularCategoryCallbackListener?.onPopularLoadSuccess(tempdir)
            }
        })
    }

    init {
        popularCategoryCallbackListener =this
        besIBestDealsCallbacListener =this
    }

    override fun onPopularLoadSuccess(popularCategoryModels: List<PopularCategoryModel>) {
       popularMutableList?.value =popularCategoryModels
    }

    override fun onPopularLoadFailed(message: String) {
        messageError.value =message
    }

    override fun onBestDealsLoadSuccess(bestDeals: List<BestDealModel>) {
        bestDealLiveData?.value = bestDeals
    }

    override fun onBestDealsLoadFailed(message: String) {
        messageError.value = message
    }

}