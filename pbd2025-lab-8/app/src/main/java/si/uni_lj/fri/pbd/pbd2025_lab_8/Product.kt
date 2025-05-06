package si.uni_lj.fri.pbd.pbd2025_lab_8

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @ColumnInfo(name = "productName")
    val name: String,

    @ColumnInfo(name = "productQuantity")
    val quantity: Int,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "productId")
    val id: Int = 0
)
