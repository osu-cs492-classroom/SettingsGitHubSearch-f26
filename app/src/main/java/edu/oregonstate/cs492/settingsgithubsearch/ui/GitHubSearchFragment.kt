package edu.oregonstate.cs492.settingsgithubsearch.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import edu.oregonstate.cs492.settingsgithubsearch.R
import edu.oregonstate.cs492.settingsgithubsearch.data.LoadingStatus

class GitHubSearchFragment : Fragment(R.layout.fragment_github_search) {
    private val viewModel: GitHubSearchViewModel by viewModels()

    private val repoListAdapter = GitHubRepoListAdapter()

    private lateinit var searchResultsListRV: RecyclerView
    private lateinit var searchErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchBoxET: EditText = view.findViewById(R.id.et_search_box)
        val searchBtn: Button = view.findViewById(R.id.btn_search)

        searchErrorTV = view.findViewById(R.id.tv_search_error)
        loadingIndicator = view.findViewById(R.id.loading_indicator)

        searchResultsListRV = view.findViewById(R.id.rv_search_results)
        searchResultsListRV.layoutManager = LinearLayoutManager(requireContext())
        searchResultsListRV.setHasFixedSize(true)

        searchResultsListRV.adapter = repoListAdapter

        viewModel.searchResults.observe(viewLifecycleOwner) { searchResult ->
            repoListAdapter.updateRepoList(searchResult)
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner) { loadingStatus ->
            loadingIndicator.visibility = View.INVISIBLE
            searchResultsListRV.visibility = View.INVISIBLE
            searchErrorTV.visibility = View.INVISIBLE
            when (loadingStatus) {
                LoadingStatus.SUCCESS -> {
                    searchResultsListRV.visibility = View.VISIBLE
                }
                LoadingStatus.LOADING -> {
                    loadingIndicator.visibility = View.VISIBLE
                }

                LoadingStatus.ERROR -> {
                    searchErrorTV.visibility = View.VISIBLE
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                searchErrorTV.text = getString(
                    R.string.search_error,
                    error
                )
            }
        }

        searchBtn.setOnClickListener {
            val query = searchBoxET.text.toString()
            if (!TextUtils.isEmpty(query)) {
                viewModel.loadRepositorySearch(query)
                searchResultsListRV.scrollToPosition(0)
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.github_search_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_settings -> {
                            Log.d("GitHubSearchFragment", "Settings action selected")
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.STARTED
        )
    }
}