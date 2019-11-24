package com.salmoukas.cerberus.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salmoukas.cerberus.Constants
import com.salmoukas.cerberus.R
import com.salmoukas.cerberus.ThisApplication
import com.salmoukas.cerberus.db.CheckConfig
import com.salmoukas.cerberus.db.CheckResult
import com.salmoukas.cerberus.util.TimeRange
import com.salmoukas.cerberus.util.TimeRangeWithCheckStatus
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.max

class StatusListFragment : Fragment() {

    private val listAdapter = StatusListAdapter()
    private var checkConfigs: List<CheckConfig> = emptyList()
    private var checkResults: List<CheckResult> = emptyList()
    private var refreshListTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate fragment
        val root = inflater.inflate(R.layout.fragment_status_list, container, false)

        // configure list view
        root.findViewById<RecyclerView>(R.id.status_list_recycler_view).let {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = listAdapter
        }

        // update routine
        val db = (activity!!.application as ThisApplication).db!!

        db.checkConfigDao().targetsLive()
            .observe(
                viewLifecycleOwner,
                Observer<List<CheckConfig>> { records ->
                    checkConfigs = records
                    Log.d("STATUS_LIST", "refresh list triggered by observable (CheckConfig)")
                    refreshListAdapterModel()
                })

        db.checkResultDao().latestLive(60 * 60 * 24)
            .observe(
                viewLifecycleOwner,
                Observer<List<CheckResult>> { records ->
                    checkResults = records
                    Log.d("STATUS_LIST", "refresh list triggered by observable (CheckResult)")
                    refreshListAdapterModel()
                })

        return root
    }

    override fun onResume() {
        super.onResume()

        refreshListTimer = Timer()
        refreshListTimer!!.scheduleAtFixedRate(
            timerTask {
                activity?.runOnUiThread {
                    Log.d("STATUS_LIST", "refresh list triggered by timer")
                    refreshListAdapterModel()
                }
            },
            1000 * 60,
            1000 * 60
        )
    }

    override fun onPause() {
        super.onPause()

        try {
            refreshListTimer?.cancel()
        } catch (e: IllegalStateException) {
        }
        refreshListTimer = null
    }

    private fun refreshListAdapterModel() {
        val now = System.currentTimeMillis() / 1000L
        listAdapter.adapterModel = StatusListAdapter.AdapterModel(
            range = TimeRange(now - 60 * 60 * 24, now),
            checks = checkConfigs.map { cit ->
                var nextBegin: Long? = null
                StatusListAdapter.AdapterModel.Check(
                    url = cit.url,
                    results = checkResults.filter { rit -> rit.configUid == cit.uid && !rit.skip }
                        .sortedBy { rit -> rit.timestampUtc }
                        .map { rit ->
                            val ourBegin = max(
                                nextBegin ?: rit.timestampUtc - 1 * 60 * Constants.INTERVAL_CHECK_CYCLE_MINUTES,
                                rit.timestampUtc - 2 * 60 * Constants.INTERVAL_CHECK_CYCLE_MINUTES
                            )
                            nextBegin = rit.timestampUtc
                            TimeRangeWithCheckStatus(
                                begin = ourBegin,
                                end = rit.timestampUtc,
                                ok = rit.succeeded
                            )
                        }
                )
            }
        )

    }
}
