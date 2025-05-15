package si.uni_lj.fri.pbd.pbd2025_lab_8.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import si.uni_lj.fri.pbd.pbd2025_lab_8.ProductRepository
import si.uni_lj.fri.pbd.pbd2025_lab_8.Product


class MainViewModel(application: Application?) : AndroidViewModel(application!!) {
    private val repository: ProductRepository = ProductRepository(application)
    internal var allProducts: LiveData<List<Product>>? = repository.allProducts
    internal var searchResults: MutableLiveData<List<Product>?> = repository.searchResults as MutableLiveData<List<Product>?>

    fun insertProduct(product: Product) {
        repository.insertProduct(product)
    }

    fun findProduct(name: String) {
        repository.findProduct(name)
    }

    fun deleteProduct(name: String) {
        repository.deleteProduct(name)
    }

}