package com.example.utc2datingapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.utc2datingapp.adapter.MessageUserAdapter
import com.example.utc2datingapp.databinding.FragmentMessageBinding
import com.example.utc2datingapp.model.MatchModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var currentUserId: String
    private lateinit var currentUserPhoneNumber: String
    private lateinit var matchesRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
        matchesRef = FirebaseDatabase.getInstance().getReference("matches")
        getData()
    }

    private fun getData() {
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        val matchesRef = FirebaseDatabase.getInstance().getReference("/matches")

        matchesRef.orderByChild("currentUserPhoneNumber").equalTo(currentUserPhoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list2 = ArrayList<MatchModel>()
                    for (data in snapshot.children) {
                        val match = data.getValue(MatchModel::class.java)
                        match?.let {
                            list2.add(it)
                        }
                    }
                    displayData(list2)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý khi có lỗi xảy ra trong quá trình lấy dữ liệu từ Firebase
                }
            })

    }

    private fun displayData(data: ArrayList<MatchModel>) {
        val adapter = MessageUserAdapter(requireContext(), data)
        binding.recycleView.adapter = adapter
    }
}
