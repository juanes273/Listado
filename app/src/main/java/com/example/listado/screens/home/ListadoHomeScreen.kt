package com.example.listado.screens.home

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listado.navigation.ListadoScreens
import kotlinx.coroutines.delay
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoHomeScreen(navController: NavController) {
    val viewModel: ListadoViewModel = viewModel()
    val newObjectTextState = remember { mutableStateOf(TextFieldValue()) }
    var objectList by remember { mutableStateOf(emptyList<String>()) }
    val currentObjectList by rememberUpdatedState(newValue = viewModel.objectList.value)
    var lists by remember { mutableStateOf(emptyList<String>()) }
    val currentLists by rememberUpdatedState(newValue = viewModel.listNames.value)
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("Selecciona lista") }
    var newListTextState by remember { mutableStateOf(TextFieldValue()) }
    var shouldCreateList by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shouldFetchList by remember { mutableStateOf(false) }
    var shouldFetchLists by remember { mutableStateOf(false) }
    var shouldFetchObjects by remember { mutableStateOf(false) }

    DisposableEffect(currentObjectList) {
        objectList = currentObjectList ?: emptyList()
        onDispose { }
    }

    DisposableEffect(currentLists) {
        lists = currentLists ?: emptyList()
        onDispose { }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchObjectList(selectedText)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchObjectLists()
    }

    LaunchedEffect(viewModel.objectList.value) {
        viewModel.fetchObjectList(selectedText)
    }

    LaunchedEffect(viewModel.listNames.value) {
        viewModel.fetchObjectLists()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp, horizontal = 16.dp)
            .offset(y = (-20).dp) // Ajusta el valor según tus necesidades
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu lista de compras",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 1.dp) // Reduje el espacio inferior
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 32.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Nombre de lista: $selectedText")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp, horizontal = 32.dp)
                .align(Alignment.CenterHorizontally)
                .offset(y = (-16).dp) // Ajusta el valor según tus necesidades
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                }
            ) {
                TextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )

                if (expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        lists.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    selectedText = item
                                    expanded = false
                                    viewModel.fetchObjectList(selectedText)
                                    shouldFetchObjects = true
                                    Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        if(selectedText != "Selecciona lista"){
            Button(
                onClick = {
                    if (selectedText != "Selecciona lista") {
                        viewModel.deleteList(selectedText)
                        objectList = emptyList()
                        selectedText = "Selecciona lista"
                        shouldFetchLists = true
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 32.dp)
            ) {
                Text(text = "Eliminar Lista")
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = newObjectTextState.value,
                onValueChange = {
                    newObjectTextState.value = it
                },textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary, // Cambia el color del texto
                    fontWeight = FontWeight.Normal // Puedes ajustar otros atributos de estilo según sea necesario
                ),
                modifier = Modifier
                    .height(30.dp)
                    .weight(10f)
                    .padding(end = 8.dp)
                    .border(1.dp, color = MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    val newObject = newObjectTextState.value.text
                    if (newObject.isNotEmpty()) {
                        viewModel.addObjectToList(newObject, selectedText)
                        shouldFetchList = true
                        newObjectTextState.value = TextFieldValue()
                        Log.d("ListadoViewModel", "Objeto agregándose: $newObject")
                    }
                },
                modifier = Modifier
                    .height(69.dp)
                    .padding(16.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = "Agregar Objeto")
            }
        }

        LaunchedEffect(shouldFetchList) {
            if (shouldFetchList) {
                delay(1000)
                viewModel.fetchObjectList(selectedText)
                Log.e("Observador","objetos antes: $objectList")
                shouldFetchList = false
            }
        }

        LaunchedEffect(shouldFetchLists) {
            if (shouldFetchLists) {
                delay(1000)
                viewModel.fetchObjectLists()
                Log.e("Observador","listas antes: $lists")
                shouldFetchLists = false
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Alinea verticalmente en el centro
        ) {
            BasicTextField(
                value = newListTextState,
                onValueChange = {
                    newListTextState = it
                },textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary, // Cambia el color del texto
                    fontWeight = FontWeight.Normal // Puedes ajustar otros atributos de estilo según sea necesario
                ),
                modifier = Modifier
                    .height(30.dp)
                    .weight(10f)
                    .padding(end = 8.dp)
                    .border(1.dp, color = MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    val newList = newListTextState.text
                    if (newList.isNotEmpty()) {
                        viewModel.createList(newList)
                        shouldFetchLists = true
                        newListTextState = TextFieldValue()
                        Log.d("ListadoViewModel", "Lista agregándose: $newList")
                    }
                },
                modifier = Modifier
                    .height(69.dp) // Ajusta la altura según tus necesidades
                    .padding(16.dp)
                    .align(Alignment.CenterVertically) // Alinea el texto verticalmente al centro
            ) {
                Text(text = "Agregar Lista")
            }
        }


        Box(
            modifier = Modifier
                .padding(vertical = 2.dp, horizontal = 16.dp)
                .height(250.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 16.dp)
            ) {
                if (objectList != null && objectList.isNotEmpty()) {
                    item {
                        // Encabezado de la tabla
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 3.dp)
                        ) {
                            Text(text = "Producto")
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "Acciones")
                        }
                    }

                    items(objectList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 3.dp)
                        ) {
                            // Columna de objeto
                            Text(text = item)

                            Spacer(modifier = Modifier.weight(1f)) // Espacio flexible

                            // Columna de acciones
                            Button(
                                onClick = {
                                    viewModel.deleteObjectFromList(item, selectedText)
                                    shouldFetchList = true
                                },
                                modifier = Modifier.padding(horizontal = 3.dp)
                            ) {
                                Text("Borrar")
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "La lista de productos está vacía",
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }



        LaunchedEffect(shouldFetchObjects) {
            if (shouldFetchObjects) {
                delay(1000)
                objectList = viewModel.objectList.value ?: emptyList()
                shouldFetchObjects = false
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    navController.navigate(ListadoScreens.UserDataContent.name)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Ver Datos")
            }

            Button(
                onClick = {
                    viewModel.signOut()
                    navController.navigate(ListadoScreens.LoginScreen.name)
                },
                modifier = Modifier.padding(16.dp)
                    .width(200.dp)
            ) {
                Text(text = "Cerrar Sesión")
            }
        }

    }
}





