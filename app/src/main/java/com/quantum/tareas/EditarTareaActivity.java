package com.quantum.tareas;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.quantum.tareas.clases.DbHelper;
import com.quantum.tareas.modelos.Tarea;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditarTareaActivity extends AppCompatActivity {

    private EditText tituloEditText, descripcionEditText;
    private Button fechaLimiteButton, guardarButton;
    private SeekBar prioridadSeekBar;
    private Spinner categoriaSpinner;
    private DbHelper dbHelper;
    private List<Tarea> tareas; // Lista de tareas
    private Tarea tareaSeleccionada; // Tarea seleccionada
    private String fechaLimite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarea);

        // Inicialización de vistas
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

        // Cargar tareas al iniciar la actividad
        cargarTareas();

        fechaLimiteButton.setOnClickListener(v -> mostrarCalendario());
        guardarButton.setOnClickListener(v -> guardarCambios());
    }

    private void cargarTareas() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbHelper.obtenerTareas(userId, new DbHelper.TaskCallback<List<Tarea>>() {
                @Override
                public void onSuccess(List<Tarea> result) {
                    // Filtrar solo tareas PENDIENTES y COMPLETADAS
                    tareas = new ArrayList<>();
                    for (Tarea tarea : result) {
                        if ("PENDIENTE".equals(tarea.getEstado()) || "COMPLETADO".equals(tarea.getEstado())) {
                            tareas.add(tarea);
                        }
                    }
                    mostrarTareas();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(EditarTareaActivity.this, "Error al cargar tareas", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarTareas() {
        String[] tareasArray = new String[tareas.size()];
        for (int i = 0; i < tareas.size(); i++) {
            tareasArray[i] = tareas.get(i).getTitulo(); // Mostrar solo el título
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una tarea")
                .setItems(tareasArray, (dialog, which) -> {
                    tareaSeleccionada = tareas.get(which); // Obtener la tarea seleccionada
                    llenarFormulario(tareaSeleccionada);
                });
        builder.show();
    }

    private void llenarFormulario(Tarea tarea) {
        tituloEditText.setText(tarea.getTitulo());
        descripcionEditText.setText(tarea.getDescripcion());
        fechaLimite = tarea.getFechaLimite();
        fechaLimiteButton.setText(fechaLimite);
        prioridadSeekBar.setProgress(tarea.getPrioridad());
        categoriaSpinner.setSelection(((ArrayAdapter) categoriaSpinner.getAdapter()).getPosition(tarea.getCategoria()));
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
                    fechaLimite = diaFormateado + "-" + mesFormateado + "-" + year; // Formato: DD-MM-YYYY
                    fechaLimiteButton.setText(fechaLimite);
                }, año, mes, dia);
        datePickerDialog.show();
    }

    private void guardarCambios() {
        String titulo = tituloEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String categoriaSeleccionada = categoriaSpinner.getSelectedItem().toString();
        int prioridadSeleccionada = prioridadSeekBar.getProgress();

        if (tareaSeleccionada == null) {
            Toast.makeText(this, "Selecciona una tarea primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica que todos los campos estén llenos
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(fechaLimite)) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar la tarea sin verificar la fecha
        tareaSeleccionada.setTitulo(titulo);
        tareaSeleccionada.setDescripcion(descripcion);
        tareaSeleccionada.setFechaLimite(fechaLimite); // Puede mantenerse igual
        tareaSeleccionada.setCategoria(categoriaSeleccionada);
        tareaSeleccionada.setPrioridad(prioridadSeleccionada);

        dbHelper.editarTarea(FirebaseAuth.getInstance().getCurrentUser().getUid(), tareaSeleccionada);

        Toast.makeText(this, "Cambios guardados con éxito", Toast.LENGTH_SHORT).show();
        finish(); // Cierra la actividad
    }
}
