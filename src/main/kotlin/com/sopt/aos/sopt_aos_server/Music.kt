package com.sopt.aos.sopt_aos_server

import javax.persistence.*

@Table(name = "music", indexes = [Index(name = "idx__music_user", columnList = "userId")])
@Entity
data class Music(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val singer: String,

    @Column(nullable = false, unique = false)
    val url: String
)
