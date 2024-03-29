package de.xuuniversity.co3.klobuddy

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
import de.xuuniversity.co3.klobuddy.wc.WcEntity


class FavoritesAdapter(
    private val items: List<WcEntity>,
    private val context: Context,
    private val callback: FavoritesAdapterCallback
): RecyclerView.Adapter<FavoritesViewHolder>() {
    interface FavoritesAdapterCallback {
        fun onNavigateToMap()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.favorite_item, parent, false)

        return FavoritesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val item = items[position]

        // Get the drawableLeftCompat of the TextView
        val drawableLeft = holder.ratingView.compoundDrawables[0]
        val imageNavigationButton = holder.ratingNavButton.drawable

        holder.ratingNavButton.setOnClickListener{
            Log.d("FavoritesAdapter", "Navigation button clicked ${item}")
            //Set the destination
            StatesSingleton.cameraPosition = CameraPosition(LatLng(item.latitude, item.longitude), 17f, 0f, 0f)

            //Navigate to map
            callback.onNavigateToMap()
        }

        if (drawableLeft != null) {
            // Create a Drawable wrapper
            val drawableWrapper = DrawableCompat.wrap(drawableLeft)
            val imageButtonWrapper = DrawableCompat.wrap(imageNavigationButton)

            // Check if dark mode is enabled
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                // Create a ColorStateList with the light color
                val colorStateList = ColorStateList.valueOf(Color.WHITE)

                // Apply the ColorStateList to the Drawable wrapper
                DrawableCompat.setTintList(drawableWrapper, colorStateList)
                DrawableCompat.setTintList(imageButtonWrapper, colorStateList)

            } else {
                // Remove the color filter to show the original color of the Drawable wrapper
                DrawableCompat.setTintList(drawableWrapper, ColorStateList.valueOf(Color.BLACK))
                DrawableCompat.setTintList(imageButtonWrapper, ColorStateList.valueOf(Color.BLACK))
            }

            // Set the Drawable wrapper as the drawableLeftCompat of the TextView
            holder.ratingView.setCompoundDrawablesWithIntrinsicBounds(
                drawableWrapper,
                null,
                null,
                null
            )
        }
        holder.nameView.text = item.description
        val rounded = String.format("%.1f", item.averageRating)
        holder.ratingView.text = rounded
    }

}