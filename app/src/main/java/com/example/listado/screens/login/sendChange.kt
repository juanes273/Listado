package com.example.listado.screens.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.listado.R
import com.example.listado.navigation.ListadoScreens
import com.example.listado.screens.login.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    navController: NavController,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var verificationCode by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    VerifyCodeContent(
        verificationCode = verificationCode,
        email = email,
        onVerificationCodeChanged = { verificationCode = it },
        onEmailChanged = { email = it },
        onVerifyCode = {
            keyboardController?.hide()
            // Lanzamos una nueva coroutina utilizando viewModelScope
            viewModel.viewModelScope.launch {
                try {
                    viewModel.verificarYEnviarLinkCambioContraseña(verificationCode, email, context)
                    // Si la verificación es exitosa, puedes navegar a la siguiente pantalla
                } catch (e: Exception) {
                    // Mostrar mensaje de error
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeContent(
    verificationCode: String,
    email: String,
    onVerificationCodeChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onVerifyCode: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.verification_code),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Aquí puedes manejar la navegación de vuelta
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
            )

            OutlinedTextField(
                value = verificationCode,
                onValueChange = { onVerificationCodeChanged(it) },
                label = { Text(text = stringResource(id = R.string.verification_code)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { onEmailChanged(it) },
                label = { Text(text = stringResource(id = R.string.email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black
                )
            )

            // Botón para verificar el código
            Button(
                onClick = onVerifyCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = verificationCode.isNotBlank() && email.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.verify_code))
            }
        }
    }
}
