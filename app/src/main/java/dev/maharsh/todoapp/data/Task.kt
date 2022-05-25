package dev.maharsh.todoapp.data

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Keep
@Parcelize
@Entity(tableName = "TaskTable")
data class Task(
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {

    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)


}