package com.prizma.app.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prizma.app.R
import com.prizma.app.controller.DummyMethods
import com.prizma.app.databinding.OrderItemBinding
import com.prizma.app.model.Order
import com.prizma.app.util.Constants
import www.sanju.motiontoast.MotionToastStyle


class OrderAdapter(private val orderList: ArrayList<Order>, val context: Context
) : RecyclerView.Adapter<OrderAdapter.MyHolder>() {



    class MyHolder(val binding: OrderItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = OrderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val order = orderList[position]


        Glide.with(context).load(order.imageUrl).into(holder.binding.image)
        holder.binding.priceText.text = order.price.toString()
        holder.binding.title.text = order.title

        holder.binding.time.text = DummyMethods.convertTime(order.id.toLong())


        if (order.response == ""){
            holder.binding.responseText.text = "Kod burada gözükecektir."

        }else{
            holder.binding.responseText.text = order.response
        }


        when (order.status){
            Constants.PENDING_ORDER -> {
                holder.binding.statusText.text = "Beklemede. Yaklaşık 24 Saat."
                holder.binding.statusText.setTextColor(context.resources.getColor(R.color.itemOrange))
            }
            Constants.SUCCESS_ORDER -> {
                holder.binding.statusText.text = "Tamamlandı."
                holder.binding.statusText.setTextColor(context.resources.getColor(R.color.appGreen))


            }
            Constants.CANCELLED_ORDER -> {
                holder.binding.statusText.text = "İptal Edildi."
                holder.binding.statusText.setTextColor(context.resources.getColor(R.color.error))


            }
        }

        holder.itemView.setOnClickListener {
            if (order.response != ""){
                val clipboard: ClipboardManager? =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("code", order.response)
                clipboard?.setPrimaryClip(clip)
                DummyMethods.showMotionToast(context,"Kopyalandı","",MotionToastStyle.SUCCESS)
            }
        }


    }






    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}