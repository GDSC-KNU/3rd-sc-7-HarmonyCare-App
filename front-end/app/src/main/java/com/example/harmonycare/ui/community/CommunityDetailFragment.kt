package com.example.harmonycare.ui.community

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harmonycare.R
import com.example.harmonycare.data.Comment
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.FragmentCommunityDetailBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView

class CommunityDetailFragment : Fragment() {

    private lateinit var binding: FragmentCommunityDetailBinding
    private lateinit var adapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val communityId = arguments?.getInt("communityId")
        val title = arguments?.getString("title")
        val content = arguments?.getString("content")

        binding.textTitle.text = title
        binding.textContent.text = content

        if (communityId != null) {
            getDataListAndSetAdapter(communityId)

            binding.buttonSend.setOnClickListener {
                val comment = binding.editTextComment.text.toString()
                if (comment.isNotBlank()) {
                    val accessToken = SharedPreferencesManager.getAccessToken()

                    if (!accessToken.isNullOrEmpty()) {
                        val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                        val apiManager = ApiManager(apiService)

                        apiManager.saveComment(accessToken, communityId, comment, onResponse = {
                            if (it == true) {
                                getDataListAndSetAdapter(communityId)
                            }
                        })
                    }

                    binding.editTextComment.setText("")
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.editTextComment.windowToken, 0) // 키보드를 숨깁니다.
                    binding.editTextComment.clearFocus()
                } else {
                    makeToast(requireContext(), "please input comment")
                }
            }
        }

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.GONE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 프래그먼트를 종료
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun getDataList(communityId: Int, onDataLoaded: (List<Comment>) -> Unit) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)


            apiManager.getComment(accessToken, communityId,
                { commentData ->
                    onDataLoaded(commentData)
                }
            )
        }
    }

    private fun getDataListAndSetAdapter(communityId: Int) {
        getDataList(communityId) { commentData ->
            adapter = CommentAdapter(commentData, onDeleteClick = { comment ->
                showDeleteConfirmationDialog(requireContext()) {
                    deleteComment(communityId, comment)
                }
            })
            binding.recyclerViewComment.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewComment.adapter = adapter
        }
    }

    private fun showDeleteConfirmationDialog(context: Context, onDeleteConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { dialog, which ->
                onDeleteConfirmed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComment(communityId: Int, comment: Comment) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            apiManager.deleteComment(accessToken, comment.commentId, { response ->
                if (response == true) {
                    getDataListAndSetAdapter(communityId)
                } else {
                    makeToast(requireContext(), "Failed to delete comment")
                }
            })

        }
    }

    fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}