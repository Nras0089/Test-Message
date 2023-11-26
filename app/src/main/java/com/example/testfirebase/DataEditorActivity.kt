package com.example.testfirebase

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class DataEditorActivity : AppCompatActivity() {

    private lateinit var editTextDataValue: EditText
    private lateinit var buttonSave: Button
    private var dataId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_editor)

        editTextDataValue = findViewById(R.id.editTextDataValue)
        buttonSave = findViewById(R.id.buttonSave)

        // Проверка на наличие данных для редактирования
        dataId = intent.getStringExtra("dataId")
        if (dataId != null) {
            // Здесь можно загрузить данные для редактирования
        }

        buttonSave.setOnClickListener {
            saveOrUpdateData()
        }
    }

    private fun saveOrUpdateData() {

    }
}
