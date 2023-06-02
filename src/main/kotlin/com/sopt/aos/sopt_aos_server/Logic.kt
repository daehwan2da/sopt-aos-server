package com.sopt.aos.sopt_aos_server

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class Logic(
    private val userRepository: UserRepository,
    private val musicRepository: MusicRepository
) {

    @Transactional
    fun saveUser(id: String, password: String, name: String, skill: String?): User {
        val user = User(nickname = id, password = password, name = name, skill = skill)
        return userRepository.save(user)
    }

    fun search(id: String, password: String? = null): User {
        val foundUser =
            userRepository.findUserByNickname(id) ?: throw IllegalArgumentException("유효하지 않은 아이디 로 접근하였습니다.")
        if (!password.isNullOrBlank() && foundUser.password != password) throw IllegalArgumentException("비밀번호가 틀렸습니다.")
        return foundUser
    }

    @Transactional
    fun saveMusic(title: String, singer: String, url: String, userId: String): Music {
        return musicRepository.save(Music(title = title, singer = singer, url = url, userId = userId))
    }

    fun findMusic(userId: String): List<Music> {
        return musicRepository.findAllByUserId(userId)
    }
}

interface UserRepository : JpaRepository<User, Long> {
    fun findUserByNickname(id: String): User?
}

interface MusicRepository : JpaRepository<Music, Long> {
    fun findAllByUserId(userId: String): List<Music>
}