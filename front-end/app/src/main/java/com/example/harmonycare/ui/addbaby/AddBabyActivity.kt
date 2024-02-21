package com.example.harmonycare.ui.addbaby

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.harmonycare.MainActivity
import com.example.harmonycare.R
import com.example.harmonycare.data.Baby
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddBabyActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextDOB: EditText
    private lateinit var buttonSubmit: Button


    private val apiService = RetrofitClient.createService(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {

        // SharedPreferences에서 authcode 가져오기
        val accessToken = SharedPreferencesManager.getAccessToken()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_baby)

        // XML 레이아웃에서 뷰 참조 가져오기
        editTextName = findViewById(R.id.editTextName)
        editTextDOB = findViewById(R.id.editTextDOB)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        // 확인 버튼 클릭 리스너 설정
        buttonSubmit.setOnClickListener {
            // 입력값 가져오기
            val name = editTextName.text.toString()
            val dob = editTextDOB.text.toString()

            // 입력값이 유효한지 검사
            if (name.isNotBlank() && dob.isNotBlank()) {
                val birthDate = dob // 생년월일을 그대로 사용

                // 아기 객체 생성
                val baby = Baby(name, "MALE", "$birthDate 00:00:00",13.0f)

                // 아기 추가 요청 보내기
                addBaby(accessToken, baby)
            } else {
                Toast.makeText(this, "Please insert your baby's name and birth.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun addBaby(accessToken: String?, baby: Baby) {
        // accessToken이 null이면 처리하지 않음
        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(this, "Access token is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        // 아기 데이터를 JSON 형식의 RequestBody로 변환
        val requestBody = createRequestBodyFromBaby(baby)

        // POST 요청 보내기
        apiService.addBaby("Bearer $accessToken", requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공적으로 추가되었음을 사용자에게 알림
                    Toast.makeText(this@AddBabyActivity, "아기가 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()

                    // MainActivity로 이동
                    val intent = Intent(this@AddBabyActivity, MainActivity::class.java)
                    startActivity(intent)

                    // 현재 Activity 종료
                    finish()
                } else {
                    // 실패한 경우에 대한 처리
                    Toast.makeText(this@AddBabyActivity, "아기 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 실패한 경우에 대한 처리
                Toast.makeText(this@AddBabyActivity, "아기 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // 아기 데이터를 JSON 형식의 RequestBody로 변환하는 함수
    private fun createRequestBodyFromBaby(baby: Baby): RequestBody {
        // 여기에 아기 데이터를 JSON 형식의 RequestBody로 변환하는 코드를 작성
        return RequestBody.create("application/json".toMediaTypeOrNull(), "{\"name\":\"${baby.name}\", \"gender\":\"${baby.gender}\", \"birthDate\":\"${baby.birthDate}\", \"birthWeight\":${baby.birthWeight}}")

    }
}

