package de.xuuniversity.co3.klobuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.xuuniversity.co3.klobuddy.wc.WcEntity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoritesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_favorites)


        val testItems = arrayOf(
            WcEntity("bt_0", "Test 1", 1.0, 1.0),
            WcEntity("bt_1", "Test 2", 2.0, 2.0),
            WcEntity("bt_2", "Test 3", 3.0, 3.0),
            WcEntity("bt_3", "Test 4", 4.0, 4.0),
            WcEntity("bt_4", "Test 5", 5.0, 5.0),
            WcEntity("bt_5", "Test 7", 7.0, 7.0),
            WcEntity("bt_6", "Test 6", 6.0, 6.0),
            WcEntity("bt_7", "Test 8", 8.0, 8.0),
            WcEntity("bt_8", "Test 9", 9.0, 9.0),
            WcEntity("bt_9", "Test 10", 10.0, 10.0),
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = FavoritesAdapter(testItems, requireContext())

    }

}