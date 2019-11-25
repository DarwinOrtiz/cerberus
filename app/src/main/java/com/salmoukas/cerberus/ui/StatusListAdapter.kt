package com.salmoukas.cerberus.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.salmoukas.cerberus.R
import com.salmoukas.cerberus.ui.model.TimeRange
import com.salmoukas.cerberus.ui.model.TimeRangeWithCheckStatus
import java.util.concurrent.TimeUnit

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
            val latest: TimeRangeWithCheckStatus?,
            val stale: Boolean
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
                    val now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                    val message = it.latest?.message
                        ?: resources.getString(R.string.status_list_status_unknown)
                    text = when {
                        it.latest != null -> now - it.latest.end
                        else -> null
                    }.let { ago ->
                        when {
                            ago == null -> message
                            (ago >= (60 * 60)) -> resources.getString(
                                R.string.status_list_hours_ago,
                                ago / (60 * 60),
                                message
                            )
                            else -> resources.getString(
                                R.string.status_list_minutes_ago,
                                ago / 60,
                                message
                            )
                        }
                    }
                    setBackgroundColor(
                        when {
                            it.stale -> context.getColor(R.color.status_stale)
                            it.latest?.ok == true -> context.getColor(R.color.status_ok)
                            else -> context.getColor(R.color.status_error)
                        }
                    )
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
