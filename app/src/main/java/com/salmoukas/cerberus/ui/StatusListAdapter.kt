package com.salmoukas.cerberus.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.salmoukas.cerberus.R
import com.salmoukas.cerberus.ui.model.TimeRange
import com.salmoukas.cerberus.ui.model.TimeRangeWithCheckStatus

class StatusListAdapter :
    RecyclerView.Adapter<StatusListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    data class AdapterModel(
        val range: TimeRange,
        val checks: List<Check>
    ) {
        data class Check(
            val url: String,
            val results: List<TimeRangeWithCheckStatus>
        )
    }

    var adapterModel: AdapterModel? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_status_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (adapterModel != null) adapterModel!!.checks.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (adapterModel != null) {
            holder.itemView.findViewById<TextView>(R.id.status_item_url_view).text =
                adapterModel!!.checks[position].url
            holder.itemView.findViewById<CheckTimelineView>(R.id.status_item_timeline_view)
                .viewModel =
                CheckTimelineView.ViewModel(
                    range = adapterModel!!.range,
                    results = adapterModel!!.checks[position].results
                )
        }
    }
}
