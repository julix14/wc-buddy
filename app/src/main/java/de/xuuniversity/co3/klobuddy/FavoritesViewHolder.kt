package de.xuuniversity.co3.klobuddy

import androidx.recyclerview.widget.RecyclerView

import android.view.View
import android.widget.TextView

class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val nameView: TextView = itemView.findViewById(R.id.favorites_name)
    val ratingView: TextView = itemView.findViewById(R.id.favorites_rating)
}
