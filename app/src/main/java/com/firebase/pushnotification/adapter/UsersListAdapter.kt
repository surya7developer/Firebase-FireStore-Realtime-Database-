package com.firebase.pushnotification.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.pushnotification.databinding.UserItemBinding
import com.firebase.pushnotification.listener.HandleButtonAction
import com.firebase.pushnotification.models.UserDetails

class UsersListAdapter(
    private val usersList: MutableList<UserDetails>, private val listener: HandleButtonAction
) : RecyclerView.Adapter<UsersListAdapter.AnimalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        binding.apply {
            userId.text = (position.plus(1)).toString()
            userName.text = usersList[position].name
            userEmail.text = usersList[position].email
        }

        binding.btnUpdate.setOnClickListener {
            listener.onClickUpdate(usersList[position])
        }

        binding.btnDelete.setOnClickListener {
            listener.onClickDelete(usersList[position].uid)
        }
    }

    fun setData(data: MutableList<UserDetails>) {
        usersList.clear()
        usersList.addAll(data)
        notifyDataSetChanged()
    }

    private lateinit var binding: UserItemBinding
    override fun getItemCount(): Int = usersList.size
    class AnimalViewHolder(binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root)
}