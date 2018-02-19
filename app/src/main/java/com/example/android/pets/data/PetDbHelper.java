package com.example.android.pets.data;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    /** Nome do arquivo de Banco de Dados */
    private static final String DATABASE_NAME = "shelter.db";
    /**
     * Versão do banco de dados. Se você alterar o esquema do banco de dados, você deve incrementar a versão do banco de dados.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constrói uma nova instância de {@link PetDbHelper}.
     *
     * @param context do aplicativo
     */
    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Essa é a chamada quando o banco de dados é criado pela primeira vez.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crie uma String que contenha o esquema SQL para criar uma tabela Pets
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + PetEntry.TABLE_NAME + " ("
                + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        // 	Execute a instrução SQL
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * Isso é chamado quando o banco de dados precisa ser atualizado.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // O banco de dados ainda está na versão 1, então não há nada que seja feito aqui.
    }

}