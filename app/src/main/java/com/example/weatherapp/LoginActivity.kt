package com.example.weatherapp

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.WeatherappTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginPage(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPage(modifier: Modifier = Modifier) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val activity = LocalContext.current.findActivity()

    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        // Parte 5: usando composables reutilizáveis DataField e PasswordField
        val fieldModifier = Modifier.fillMaxWidth(fraction = 0.9f)

        Text(
            text = "Bem-vindo/a!",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.size(12.dp))

        DataField(
            label = "Digite seu e-mail",
            value = email,
            modifier = fieldModifier,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.size(12.dp))

        PasswordField(
            label = "Digite sua senha",
            value = password,
            modifier = fieldModifier,
            onValueChange = { password = it }
        )

        Spacer(modifier = Modifier.size(16.dp))

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(fraction = 0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Parte 2 Passo 8: botão habilitado apenas se campos preenchidos
            // Parte 3 Passo 2: onClick navega para MainActivity
            Button(
                onClick = {
                    activity?.let {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity) { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(activity, "Login OK!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(activity, "Login FALHOU!", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                enabled = email.isNotEmpty() && password.isNotEmpty()
            ) {
                Text("Login")
            }

            Button(
                onClick = { email = ""; password = "" }
            ) {
                Text("Limpar")
            }
        }

        Spacer(modifier = modifier.size(8.dp))

        // Parte 4 Passo 4: botão que navega para RegisterActivity
        Button(
            onClick = {
                activity?.startActivity(
                    Intent(activity, RegisterActivity::class.java)
                )
            }
        ) {
            Text("Criar conta")
        }
    }
}

