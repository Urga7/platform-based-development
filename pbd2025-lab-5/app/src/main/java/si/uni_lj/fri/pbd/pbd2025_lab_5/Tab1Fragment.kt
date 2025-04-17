package si.uni_lj.fri.pbd.pbd2025_lab_5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import si.uni_lj.fri.pbd.pbd2025_lab_5.databinding.FragmentTab1Binding
import java.text.SimpleDateFormat
import java.util.*


class Tab1Fragment : Fragment() {
    private lateinit var binding: FragmentTab1Binding
    private val listItems = ArrayList<String>()
    private var adapter: ArrayAdapter<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTab1Binding.inflate(layoutInflater)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        binding.listView.adapter = adapter

        binding.fab.setOnClickListener { view ->
            addListItem()
            Snackbar.make(view, "Item added to the list", Snackbar.LENGTH_LONG)
                .setAction("Undo", undoOnClickListener)
                .show()
        }

        return binding.root
    }

    private val undoOnClickListener = View.OnClickListener { view ->
        listItems.removeAt(listItems.size - 1)
        adapter?.notifyDataSetChanged()
        Snackbar
            .make(view, "Item removed", Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show()
    }

    private fun addListItem() {
        val dateFormat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US)
        listItems.add(dateFormat.format(Date()))
        adapter?.notifyDataSetChanged()
    }
}