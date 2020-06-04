package net.mbs.urbanfood.ui.comment

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders.of
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog

import net.mbs.urbanfood.R
import net.mbs.urbanfood.adapter.MyCommentAdapter
import net.mbs.urbanfood.callBacks.ICommentCallbackListener
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.CommentModel



class CommentFragment : BottomSheetDialogFragment(),ICommentCallbackListener {


    private lateinit var viewModel: CommentViewModel

    private var recycler_comment:RecyclerView?=null
    private var dialog:AlertDialog?=null
    private var listener:ICommentCallbackListener
    init {
        listener=this
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(CommentViewModel::class.java)
        val root= inflater.inflate(R.layout.comment_fragment, container, false)
        initView(root)
        loadCommentFromFireBase()
            viewModel.getMutableLiveDataComment().observe(viewLifecycleOwner, Observer {
                val adapter = MyCommentAdapter(requireContext(),it)
                recycler_comment?.adapter = adapter
            })
        return root
    }

    private fun loadCommentFromFireBase() {
        dialog?.show()
val commentModels = ArrayList<CommentModel>()
FirebaseDatabase.getInstance().getReference(Common.COMMENT_REFERENCE)
    .child(Common.FOOD_SELECTED?.id!!)
    .orderByChild("commentTimeStamp")
    .limitToLast(100)
    .addListenerForSingleValueEvent(object :ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {
            listener.onCommentLoadFailed(p0.message)
            dialog?.dismiss()
        }

        override fun onDataChange(p0: DataSnapshot) {
            for(commentSnapshot in p0.children){
                val commentModel = commentSnapshot.getValue(CommentModel::class.java)
                commentModels.add(commentModel!!)
            }
            listener.onCommentLoadSuccess(commentModels)
            dialog?.dismiss()
        }
    })
    }

    private fun initView(root: View?) {
        dialog=SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        recycler_comment = root?.findViewById(R.id.recycler_comment) as RecyclerView

        recycler_comment?.setHasFixedSize(true);

        val layoutManager = LinearLayoutManager(getContext(),RecyclerView.VERTICAL,true);
        recycler_comment?.setLayoutManager(layoutManager);
        recycler_comment?.addItemDecoration( DividerItemDecoration(getContext(),layoutManager.getOrientation()));

    }


    override fun onCommentLoadSuccess(commentModels: List<CommentModel>) {
       viewModel.setCommentModel(commentModels)
    }

    override fun onCommentLoadFailed(message: String) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    companion object{
        private var instance:CommentFragment?=null

        fun getInstance():CommentFragment{
            if(instance == null)
                instance =  CommentFragment()
            return instance!!
        }

    }
}
