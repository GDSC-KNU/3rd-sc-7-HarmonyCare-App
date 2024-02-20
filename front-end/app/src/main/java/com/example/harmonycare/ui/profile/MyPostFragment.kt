package com.example.harmonycare.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harmonycare.R
import com.example.harmonycare.data.Comment
import com.example.harmonycare.data.Post
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.FragmentCommunityBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.example.harmonycare.ui.community.CommunityFragmentDirections
import com.example.harmonycare.ui.community.PostAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

private var _binding: FragmentCommunityBinding? = null
private val binding get() = _binding!!
private lateinit var adapter: PostAdapter
class MyPostFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.visibility = View.GONE

        getDataListAndSetAdapter()

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 프래그먼트를 종료
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun getDataList(onDataLoaded: (List<Post>) -> Unit) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)


            apiManager.getMyCommunity(accessToken,
                { myCommunityData ->
                    val sortedData = myCommunityData.sortedByDescending { it.communityId }
                    onDataLoaded(sortedData)
                }
            )
        }
    }

    private fun getDataListAndSetAdapter() {
        getDataList { communityData ->
            adapter = PostAdapter(communityData, true,
                onItemClick = { post ->
                    val action = MyPostFragmentDirections.actionNavigationMypostToNavigationCommunityDetail(
                        communityId = post.communityId,
                        title = post.title,
                        content = post.content
                    )
                    findNavController().navigate(action)
                },
                onDeleteClick = { post ->
                    showDeleteConfirmationDialog(requireContext()) {
                        deleteCommunity(post)
                    }
                }
            )
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
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

    private fun deleteCommunity(post: Post) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            apiManager.deleteAllComment(accessToken, post.communityId, { responseComment ->
                if (responseComment == true) {
                    apiManager.deleteCommunity(accessToken, post.communityId, { response ->
                        if (response == true) {
                            getDataListAndSetAdapter()
                        } else {
                            makeToast(requireContext(), "Failed to delete community")
                        }
                    })
                }
            })


        }
    }

    fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}