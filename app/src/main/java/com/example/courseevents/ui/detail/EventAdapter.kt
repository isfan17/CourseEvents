package com.example.courseevents.ui.detail

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courseevents.databinding.ItemEventBinding
import com.example.courseevents.model.Event
import com.example.courseevents.ui.insert.EventAddUpdateActivity

class EventAdapter(context: Context, private var listEvents: ArrayList<Event>): RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    private val mContext = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(listEvents[position])
    }

    override fun getItemCount(): Int = listEvents.size

    fun searchDataList(searchList: ArrayList<Event>) {
        listEvents = searchList
        notifyDataSetChanged()
    }

    inner class EventViewHolder(private val binding: ItemEventBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvItemName.text = event.name
            binding.tvItemDate.text = event.date
            binding.tvItemTime.text = event.time

            binding.fabAction.setOnClickListener{
                val intent = Intent(mContext, EventAddUpdateActivity::class.java)
                intent.putExtra(EventAddUpdateActivity.EXTRA_COURSE_ID, event.course_id)
                intent.putExtra(EventAddUpdateActivity.EXTRA_EVENT_ID, event.id)
                intent.putExtra(EventAddUpdateActivity.EXTRA_EVENT_NAME, event.name)
                intent.putExtra(EventAddUpdateActivity.EXTRA_EVENT_DATE, event.date)
                intent.putExtra(EventAddUpdateActivity.EXTRA_EVENT_TIME, event.time)
                mContext.startActivity(intent)
            }
        }
    }
}
