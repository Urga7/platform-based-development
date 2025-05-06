package si.uni_lj.fri.pbd.pbd2025_lab_8.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.pbd2025_lab_8.Product
import si.uni_lj.fri.pbd.pbd2025_lab_8.R
import si.uni_lj.fri.pbd.pbd2025_lab_8.databinding.MainFragmentBinding
import java.util.*


class MainFragment : Fragment() {

    private var _binding: MainFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private var mViewModel: MainViewModel? = null
    private var adapter: ProductListAdapter? = null
    private var productId: TextView? = null
    private var productName: EditText? = null
    private var productQuantity: EditText? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        productId = binding.productID
        productName = binding.productName
        productQuantity = binding.productQuantity
        listenerSetup()
        observerSetup()
        recyclerSetup()
    }

    private fun listenerSetup() {
        val addButton = binding.addButton
        val findButton = binding.findButton
        val deleteButton = binding.deleteButton

        addButton.setOnClickListener {
            val name = productName?.text.toString()
            val quantity = productQuantity?.text.toString()
            if (name == "" || quantity == "") {
                productId?.text = "Incomplete information"
                return@setOnClickListener
            }

            val product = Product(name, Integer.parseInt(quantity))
            mViewModel?.insertProduct(product)
            clearFields()
        }

        findButton.setOnClickListener {
            val name = productName?.text.toString()
            mViewModel?.findProduct(name)
        }

        deleteButton.setOnClickListener {
            val name = productName?.text.toString()
            mViewModel?.deleteProduct(name)
            clearFields()
        }
    }

    private fun observerSetup() {
        mViewModel?.allProducts?.observe(viewLifecycleOwner) { products ->
            adapter?.setProductList(products)
        }


        mViewModel?.searchResults?.observe(viewLifecycleOwner) { products ->
            if (products == null || products.isEmpty()) {
                productId?.text = "No Match"
                return@observe
            }

            productId?.text = String.format(Locale.US, "%d", products[0].id)
            productName?.setText(products[0].name)
            productQuantity?.setText(String.format(Locale.US, "%d", products[0].quantity))
        }
    }

    private fun recyclerSetup() {
        adapter = ProductListAdapter(R.layout.product_list_item)
        val recyclerView: RecyclerView = binding.productRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun clearFields() {
        productId?.text = ""
        productName?.setText("")
        productQuantity?.setText("")
    }

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}
