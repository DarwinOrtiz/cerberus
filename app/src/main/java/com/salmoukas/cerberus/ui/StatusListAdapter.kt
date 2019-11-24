package com.salmoukas.cerberus.ui

import android.graphics.Color
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
            val results: List<TimeRangeWithCheckStatus>,
            val latest: TimeRangeWithCheckStatus?
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
            adapterModel!!.checks[position].let {
                holder.itemView.findViewById<TextView>(R.id.status_item_url_view).text = it.url
                holder.itemView.findViewById<TextView>(R.id.status_item_text_view).apply {
                    val now = System.currentTimeMillis() / 1000L
                    val ago = if (it.latest != null) now - it.latest.end else null
                    val agoStr =
                        if (ago != null) (
                                if (ago >= (60 * 60)) (ago / (60 * 60)).toString() + " hours"
                                else (ago / 60).toString() + " minutes")
                        else null
                    text = (if (agoStr != null) "$agoStr ago: " else "") +
                            (it.latest?.message ?: "unknown status")
                    setBackgroundColor(if (it.latest?.ok == true) Color.GREEN else Color.RED)
                }
                holder.itemView.findViewById<CheckTimelineView>(R.id.status_item_timeline_view)
                    .viewModel =
                    CheckTimelineView.ViewModel(
                        range = adapterModel!!.range,
                        results = it.results
                    )
            }
        }
    }
}
