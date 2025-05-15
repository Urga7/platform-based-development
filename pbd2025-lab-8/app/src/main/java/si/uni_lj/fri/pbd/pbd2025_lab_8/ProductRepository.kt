package si.uni_lj.fri.pbd.pbd2025_lab_8

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class ProductRepository(application: Application?) {
    private val productDao: ProductDao?
    val searchResults = MutableLiveData<List<Product>>()
    val allProducts: LiveData<List<Product>>?

    fun insertProduct(newproduct: Product) {
        ProductRoomDatabase.databaseWriteExecutor?.execute(Runnable {
            productDao?.insertProduct(newproduct)
        })
    }

    fun deleteProduct(name: String) {
        ProductRoomDatabase.databaseWriteExecutor?.execute(Runnable {
            productDao?.deleteProduct(name)
        })
        
    }

    fun findProduct(name: String) {
        ProductRoomDatabase.databaseWriteExecutor?.execute(Runnable {
            searchResults.postValue(productDao?.findProduct(name))
        })
    }

     init {
         val db: ProductRoomDatabase? = application?.let { ProductRoomDatabase.getDatabase(it.applicationContext) }
         productDao = db?.productDao()
         allProducts = productDao?.allProducts
    }
}