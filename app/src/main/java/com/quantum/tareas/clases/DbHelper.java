package com.quantum.tareas.clases;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quantum.tareas.modelos.Tarea;
import com.quantum.tareas.modelos.Usuario;

import java.util.ArrayList;
import java.util.List;

public class DbHelper {
    private DatabaseReference databaseReference;

    public DbHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Registrar un nuevo usuario
    public void registrarUsuario(Usuario usuario) {
        databaseReference.child("usuarios").child(usuario.getId()).setValue(usuario);
    }

    // Agregar una nueva tarea
    public void agregarTarea(String userId, Tarea tarea) {
        databaseReference.child("usuarios").child(userId).child("tareas").child(tarea.getId()).setValue(tarea)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tarea guardada con éxito
                    } else {
                        // Manejo de errores
                        Log.e("DbHelper", "Error al agregar tarea", task.getException());
                    }
                });
    }

    // Obtener todas las tareas de un usuario
    public void obtenerTareas(String userId, final TaskCallback<List<Tarea>> callback) {
        databaseReference.child("usuarios").child(userId).child("tareas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Tarea> tareas = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tarea tarea = snapshot.getValue(Tarea.class);
                    if (tarea != null) {
                        tareas.add(tarea);
                    }
                }
                callback.onSuccess(tareas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    // Eliminar una tarea
    public void eliminarTarea(String userId, String tareaId) {
        databaseReference.child("usuarios").child(userId).child("tareas").child(tareaId).removeValue();
    }

    // Editar una tarea
    public void editarTarea(String userId, Tarea tarea) {
        databaseReference.child("usuarios").child(userId).child("tareas").child(tarea.getId()).setValue(tarea);
    }

    // Interfaz de callback para obtener tareas
    public interface TaskCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
