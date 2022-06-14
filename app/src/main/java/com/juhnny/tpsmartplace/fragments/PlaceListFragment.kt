package com.juhnny.tpsmartplace.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juhnny.tpsmartplace.activities.MainActivity
import com.juhnny.tpsmartplace.adapters.PlaceListRecyclerAdapter
import com.juhnny.tpsmartplace.databinding.FragmentPlaceListBinding

class PlaceListFragment : Fragment() {

    val mainActivity by lazy { requireActivity() as MainActivity }
    val b by lazy { FragmentPlaceListBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPlaceListRecyclerAdapter()
    }

    fun setPlaceListRecyclerAdapter(){
        val ma = activity as MainActivity
        //adapter가 만들어지는 당시에 아직 네트워크작업이 안끝났을 수도 있다. 그러면 아직 searchPlaceResponse는 null인 상태
        //아직 MainActivity에서의 파싱 작업이 완료되지 않았다면
        if(mainActivity.searchPlaceResponse == null) return
        b.recycler.adapter = PlaceListRecyclerAdapter(requireContext(), mainActivity.searchPlaceResponse!!.documents)
        //non-null assault는 null인 경우에 대한 처리를 해주고 사용하는 게 확실!
        //Fragment가 Activity에 붙지 않은 경우에는 getActivity()나 getContext()를 해도 null이 온다.
        //requireActivity()나 requireContext()는 Fragment가 Activity에 붙지 않은 경우에 대한 예외처리가 돼있음.
        //IllegalStateException("Fragment " + this + " not attached to a context.")
    }

}