package si.uni_lj.fri.pbd.classproject3.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import si.uni_lj.fri.pbd.classproject3.Constants
import si.uni_lj.fri.pbd.classproject3.database.dao.RecipeDao
import si.uni_lj.fri.pbd.classproject3.database.entity.RecipeDetails

@androidx.room.Database(entities = [RecipeDetails::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {

    /**
     * Abstract method to get the Data Access Object for RecipeDetails.
     * @return The RecipeDao instance.
     */
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        /**
         * Gets the singleton instance of the RecipeDatabase.
         *
         * @param context The application context.
         * @return The singleton instance of RecipeDatabase.
         */
        fun getDatabase(context: Context): RecipeDatabase {
            // Multiple threads can ask for the database at the same time,
            // so we ensure we only initialize it once.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    Constants.DB_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}