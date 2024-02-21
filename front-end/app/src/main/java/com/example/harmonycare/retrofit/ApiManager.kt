package com.example.harmonycare.retrofit

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.harmonycare.data.Checklist
import com.example.harmonycare.data.Comment
import com.example.harmonycare.data.Post
import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.harmonycare.data.Record
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/login")
    fun loginUser(@Body requestBody: RequestBody): Call<ApiResponse>

    @POST("/api/v1/baby")
    fun addBaby(
        @Header("Authorization") accessToken: String,
        @Body requestBody: RequestBody
    ): Call<Void>

    @POST("/api/v1/record")
    fun saveRecord(
        @Header("Authorization") authToken: String,
        @Body requestBody: RecordSaveRequest
    ): Call<SaveRecordResponse>

    @GET("/api/v1/record")
    fun getRecordsForDay(
        @Query("day") day: String,
        @Query("range") range: Int,
        @Header("Authorization") authToken: String
    ): Call<RecordGetResponse>

    @PUT("/api/v1/record/{recordId}")
    fun updateRecord(
        @Path("recordId") recordId: Int,
        @Header("Authorization") authToken: String,
        @Body requestBody: RecordSaveRequest
    ): Call<SaveRecordResponse>

    @DELETE("/api/v1/record/{recordId}")
    fun deleteRecord(
        @Path("recordId") recordId: Int,
        @Header("Authorization") authToken: String
    ): Call<RecordDeleteResponse>

    @GET("/api/v1/checklist/me")
    fun getChecklist(
        @Header("Authorization") authToken: String
    ): Call<ApiSuccessResultListChecklistReadResponse>

    @POST("/api/v1/checklist")
    fun saveChecklist(
        @Header("Authorization") authToken: String,
        @Body requestBody: ChecklistSaveRequest
    ): Call<SaveRecordResponse>

    @DELETE("/api/v1/checklist/{checklistId}")
    fun deleteChecklist(
        @Path("checklistId") checklistId: Int,
        @Header("Authorization") authToken: String
    ): Call<RecordDeleteResponse>

    @PUT("/api/v1/checklist/check/{checklistId}")
    fun toggleChecklist(
        @Path("checklistId") checklistId: Int,
        @Header("Authorization") authToken: String
    ): Call<ApiSuccessResultBoolean>

    @PUT("/api/v1/checklist/{checklistId}")
    fun updateChecklist(
        @Path("checklistId") checklistId: Int,
        @Header("Authorization") authToken: String,
        @Body requestBody: ChecklistSaveRequest
    ): Call<SaveRecordResponse>

    @GET("/api/v1/community")
    fun getCommunity(
        @Header("Authorization") authToken: String
    ): Call<ApiSuccessResultListCommunityReadResponse>

    @POST("/api/v1/community")
    fun saveCommunity(
        @Header("Authorization") authToken: String,
        @Body requestBody: CommunitySaveRequest
    ): Call<SaveRecordResponse>

    @POST("/api/v1/comment")
    fun saveComment(
        @Header("Authorization") authToken: String,
        @Body requestBody: CommentSaveRequest
    ): Call<SaveRecordResponse>

    @GET("/api/v1/comment/{communityId}")
    fun getComment(
        @Path("communityId") communityId: Int,
        @Header("Authorization") authToken: String
    ): Call<ApiSuccessResultListCommentReadResponse>

    @DELETE("/api/v1/comment/{commentId}")
    fun deleteComment(
        @Path("commentId") commentId: Int,
        @Header("Authorization") authToken: String
    ): Call<RecordDeleteResponse>

    @GET("/api/v1/member/profiles")
    fun getProfile(
        @Header("Authorization") authToken: String
    ): Call<ApiSuccessResultProfileReadResponse>

    @GET("/api/v1/community/me")
    fun getMyCommunity(
        @Header("Authorization") authToken: String
    ): Call<ApiSuccessResultListCommunityReadResponse>

    @DELETE("/api/v1/community/{communityId}")
    fun deleteCommunity(
        @Path("communityId") communityId: Int,
        @Header("Authorization") authToken: String
    ): Call<RecordDeleteResponse>

    @GET("/api/v1/checklist/tip")
    fun getTip(
        @Header("Authorization") authToken: String
    ): Call<TipResponse>
}

