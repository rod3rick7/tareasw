package com.cdelao.tareas;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AlertDialog;

import com.cdelao.tareas.clases.DbHelper;
import com.cdelao.tareas.modelos.Tarea;
import java.util.Calendar;

public class EditarTareaActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private Spinner spinnerCategoria;
    private Spinner spinnerPrioridad;
    private TextView tvFechaLimite;
    private Button btnEstado;
    private Button btnGuardar;
    private Tarea tarea; // Modelo Tarea para almacenar los detalles
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarea);

        dbHelper = new DbHelper(this);
        EditText etTitulo = findViewById(R.id.etTitulo);
        EditText etDescripcion = findViewById(R.id.etDescripcion);
        tvFechaLimite = findViewById(R.id.tvFechaLimite);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad);
        Button btnSeleccionarTarea = findViewById(R.id.btnSeleccionarTarea);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEstado = findViewById(R.id.btnEstado);

        bloquearFormulario(true);
        configurarSpinners();

        tvFechaLimite.setOnClickListener(v -> mostrarDatePicker());

        btnSeleccionarTarea.setOnClickListener(v -> mostrarTareas());

        btnEstado.setOnClickListener(v -> {
            if (tarea != null) {
                String nuevoEstado = tarea.getEstado().equals("COMPLETADO") ? "PENDIENTE" : "COMPLETADO";
                tarea.setEstado(nuevoEstado);
                btnEstado.setText(nuevoEstado.equals("COMPLETADO") ? "Marcar como Pendiente" : "Marcar como Completado");
                habilitarGuardar();
            }
        });


        btnGuardar.setOnClickListener(v -> {
            if (tarea != null) {
                tarea.setTitulo(etTitulo.getText().toString());
                tarea.setDescripcion(etDescripcion.getText().toString());
                tarea.setFechaLimite(tvFechaLimite.getText().toString());
                tarea.setPrioridad(spinnerPrioridad.getSelectedItem().toString()); // Guardar como String
                tarea.setCategoria(spinnerCategoria.getSelectedItem().toString());

                actualizarTarea(tarea);
            }
        });

        etTitulo.addTextChangedListener(textWatcher);
        etDescripcion.addTextChangedListener(textWatcher);
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


    //actualizar estado de tarea como completada


    @SuppressLint("Range")
    private void mostrarTareas() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DbHelper.TABLE_TAREAS,
                new String[]{DbHelper.COLUMN_TAREA_ID, DbHelper.COLUMN_TITULO},
                DbHelper.COLUMN_ESTADO + " = ?",
                new String[]{"PENDIENTE"},
                null, null, null);

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No hay tareas disponibles", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        final String[] tareas = new String[cursor.getCount()];
        final int[] tareaIds = new int[cursor.getCount()];
        int index = 0;

        while (cursor.moveToNext()) {
            tareaIds[index] = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_TAREA_ID));
            tareas[index] = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TITULO));
            index++;
        }
        cursor.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Tarea")
                .setItems(tareas, (dialog, which) -> {
                    cargarDetallesTarea(tareaIds[which]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @SuppressLint("Range")
    private void cargarDetallesTarea(int tareaId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DbHelper.TABLE_TAREAS,
                null,
                DbHelper.COLUMN_TAREA_ID + " = ?",
                new String[]{String.valueOf(tareaId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            tarea = new Tarea(
                    cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_TAREA_ID)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TITULO)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPCION)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_FECHA_LIMITE)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_PRIORIDAD)), // Cargar como String
                    cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ESTADO)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CATEGORIA))
            );

            ((EditText) findViewById(R.id.etTitulo)).setText(tarea.getTitulo());
            ((EditText) findViewById(R.id.etDescripcion)).setText(tarea.getDescripcion());
            tvFechaLimite.setText(tarea.getFechaLimite());

            for (int i = 0; i < spinnerCategoria.getAdapter().getCount(); i++) {
                if (spinnerCategoria.getAdapter().getItem(i).equals(tarea.getCategoria())) {
                    spinnerCategoria.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < spinnerPrioridad.getAdapter().getCount(); i++) {
                if (spinnerPrioridad.getAdapter().getItem(i).equals(tarea.getPrioridad())) {
                    spinnerPrioridad.setSelection(i);
                    break;
                }
            }

            btnEstado.setText(tarea.getEstado().equals("COMPLETADO") ? "Marcar como Pendiente" : "Marcar como Completado");
            bloquearFormulario(false);
        }
        cursor.close();
    }

    private void habilitarGuardar() {
        btnGuardar.setEnabled(isEditing);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isEditing = true;
            habilitarGuardar();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String fechaSeleccionada = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            tvFechaLimite.setText(fechaSeleccionada);
            isEditing = true;
            habilitarGuardar();
        }, year, month, day);

        datePickerDialog.show();
    }

    private void bloquearFormulario(boolean bloquear) {
        findViewById(R.id.etTitulo).setEnabled(!bloquear);
        findViewById(R.id.etDescripcion).setEnabled(!bloquear);
        tvFechaLimite.setEnabled(!bloquear);
        spinnerCategoria.setEnabled(!bloquear);
        spinnerPrioridad.setEnabled(!bloquear);
        btnEstado.setEnabled(!bloquear);
        btnGuardar.setEnabled(!bloquear);
        btnGuardar.setVisibility(bloquear ? View.GONE : View.VISIBLE);
    }
}
