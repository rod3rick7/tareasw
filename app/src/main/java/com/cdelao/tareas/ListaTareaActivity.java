package com.cdelao.tareas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cdelao.tareas.clases.DbHelper;
import com.cdelao.tareas.modelos.Tarea;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.Predicate;

public class ListaTareaActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private ListView listViewTareas;
    private ArrayList<Tarea> tareas;
    private ArrayAdapter<Tarea> adapter;

    private String lastFilteredPriority = null;
    private String lastFilteredEstado = null;
    private String lastFilteredFecha = null;

    private Button btnQuitarFiltros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarea);

        dbHelper = new DbHelper(this);
        listViewTareas = findViewById(R.id.listViewTareas);
        tareas = new ArrayList<>();

        cargarTareas();
        setupAdapter();

        // Configurar el OnItemClickListener
        listViewTareas.setOnItemClickListener((parent, view, position, id) -> {
            Tarea tareaSeleccionada = tareas.get(position);
            mostrarDetallesTarea(tareaSeleccionada);
        });

        // Botones de filtros
        Button btnFiltrarPrioridad = findViewById(R.id.btnFiltrarPrioridad);
        Button btnFiltrarEstado = findViewById(R.id.btnFiltrarEstado);
        Button btnFiltrarFecha = findViewById(R.id.btnFiltrarFecha);
        btnQuitarFiltros = findViewById(R.id.btnQuitarFiltros);

        btnFiltrarPrioridad.setOnClickListener(v -> mostrarDialogoPrioridad());
        btnFiltrarEstado.setOnClickListener(v -> mostrarDialogoEstado());
        btnFiltrarFecha.setOnClickListener(v -> mostrarDialogoFecha());

        btnQuitarFiltros.setOnClickListener(v -> clearFilters());
        btnQuitarFiltros.setVisibility(View.GONE); // Oculto inicialmente
    }

    private void mostrarDetallesTarea(Tarea tarea) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DETALLES")
                .setMessage("Título: " + tarea.getTitulo() + "\n" +
                        "Descripción: " + tarea.getDescripcion() + "\n" +
                        "Fecha Límite: " + tarea.getFechaLimite() + "\n" +
                        "Prioridad: " + tarea.getPrioridad() + "\n" +
                        "Estado: " + tarea.getEstado() + "\n" +
                        "Categoría: " + tarea.getCategoria()) // Si tienes este atributo
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void cargarTareas() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + DbHelper.TABLE_TAREAS +
                        " WHERE " + DbHelper.COLUMN_ESTADO + " IN (?, ?)",
                new String[]{"PENDIENTE", "COMPLETADO"});

        tareas.clear();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_TAREA_ID));
            @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TITULO));
            @SuppressLint("Range") String descripcion = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_DESCRIPCION));
            @SuppressLint("Range") String fechaLimite = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_FECHA_LIMITE));
            @SuppressLint("Range") String prioridad = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_PRIORIDAD));
            @SuppressLint("Range") String estado = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ESTADO));
            @SuppressLint("Range") String categoria = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CATEGORIA)); // Asegúrate de que esta columna existe

            Tarea tarea = new Tarea(id, titulo, descripcion, fechaLimite, prioridad, estado, categoria); // Incluir categoría
            tareas.add(tarea);
        }
        cursor.close();
        database.close();
    }


    private void setupAdapter() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tareas);
        listViewTareas.setAdapter(adapter);
    }

    private void mostrarDialogoPrioridad() {
        String[] opciones = getResources().getStringArray(R.array.prioridades_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Prioridad")
                .setItems(opciones, (dialog, which) -> {
                    String selected = opciones[which];
                    if (!selected.equals(lastFilteredPriority)) {
                        lastFilteredPriority = selected;
                        lastFilteredEstado = null; // Limpiar otros filtros
                        lastFilteredFecha = null; // Limpiar otros filtros
                        btnQuitarFiltros.setVisibility(View.VISIBLE); // Mostrar botón
                        filtrarPorPrioridad(selected);
                    } else {
                        clearFilters();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEstado() {
        String[] opciones = getResources().getStringArray(R.array.estados_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Estado")
                .setItems(opciones, (dialog, which) -> {
                    String selected = opciones[which];
                    if (!selected.equals(lastFilteredEstado)) {
                        lastFilteredEstado = selected;
                        lastFilteredPriority = null; // Limpiar otros filtros
                        lastFilteredFecha = null; // Limpiar otros filtros
                        btnQuitarFiltros.setVisibility(View.VISIBLE); // Mostrar botón
                        filtrarPorEstado(selected);
                    } else {
                        clearFilters();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoFecha() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    if (!selectedDate.equals(lastFilteredFecha)) {
                        lastFilteredFecha = selectedDate;
                        lastFilteredPriority = null; // Limpiar otros filtros
                        lastFilteredEstado = null; // Limpiar otros filtros
                        btnQuitarFiltros.setVisibility(View.VISIBLE); // Mostrar botón
                        filtrarPorFecha(selectedDate);
                    } else {
                        clearFilters();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
    //
    private void filtrarPorPrioridad(String prioridad) {
        filtrarTareas(tarea -> tarea.getPrioridad().equals(prioridad));
    }

    private void filtrarPorEstado(String estado) {
        filtrarTareas(tarea -> tarea.getEstado().equals(estado));
    }

    private void filtrarPorFecha(String fecha) {
        filtrarTareas(tarea -> tarea.getFechaLimite().equals(fecha)); // Filtrar por fecha
    }

    private void filtrarTareas(Predicate<Tarea> predicate) {
        ArrayList<Tarea> tareasFiltradas = new ArrayList<>();
        for (Tarea tarea : tareas) {
            if (predicate.test(tarea)) {
                tareasFiltradas.add(tarea);
            }
        }

        adapter.clear();
        if (tareasFiltradas.isEmpty()) {
            Toast.makeText(this, "No hay registros con este filtro", Toast.LENGTH_SHORT).show();
        } else {
            adapter.addAll(tareasFiltradas);
        }
        adapter.notifyDataSetChanged();
    }

    private void clearFilters() {
        lastFilteredPriority = null;
        lastFilteredEstado = null;
        lastFilteredFecha = null;

        cargarTareas(); // Recargar solo las tareas PENDIENTES
        setupAdapter(); // Reiniciar el adaptador
        btnQuitarFiltros.setVisibility(View.GONE); // Ocultar botón
    }
}