class ApiManager(private val apiService: ApiService) {

    fun loginUser(authCode: String, onResponse: (accessToken: String, hasBaby: Boolean) -> Unit, onFailure: () -> Unit) {
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), "{\"authcode\": \"$authCode\"}")
        val call = apiService.loginUser(requestBody)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()?.response
                    val accessToken = responseData?.token?.accessToken
                    val hasBaby = responseData?.hasBaby ?: false
                    if (accessToken != null) {
                        onResponse(accessToken, hasBaby)
                        return
                    }
                }
                onFailure()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onFailure()
            }
        })
    }

    fun saveRecord(
        accessToken: String,
        recordTask: String,
        startTime: String,
        endTime: String,
        description: String,
        onResponse: (Int) -> Unit,
        onFailure: () -> Unit
    ) {
        val recordRequest = RecordSaveRequest(recordTask, startTime, endTime, description)
        val call = apiService.saveRecord("Bearer $accessToken", recordRequest)
        call.enqueue(object : Callback<SaveRecordResponse> {
            override fun onResponse(call: Call<SaveRecordResponse>, response: Response<SaveRecordResponse>) {
                if (response.isSuccessful) {
                    val saveRecordResponse = response.body()
                    if (saveRecordResponse != null) {
                        onResponse(saveRecordResponse.status)
                        return
                    }
                }
                onFailure()
            }

            override fun onFailure(call: Call<SaveRecordResponse>, t: Throwable) {
                onFailure()
            }
        })
    }

    fun getRecordsForDay(day: String, range: Int, authToken: String, onResponse: (List<Record>?) -> Unit, onFailure: (Throwable) -> Unit) {
        val call = apiService.getRecordsForDay(day, range, authToken)
        call.enqueue(object : Callback<RecordGetResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<RecordGetResponse>,
                response: Response<RecordGetResponse>
            ) {
                if (response.isSuccessful) {
                    val recordGetResponse: RecordGetResponse? = response.body()

                    onResponse(recordGetResponse?.response?.map { recordRequest ->
                        Record(
                            recordId = recordRequest.recordId,
                            recordTask = recordRequest.recordTask,
                            startTime = LocalDateTime.parse(recordRequest.startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            endTime = LocalDateTime.parse(recordRequest.endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            description = recordRequest.description
                        )
                    })
                } else {
                    onFailure(Throwable("Unsuccessful response: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<RecordGetResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun updateRecord(
        recordId: Int,
        recordTask: String,
        startTime: String,
        endTime: String,
        description: String,
        accessToken: String,
        onResponse: (Boolean) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val recordRequest = RecordSaveRequest(recordTask, startTime, endTime, description)
        val call = apiService.updateRecord(recordId, "Bearer $accessToken", recordRequest)
        call.enqueue(object : Callback<SaveRecordResponse> {
            override fun onResponse(
                call: Call<SaveRecordResponse>,
                response: Response<SaveRecordResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                } else {
                    onFailure(Throwable("Failed to update record: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<SaveRecordResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun deleteRecord(
        recordId: Int, accessToken: String, onResponse: (Boolean) -> Unit
    ) {
        val call = apiService.deleteRecord(recordId, "Bearer $accessToken")
        call.enqueue(object : Callback<RecordDeleteResponse> {
            override fun onResponse(
                call: Call<RecordDeleteResponse>,
                response: Response<RecordDeleteResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<RecordDeleteResponse>, t: Throwable) {
                onResponse(false)
            }

        })
    }

    fun getChecklist(accessToken: String, checklistData: (List<Checklist>) -> Unit) {
        val call = apiService.getChecklist("Bearer $accessToken")
        call.enqueue(object: Callback<ApiSuccessResultListChecklistReadResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<ApiSuccessResultListChecklistReadResponse>,
                response: Response<ApiSuccessResultListChecklistReadResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData: ApiSuccessResultListChecklistReadResponse? = response.body()

                    responseData?.response?.map { checklist ->
                        Checklist(
                            checklistId = checklist.checklistId,
                            title = checklist.title,
                            days = checklist.days,
                            checkTime = LocalDateTime.parse(checklist.checkTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            isCheck = checklist.isCheck
                        )
                    }?.let { checklistData(it) }
                }
            }

            override fun onFailure(
                call: Call<ApiSuccessResultListChecklistReadResponse>,
                t: Throwable
            ) {
                Log.d("kkang", "checklist read failed")
            }

        })
    }

    fun saveChecklist(accessToken: String, title: String, days: List<String>, checkTime: String, onResponse: (Boolean) -> Unit) {
        val requestBody = ChecklistSaveRequest(title, days, checkTime)
        val call = apiService.saveChecklist("Bearer $accessToken", requestBody)
        call.enqueue(object: Callback<SaveRecordResponse> {
            override fun onResponse(
                call: Call<SaveRecordResponse>,
                response: Response<SaveRecordResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<SaveRecordResponse>, t: Throwable) {
                Log.d("kkang", "save checklist failed")
            }

        })
    }

    fun deleteChecklist(checklistId: Int, accessToken: String, onResponse: (Boolean) -> Unit) {
        val call = apiService.deleteChecklist(checklistId, "Bearer $accessToken")
        call.enqueue(object: Callback<RecordDeleteResponse> {
            override fun onResponse(
                call: Call<RecordDeleteResponse>,
                response: Response<RecordDeleteResponse>
            ) {
                if (response.isSuccessful) onResponse(true)
            }

            override fun onFailure(call: Call<RecordDeleteResponse>, t: Throwable) {
                Log.d("kkang", "delete checklist failed")
            }

        })
    }

    fun toggleChecklist(checklistId: Int, accessToken: String, onResponse: (Boolean) -> Unit) {
        val call = apiService.toggleChecklist(checklistId, "Bearer $accessToken")
        call.enqueue(object: Callback<ApiSuccessResultBoolean> {
            override fun onResponse(
                call: Call<ApiSuccessResultBoolean>,
                response: Response<ApiSuccessResultBoolean>
            ) {
                if (response.isSuccessful) onResponse(true)
            }

            override fun onFailure(call: Call<ApiSuccessResultBoolean>, t: Throwable) {
                Log.d("kkang", "toggle checklist failed")
            }

        })
    }

    fun updateChecklist(
        checklistId: Int,
        accessToken: String,
        title: String,
        days: List<String>,
        checkTime: String,
        onResponse: (Boolean) -> Unit) {
        val requestBody = ChecklistSaveRequest(title, days, checkTime)
        val call = apiService.updateChecklist(checklistId, "Bearer $accessToken", requestBody)
        call.enqueue(object: Callback<SaveRecordResponse> {
            override fun onResponse(
                call: Call<SaveRecordResponse>,
                response: Response<SaveRecordResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<SaveRecordResponse>, t: Throwable) {
                Log.d("kkang", "update checklist failed")
            }

        })
    }

    fun getCommunity(accessToken: String, communityData: (List<Post>) -> Unit) {
        val call = apiService.getCommunity("Bearer $accessToken")
        call.enqueue(object: Callback<ApiSuccessResultListCommunityReadResponse> {
            override fun onResponse(
                call: Call<ApiSuccessResultListCommunityReadResponse>,
                response: Response<ApiSuccessResultListCommunityReadResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData: ApiSuccessResultListCommunityReadResponse? = response.body()
                    val postList: List<Post>? = responseData?.response
                    if (postList != null) {
                        communityData(postList)
                    }
                }
            }

            override fun onFailure(
                call: Call<ApiSuccessResultListCommunityReadResponse>,
                t: Throwable
            ) {
                Log.d("kkang", "get community failed")
            }

        })
    }

    fun saveCommunity(accessToken: String, title: String, content: String, onResponse: (Boolean) -> Unit) {
        val requestBody = CommunitySaveRequest(title, content)
        val call = apiService.saveCommunity("Bearer $accessToken", requestBody)
        call.enqueue(object: Callback<SaveRecordResponse> {
            override fun onResponse(
                call: Call<SaveRecordResponse>,
                response: Response<SaveRecordResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<SaveRecordResponse>, t: Throwable) {
                Log.d("kkang", "save community failed")
            }

        })
    }

    fun saveComment(accessToken: String, communityId: Int, content: String, onResponse: (Boolean) -> Unit) {
        val requestBody = CommentSaveRequest(communityId, content)
        val call = apiService.saveComment("Bearer $accessToken", requestBody)
        call.enqueue(object : Callback<SaveRecordResponse> {
            override fun onResponse(
                call: Call<SaveRecordResponse>,
                response: Response<SaveRecordResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<SaveRecordResponse>, t: Throwable) {
                Log.d("kkang", "save comment failed")
            }
        })
    }

    fun getComment(accessToken: String, communityId: Int, commentData: (List<Comment>) -> Unit) {
        val call = apiService.getComment(communityId, "Bearer $accessToken")
        call.enqueue(object: Callback<ApiSuccessResultListCommentReadResponse> {
            override fun onResponse(
                call: Call<ApiSuccessResultListCommentReadResponse>,
                response: Response<ApiSuccessResultListCommentReadResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData: ApiSuccessResultListCommentReadResponse? = response.body()
                    val commentList: List<Comment>? = responseData?.response
                    if (commentList != null) {
                        commentData(commentList)
                    }
                }
            }

            override fun onFailure(
                call: Call<ApiSuccessResultListCommentReadResponse>,
                t: Throwable
            ) {
                Log.d("kkang", "get comment failed")
            }

        })
    }

    fun deleteComment(accessToken: String, commentId: Int, onResponse: (Boolean) -> Unit) {
        val call = apiService.deleteComment(commentId, "Bearer $accessToken")
        call.enqueue(object: Callback<RecordDeleteResponse> {
            override fun onResponse(
                call: Call<RecordDeleteResponse>,
                response: Response<RecordDeleteResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<RecordDeleteResponse>, t: Throwable) {
                Log.d("kkang", "delete comment failed")
            }

        })
    }

    fun getProfile(accessToken: String, onResponse: (ProfileReadResponse) -> Unit) {
        val call = apiService.getProfile("Bearer $accessToken")
        call.enqueue(object: Callback<ApiSuccessResultProfileReadResponse> {
            override fun onResponse(
                call: Call<ApiSuccessResultProfileReadResponse>,
                response: Response<ApiSuccessResultProfileReadResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData: ApiSuccessResultProfileReadResponse? = response.body()
                    val profileResponse: ProfileReadResponse? = responseData?.response
                    if (profileResponse != null) {
                        onResponse(profileResponse)
                    }
                }
            }

            override fun onFailure(call: Call<ApiSuccessResultProfileReadResponse>, t: Throwable) {
                Log.d("kkang", "get profile failed")
            }

        })
    }

    fun getMyCommunity(accessToken: String, myCommunityData: (List<Post>) -> Unit) {
        val call = apiService.getMyCommunity("Bearer $accessToken")
        call.enqueue(object: Callback<ApiSuccessResultListCommunityReadResponse> {
            override fun onResponse(
                call: Call<ApiSuccessResultListCommunityReadResponse>,
                response: Response<ApiSuccessResultListCommunityReadResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData: ApiSuccessResultListCommunityReadResponse? = response.body()
                    val postList: List<Post>? = responseData?.response
                    if (postList != null) {
                        myCommunityData(postList)
                    }
                }
            }

            override fun onFailure(
                call: Call<ApiSuccessResultListCommunityReadResponse>,
                t: Throwable
            ) {
                Log.d("kkang", "get my post failed")
            }
        })
    }

    fun deleteCommunity(accessToken: String, communityId: Int, onResponse: (Boolean) -> Unit) {
        val callDeleteCommunity = apiService.deleteCommunity(communityId, "Bearer $accessToken")
        callDeleteCommunity.enqueue(object: Callback<RecordDeleteResponse> {
            override fun onResponse(
                call: Call<RecordDeleteResponse>,
                response: Response<RecordDeleteResponse>
            ) {
                if (response.isSuccessful) {
                    onResponse(true)
                }
            }

            override fun onFailure(call: Call<RecordDeleteResponse>, t: Throwable) {
                Log.d("kkang", "delete my post failed")
            }

        })
    }

    fun deleteAllComment(accessToken: String, communityId: Int, onResponse: (Boolean) -> Unit) {
        getComment(accessToken, communityId) { comment ->
            if (comment.isNullOrEmpty()) {
                onResponse(true)
                return@getComment
            }
            val commentIds = comment.map { it.commentId }

            var isSuccess = true // 모든 댓글이 성공적으로 삭제되었는지 여부를 추적하는 변수

            for (commentId in commentIds) {
                deleteComment(accessToken, commentId) { isDeleted ->
                    if (!isDeleted) {
                        // 댓글 삭제에 실패하면 isSuccess를 false로 설정하고 반복을 종료합니다.
                        isSuccess = false
                        return@deleteComment
                    }
                }
            }

            // 모든 댓글 삭제가 완료되면 isSuccess 값을 onResponse 콜백으로 전달합니다.
            onResponse(isSuccess)
        }
    }

    fun getTip(accessToken: String, onResponse: (String) -> Unit) {
        val call = apiService.getTip("Bearer $accessToken")
        call.enqueue(object: Callback<TipResponse> {
            override fun onResponse(call: Call<TipResponse>, response: Response<TipResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onResponse(it.response) }
                }
            }

            override fun onFailure(call: Call<TipResponse>, t: Throwable) {
            }

        })
    }
}

// API 응답을 모델링할 클래스 정의
data class ApiResponse(
    val status: Int,
    val response: ResponseData
)

data class ResponseData(
    val hasBaby: Boolean,
    val token: TokenData
)

data class TokenData(
    val grantType: String,
    @SerializedName("accessToken") val accessToken: String
)

// 기록 저장하기
data class SaveRecordResponse(
    val status: Int,
    val response: Int
)

data class RecordSaveRequest(
    val recordTask: String,
    val startTime: String,
    val endTime: String,
    val description: String
)

// 기록 불러오기
data class RecordGetRequest(
    val recordId: Int,
    val recordTask: String,
    val startTime: String,
    val endTime: String,
    val description: String
)

data class RecordGetResponse(
    val status: Int,
    val response: List<RecordGetRequest>
)

data class RecordDeleteResponse(
    val status: Int
)

// 체크리스트 조회하기
data class ApiSuccessResultListChecklistReadResponse(
    val status: Int,
    val response: List<ChecklistReadResponse>
)

data class ChecklistReadResponse(
    val checklistId: Int,
    val title: String,
    val days: List<String>,
    val checkTime: String,
    val isCheck: Boolean
)

// 체크리스트 저장하기
data class ChecklistSaveRequest(
    val title: String,
    val days: List<String>,
    val checkTime: String
)

// 체크리스트 체크 토글
data class ApiSuccessResultBoolean(
    val status: Int,
    val response: Boolean
)

// 커뮤니티 글 조회
data class ApiSuccessResultListCommunityReadResponse(
    val status: Int,
    val response: List<Post>
)

// 커뮤니티 글 작성
data class CommunitySaveRequest(
    val title: String,
    val content: String
)

// 커뮤니티 댓글 작성
data class CommentSaveRequest(
    val communityId: Int,
    val content: String
)

// 커뮤니티 댓글 읽기
data class ApiSuccessResultListCommentReadResponse(
    val status: Int,
    val response: List<Comment>
)

// 프로필 불러오기
data class ApiSuccessResultProfileReadResponse(
    val status: Int,
    val response: ProfileReadResponse
)

data class ProfileReadResponse(
    val parentName: String,
    val email: String,
    val babyName: String,
    val babyBirthDate: String
)

data class TipResponse(
    val status: Int,
    val response: String
)