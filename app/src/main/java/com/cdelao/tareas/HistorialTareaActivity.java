package com.cdelao.tareas;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cdelao.tareas.clases.DbHelper;
import com.cdelao.tareas.modelos.Tarea;

import java.util.ArrayList;

public class HistorialTareaActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private ListView listViewHistorialCompletadas;
    private ListView listViewHistorialEliminadas;
    private ArrayList<Tarea> tareasCompletadas;
    private ArrayList<Tarea> tareasEliminadas;
    private ArrayAdapter<Tarea> adapterCompletadas;
    private ArrayAdapter<Tarea> adapterEliminadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_tarea);

        dbHelper = new DbHelper(this);
        listViewHistorialCompletadas = findViewById(R.id.listViewHistorialCompletadas);
        listViewHistorialEliminadas = findViewById(R.id.listViewHistorialEliminadas);
        tareasCompletadas = new ArrayList<>();
        tareasEliminadas = new ArrayList<>();

        cargarTareasCompletadas();
        cargarTareasEliminadas();
        mostrarTareasCompletadas();
        mostrarTareasEliminadas();

        // Configurar el listener para las tareas completadas
        listViewHistorialCompletadas.setOnItemClickListener((parent, view, position, id) -> {
            Tarea tareaSeleccionada = tareasCompletadas.get(position);
            mostrarDetallesTarea(tareaSeleccionada, true); // Pasar true para indicar que es completada
        });

        // Configurar el listener para las tareas eliminadas
        listViewHistorialEliminadas.setOnItemClickListener((parent, view, position, id) -> {
            Tarea tareaSeleccionada = tareasEliminadas.get(position);
            mostrarDetallesTarea(tareaSeleccionada, false); // Pasar false para indicar que es eliminada
        });
    }

    @SuppressLint("Range")
    private void cargarTareasCompletadas() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DbHelper.TABLE_TAREAS,
                new String[]{"*"}, // Cargar todos los campos
                DbHelper.COLUMN_ESTADO + " = ?",
                new String[]{"COMPLETADO"},
                null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_TAREA_ID));
            String titulo = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TITULO));
            String descripcion = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPCION));
            String fechaLimite = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_FECHA_LIMITE));
            String prioridad = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_PRIORIDAD));
            String estado = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ESTADO));
            String categoria = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CATEGORIA));

            Tarea tarea = new Tarea(id, titulo, descripcion, fechaLimite, prioridad, estado, categoria);
            tareasCompletadas.add(tarea);
        }
        cursor.close();
        database.close();
    }

    @SuppressLint("Range")
    private void cargarTareasEliminadas() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DbHelper.TABLE_TAREAS,
                new String[]{"*"}, // Cargar todos los campos
                DbHelper.COLUMN_ESTADO + " = ?",
                new String[]{"ELIMINADA"},
                null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_TAREA_ID));
            String titulo = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TITULO));
            String descripcion = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPCION));
            String fechaLimite = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_FECHA_LIMITE));
            String prioridad = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_PRIORIDAD));
            String estado = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ESTADO));
            String categoria = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CATEGORIA));

            Tarea tarea = new Tarea(id, titulo, descripcion, fechaLimite, prioridad, estado, categoria);
            tareasEliminadas.add(tarea);
        }
        cursor.close();
        database.close();
    }

    private void mostrarTareasCompletadas() {
        if (tareasCompletadas.isEmpty()) {
            Toast.makeText(this, "No hay tareas completadas", Toast.LENGTH_SHORT).show();
        } else {
            adapterCompletadas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tareasCompletadas);
            listViewHistorialCompletadas.setAdapter(adapterCompletadas);
        }
    }

    private void mostrarTareasEliminadas() {
        if (tareasEliminadas.isEmpty()) {
            Toast.makeText(this, "No hay tareas eliminadas", Toast.LENGTH_SHORT).show();
        } else {
            adapterEliminadas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tareasEliminadas);
            listViewHistorialEliminadas.setAdapter(adapterEliminadas);
        }
    }

    private void mostrarDetallesTarea(Tarea tarea, boolean esCompletada) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SpannableStringBuilder mensaje = new SpannableStringBuilder();

        mensaje.append("Título: ").append(tarea.getTitulo()).append("\n")
                .append("Descripción: ").append(tarea.getDescripcion()).append("\n")
                .append("Fecha Límite: ").append(tarea.getFechaLimite()).append("\n")
                .append("Prioridad: ").append(tarea.getPrioridad()).append("\n")
                .append("Categoría: ").append(tarea.getCategoria()).append("\n")
                .append("Estado: ").append(tarea.getEstado());

        // Aplicar negrita a los títulos
        int endIndex = 0;
        for (String title : new String[]{"Título:", "Descripción:", "Fecha Límite:", "Prioridad:", "Categoría:", "Estado:"}) {
            endIndex = mensaje.toString().indexOf(title) + title.length();
            mensaje.setSpan(new StyleSpan(Typeface.BOLD), mensaje.toString().indexOf(title), endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        builder.setTitle("DETALLES")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .setNegativeButton("Restaurar", (dialog, which) -> {
                    if (esCompletada) {
                        restaurarTareaCompletada(tarea);
                    } else {
                        restaurarTareaEliminada(tarea);
                    }
                })
                .show();
    }

    private void restaurarTareaCompletada(Tarea tarea) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_ESTADO, "PENDIENTE");

        // Actualiza la tarea en la base de datos
        database.update(DbHelper.TABLE_TAREAS, values, DbHelper.COLUMN_TAREA_ID + " = ?", new String[]{String.valueOf(tarea.getId())});
        database.close();

        // Elimina la tarea de la lista en memoria
        tareasCompletadas.remove(tarea);
        adapterCompletadas.notifyDataSetChanged(); // Actualiza la vista

        Toast.makeText(this, "Tarea restaurada", Toast.LENGTH_SHORT).show();
    }

    private void restaurarTareaEliminada(Tarea tarea) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_ESTADO, "PENDIENTE");

        // Actualiza la tarea en la base de datos
        database.update(DbHelper.TABLE_TAREAS, values, DbHelper.COLUMN_TAREA_ID + " = ?", new String[]{String.valueOf(tarea.getId())});
        database.close();

        // Elimina la tarea de la lista en memoria
        tareasEliminadas.remove(tarea);
        adapterEliminadas.notifyDataSetChanged(); // Actualiza la vista

        Toast.makeText(this, "Tarea restaurada", Toast.LENGTH_SHORT).show();
    }
}
