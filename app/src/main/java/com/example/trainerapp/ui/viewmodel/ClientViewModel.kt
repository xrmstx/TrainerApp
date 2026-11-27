package com.example.trainerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val clients = dao.getAllClients()

    private val _newClientName = MutableStateFlow("")
    val newClientName: StateFlow<String> = _newClientName.asStateFlow()

    // Сообщение для пользователя: ошибки вставки, дубли и т.п.
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun onNameChange(name: String) { _newClientName.value = name }

    fun addClient() {
        val name = _newClientName.value.trim()

        // Пустое поле — не ошибка, просто ничего не делаем.
        // Кнопку в экране лучше гасить: enabled = newClientName.isNotBlank()
        if (name.isEmpty()) return

        viewModelScope.launch {
            try {
                dao.insertClient(Client(name = name))
                _newClientName.value = ""
            } catch (e: Exception) {
                when {
                    e.message?.contains("UNIQUE") == true ->
                        _snackbarMessage.value = "Такой клиент уже есть"
                    else ->
                        _snackbarMessage.value = "Не удалось добавить клиента. Попробуйте позже."
                }
            }
        }
    }

    fun clearSnackbar() { _snackbarMessage.value = null }
}
