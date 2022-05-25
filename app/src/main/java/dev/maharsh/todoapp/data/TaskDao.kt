package dev.maharsh.todoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("select * from TaskTable where (completed !=:hideCompleted or completed= 0) and name LIKE '%' || :searchQuery || '%' order by important desc , name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("select * from TaskTable where (completed !=:hideCompleted or completed= 0) and name LIKE '%' || :searchQuery || '%' order by important desc , created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("DELETE from TaskTable where completed = 1")
    suspend fun deleteCompletedTasks()


    fun getTasks(
        searchQuery: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean
    ): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(searchQuery, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(searchQuery, hideCompleted)
        }


}