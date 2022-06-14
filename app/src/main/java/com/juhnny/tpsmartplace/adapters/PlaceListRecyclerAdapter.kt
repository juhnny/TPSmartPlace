package com.juhnny.tpsmartplace.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.activities.PlaceUrlActivity
import com.juhnny.tpsmartplace.databinding.RecyclerItemPlaceListBinding
import com.juhnny.tpsmartplace.model.Place

class PlaceListRecyclerAdapter(val context:Context, var documents:MutableList<Place>) : RecyclerView.Adapter<PlaceListRecyclerAdapter.VH>(){

    inner class VH(itemView:View) : RecyclerView.ViewHolder(itemView){
    //원래 ViewHolder는 itemView라는 멤버변수가 있어서 itemView를 꼭 멤버변수로 만들 필요가 없다. 인수로 넘겨주기만 하면 됨. 만들면 오히려 겹쳐버리려나?
        val binding = RecyclerItemPlaceListBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        var itemView = LayoutInflater.from(context).inflate(R.layout.recycler_item_place_list, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        var place = documents[position]
        holder.binding.tvPlaceName.text = place.place_name
        holder.binding.tvAddressName.text = if(place.road_address_name == "") place.address_name else place.road_address_name
        holder.binding.tvDistance.text = place.distance + "m"
        holder
        //장소 아이템뷰를 클릭했을 때 장소에 대한 상세정보 화면으로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("placeUrl", place.place_url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        Log.e("PlaceListRecyclerAdapter", "${documents.size}")
        return documents.size
    }
}