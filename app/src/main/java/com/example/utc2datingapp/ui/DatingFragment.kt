package com.example.utc2datingapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.utc2datingapp.R
import com.example.utc2datingapp.activity.FilterActivity
import com.example.utc2datingapp.adapter.DatingAdapter
import com.example.utc2datingapp.databinding.FragmentDatingBinding
import com.example.utc2datingapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.yuyakaido.android.cardstackview.*

class DatingFragment : Fragment(), CardStackListener {
    private lateinit var binding: FragmentDatingBinding
    private lateinit var manager: CardStackLayoutManager
    private lateinit var heartImageView: ImageView
    private lateinit var cancelImageView: ImageView
    private var currentPosition: Int = 0
    private var lastDirection: Direction? = null
    private var isRewinding: Boolean = false
    private  lateinit var list: ArrayList<UserModel>
    private lateinit var database: FirebaseDatabase
    private lateinit var currentUserPhone: String


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.buttonfilter, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                // Xử lý sự kiện khi người dùng nhấp vào nút lọc
                val intent = Intent(requireContext(), FilterActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        getData()
        setHasOptionsMenu(true)

        // Lấy số điện thoại của người dùng hiện tại (đặt giá trị cho currentUserPhone)
        currentUserPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

        // Khởi tạo Firebase Database
        database = FirebaseDatabase.getInstance()

        // Xử lý sự kiện click cho ImageView "heart"
        binding.heart.setOnClickListener {
            Log.d("DatingFragment", "Heart clicked") // Log khi nhấp vào ImageView "heart"
            if (currentPosition < list.size) {
                // Lấy số điện thoại của người dùng hiện tại và người dùng được thích
                val currentUserPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
                val likedUserPhone = list[currentPosition].number

                // Cập nhật trạng thái thích cho người dùng hiện tại và người dùng được thích
                updateLikeStatus(currentUserPhone, likedUserPhone, true)

                currentPosition++

                // Kiểm tra xem đã cuối danh sách người dùng chưa
                if (currentPosition < list.size) {
                    // Lướt card stack đến vị trí tiếp theo
                    manager.scrollToPosition(currentPosition)
                } else {
                    Toast.makeText(requireContext(), "Đây là thẻ cuối cùng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Xử lý sự kiện click cho ImageView "cancel"
        binding.cancel.setOnClickListener {
            val direction = Direction.Left
            rewindCard(direction)
        }
    }

    private fun init() {
        manager = CardStackLayoutManager(requireContext(), this)
        binding.cardStackView.layoutManager = manager

        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual) // Bật chế độ trượt tự động và thủ công
        manager.setVisibleCount(3)
        manager.setTranslationInterval(12.0f) // Đặt khoảng cách giữa các card
        manager.setScaleInterval(0.95f) // Đặt tỷ lệ thu phóng của card
        manager.setMaxDegree(20.0f) // Đặt góc xoay tối đa của card
        manager.setDirections(Direction.HORIZONTAL)
    }

    private fun getData() {
        FirebaseDatabase.getInstance().getReference("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val currentUserLikedRef = database.reference.child("users").child(currentUserPhone).child("likedUsers")
                        currentUserLikedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(likedUsersSnapshot: DataSnapshot) {
                                val likedUsersList = ArrayList<String>()
                                val likedByUsersList = ArrayList<String>()

                                for (likedUserSnapshot in likedUsersSnapshot.children) {
                                    val likedUserPhone = likedUserSnapshot.key!!
                                    likedUserPhone?.let {
                                        likedUsersList.add(it)
                                    }
                                }

                                for (data in snapshot.children) {
                                    val model = data.getValue(UserModel::class.java)
                                    model?.let {
                                        if (it.number == currentUserPhone) {
                                            return@let // Bỏ qua người dùng hiện tại
                                        }

                                        val likedByUserRef = database.reference.child("users").child("likedBy")
                                        likedByUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(likedByUsersSnapshot: DataSnapshot) {
                                                val likedByUserPhone = likedByUsersSnapshot.key
                                                likedByUserPhone?.let {
                                                    likedByUsersList.add(it)
                                                }

                                                val shouldSkipUser = likedByUsersList.contains(currentUserPhone) || likedUsersList.contains(it.number)
                                                if (!shouldSkipUser) {
                                                    list.add(it)
                                                }

                                                if (likedByUsersSnapshot.childrenCount == snapshot.childrenCount - 1) {
                                                    // Kiểm tra nếu đã xét tất cả người dùng, thì hiển thị danh sách
                                                    list.shuffle() // Shuffle the list of users

                                                    binding.cardStackView.itemAnimator = DefaultItemAnimator()
                                                    binding.cardStackView.adapter = DatingAdapter(requireContext(), list)

                                                    heartImageView = binding.heart
                                                    cancelImageView = binding.cancel

                                                    for (user in list) {
                                                        Log.d("DatingFragment", "User: $user")
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


    private var isCardAnimating: Boolean = false

    private fun swipeCard(direction: Direction) {
        if (!isCardAnimating) {
            isCardAnimating = true
            val targetPosition = if (direction == Direction.Right) currentPosition else currentPosition + 1

            if (targetPosition >= 0 && targetPosition < list.size) {
                val viewHolder = binding.cardStackView.findViewHolderForAdapterPosition(currentPosition)

                viewHolder?.itemView?.let {
                    val setting = SwipeAnimationSetting.Builder()
                        .setDirection(direction)
                        .setDuration(200)
                        .build()
                    manager.setSwipeAnimationSetting(setting)
                    binding.cardStackView.swipe()
                    Log.d("DatingFragment", "Swipe animation: $direction")

                    // Update currentPosition and lastDirection
                    currentPosition = targetPosition
                    lastDirection = direction

                    // Update like status on Firebase
                    val likedUserPhone = list[targetPosition].number
                    updateLikeStatus(currentUserPhone, likedUserPhone, true)
                }
            }

            // Wait for animation to complete before allowing next swipe
            Handler().postDelayed({
                isCardAnimating = false
            }, 200)
        }
    }


    private fun rewindCard(direction: Direction) {
        if (!isCardAnimating) {
            isCardAnimating = true
            val targetPosition = if (direction == Direction.Left) currentPosition else currentPosition + 1
            if (targetPosition >= 0 && targetPosition < list.size) {
                val viewHolder = binding.cardStackView.findViewHolderForAdapterPosition(currentPosition)

                viewHolder?.itemView?.let {
                    val setting = SwipeAnimationSetting.Builder()
                        .setDirection(direction)
                        .setDuration(200)
                        .build()
                    manager.setSwipeAnimationSetting(setting)
                    binding.cardStackView.swipe()
                    Log.d("DatingFragment", "Swipe animation: $direction")

                    // Update currentPosition and lastDirection
                    currentPosition = targetPosition
                    lastDirection = direction
                }
            }

            // Wait for animation to complete before allowing next swipe
            Handler().postDelayed({
                isCardAnimating = false
            }, 200)
        }
    }
    private fun updateLikeStatus(currentUserPhone: String?, likedUserPhone: String?, isLiked: Boolean) {
        val currentUser = currentUserPhone.orEmpty()
        val likedUser = likedUserPhone.orEmpty()

        val currentUserLikedRef = database.reference.child("users").child(currentUser).child("likedUsers")
        val likedUserLikedByRef = database.reference.child("users").child(likedUser).child("likedBy")

        if (isLiked) {
            currentUserLikedRef.child(likedUser).setValue(true)
            likedUserLikedByRef.child(currentUser).setValue(true)

            // Lấy thông tin người dùng hiện tại
            val currentUserRef = database.reference.child("users").child(currentUser)
            currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUserModel = snapshot.getValue(UserModel::class.java)

                    // Lấy thông tin người dùng được thích
                    val likedUserRef = database.reference.child("users").child(likedUser)
                    likedUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(likedUserSnapshot: DataSnapshot) {
                            val likedUserModel = likedUserSnapshot.getValue(UserModel::class.java)

                            // Cập nhật thông tin người dùng vào likeUsers và likeBy
                            currentUserLikedRef.child(likedUser).setValue(likedUserModel)
                            likedUserLikedByRef.child(currentUser).setValue(currentUserModel)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Xử lý khi có lỗi xảy ra
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý khi có lỗi xảy ra
                }
            })
        } else {
            currentUserLikedRef.child(likedUser).removeValue()
            likedUserLikedByRef.child(currentUser).removeValue()
        }
    }




    override fun onCardDragging(direction: Direction?, ratio: Float) {
        val scaleFactor = 1.0f + (0.2f * ratio) // Tăng kích thước ban đầu theo tỷ lệ

        if (direction == Direction.Right) {
            heartImageView.scaleX = scaleFactor
            heartImageView.scaleY = scaleFactor
        } else if (direction == Direction.Left) {
            cancelImageView.scaleX = scaleFactor
            cancelImageView.scaleY = scaleFactor
        }
    }

    override fun onCardSwiped(direction: Direction?) {
        Log.d("DatingFragment", "Card swiped: $direction")
        if (currentPosition == list.size) {
            Toast.makeText(requireContext(), "Đây là thẻ cuối cùng", Toast.LENGTH_SHORT).show()
        } else {
            val scaleUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            val scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)

            val animationSet = AnimationSet(true)
            animationSet.addAnimation(scaleUpAnimation)
            animationSet.addAnimation(scaleDownAnimation)

            when (direction) {
                Direction.Right -> {
                    heartImageView.startAnimation(animationSet)
                    val likedUserPhone = list[currentPosition].number
                    val currentUserPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber
                    updateLikeStatus(currentUserPhone, likedUserPhone, true)
                }
                Direction.Left -> {
                    cancelImageView.startAnimation(animationSet)
                    currentPosition++
                    if (currentPosition < 0) {
                        currentPosition = 0
                    }
                }
                else -> {
                    // Xử lý các hướng khác nếu cần
                }
            }

            // Đặt currentPosition mới
            val targetPosition = currentPosition

            // Lướt card stack đến vị trí tiếp theo
            manager.scrollToPosition(targetPosition)

            // Đặt lại tỷ lệ của các image view
            heartImageView.scaleX = 1.0f
            heartImageView.scaleY = 1.0f
            cancelImageView.scaleX = 1.0f
            cancelImageView.scaleY = 1.0f

            lastDirection = direction
        }
    }



    override fun onCardRewound() {
        Log.d("DatingFragment", "Card rewound")
    }

    override fun onCardCanceled() {
        Log.d("DatingFragment", "Card canceled")
    }

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}