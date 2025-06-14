package si.uni_lj.fri.pbd.pbd2025_lab_8.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.pbd2025_lab_8.Product
import si.uni_lj.fri.pbd.pbd2025_lab_8.R

class ProductListAdapter(private val productItemLayout: Int) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    private var productList: List<Product>? = null
    fun setProductList(products: List<Product>?) {
        productList = products
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (productList == null) 0 else productList!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(
                parent.context).inflate(productItemLayout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, listPosition: Int) {
        val item = holder.item
        item.text = productList!![listPosition].name
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: TextView = itemView.findViewById(R.id.product_row)
    }
}