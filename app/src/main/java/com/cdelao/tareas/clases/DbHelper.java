package com.cdelao.tareas.clases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tareas.db";
    private static final int DATABASE_VERSION = 1;

    // Nombres de las tablas
    public static final String TABLE_USUARIOS = "USUARIOS";
    public static final String TABLE_TAREAS = "TAREAS";

    // Columnas de la tabla USUARIOS
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "NOMBRE_USUARIO";
    public static final String COLUMN_PASSWORD = "CONTRASENA";

    // Columnas de la tabla TAREAS
    public static final String COLUMN_TAREA_ID = "ID_TAREA";
    public static final String COLUMN_TITULO = "TITULO";
    public static final String COLUMN_DESCRIPCION = "DESCRIPCION";
    public static final String COLUMN_FECHA_LIMITE = "FECHA_LIMITE";
    public static final String COLUMN_PRIORIDAD = "PRIORIDAD"; // Cambiar a TEXT
    public static final String COLUMN_ESTADO = "ESTADO"; // Completada o Pendiente
    public static final String COLUMN_CATEGORIA = "CATEGORIA"; // Trabajo, Personal, Urgente

    private static final String TABLE_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL);";

    private static final String TABLE_CREATE_TAREAS =
            "CREATE TABLE " + TABLE_TAREAS + " (" +
                    COLUMN_TAREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITULO + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPCION + " TEXT, " +
                    COLUMN_FECHA_LIMITE + " TEXT, " +
                    COLUMN_PRIORIDAD + " TEXT, " + // Cambiar a TEXT
                    COLUMN_ESTADO + " TEXT NOT NULL, " +
                    COLUMN_CATEGORIA + " TEXT);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USUARIOS);
        db.execSQL(TABLE_CREATE_TAREAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAREAS);
        onCreate(db);
    }
}