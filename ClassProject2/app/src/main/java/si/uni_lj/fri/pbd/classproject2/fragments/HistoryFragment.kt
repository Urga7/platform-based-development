package si.uni_lj.fri.pbd.classproject2.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.classproject2.databinding.FragmentHistoryBinding
import si.uni_lj.fri.pbd.classproject2.databinding.ItemHistoryCardBinding
import si.uni_lj.fri.pbd.classproject2.utils.Helpers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadStepHistory()
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(emptyMap()) // Initialize with an empty map
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun loadStepHistory() {
        val stepHistory = getStepsForLastSevenDays()
        if (stepHistory.isNotEmpty()) {
            binding.emptyHistoryTextView.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE
            historyAdapter.updateData(stepHistory)
        } else {
            binding.emptyHistoryTextView.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
        }
    }

    private fun getStepsForLastSevenDays(): Map<String, Int> {
        val stepHistory = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()
        val sharedPreferences = requireContext().getSharedPreferences(Helpers.STEP_PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Go back one day to exclude the current day
        calendar.add(Calendar.DAY_OF_MONTH, -1)

        for (i in 0..6) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val key = String.format(Locale.getDefault(), "step_count_%04d_%02d_%02d", year, month, day)
            val steps = sharedPreferences.getInt(key, 0)
            val dateString = dateFormatter.format(calendar.time)
            stepHistory[dateString] = steps
            calendar.add(Calendar.DAY_OF_MONTH, -1) // Go to the previous day
        }
        return stepHistory
    }

    private class HistoryAdapter(private var historyData: Map<String, Int>) :
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        class HistoryViewHolder(binding: ItemHistoryCardBinding) : RecyclerView.ViewHolder(binding.root) {
            val dateTextView: TextView = binding.dateTextView
            val stepsTextView: TextView = binding.stepsTextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val binding = ItemHistoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return HistoryViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val sortedKeys = historyData.keys.sortedDescending()
            val date = sortedKeys[position]
            val steps = historyData[date] ?: 0
            holder.dateTextView.text = date
            holder.stepsTextView.text = "Steps: $steps"
        }

        override fun getItemCount(): Int = historyData.size

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: Map<String, Int>) {
            historyData = newData
            notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}