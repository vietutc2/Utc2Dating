package com.example.utc2datingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utc2datingapp.databinding.ItemUserLayoutBinding
import com.example.utc2datingapp.model.UserModel

class DatingAdapter(val context: Context, val list: ArrayList<UserModel>) : RecyclerView.Adapter<DatingAdapter.DatingiewHolder>() {
    inner class DatingiewHolder(val binding : ItemUserLayoutBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatingiewHolder {
        return DatingiewHolder(ItemUserLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: DatingiewHolder, position: Int) {
        holder.binding.textView4.text = list[position].name
        holder.binding.textView3.text = list[position].age
        Glide.with(context).load(list[position].image).into(holder.binding.userImage)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}