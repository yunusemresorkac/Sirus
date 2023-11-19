package com.prizma.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prizma.app.R
import com.prizma.app.databinding.LeaderItemBinding
import com.prizma.app.model.User

class LeaderAdapter(private val userList: ArrayList<User>, val context: Context
) : RecyclerView.Adapter<LeaderAdapter.MyHolder>() {



    class MyHolder(val binding: LeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = LeaderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val user = userList[position]


        holder.binding.leaderCountText.text = "${position + 1}."

        holder.binding.username.text = user.username
        holder.binding.rate.text = "Doğru Sayısı: ${user.correctAnswer}"



        when (position) {
            0 -> {
                holder.binding.leaderOrderBg.setBackgroundColor(context.resources.getColor(R.color.green_800))
            }
            1 -> {
                holder.binding.leaderOrderBg.setBackgroundColor(context.resources.getColor(R.color.appOrange))
            }
            2 -> {
                holder.binding.leaderOrderBg.setBackgroundColor(context.resources.getColor(R.color.appBlue))
            }
            else -> {
                holder.binding.leaderOrderBg.setBackgroundColor(context.resources.getColor(R.color.black))

            }
        }


    }






    override fun getItemCount(): Int {
        return userList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}