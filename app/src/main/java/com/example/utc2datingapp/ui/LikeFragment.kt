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
import com.example.utc2datingapp.adapter.LikeAdapter
import com.example.utc2datingapp.databinding.FragmentDatingBinding
import com.example.utc2datingapp.model.MatchModel
import com.example.utc2datingapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.yuyakaido.android.cardstackview.*

class LikeFragment : Fragment(), CardStackListener {
    private lateinit var binding: FragmentDatingBinding
    private lateinit var manager: CardStackLayoutManager
    private lateinit var heartImageView: ImageView
    private lateinit var cancelImageView: ImageView
    private var currentPosition: Int = 0
    private var lastDirection: Direction? = null
    private var isRewinding: Boolean = false
    private lateinit var list: ArrayList<UserModel>
    private var leftSwipeCount: Int = 0 // Biến đếm số lần trượt sang trái
    private val MAX_LEFT_SWIPE_COUNT: Int = 3 // Số lần trượt sang trái tối đa

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

        // Xử lý sự kiện click cho ImageView "heart"
        binding.heart.setOnClickListener {
            Log.d("LikeFragment", "Heart clicked") // Log khi nhấp vào ImageView "heart"
            val direction = Direction.Right
            swipeCard(direction)
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
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber

        FirebaseDatabase.getInstance().getReference("/users/$currentUserPhoneNumber/likedBy")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        list = ArrayList()
                        for (data in snapshot.children) {
                            val model = data.getValue(UserModel::class.java)
                            model?.let {
                                list.add(it)
                            }
                        }

                        list.shuffle() // Shuffle the list of users

                        binding.cardStackView.itemAnimator = DefaultItemAnimator()
                        binding.cardStackView.adapter = LikeAdapter(requireContext(), list)

                        heartImageView = binding.heart
                        cancelImageView = binding.cancel

                        for (user in list) {
                            Log.d("LikeFragment", "User: $user")
                        }
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

                    // Update currentPosition and lastDirection
                    currentPosition = targetPosition
                    lastDirection = direction

                    // Get the user at the current position
                    val user = list[currentPosition]
                    // TODO: Do something with the user (e.g., show user information, create a match, etc.)
                    Log.d("LikeFragment", "Swiped user: $user")
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

                    // Update currentPosition and lastDirection
                    currentPosition = targetPosition
                    lastDirection = direction

                    // Get the user at the current position
                    val user = list[currentPosition]
                    // TODO: Do something with the user (e.g., show user information, create a match, etc.)
                    Log.d("LikeFragment", "Rewound user: $user")
                }
            }

            // Wait for animation to complete before allowing next swipe
            Handler().postDelayed({
                isCardAnimating = false
            }, 200)
        }
    }

    private fun createMatch(matchedUser: UserModel) {
        val currentUserPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber

        val matchesRef = FirebaseDatabase.getInstance().getReference("/matches")
        val matchId = matchesRef.push().key

        if (matchId != null && currentUserPhoneNumber != null) {
            val match = MatchModel(
                matchId = matchId,
                currentUserPhoneNumber = currentUserPhoneNumber,
                matchedUserPhoneNumber = matchedUser.number,
                matchedUserImage = matchedUser.image,
                matchedUserName = matchedUser.name,
                matchedUserCity = matchedUser.city)
            matchesRef.child(matchId).setValue(match)
                .addOnSuccessListener {
                    // Match created successfully
                    // Perform any additional actions if needed
                }
                .addOnFailureListener { exception ->
                    // Failed to create match
                    // Handle the error accordingly
                }
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
        Log.d("LikeFragment", "Card swiped: $direction")
        if (currentPosition == list.size) {
            Toast.makeText(requireContext(), "This is the last card", Toast.LENGTH_SHORT).show()
        } else {
            val scaleUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            val scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)

            val animationSet = AnimationSet(true)
            animationSet.addAnimation(scaleUpAnimation)
            animationSet.addAnimation(scaleDownAnimation)

            val targetPosition = currentPosition

            when (direction) {
                Direction.Right -> {
                    heartImageView.startAnimation(animationSet)
                    currentPosition++
                    createMatch(list[targetPosition])
                }
                Direction.Left -> {
                    cancelImageView.startAnimation(animationSet)
                    currentPosition++
                    if (currentPosition < 0) {
                        currentPosition = 0
                    }
                }
                else -> {
                    // Handle other directions if needed
                }
            }

            // Scroll the card stack to the next position
            manager.scrollToPosition(currentPosition)

            // Reset the scale of the image views
            heartImageView.scaleX = 1.0f
            heartImageView.scaleY = 1.0f
            cancelImageView.scaleX = 1.0f
            cancelImageView.scaleY = 1.0f

            lastDirection = direction
        }
    }

    override fun onCardRewound() {
        Log.d("LikeFragment", "Card rewound")
    }

    override fun onCardCanceled() {
        Log.d("LikeFragment", "Card canceled")
    }

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}
