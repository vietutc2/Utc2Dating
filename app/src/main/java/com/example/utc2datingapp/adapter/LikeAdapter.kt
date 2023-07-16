package com.example.utc2datingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utc2datingapp.databinding.ItemUserLayoutBinding
import com.example.utc2datingapp.model.UserModel

class LikeAdapter(val context: Context, val list: ArrayList<UserModel>) : RecyclerView.Adapter<LikeAdapter.LikeViewHolder>() {

    inner class LikeViewHolder(val binding: ItemUserLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemUserLayoutBinding.inflate(inflater, parent, false)
        return LikeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val currentItem = list[position]
        holder.binding.textView4.text = currentItem.name
        holder.binding.textView3.text = currentItem.age.toString() // Đảm bảo chuyển đổi đúng kiểu dữ liệu
        Glide.with(context).load(currentItem.image).into(holder.binding.userImage)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
