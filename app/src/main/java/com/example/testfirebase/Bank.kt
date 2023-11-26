package com.example.testfirebase
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class Bank : AppCompatActivity() {


    private lateinit var database: DatabaseReference

    private fun getAccountData(accountId: String) {
        database.child(accountId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val balance = dataSnapshot.child("balance").getValue(Int::class.java)
                findViewById<TextView>(R.id.tvAccountId).text = "ID счета: $accountId"
                findViewById<TextView>(R.id.tvAccountBalance).text = "Баланс: $balance"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок, например, показать сообщение об ошибке
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        // Инициализация Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts")

        // Получение UID текущего пользователя
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUID != null) {
            // Используйте UID для получения данных о счете и инициализации в базе данных
            getAccountData(currentUserUID)
            initializeUserInDatabase(currentUserUID)
        } else {
            // Пользователь не аутентифицирован, обработайте эту ситуацию
        }

        // Установка обработчика нажатия на кнопку перевода
        val btnTransfer = findViewById<Button>(R.id.btnTransfer)
        btnTransfer.setOnClickListener {
            performTransfer()
        }
    }

    // ... (Другие методы, включая getAccountData)

    private fun performTransfer() {
        // Получение суммы перевода из EditText
        val transferAmountString = findViewById<EditText>(R.id.etTransferAmount).text.toString()
        val transferAmount = transferAmountString.toIntOrNull()

        // Получение UID получателя из EditText
        val recipientId = findViewById<EditText>(R.id.etRecipientId).text.toString()

        // Проверка на корректность введенных данных
        if (transferAmount == null || transferAmount <= 0) {
            showToast("Введите корректную сумму перевода")
            return
        }

        if (recipientId.isEmpty()) {
            showToast("Введите UID получателя")
            return
        }

        // Получение UID отправителя из текущего пользователя
        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        if (senderId == null) {
            showToast("Ошибка аутентификации. Попробуйте снова.")
            return
        }

        // Перевод средств
        updateAccountBalance(senderId, -transferAmount) { success ->
            if (success) {
                updateAccountBalance(recipientId, transferAmount) { successRecipient ->
                    if (successRecipient) {
                        showToast("Перевод выполнен успешно")
                    } else {
                        showToast("Ошибка при переводе на счет получателя")
                        // Возвращаем средства обратно отправителю в случае неудачи
                        updateAccountBalance(senderId, transferAmount)
                    }
                }
            } else {
                showToast("Недостаточно средств для перевода или ошибка")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun initializeUserInDatabase(userUID: String?) {
        userUID ?: return

        val databaseReference = FirebaseDatabase.getInstance().getReference("accounts")
        databaseReference.child(userUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Если счет пользователя не существует, создаем новую запись
                    val newAccount = Account(uniqueIdentifier = userUID, balance = 0)
                    databaseReference.child(userUID).setValue(newAccount)
                }
                // Если счет пользователя уже существует, ничего не делаем
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок
            }
        })
    }

    private fun updateAccountBalance(accountId: String, amount: Int, onComplete: (Boolean) -> Unit = {}) {
        database.child(accountId).child("balance").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentBalance = mutableData.getValue(Int::class.java) ?: return Transaction.abort()
                if (amount < 0 && currentBalance + amount < 0) {
                    // Недостаточно средств для снятия
                    return Transaction.abort()
                }
                mutableData.value = currentBalance + amount
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                onComplete(b && databaseError == null)
                if (b && databaseError == null) {
                    // Обновление пользовательского интерфейса отправителя после успешной транзакции
                    val senderId = FirebaseAuth.getInstance().currentUser?.uid
                    if (senderId != null) {
                        getAccountData(senderId)
                    }
                }
            }
        })
    }}
