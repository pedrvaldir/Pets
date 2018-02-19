
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;


/**
 * Exibe a lista de animais de estimação que foram inseridos e armazenados no aplicativo.
 */
public class CatalogActivity extends AppCompatActivity {

    /** Database helper que nos dará acesso ao banco de dados */
    private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Configurar FAB para abrir o EditorActividade
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Para acessar nosso banco de dados, instanciamos nossa subclasse de SQLiteOpenHelper
        //e passar o contexto, que é a atividade atual.
        mDbHelper = new PetDbHelper(this);

        //displayDatabaseInfo();
    }

    @Override
    protected void onStart(){
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Método auxiliar temporário para exibir informações no TextView na tela sobre o estado de
     * o banco de dados de animais de estimação.
     */
    private void displayDatabaseInfo() {

        // Crie e/ou abra um banco de dados para ler
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        Cursor cursor = db.query(
                PetEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        TextView displayView = (TextView) findViewById(R.id.text_view_pet);

        try {

           displayView.setText("A tabela de cachorros contém:"  + cursor.getCount() + "cachorros.\n\n");
           displayView.append(PetEntry._ID + "-" +
                   PetEntry.COLUMN_PET_NAME +"-"+
                   PetEntry.COLUMN_PET_BREED +"-"+
                   PetEntry.COLUMN_PET_GENDER + "-"+
                   PetEntry.COLUMN_PET_WEIGHT+"\n");

            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            while (cursor.moveToNext()) {

            int currentID = cursor.getInt(idColumnIndex);
            String currentName = cursor.getString(nameColumnIndex);
            String currentBreed = cursor.getString(breedColumnIndex);
            int currentGender = cursor.getInt(genderColumnIndex);
            int currentWeight = cursor.getInt(weightColumnIndex);

            displayView.append(("\n" + currentID + " - " +
                    currentName + " - " +
                    currentBreed + " - " +
                    currentGender + " - " +
                    currentWeight));
            }

        } finally {


            // Sempre feche o cursor quando terminar de ler. Isso liberta todos os seus
            // recursos e torna inválido.
            cursor.close();
        }
    }

    /**
     * Método auxiliar para inserir dados de animais de estimação codificados no banco de dados. Apenas para fins de depuração.
     */
    private void insertPet() {
        //Obtém o banco de dados no modo de gravação
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Crie um objeto ContentValues ​​onde os nomes das colunas são as chaves,
        // e os atributos de animais de Toto são os valores.

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insira uma nova linha para o Toto no banco de dados, retornando o ID dessa nova linha.
        // O primeiro argumento para db.insert () é o nome da tabela de animais de estimação.
        // O segundo argumento fornece o nome de uma coluna na qual o framework
        // pode inserir NULL no caso de o ContentValues ​​estar vazio (se
        // Isto é definido como "nulo", então a estrutura não irá inserir uma linha quando
        // não há valores).
        // O terceiro argumento é o objeto ContentValues ​​que contém a informação para o Toto.
        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);

        Log.v("CatalogActivity", "new row id" + newRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar as opções de menu do arquivo res / menu / menu_catalog.xml
        // Isso adiciona itens de menu à barra de aplicativos.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //// O usuário clicou em uma opção de menu no menu de transbordamento da barra de aplicativos
        switch (item.getItemId()) {
            // Responda a um clique na opção de menu "Inserir dados falsos"
            case R.id.action_insert_dummy_data:
                insertPet();
                displayDatabaseInfo();
                return true;
            // Responda a um clique na opção de menu "Excluir todas as entradas"
            case R.id.action_delete_all_entries:
                //Não faça nada por enquanto
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
