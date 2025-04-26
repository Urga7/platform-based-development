package si.uni_lj.fri.pbd.classproject2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.uni_lj.fri.pbd.classproject2.databinding.FragmentDashboardBinding
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getStepCount


class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val stepsToday = getStepCount(requireContext())
        binding.stepsText.text = stepsToday.toString()

        binding.buttonGetSteps.setOnClickListener {
            val stepsTodayNow = getStepCount(requireContext())
            binding.stepsText.text = stepsTodayNow.toString()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}