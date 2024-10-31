package com.quantum.tareas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.quantum.tareas.clases.DbHelper;
import com.quantum.tareas.modelos.Tarea;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class CrearTareaActivity extends AppCompatActivity {

    private EditText tituloEditText, descripcionEditText;
    private Button fechaLimiteButton, guardarButton;
    private SeekBar prioridadSeekBar;
    private Spinner categoriaSpinner;
    private String fechaLimite;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        // Inicialización de las vistas
        tituloEditText = findViewById(R.id.tituloEditText);
        descripcionEditText = findViewById(R.id.descripcionEditText);
        fechaLimiteButton = findViewById(R.id.fechaLimiteButton);
        guardarButton = findViewById(R.id.guardarButton);
        prioridadSeekBar = findViewById(R.id.prioridadSeekBar);
        categoriaSpinner = findViewById(R.id.categoriaSpinner);
        dbHelper = new DbHelper();

        // Configurar el Spinner de categorías
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categorias_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriaSpinner.setAdapter(adapter);

        // Establecer "Personal" como categoría predeterminada
        categoriaSpinner.setSelection(adapter.getPosition("PERSONAL"));

        // Configurar el botón de selección de fecha
        fechaLimiteButton.setOnClickListener(v -> mostrarCalendario());

        // Configurar el botón de guardar tarea
        guardarButton.setOnClickListener(v -> guardarTarea());

        // Configurar el SeekBar
        prioridadSeekBar.setMax(5);
    }

    private void mostrarCalendario() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String diaFormateado = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                    String mesFormateado = String.format(Locale.getDefault(), "%02d", month + 1);
                    fechaLimite = diaFormateado + "-" + mesFormateado + "-" + year;
                    fechaLimiteButton.setText(fechaLimite);
                }, año, mes, dia);
        datePickerDialog.show();
    }

    private void guardarTarea() {
        String titulo = tituloEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String categoriaSeleccionada = categoriaSpinner.getSelectedItem().toString();
        int prioridadSeleccionada = prioridadSeekBar.getProgress();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(fechaLimite)) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Tarea tarea = new Tarea(
                    UUID.randomUUID().toString(),
                    titulo,
                    descripcion,
                    fechaLimite,
                    categoriaSeleccionada,
                    prioridadSeleccionada,
                    "PENDIENTE"
            );
            dbHelper.agregarTarea(userId, tarea);
            Toast.makeText(this, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
        }
    }

}
