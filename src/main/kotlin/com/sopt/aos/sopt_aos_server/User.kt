package com.sopt.aos.sopt_aos_server

import javax.persistence.*

@Entity(name = "sopt")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val nickname: String,
    @Column(nullable = false)
    val password: String,

    var name: String,
    var skill: String? = null
)