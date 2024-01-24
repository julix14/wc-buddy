package de.xuuniversity.co3.klobuddy

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_favorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = FavoritesAdapter(StatesSingleton.favoriteWCEntities, requireContext(), activity as FavoritesAdapter.FavoritesAdapterCallback)

        //Watch for changes in the database and updates the recyclerview
        lifecycleScope.launch {

            val db = RoomDatabaseSingleton.getDatabase(activity as Context)
            val wcDao = db.wcDao()

            val flow = wcDao.getAllFavoritesByUserIDFlowDistinct(StatesSingleton.userId)

            flow.collect {
                StatesSingleton.favoriteWCEntities = it
                recyclerView.adapter = FavoritesAdapter(StatesSingleton.favoriteWCEntities, requireContext(), activity as FavoritesAdapter.FavoritesAdapterCallback)
            }
        }
    }
}