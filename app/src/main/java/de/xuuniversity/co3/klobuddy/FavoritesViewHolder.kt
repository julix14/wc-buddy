package de.xuuniversity.co3.klobuddy

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val nameView: TextView = view.findViewById(R.id.favorites_name)
    val ratingView: TextView = view.findViewById(R.id.favorites_rating)
    val ratingNavButton: ImageButton = view.findViewById(R.id.favorites_start_navigation)
}
