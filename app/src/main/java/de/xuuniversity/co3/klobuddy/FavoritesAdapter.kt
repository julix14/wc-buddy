package de.xuuniversity.co3.klobuddy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.xuuniversity.co3.klobuddy.wc.WcEntity



class FavoritesAdapter(
    private val items: List<WcEntity>,
    private val context: Context
): RecyclerView.Adapter<FavoritesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.favorite_item, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val item = items[position]
        holder.nameView.text = item.description
        val rounded = String.format("%.1f", item.rating).toDouble()
        holder.ratingView.text = rounded.toString()
    }

}