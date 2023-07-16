package com.example.utc2datingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utc2datingapp.databinding.MatchItemLayoutBinding
import com.example.utc2datingapp.model.MatchModel

class MessageUserAdapter(val context: Context, val list: ArrayList<MatchModel>) : RecyclerView.Adapter<MessageUserAdapter.MessageUserViewHolder>() {
    inner class MessageUserViewHolder(val binding: MatchItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageUserViewHolder {
        return MessageUserViewHolder(MatchItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MessageUserViewHolder, position: Int) {
        val matchedUser = list[position]

        Glide.with(context).load(matchedUser.matchedUserImage).into(holder.binding.userImage)
        holder.binding.userName.text = matchedUser.matchedUserName // Update this line

    }

    override fun getItemCount(): Int {
        return list.size
    }


}
