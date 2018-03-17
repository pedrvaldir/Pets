
package com.example.android.pets;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 *
 * Exibe a lista de animais de estimação que foram inseridos e armazenados no aplicativo.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identificador para o carregador de dados para animais de estimação  */
    private static final int PET_LOADER = 0;

    /**Adaptador para ListView */
    PetCursorAdapter mCursorAdapter;

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

        // Encontre o ListView que será preenchido com os dados do animal de estimação
        ListView petListView = (ListView) findViewById(R.id.list);

        // Encontre e defina a exibição vazia no ListView, de modo que ele só mostra quando a lista possui 0 itens.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Configure um Adaptador para criar um item de lista para cada linha de dados do animal de estimação no Cursor.
        // Ainda não há dados para animais de estimação (até o carregador terminar), então passe nulo para o cursor.
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        //Configure o ouvinte do clique do item
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Crie uma nova intenção para acessar {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Forme o URI de conteúdo que representa o animal de estimação específico que foi clicado,
                // anexando o "id" (passado como entrada para este método) para o
                // {@link PetEntry # CONTENT_URI}.
                // Por exemplo, o URI seria "conteúdo: //com.example.android.pets/pets/2"
                // se o animal de estimação com ID 2 foi clicado.
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                // Defina o URI no campo de dados da intenção
                intent.setData(currentPetUri);

                // Inicie o {@link EditorActivity} para exibir os dados para o animal de estimação atual.
                startActivity(intent);
            }
        });

        // Comece o carregador
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    /**
     * Método auxiliar para inserir dados de animais de estimação codificados no banco de dados. Apenas para fins de depuração.
     */
    private void insertPet() {
        // Crie um objeto ContentValues onde os nomes das colunas são as chaves,
        // e os atributos de animais de Toto são os valores.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insira uma nova linha para o Toto no provedor usando o ContentResolver.
        // Use o {@link PetEntry # CONTENT_URI} para indicar que queremos inserir
        // na tabela do banco de dados de animais de estimação.
        // Receba o novo URI de conteúdo que nos permitirá acessar os dados da Toto no futuro.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    /**
     * Método auxiliar para excluir todos os animais de estimação no banco de dados.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar as opções de menu do arquivo res / menu / menu_catalog.xml.
        // Isso adiciona itens de menu à barra de aplicativos.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // O usuário clicou em uma opção de menu no menu de transbordamento da barra de aplicativos
        switch (item.getItemId()) {
            // Responda a um clique na opção de menu "Inserir dados falsos"
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Responda a um clique na opção de menu "Excluir todas as entradas"
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define uma projeção que especifica as colunas da tabela de que nos preocupamos.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        // Este carregador executará o método de consulta do ContentProvider em uma linha de fundo
        return new CursorLoader(this,   // Parent activity context
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Atualize {@link PetCursorAdapter} com este novo cursor contendo dados de estimação atualizados
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback chamado quando os dados precisam ser excluídos
        mCursorAdapter.swapCursor(null);
    }
}