package com.quantum.tareas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quantum.tareas.clases.DbHelper;
import com.quantum.tareas.modelos.Tarea;

import java.util.ArrayList;
import java.util.List;

public class BorrarTareaActivity extends AppCompatActivity {

    private ListView tareasListView;
    private Button borrarButton;
    private DbHelper dbHelper;
    private List<Tarea> tareas;
    private boolean[] checkedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrar_tarea);

        tareasListView = findViewById(R.id.tareasListView);
        borrarButton = findViewById(R.id.borrarButton);
        dbHelper = new DbHelper();
        tareas = new ArrayList<>();

        // Obtener usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbHelper.obtenerTareas(userId, new DbHelper.TaskCallback<List<Tarea>>() {
                @Override
                public void onSuccess(List<Tarea> result) {
                    tareas = new ArrayList<>();
                    // Filtrar solo tareas que no estén eliminadas
                    for (Tarea tarea : result) {
                        if (!"ELIMINADO".equals(tarea.getEstado())) {
                            tareas.add(tarea);
                        }
                    }
                    mostrarTareas();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(BorrarTareaActivity.this, "Error al obtener tareas", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Configurar el botón de eliminar tareas
        borrarButton.setOnClickListener(v -> mostrarConfirmacion());
        borrarButton.setVisibility(View.GONE); // Inicialmente ocultar el botón

        // Listener para manejar la selección de tareas
        tareasListView.setOnItemClickListener((parent, view, position, id) -> {
            // Verificar si hay alguna tarea seleccionada
            boolean hasSelectedTasks = false;
            for (int i = 0; i < tareas.size(); i++) {
                if (tareasListView.isItemChecked(i)) {
                    hasSelectedTasks = true;
                    break;
                }
            }
            // Mostrar u ocultar el botón de eliminar
            borrarButton.setVisibility(hasSelectedTasks ? View.VISIBLE : View.GONE);
        });
    }

    private void mostrarTareas() {
        String[] nombresTareas = new String[tareas.size()];
        checkedItems = new boolean[tareas.size()];

        for (int i = 0; i < tareas.size(); i++) {
            nombresTareas[i] = tareas.get(i).getTitulo();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, nombresTareas);
        tareasListView.setAdapter(adapter);
        tareasListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void mostrarConfirmacion() {
        final List<String> tareasSeleccionadas = new ArrayList<>();

        for (int i = 0; i < tareas.size(); i++) {
            if (tareasListView.isItemChecked(i)) {
                tareasSeleccionadas.add(tareas.get(i).getTitulo());
            }
        }

        if (tareasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "No se ha seleccionado ninguna tarea", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensaje = tareasSeleccionadas.size() == 1
                ? "Se eliminará la siguiente tarea:\n\n" // Mensaje singular con salto de línea
                : "Se eliminarán las siguientes tareas:\n\n"; // Mensaje plural con salto de línea

        StringBuilder detalles = new StringBuilder(mensaje);
        for (String tarea : tareasSeleccionadas) {
            detalles.append(" • ").append(tarea).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("ELIMINACIÓN")
                .setMessage(detalles.toString())
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarTareas(tareasSeleccionadas);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTareas(List<String> tareasSeleccionadas) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            for (String tareaTitulo : tareasSeleccionadas) {
                for (Tarea tarea : tareas) {
                    if (tarea.getTitulo().equals(tareaTitulo)) {
                        tarea.setEstado("ELIMINADO");
                        dbHelper.editarTarea(userId, tarea);
                    }
                }
            }
            // Muestra el Toast solo una vez después de eliminar todas las tareas
            Toast.makeText(this, "Se ha eliminado correctamente", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad
        } else {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
        }
    }
}
