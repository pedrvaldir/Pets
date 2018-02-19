
package com.example.android.pets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 *Permite ao usuário criar um novo animal de estimação ou editar um existente.
 */
public class EditorActivity extends AppCompatActivity {

    /** EditText campo para inserir o nome do cachorro*/
    private EditText mNameEditText;

    /** EditText campo para inserir a raça do cachorro */
    private EditText mBreedEditText;

    /** EditText campo para inserir o peso do cachorro */
    private EditText mWeightEditText;

    /** EditText campo para entrar no gênero do cachorro*/
    private Spinner mGenderSpinner;


    /**
     * Sexo dos cachorros. Os valores possíveis são
     * 0 para sexo desconhecido, 1 para macho, 2 para femea.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Encontre todas as visualizações relevantes que precisaremos para ler a entrada do usuário de
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    /**
     * Configure o spinner suspenso que permite ao usuário selecionar o gênero do animal de estimação.
     */
    private void setupSpinner() {
        // Crie um adaptador spinner. As opções de lista são da matriz String que usará
        // o spinner usará o layout padrão
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Especificar estilo de layout suspenso - exibição de lista simples com 1 item por linha
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Aplique o adaptador no  Spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Defina o inteiro mSeleccionado para os valores constantes
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Como AdapterView é uma classe abstrata, onNothingSelected deve ser definido
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     *Obter entrada de usuários do editor e salvar novos animais de estimação no banco de dados.
     */
    private void insertPet() {
        // Ler dos campos de entrada
        // Use aparar para eliminar o espaço em branco principal ou posterior
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weight = Integer.parseInt(weightString);

        // Criar ajudante de banco de dados
        PetDbHelper mDbHelper = new PetDbHelper(this);

        //Obtém o banco de dados no modo de gravação
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Crie um objeto ContentValues onde os nomes das colunas são as chaves,
        // e os atributos de animal de estimação do editor são os valores.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        // Insira uma nova linha para o animal de estimação no banco de dados, retornando o ID dessa nova linha.
        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);

        // Mostra uma mensagem de brinde dependendo se a inserção foi ou não bem sucedida
        if (newRowId == -1) {
            // Se a ID da linha for -1, então houve um erro na inserção.
            Toast.makeText(this, "Erro ao salvar os animais", Toast.LENGTH_SHORT).show();
        } else {
            // Caso contrário, a inserção foi bem sucedida e podemos exibir um brinde com a ID da linha.

            Toast.makeText(this, "Cachorro salvo com identificação de linha: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflar as opções de menu do arquivo res / menu / menu_editor.xml.
        // Isso adiciona itens de menu à barra de aplicativos.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // O usuário clicou em uma opção de menu no menu de transbordamento da barra de aplicativos
        switch (item.getItemId()) {
            //Responda a um clique na opção de menu "Salvar"
            case R.id.action_save:
                // Salvar cachorro para o banco de dados
                insertPet();
                //finaliza atividade
                finish();
                return true;
            //  Responda a um clique na opção de menu "Excluir"
            case R.id.action_delete:
                //  Não faça nada por enquanto
                return true;
            //Responda a um clique no botão de seta "Para cima" na barra de aplicativos
            case android.R.id.home:
                //  Navegue de volta para a atividade pai (atividade do catálogo)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}