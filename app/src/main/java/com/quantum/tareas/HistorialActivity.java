package com.quantum.tareas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.quantum.tareas.clases.DbHelper;
import com.quantum.tareas.modelos.Tarea;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private ListView tareasListView;
    private Button restoreButton;
    private Button eliminarButton; // Botón para eliminar permanentemente
    private DbHelper dbHelper;
    private List<Tarea> tareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        tareasListView = findViewById(R.id.tareasListView);
        restoreButton = findViewById(R.id.toggleButton);
        eliminarButton = findViewById(R.id.eliminarButton); // Inicializar el botón de eliminar
        dbHelper = new DbHelper();
        tareas = new ArrayList<>();

        cargarTareas();

        restoreButton.setOnClickListener(v -> restaurarTareasSeleccionadas());
        eliminarButton.setOnClickListener(v -> eliminarTareasSeleccionadas()); // Manejar el clic en eliminar

        tareasListView.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarDetallesTarea(tareas.get(position));
            return true; // Indica que el evento ha sido manejado
        });
    }

    private void cargarTareas() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbHelper.obtenerTareas(userId, new DbHelper.TaskCallback<List<Tarea>>() {
                @Override
                public void onSuccess(List<Tarea> result) {
                    tareas.clear();
                    for (Tarea tarea : result) {
                        // Solo se muestran tareas eliminadas
                        if ("ELIMINADO".equals(tarea.getEstado())) {
                            tareas.add(tarea);
                        }
                    }
                    mostrarTareas();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(HistorialActivity.this, "Error al cargar tareas", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarTareas() {
        String[] tareasArray = new String[tareas.size()];

        for (int i = 0; i < tareas.size(); i++) {
            tareasArray[i] = tareas.get(i).getTitulo(); // Mostrar solo el título
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, tareasArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Aplicar estilo tachado si la tarea está eliminada
                if ("ELIMINADO".equals(tareas.get(position).getEstado())) {
                    textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    textView.setPaintFlags(textView.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                }
                return view;
            }
        };

        tareasListView.setAdapter(adapter);
        tareasListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }


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

    private void restaurarTareasSeleccionadas() {
        List<String> tareasSeleccionadas = new ArrayList<>();
        for (int i = 0; i < tareasListView.getCount(); i++) {
            if (tareasListView.isItemChecked(i)) {
                tareasSeleccionadas.add(tareas.get(i).getTitulo());
            }
        }

        if (tareasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "No se ha seleccionado ninguna tarea", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir el mensaje con el formato deseado
        StringBuilder mensajeBuilder = new StringBuilder();
        if (tareasSeleccionadas.size() == 1) {
            mensajeBuilder.append("¿Deseas restaurar esta tarea?\n");
        } else {
            mensajeBuilder.append("¿Deseas restaurar estas tareas?\n");
        }

        // Agregar las tareas al mensaje
        for (String titulo : tareasSeleccionadas) {
            mensajeBuilder.append("• ").append(titulo).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("RESTAURACIÓN")
                .setMessage(mensajeBuilder.toString())
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    int countRestauradas = 0; // Contador de tareas restauradas
                    for (String titulo : tareasSeleccionadas) {
                        for (Tarea tarea : tareas) {
                            if (tarea.getTitulo().equals(titulo)) {
                                restaurarTarea(tarea);
                                countRestauradas++; // Incrementar el contador
                            }
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void restaurarTarea(Tarea tarea) {
        tarea.setEstado("PENDIENTE");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbHelper.editarTarea(userId, tarea);
            Toast.makeText(this, "Tarea restaurada", Toast.LENGTH_SHORT).show();
            cargarTareas(); // Recargar tareas para reflejar el cambio
        } else {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarTareasSeleccionadas() {
        List<Tarea> tareasSeleccionadas = new ArrayList<>();
        for (int i = 0; i < tareasListView.getCount(); i++) {
            if (tareasListView.isItemChecked(i)) {
                tareasSeleccionadas.add(tareas.get(i)); // Agregar la tarea completa
            }
        }

        if (tareasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "No se ha seleccionado ninguna tarea", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mensaje de confirmación
        StringBuilder mensajeBuilder = new StringBuilder();
        if (tareasSeleccionadas.size() == 1) {
            mensajeBuilder.append("¿Deseas eliminar esta tarea permanentemente? Esta acción no se puede deshacer\n");
        } else {
            mensajeBuilder.append("¿Deseas eliminar estas tareas permanentemente? Esta acción no se puede deshacer\n");
        }

        for (Tarea tarea : tareasSeleccionadas) {
            mensajeBuilder.append(" • ").append(tarea.getTitulo()).append("\n"); // Mostrar títulos de las tareas
        }

        new AlertDialog.Builder(this)
                .setTitle("ELIMINACIÓN")
                .setMessage(mensajeBuilder.toString())
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    for (Tarea tarea : tareasSeleccionadas) {
                        eliminarTarea(tarea.getId()); // Llama al método con el ID de la tarea
                    }
                    // Mostrar el toast una sola vez después de eliminar todas las tareas
                    Toast.makeText(this, "Tareas eliminadas permanentemente", Toast.LENGTH_SHORT).show();
                    cargarTareas(); // Recargar tareas para reflejar el cambio
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTarea(String tareaId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbHelper.eliminarTarea(userId, tareaId);
            // No se muestra el toast aquí
        } else {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
        }
    }

}
