package com.example.utc2datingapp.model


data class UserModel(

    val number : String? ="",
    val name : String? ="",
    val email: String? ="",
    val city : String? ="",
    val job : String? ="",
    val gender : String? ="",
    val relationship : String? ="",
    val star : String? ="",
    val image : String? ="",
    val age : String? ="",
    val status : String? ="",
    val birthday : String? ="",
    val latitude: Double? = 0.0, // Vị trí latitude
    val longitude: Double? = 0.0, // Vị trí longitude

)

