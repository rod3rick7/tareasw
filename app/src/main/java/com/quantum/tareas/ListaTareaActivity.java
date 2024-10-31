package com.quantum.tareas;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.quantum.tareas.clases.DbHelper;
import com.quantum.tareas.modelos.Tarea;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListaTareaActivity extends AppCompatActivity {

    private ListView tareasListView;
    private Button completarButton, filtroPendienteButton, filtroCompletadoButton, filtroPrioridadButton, filtroFechaButton;
    private DbHelper dbHelper;
    private List<Tarea> tareas;
    private List<Tarea> tareasFiltradas;
    private String filtroActivo = "";
    private int prioridadSeleccionada = -1;
    private String fechaSeleccionada = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarea);

        tareasListView = findViewById(R.id.listViewTareas);
        completarButton = findViewById(R.id.btnMarcarComoCompletado);
        filtroPendienteButton = findViewById(R.id.btnFiltroPendiente);
        filtroCompletadoButton = findViewById(R.id.btnFiltroCompletado);
        filtroPrioridadButton = findViewById(R.id.btnFiltroPrioridad);
        filtroFechaButton = findViewById(R.id.btnFiltroFecha);

        dbHelper = new DbHelper();
        tareas = new ArrayList<>();
        tareasFiltradas = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbHelper.obtenerTareas(userId, new DbHelper.TaskCallback<List<Tarea>>() {
                @Override
                public void onSuccess(List<Tarea> result) {
                    tareas = result;
                    aplicarFiltro("PENDIENTE"); // Mostrar solo tareas pendientes inicialmente
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ListaTareaActivity.this, "Error al obtener tareas", Toast.LENGTH_SHORT).show();
                }
            });
        }

        completarButton.setOnClickListener(v -> {
            int position = tareasListView.getCheckedItemPosition();
            if (position >= 0) {
                Tarea tareaSeleccionada = tareasFiltradas.get(position);
                String nuevoEstado = "COMPLETADO".equals(tareaSeleccionada.getEstado()) ? "PENDIENTE" : "COMPLETADO";
                tareaSeleccionada.setEstado(nuevoEstado);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    dbHelper.editarTarea(userId, tareaSeleccionada);
                }

                Toast.makeText(ListaTareaActivity.this, "Tarea marcada como " + nuevoEstado.toLowerCase(), Toast.LENGTH_SHORT).show();

                // Actualizar la lista de tareas y el botón
                mostrarTareas();
                actualizarBotonCompletar();
            } else {
                Toast.makeText(ListaTareaActivity.this, "No se ha seleccionado ninguna tarea", Toast.LENGTH_SHORT).show();
            }
        });

        completarButton.setVisibility(View.GONE); // Inicialmente ocultar el botón

        tareasListView.setOnItemClickListener((parent, view, position, id) -> {
            tareasListView.clearChoices();
            tareasListView.setItemChecked(position, true);
            actualizarBotonCompletar();
        });

        // Configurar el OnItemLongClickListener para mostrar detalles
        tareasListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Tarea tareaSeleccionada = tareasFiltradas.get(position);
            mostrarDetallesTarea(tareaSeleccionada);
            return true; // Indica que el evento se ha manejado
        });

        filtroPendienteButton.setOnClickListener(v -> aplicarFiltro("PENDIENTE"));
        filtroCompletadoButton.setOnClickListener(v -> aplicarFiltro("COMPLETADO"));
        filtroPrioridadButton.setOnClickListener(v -> mostrarDialogoPrioridad());
        filtroFechaButton.setOnClickListener(v -> mostrarDatePicker());
    }

    // Método para mostrar los detalles de la tarea
    private void mostrarDetallesTarea(Tarea tarea) {
        SpannableStringBuilder mensaje = new SpannableStringBuilder();

        // Agregar cada línea con negrita donde se necesita
        mensaje.append("Título:\n").append(tarea.getTitulo()).append("\n\n");
        mensaje.append("Descripción:\n").append(tarea.getDescripcion()).append("\n\n");
        mensaje.append("Fecha Límite:\n").append(tarea.getFechaLimite()).append("\n\n");
        mensaje.append("Categoría:\n").append(tarea.getCategoria()).append("\n\n");
        mensaje.append("Prioridad:\n").append(String.valueOf(tarea.getPrioridad())).append("\n\n");
        mensaje.append("Estado:\n").append(tarea.getEstado());

        // Aplicar negrita a los títulos de las secciones
        int start = 0;
        String[] lineas = mensaje.toString().split("\n");
        for (String linea : lineas) {
            int length = linea.length();
            if (linea.contains(":")) {
                mensaje.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, start + linea.indexOf(':') + 1, 0);
            }
            start += length + 1; // +1 para la nueva línea
        }

        new AlertDialog.Builder(this)
                .setTitle("DETALLES")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarTareas() {
        tareasFiltradas.clear();
        for (Tarea tarea : tareas) {
            boolean incluir = false;
            if (filtroActivo.isEmpty() || tarea.getEstado().equals(filtroActivo)) {
                incluir = true;
            } else if (filtroActivo.equals("PRIORIDAD") && tarea.getPrioridad() == prioridadSeleccionada) {
                incluir = true;
            } else if (filtroActivo.equals("FECHA") && tarea.getFechaLimite().equals(fechaSeleccionada)) {
                incluir = true;
            }

            if (incluir) {
                tareasFiltradas.add(tarea);
            }
        }

        if (tareasFiltradas.isEmpty()) {
            Toast.makeText(this, "No hay registros con este filtro", Toast.LENGTH_SHORT).show();
        }

        String[] nombresTareas = new String[tareasFiltradas.size()];
        for (int i = 0; i < tareasFiltradas.size(); i++) {
            nombresTareas[i] = tareasFiltradas.get(i).getTitulo();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, nombresTareas);
        tareasListView.setAdapter(adapter);
        tareasListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        completarButton.setVisibility(View.GONE);
    }


    private void aplicarFiltro(String estado) {
        filtroActivo = estado;
        mostrarTareas();
        completarButton.setVisibility(View.GONE);
    }

    private void actualizarBotonCompletar() {
        int position = tareasListView.getCheckedItemPosition();
        if (position >= 0) {
            Tarea tareaSeleccionada = tareasFiltradas.get(position);
            completarButton.setText("COMPLETADO".equals(tareaSeleccionada.getEstado()) ? "Marcar como Pendiente" : "Marcar como Completado");
            completarButton.setVisibility(View.VISIBLE);
        } else {
            completarButton.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoPrioridad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Prioridad");

        final SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(5);
        seekBar.setProgress(1);
        builder.setView(seekBar);

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            prioridadSeleccionada = seekBar.getProgress();
            aplicarFiltroPorPrioridad(prioridadSeleccionada);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void aplicarFiltroPorPrioridad(int prioridad) {
        filtroActivo = "PRIORIDAD"; // Actualizar el filtro activo
        mostrarTareas(); // Mostrar tareas filtradas
        completarButton.setVisibility(View.GONE);
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String diaFormateado = String.format("%02d", selectedDay);
            String mesFormateado = String.format("%02d", selectedMonth + 1);
            fechaSeleccionada = diaFormateado + "-" + mesFormateado + "-" + selectedYear;
            aplicarFiltroPorFecha(fechaSeleccionada);
        }, year, month, day);

        datePickerDialog.setTitle("Seleccionar Fecha");
        datePickerDialog.show();
    }

    private void aplicarFiltroPorFecha(String fecha) {
        filtroActivo = "FECHA"; // Actualizar el filtro activo
        mostrarTareas(); // Mostrar tareas filtradas
        completarButton.setVisibility(View.GONE);
    }
}
