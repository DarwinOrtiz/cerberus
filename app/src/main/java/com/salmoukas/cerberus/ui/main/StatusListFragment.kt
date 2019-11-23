package com.salmoukas.cerberus.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salmoukas.cerberus.R
import com.salmoukas.cerberus.ThisApplication
import com.salmoukas.cerberus.db.CheckConfig
import com.salmoukas.cerberus.db.CheckResult
import com.salmoukas.cerberus.util.TimeRange
import com.salmoukas.cerberus.util.TimeRangeWithCheckStatus

class StatusListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_status_list, container, false)

        // create list view
        val adapter = StatusListAdapter()
        root.findViewById<RecyclerView>(R.id.status_list_recycler_view).let {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
        }

        // update routine
        var checkConfigs: List<CheckConfig> = emptyList()
        var checkResults: List<CheckResult> = emptyList()

        var updateAdapterModel = {
            val now = System.currentTimeMillis() / 1000L
            adapter.adapterModel = StatusListAdapter.AdapterModel(
                range = TimeRange(now - 60 * 60 * 24, now),
                checks = checkConfigs.map { cit ->
                    StatusListAdapter.AdapterModel.Check(
                        url = cit.url,
                        results = checkResults.filter { rit -> rit.configUid == cit.uid && !rit.skip }
                            .map { rit ->
                                TimeRangeWithCheckStatus(
                                    rit.timestampUtc - 60 * 10, // TODO
                                    rit.timestampUtc,
                                    rit.succeeded
                                )
                            }
                    )
                }
            )
        }

        val db = (activity!!.application as ThisApplication).db!!
        db.checkConfigDao().targetsLive()
            .observe(
                this,
                Observer<List<CheckConfig>> { records ->
                    checkConfigs = records
                    updateAdapterModel()
                })
        db.checkResultDao().latestLive(60 * 60 * 24)
            .observe(
                this,
                Observer<List<CheckResult>> { records ->
                    checkResults = records
                    updateAdapterModel()
                })

        return root
    }
}
