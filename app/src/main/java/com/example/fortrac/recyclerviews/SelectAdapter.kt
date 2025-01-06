package com.example.fortrac.recyclerviews


import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.String
import kotlin.Any
import kotlin.Int

internal class SelectAdapter(var data: List<*>) :
    RecyclerView.Adapter<Any?>() {
    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): Any {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.student_list_row, viewGroup, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: Any, i: Int) {
        val data: studentData = data[i]
        viewHolder.name.setText(data.name)
        viewHolder.age.setText(String.valueOf(data.age))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var age: TextView

        init {
            name = itemView.findViewById(R.id.name)
            age = itemView.findViewById(R.id.age)
        }
    }
}