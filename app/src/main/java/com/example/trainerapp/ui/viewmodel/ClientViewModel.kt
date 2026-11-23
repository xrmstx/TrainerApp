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

    fun onNameChange(name: String) { _newClientName.value = name }

    fun addClient() {
        val name = _newClientName.value.trim()
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                dao.insertClient(Client(name = name))
                _newClientName.value = ""
            }
        }
    }
}
