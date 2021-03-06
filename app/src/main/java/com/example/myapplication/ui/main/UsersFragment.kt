package com.example.myapplication.ui.main

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.ItemAdapter
import com.example.myapplication.R
import com.example.myapplication.api.Client
import com.example.myapplication.api.Service
import com.example.myapplication.controller.Settings
import com.example.myapplication.model.Item
import com.example.myapplication.model.ItemResponse
import kotlinx.android.synthetic.main.user_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A placeholder fragment containing a simple view.
 */
class UsersFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var noConnection: TextView
    private lateinit var item: Item
    private lateinit var swipeContainer: SwipeRefreshLayout

    //initialize a shared pref object
    val settingsMain = Settings(activity?.applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val settingsM = Settings(activity!!.applicationContext)

        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root : View = inflater.inflate(R.layout.user_layout, container, false)

        // THIS IS WHERE THE MAGIC HAPPENS
        // recyclerview
        // text view
        // swiper

        noConnection = root.findViewById(R.id.noConnection)

        recyclerView = root.findViewById(R.id.userView)
        recyclerView.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.smoothScrollToPosition(0)

        swipeContainer = root.findViewById(R.id.swipeContainer)

        val textView: TextView = root.findViewById(R.id.section_label)
        val secondText: TextView = root.findViewById(R.id.noConnection)
        pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
            loadJSON()
        })
        return root
    }

    private fun loadJSON() {
        val location = settingsMain.getSearchLocationSetting()
        try {
            val locationUrl = "/search/users?q=location:Fullerton"
            //val locationUrl = "/search/users?q=location:$location"
            val Client = Client()
            val apiService = Client.getClient()!!.create(Service::class.java)
            val call: Call<ItemResponse> = apiService.getItems(locationUrl)
            call.enqueue(object : Callback<ItemResponse?> {
                override fun onResponse(
                    call: Call<ItemResponse?>?,
                    response: Response<ItemResponse?>
                ) {
                    val items: List<Item> = response.body()?.getItems()!!
                    recyclerView.adapter = ItemAdapter(activity!!.applicationContext, items)
                    recyclerView.smoothScrollToPosition(0)
                    swipeContainer.isRefreshing = false
                }

                override fun onFailure(call: Call<ItemResponse?>?, t: Throwable) {

                    Log.d("Sad face", t.toString())
                    noConnection.visibility = View.VISIBLE
                }
            })
        } catch (e: java.lang.Exception) {
            Log.d("Sad face", e.toString())
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        internal const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): UsersFragment {
            return UsersFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}