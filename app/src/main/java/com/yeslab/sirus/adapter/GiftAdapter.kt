package com.yeslab.sirus.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yeslab.fastprefs.FastPrefs
import com.yeslab.sirus.R
import com.yeslab.sirus.controller.DummyMethods
import com.yeslab.sirus.databinding.GiftItemBinding
import com.yeslab.sirus.interfaces.GiftClick
import com.yeslab.sirus.model.Gift
import com.yeslab.sirus.model.User
import www.sanju.motiontoast.MotionToastStyle

class GiftAdapter(private val giftList: ArrayList<Gift>, val context: Context, val giftClick: GiftClick
) : RecyclerView.Adapter<GiftAdapter.MyHolder>() {



    class MyHolder(val binding: GiftItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = GiftItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val gift = giftList[position]

        holder.binding.title.text = gift.title
        Glide.with(context).load(gift.imageUrl).into(holder.binding.image)

        holder.binding.priceText.text = gift.price.toString()

        val fastPrefs = FastPrefs(context)
        val user : User? = fastPrefs.get("user",null)



        holder.itemView.setOnClickListener {
            giftClick.clickGift(giftList[position])
        }


    }






    override fun getItemCount(): Int {
        return giftList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}