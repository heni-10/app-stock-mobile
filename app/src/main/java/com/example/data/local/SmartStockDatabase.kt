package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.InventoryMovement
import com.example.data.model.OnlineOrder
import com.example.data.model.Product
import com.example.data.model.ProductVariant
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.Supplier

@Database(
    entities = [
        BusinessProfile::class,
        Category::class,
        Product::class,
        ProductVariant::class,
        InventoryMovement::class,
        Sale::class,
        SaleItem::class,
        OnlineOrder::class,
        Customer::class,
        Supplier::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartStockDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun saleDao(): SaleDao
    abstract fun orderDao(): OrderDao
    abstract fun customerSupplierDao(): CustomerSupplierDao
    abstract fun businessProfileDao(): BusinessProfileDao

    companion object {
        @Volatile
        private var INSTANCE: SmartStockDatabase? = null

        fun getDatabase(context: Context): SmartStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartStockDatabase::class.java,
                    "smartstock_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
