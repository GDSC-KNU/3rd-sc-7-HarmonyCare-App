/*
package com.example.harmonycare.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.harmonycare.R
import com.example.harmonycare.databinding.FragmentLoginBinding
import com.example.harmonycare.login.BaseFragment
import com.example.harmonycare.databinding.FragmentLoginTestBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.example.harmonycare.login.DataBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class LoginTestFragment : BaseFragment(), DataBinding<FragmentLoginBinding> {
    override val binding by lazy { DataBinding.get<FragmentLoginBinding>(this, R.layout.fragment_login_test) }

    private val viewModel by activityViewModels<LoginTestActivity>()

    companion object {
        private const val GOOGLE_LOGIN = 1000
    }
    companion object {
        private const val GOOGLE_LOGIN = 1000
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            GOOGLE_LOGIN -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.result!!


                    updateViewModelWithGoogleAccount(account)

                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun onCreateOnSigninWithGoogle() {
        binding.setOnSigninWithGoogle {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope(PeopleServiceScopes.USER_BIRTHDAY_READ),
                    Scope(PeopleServiceScopes.USER_GENDER_READ)
                )
                .requestServerAuthCode(getString(R.string.google_client_id))
                .build()

            val client = GoogleSignIn.getClient(requireActivity(), gso)

            val signInIntent: Intent = client.signInIntent
            startActivityForResult(signInIntent, GOOGLE_LOGIN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onViewCreated(R.id.google)
    }
    private fun onCreateDataBinding() {

        binding.lifecycleOwner = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        onCreateDataBinding()
        onCreateOnSigninWithGoogle()
        return binding.root
    }

    private fun onCreateDataBinding() {
        binding.lifecycleOwner = this
    }

}*/
