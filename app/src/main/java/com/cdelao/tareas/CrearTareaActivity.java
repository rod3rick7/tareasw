package com.cdelao.tareas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.cdelao.tareas.clases.DbHelper;
import com.cdelao.tareas.modelos.Tarea;
import java.util.Calendar;

public class CrearTareaActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private Spinner spinnerCategoria;
    private Spinner spinnerPrioridad;
    private TextView tvFechaLimite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        dbHelper = new DbHelper(this);

        EditText etTitulo = findViewById(R.id.etTitulo);
        EditText etDescripcion = findViewById(R.id.etDescripcion);
        tvFechaLimite = findViewById(R.id.tvFechaLimite);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Configurar los Spinners
        configurarSpinners();

        // Configurar el DatePicker
        tvFechaLimite.setOnClickListener(v -> mostrarDatePicker());

        // Guardar tarea
        btnGuardar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString();
            String descripcion = etDescripcion.getText().toString();
            String fechaLimite = tvFechaLimite.getText().toString();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String prioridadStr = spinnerPrioridad.getSelectedItem().toString();

            // Validar campos
            if (titulo.isEmpty() || descripcion.isEmpty() || fechaLimite.isEmpty()) {
                Toast.makeText(CrearTareaActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear tarea y guardarla en la base de datos
            Tarea nuevaTarea = new Tarea(-1, titulo, descripcion, fechaLimite, prioridadStr, "PENDIENTE", categoria);
            guardarTarea(nuevaTarea);
        });
    }

    private void configurarSpinners() {
        ArrayAdapter<CharSequence> categoriaAdapter = ArrayAdapter.createFromResource(this,
                R.array.categorias_array, android.R.layout.simple_spinner_item);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        ArrayAdapter<CharSequence> prioridadAdapter = ArrayAdapter.createFromResource(this,
                R.array.prioridades_array, android.R.layout.simple_spinner_item);
        prioridadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(prioridadAdapter);
    }

    private void guardarTarea(Tarea tarea) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_TITULO, tarea.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, tarea.getDescripcion());
        values.put(DbHelper.COLUMN_FECHA_LIMITE, tarea.getFechaLimite());
        values.put(DbHelper.COLUMN_PRIORIDAD, tarea.getPrioridad()); // Aquí guardamos como String
        values.put(DbHelper.COLUMN_ESTADO, tarea.getEstado());
        values.put(DbHelper.COLUMN_CATEGORIA, tarea.getCategoria());

        long newRowId = database.insert(DbHelper.TABLE_TAREAS, null, values);
        database.close();

        if (newRowId == -1) {
            Toast.makeText(CrearTareaActivity.this, "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CrearTareaActivity.this, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String fechaSeleccionada = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            tvFechaLimite.setText(fechaSeleccionada);
        }, year, month, day);

        datePickerDialog.show();
    }
}
