package com.example.harmonycare.ui.community

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harmonycare.R
import com.example.harmonycare.data.Post
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.CommunityDialogBinding
import com.example.harmonycare.databinding.FragmentCommunityBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataListAndSetAdapter()

        binding.fab.setOnClickListener {
            val fullDialog = Dialog(requireContext(), R.style.FullScreenDialog)
            val fullDialogBinding = CommunityDialogBinding.inflate(layoutInflater)
            fullDialog.setContentView(fullDialogBinding.root)

            fullDialogBinding.buttonClose.setOnClickListener {
                fullDialog.dismiss()
            }
            fullDialogBinding.buttonSave.setOnClickListener {
                val title = fullDialogBinding.editTitle.text.toString()
                val content = fullDialogBinding.editContent.text.toString()

                if (title.isBlank() or content.isBlank()) {
                    makeToast(requireContext(), "please input content")
                } else {
                    val accessToken = SharedPreferencesManager.getAccessToken()

                    if (!accessToken.isNullOrEmpty()) {
                        val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                        val apiManager = ApiManager(apiService)


                        apiManager.saveCommunity(accessToken, title, content, onResponse = {
                            if (it == true) {
                                getDataListAndSetAdapter()
                            } else {
                                makeToast(requireContext(), "community save failed")
                            }
                        })
                    }
                    fullDialog.dismiss()
                }
            }

            fullDialog.show()
        }

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기를 누르면 액티비티를 종료
            requireActivity().finish()
        }
    }

    private fun getDataList(onDataLoaded: (List<Post>) -> Unit) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)


            apiManager.getCommunity(accessToken,
                { communityData ->
                    val sortedData = communityData.sortedByDescending { it.communityId }
                    onDataLoaded(sortedData)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataListAndSetAdapter() {
        getDataList { communityData ->
            adapter = PostAdapter(communityData, false,
                onItemClick = { post ->
                    val action = CommunityFragmentDirections.actionCommunityDetail(
                        communityId = post.communityId,
                        title = post.title,
                        content = post.content
                    )
                    findNavController().navigate(action)
                },
                onDeleteClick = {

                }
            )
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}