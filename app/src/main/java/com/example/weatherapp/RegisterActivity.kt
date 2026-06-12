package com.example.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherapp.ui.theme.WeatherappTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterPage(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Parte 4: tela de cadastro
@Preview(showBackground = true)
@Composable
fun RegisterPage(modifier: Modifier = Modifier) {
    // Parte 4 Passo 3: variáveis de estado para cada campo
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }
    val activity = LocalContext.current.findActivity()

    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        val fieldModifier = Modifier.fillMaxWidth(fraction = 0.9f)

        Text(
            text = "Criar conta",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.size(12.dp))

        // Parte 5: usando DataField e PasswordField reutilizáveis
        DataField(
            label = "Nome",
            value = name,
            modifier = fieldModifier,
            onValueChange = { name = it }
        )

        Spacer(modifier = Modifier.size(8.dp))

        DataField(
            label = "E-mail",
            value = email,
            modifier = fieldModifier,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.size(8.dp))

        PasswordField(
            label = "Senha",
            value = password,
            modifier = fieldModifier,
            onValueChange = { password = it }
        )

        Spacer(modifier = Modifier.size(8.dp))

        PasswordField(
            label = "Confirme a senha",
            value = passwordConfirm,
            modifier = fieldModifier,
            onValueChange = { passwordConfirm = it }
        )

        Spacer(modifier = Modifier.size(16.dp))

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(fraction = 0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Parte 4 Passo 6: habilitado apenas se campos preenchidos e senhas iguais
            Button(
                onClick = {
                    activity?.let {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity) { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(activity,
                                        "Registro OK!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(activity,
                                        "Registro FALHOU!", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                enabled = name.isNotEmpty()
                        && email.isNotEmpty()
                        && password.isNotEmpty()
                        && passwordConfirm.isNotEmpty()
                        && password == passwordConfirm
            ) {
                Text("Registrar")
            }

            Button(
                onClick = {
                    name = ""
                    email = ""
                    password = ""
                    passwordConfirm = ""
                }
            ) {
                Text("Limpar")
            }
        }
    }
}
