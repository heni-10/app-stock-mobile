package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM product_variants ORDER BY id ASC")
    fun getAllVariants(): Flow<List<ProductVariant>>

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    fun getVariantsForProduct(productId: Long): Flow<List<ProductVariant>>

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    suspend fun getVariantsForProductSync(productId: Long): List<ProductVariant>

    @Query("SELECT * FROM product_variants WHERE id = :variantId")
    suspend fun getVariantById(variantId: Long): ProductVariant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ProductVariant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariant>): List<Long>

    @Update
    suspend fun updateVariant(variant: ProductVariant)

    @Query("UPDATE product_variants SET currentQuantity = :newQuantity WHERE id = :variantId")
    suspend fun updateVariantQuantity(variantId: Long, newQuantity: Int)

    @Delete
    suspend fun deleteVariant(variant: ProductVariant)

    @Query("DELETE FROM product_variants WHERE productId = :productId")
    suspend fun deleteVariantsForProduct(productId: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_movements ORDER BY createdAt DESC")
    fun getAllMovements(): Flow<List<InventoryMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<InventoryMovement>)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Query("UPDATE sales SET status = :status WHERE id = :saleId")
    suspend fun updateSaleStatus(saleId: Long, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsSync(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items ORDER BY id DESC")
    fun getAllSaleItems(): Flow<List<SaleItem>>
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM online_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OnlineOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OnlineOrder): Long

    @Query("UPDATE online_orders SET orderStatus = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)
}

@Dao
interface CustomerSupplierDao {
    @Query("SELECT * FROM customers ORDER BY totalSpent DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Query("UPDATE customers SET totalSpent = totalSpent + :addedAmount, totalOrders = totalOrders + 1 WHERE id = :customerId")
    suspend fun updateCustomerStats(customerId: Long, addedAmount: Double)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long
}

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<BusinessProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: BusinessProfile)
}
