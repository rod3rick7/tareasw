package com.cdelao.tareas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cdelao.tareas.clases.DbHelper;
import com.cdelao.tareas.modelos.Tarea;

import java.util.ArrayList;


public class BorrarTareaActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private ListView listViewTareas;
    private Button btnBorrar;
    private ArrayList<Tarea> tareas;
    private ArrayAdapter<Tarea> adapter;
    private Tarea tareaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrar_tarea);

        dbHelper = new DbHelper(this);
        listViewTareas = findViewById(R.id.listViewTareas);
        btnBorrar = findViewById(R.id.btnBorrar);
        tareas = new ArrayList<>();

        cargarTareas();

        // Configurar el ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, tareas);
        listViewTareas.setAdapter(adapter);
        listViewTareas.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Manejar la selección de una tarea
        listViewTareas.setOnItemClickListener((parent, view, position, id) -> {
            tareaSeleccionada = tareas.get(position);
            btnBorrar.setEnabled(true);
        });

        // Configurar el botón de borrar
        btnBorrar.setOnClickListener(v -> mostrarConfirmacionBorrar());
    }

    private void cargarTareas() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + DbHelper.TABLE_TAREAS +
                        " WHERE " + DbHelper.COLUMN_ESTADO + " IN (?, ?)",
                new String[]{"PENDIENTE", "COMPLETADO"});

        tareas.clear();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int idTarea = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_TAREA_ID));
            @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TITULO));
            @SuppressLint("Range") String descripcion = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPCION));
            @SuppressLint("Range") String fechaLimite = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_FECHA_LIMITE));
            @SuppressLint("Range") String prioridad = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_PRIORIDAD));
            @SuppressLint("Range") String estado = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ESTADO));
            @SuppressLint("Range") String categoria = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CATEGORIA));

            // Crear objeto Tarea
            Tarea tarea = new Tarea(idTarea, titulo, descripcion, fechaLimite, prioridad, estado, categoria);
            tareas.add(tarea);
        }
        cursor.close();
        database.close();
    }

    private void mostrarConfirmacionBorrar() {
        if (tareaSeleccionada != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage("¿Estás seguro de borrar \"" + tareaSeleccionada.getTitulo() + "\"?")
                    .setPositiveButton("Sí", (dialog, which) -> borrarTarea(tareaSeleccionada.getId()))
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void borrarTarea(int tareaId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Actualizar el estado de la tarea a "ELIMINADA"
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_ESTADO, "ELIMINADA");

        int rowsAffected = database.update(DbHelper.TABLE_TAREAS, values, DbHelper.COLUMN_TAREA_ID + "=?", new String[]{String.valueOf(tareaId)});
        database.close();

        if (rowsAffected > 0) {
            Toast.makeText(this, "Tarea marcada como eliminada", Toast.LENGTH_SHORT).show();
            cargarTareas();
            adapter.notifyDataSetChanged();
            btnBorrar.setEnabled(false);
        } else {
            Toast.makeText(this, "Error al actualizar la tarea", Toast.LENGTH_SHORT).show();
        }
    }
}
