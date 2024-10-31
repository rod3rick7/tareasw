package com.quantum.tareas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button crearTareaButton = findViewById(R.id.crearTareaButton);
        Button borrarTareaButton = findViewById(R.id.borrarTareaButton);
        Button editarTareaButton = findViewById(R.id.editarTareaButton);
        Button listaTareaButton = findViewById(R.id.listaTareaButton);
        Button historialButton = findViewById(R.id.historialButton);

        crearTareaButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CrearTareaActivity.class);
            startActivity(intent);
        });

        borrarTareaButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, BorrarTareaActivity.class);
            startActivity(intent);
        });

        editarTareaButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, EditarTareaActivity.class);
            startActivity(intent);
        });

        listaTareaButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ListaTareaActivity.class);
            startActivity(intent);
        });

        historialButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, HistorialActivity.class);
            startActivity(intent);
        });
    }
}
