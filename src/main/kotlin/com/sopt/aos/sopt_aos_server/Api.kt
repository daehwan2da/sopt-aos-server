package com.sopt.aos.sopt_aos_server

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
class Api(
    private val logic: Logic,
    @Qualifier("s3C") private val s3Client: AmazonS3Client
) {

    @PostMapping("/upload")
    fun upload(
        file: MultipartFile
    ): ResponseEntity<Response<UploadResponse>>? {
        val uuid = UUID.randomUUID().toString()
        try {
            s3Client.putObject(
                "my-daehwan-bucket",
                uuid,
                file.inputStream,
                ObjectMetadata().apply {
                    contentType = file.contentType;
                    contentLength = file.size
                })
        } catch (t: Throwable) {
            throw UploadEx()
        }
        return ResponseEntity.ok(
            Response.success(
                "업로드 성공",
                UploadResponse(imageUrl = "https://my-daehwan-bucket.s3.ap-northeast-2.amazonaws.com/${uuid}")
            )
        );
    }

    fun imageUpload(file: MultipartFile): String {
        val uuid = UUID.randomUUID().toString()

        s3Client.putObject(
            "my-daehwan-bucket",
            uuid,
            file.inputStream,
            ObjectMetadata().apply {
                contentType = file.contentType;
                contentLength = file.size
            })

        return "https://my-daehwan-bucket.s3.ap-northeast-2.amazonaws.com/${uuid}"
    }

    @PostMapping("/music")
    fun register(
        @RequestHeader("id") id: String,
        @RequestParam image: MultipartFile,
        @RequestParam title: String,
        @RequestParam singer: String
    ): ResponseEntity<Response<MusicResponse>> {
        try {
            // upload Image
            val result = logic.saveMusic(title = title, singer = singer, url = imageUpload(image), userId = id)

            return ResponseEntity.ok(
                Response.success(message = "회원가입에 성공했습니다.", data = MusicResponse.from(result))
            )
        } catch (t: Throwable) {
            throw UploadEx()
        }
    }

    @GetMapping("/{id}/music")
    fun getList(
        @PathVariable("id") id: String
    ): ResponseEntity<Response<MusicsResponse>> {
        val musics: List<Music> = logic.findMusic(id)

        return ResponseEntity.ok(
            Response.success(message = "음악 리스트 조회", data = MusicsResponse.from(musics))
        )
    }

    @GetMapping("/readiness")
    fun healthCheck(): Response<Nothing?> {
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
    fun handleDataException(e: DataIntegrityViolationException): ResponseEntity<Response<Nothing>> =
        ResponseEntity.status(409).body(Response.error(message = "중복된 리소스가 발생했습니다."))

    @ExceptionHandler(UploadEx::class)
    fun handleUpload(e: UploadEx): ResponseEntity<Response<Nothing>> =
        ResponseEntity.status(500).body(Response.error(message = "이미지 업로드 중 오류가 발셍했습니다."))


    class UploadEx : RuntimeException() {

    }
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

data class MusicResponse(
    val title: String,
    val singer: String,
    val url: String
) {
    companion object {
        fun from(music: Music) = MusicResponse(title = music.title, singer = music.singer, url = music.url)
    }
}

data class MusicsResponse(
    val musicList: List<MusicResponse>
) {
    companion object {
        fun from(musics: List<Music>) = MusicsResponse(musics.map { MusicResponse.from(it) })
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

data class UploadResponse(
    val imageUrl: String
)

@ControllerAdvice
class advice {
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleUploadOver(e: MaxUploadSizeExceededException): ResponseEntity<Response<Nothing>> =
        ResponseEntity.status(400).body(Response.error(message = "100 KB 이하의 이미지를 사용하세요."))

}