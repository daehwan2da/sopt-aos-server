package com.sopt.aos.sopt_aos_server

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*

@RestController
class Api(
    private val logic: Logic
) {

    @GetMapping("/readiness")
    fun healthCheck() : Response<Nothing?> {
        return Response.success(message = "서버 통신에 성공했습니다.", data = null)
    }

    @PostMapping("/sign-up")
    fun SignUp(
        @RequestBody signUpRequest: SignUpRequest
    ): ResponseEntity<Response<SignUpResponse>> {

        if (signUpRequest.id.isBlank()) throw IllegalArgumentException("아이디가 정상적으로 입력되지 않았습니다.")
        if (signUpRequest.password.isBlank()) throw IllegalArgumentException("패스워드가 정상적으로 입력되지 않았습니다.")
        if (signUpRequest.name.isBlank()) throw IllegalArgumentException("이름이 정상적으로 입력되지 않았습니다.")

        val registeredUser = logic.saveUser(
            id = signUpRequest.id,
            password = signUpRequest.password,
            name = signUpRequest.name,
            skill = signUpRequest.skill
        )
        return ResponseEntity.ok(
            Response.success(message = "회원가입에 성공했습니다.", data = SignUpResponse.from(registeredUser))
        )
    }

    @GetMapping("/info/{id}")
    fun getInfo(
        @PathVariable(name = "id") id: String
    ): ResponseEntity<Response<InfoResponse>> {
        val foundUser = logic.search(id)

        return ResponseEntity.ok(
            Response.success(
                message = "정보가 정상적으로 조회되었습니다.",
                data = InfoResponse(
                    id = foundUser.nickname,
                    name = foundUser.name,
                    skill = foundUser.skill
                )
            )
        )
    }

    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody signInRequest: SignInRequest
    ): ResponseEntity<Response<InfoResponse>> {
        if (signInRequest.id.isBlank()) throw IllegalArgumentException("아이디가 정상적으로 입력되지 않았습니다.")
        if (signInRequest.password.isBlank()) throw IllegalArgumentException("패스워드가 정상적으로 입력되지 않았습니다.")

        val foundUser = logic.search(signInRequest.id, signInRequest.password)

        return ResponseEntity.ok(
            Response.success(
                message = "정보가 정상적으로 조회되었습니다.",
                data = InfoResponse(
                    id = foundUser.nickname,
                    name = foundUser.name,
                    skill = foundUser.skill
                )
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<Response<Nothing>> =
        ResponseEntity.status(400).body(Response.error(message = e.message ?: "잘못된 접급입니다."))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidException(e: IllegalArgumentException): ResponseEntity<Response<Nothing>> =
        ResponseEntity.status(400).body(Response.error(message = e.message ?: "잘못된 접급입니다."))

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataException(e: DataIntegrityViolationException) : ResponseEntity<Response<Nothing>> =
        ResponseEntity.status(409).body(Response.error(message = "중복된 아이디로 가입을 시도했습니다."))
}


@JsonInclude(JsonInclude.Include.NON_NULL)
data class Response<T>(
    val status: Int,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> success(message: String, data: T) = Response(status = 200, message = message, data = data)
        fun error(message: String) = Response(status = 400, message = message, data = null)
    }
}


@JsonInclude(JsonInclude.Include.NON_NULL)
data class SignUpResponse(
    val name: String,
    val skill: String?
) {
    companion object {
        fun from(user: User) = SignUpResponse(name = user.name, skill = user.skill)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InfoResponse(
    val id: String,
    val name: String?,
    val skill: String?
)

data class SignUpRequest(
    val id: String,
    val password: String,
    val name: String,
    val skill: String?
)

data class SignInRequest(
    val id: String,
    val password: String
)