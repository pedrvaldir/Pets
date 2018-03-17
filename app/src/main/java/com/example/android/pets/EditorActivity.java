
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Permite ao usuário criar um novo animal de estimação ou editar um existente.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identificador para o carregador de dados para animais de estimação */
    private static final int EXISTING_PET_LOADER = 0;

    /** URI de conteúdo para o animal de estimação existente (nulo se é um novo animal de estimação) */
    private Uri mCurrentPetUri;

    /** Campo EditText para inserir o nome do animal de estimação*/
    private EditText mNameEditText;

    /** Campo EditText para inserir a raça do animal de estimação */
    private EditText mBreedEditText;

    /**Campo EditText para inserir o peso do animal de estimação */
    private EditText mWeightEditText;

    /** Campo EditText para inserir o gênero do animal de estimação */
    private Spinner mGenderSpinner;

    /**
     *Sexo do animal de estimação. Os possíveis valores válidos estão no arquivo PetContract.java:
     * {@link PetEntry#GENDER_UNKNOWN}, {@link PetEntry#GENDER_MALE}, or
     * {@link PetEntry#GENDER_FEMALE}.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    /** Flag booleana que acompanha se o animal de estimação foi editado (verdadeiro) ou não (falso) */
    private boolean mPetHasChanged = false;

    /**
     * OnTouchListener que escuta qualquer usuário toca em uma Vista, implicando que eles estão modificando
     * a vista, e mudamos o booleano mPetHasChanged para verdadeiro.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine a intenção que foi usada para iniciar esta atividade,
        // para descobrir se estamos criando um novo animal de estimação ou editando um existente.
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        // Se a intenção NÃO contiver URI de conteúdo para animais de estimação, então sabemos que somos
        // criando um novo animal de estimação.
        if (mCurrentPetUri == null) {
            // Este é um novo animal de estimação, então altere a barra do aplicativo para dizer "Adicionar um animal de estimação"
            setTitle(getString(R.string.editor_activity_title_new_pet));

            // Invalide o menu de opções, então a opção de menu "Excluir" pode ser oculta.
            // (Não faz sentido excluir um animal de estimação que ainda não foi criado).
            invalidateOptionsMenu();
        } else {
            // Caso contrário, este é um animal de estimação existente, então altere a barra de aplicativos para dizer "Editar animais de estimação"
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            // Inicialize um carregador para ler os dados do animal de estimação do banco de dados
            // e exibir os valores atuais no editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Encontre todas as visualizações relevantes que precisaremos para ler a entrada do usuário de
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Configuração OnTouchListeners em todos os campos de entrada, para que possamos determinar se o usuário
        // tocou ou modificou-os. Isso nos permitirá saber se há mudanças não guardadas
        // ou não, se o usuário tentar deixar o editor sem salvar.
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Configure o spinner suspenso que permite ao usuário selecionar o gênero do animal de estimação.
     */
    private void setupSpinner() {
        // Crie um adaptador para spinner. As opções de lista são da matriz String que usará
        // o spinner usará o layout padrão
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Especifique o estilo de layout suspenso - exibição de lista simples com 1 item por linha
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // // Aplique o adaptador ao spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Define o inteiro mSeleccionado para os valores constantes
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Como AdapterView é uma classe abstrata, onNothingSelected deve ser definido
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save pet into database.
     */
    private void savePet() {
        // Ler dos campos de entrada
        // Use trim para eliminar o espaço em branco principal ou posterior
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        // Verifique se isso é suposto ser um novo animal de estimação
        // e verifique se todos os campos do editor estão em branco
        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
            // Uma vez que nenhum campo foi modificado, podemos retornar cedo sem criar um novo animal de estimação.
            // Não é necessário criar ContentValues e não é necessário fazer nenhuma operação do ContentProvider.
            return;
        }

        // Crie um objeto ContentValues onde os nomes das colunas são as chaves,
        // e os atributos do animal de estimação do editor são os valores.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentPetUri == null) {
            // Este é um animal de estimação NOVO, então insira um novo animal de estimação no provedor,
            // retornando o conteúdo URI para o novo animal de estimaçãot.
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            // Mostra uma mensagem de brinde dependendo se a inserção foi ou não bem sucedida.
            if (newUri == null) {
                // Se o novo conteúdo URI é nulo, houve um erro na inserção.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Caso contrário, a inserção foi bem sucedida e podemos exibir um brinde.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Caso contrário, este é um animal de estimação EXISTENTE, então atualize o animal de estimação com conteúdo URI: mCurrentPetUri
            // e passar no novo ContentValues. Passe em nulo para seleção e seleção
            // porque mCurrentPetUri já identificará a linha correta no banco de dados que
            // queremos modificar.
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);

            // Mostra uma mensagem de brinde dependendo se a atualização foi ou não bem-sucedida.
            if (rowsAffected == 0) {
                // Se nenhuma linha foi afetada, houve um erro com a atualização.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Caso contrário, a atualização foi bem-sucedida e podemos exibir um brinde.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar as opções de menu do arquivo res / menu / menu_editor.xml.
        // Isso adiciona itens de menu à barra de aplicativos.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Este método é chamado após invalidateOptionsMenu (), de modo que o
     * O menu pode ser atualizado (alguns itens do menu podem ser ocultos ou visíveis).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Se este é um novo animal de estimação, esconda o item de menu "Excluir".
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // O usuário clicou em uma opção de menu no menu de transbordamento da barra do aplicativo
        switch (item.getItemId()) {
            // Responda a um clique na opção de menu "Salvar"
            case R.id.action_save:
                // Save pet to database
                savePet();
                // atividade de saída
                finish();
                return true;
            // Responda a um clique na opção de menu "Excluir"
            case R.id.action_delete:
                // diálogo de confirmação de pop-up para exclusão
                showDeleteConfirmationDialog();
                return true;
            // Responda a um clique no botão de seta "Para cima" na barra de aplicativos
            case android.R.id.home:
                // Se o animal de estimação não mudou, continue com a navegação para a atividade pai
                // qual é o {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Caso contrário, se houver mudanças não salvas, configure uma caixa de diálogo para avisar o usuário.
                // Crie um ouvinte de clique para lidar com o usuário confirmando isso
                // as alterações devem ser descartadas.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // O usuário clicou no botão "Descartar", navegue até a atividade pai.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Mostra uma caixa de diálogo que notifica o usuário que eles não registraram alterações
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Este método é chamado quando o botão Voltar é pressionado.
     */
    @Override
    public void onBackPressed() {
        // Se o animal de estimação não tiver mudado, continue com o botão de controle de trás.
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Caso contrário, se houver mudanças não salvas, configure uma caixa de diálogo para avisar o usuário.
        // Crie um ouvinte de clique para lidar com o usuário confirmando que as alterações devem ser descartadas.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // O usuário clicou no botão "Descartar", feche a atividade atual.
                        finish();
                    }
                };

        // Mostra a caixa de diálogo de que há mudanças não guardadas
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Uma vez que o editor mostra todos os atributos do animal de estimação, defina uma projeção que contenha
        // todas as colunas da mesa do animal de estimação
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };

        // Este carregador executará o método de consulta do ContentProvider em uma linha de fundo
        return new CursorLoader(this,   // Contexto da atividade principal
                mCurrentPetUri,         // Consulta o conteúdo URI para o animal de estimação atual
                projection,             // Colunas para incluir no Cursor resultante
                null,                   // Nenhuma cláusula de seleção
                null,                   // Sem argumentos de seleção
                null);                  // ordem de seleção padrão
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Com antecedência cedo se o cursor for nulo ou houver menos de 1 linha no cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Prossiga com a mudança para a primeira linha do cursor e lendo dados dele
        // (Esta deve ser a única linha no cursor)
        if (cursor.moveToFirst()) {
            // Encontre as colunas de atributos de animais que nos interessa
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            //Extraia o valor do Cursor para o índice de coluna dado
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            // Atualize as visualizações na tela com os valores do banco de dados
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            // O gênero é um spinner suspenso, portanto, mapeie o valor constante do banco de dados
            // em uma das opções suspensas (0 é Desconhecido, 1 é Masculino, 2 é Feminino).
            // Então chame setSelection () para que a opção seja exibida na tela como a seleção atual.
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Se o carregador for invalidado, limpe todos os dados dos campos de entrada.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0); // Selecione o gênero "Desconhecido"
    }

    /**
     * Mostrar uma caixa de diálogo que avisa o usuário que há mudanças não salvas que serão perdidas
     * se continuarem deixando o editor.
     *
     * @param discardButtonClickListener é o ouvinte de cliques para o que fazer quando
     * o usuário confirma que deseja descartar suas mudanças
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Crie um AlertDialog.Builder e defina a mensagem, e clique em ouvintes
        // para os botões positivo e negativo na caixa de diálogo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão "Manter edição", então ignore o diálogo
                // e continue editando o animal de estimação.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Crie e mostre o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Solicite ao usuário que confirme que deseja excluir este animal de estimação.
     */
    private void showDeleteConfirmationDialog() {
        // Crie um AlertDialog.Builder e defina a mensagem, e clique em ouvintes
        // para os botões positivo e negativo na caixa de diálogo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão "Excluir", então exclua o animal de estimação.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão "Cancelar", então ignore o diálogo
                // e continue editando o animal de estimação.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Crie e mostre o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Execute a exclusão do animal de estimação no banco de dados.
     */
    private void deletePet() {
        //Execute somente a exclusão se este for um animal de estimação existente
        if (mCurrentPetUri != null) {
            // Chame o ContentResolver para excluir o animal de estimação no URI do conteúdo fornecido.
            // Passar nulo para seleção e seleção porque o mCurrentPetUri
            // URI de conteúdo já identifica o animal de estimação que queremos.
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            // Mostre uma mensagem de brinde dependendo se a exclusão foi ou não bem-sucedida.
            if (rowsDeleted == 0) {
                //Se nenhuma linha foi excluída, houve um erro com a exclusão.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Caso contrário, a exclusão foi bem sucedida e podemos exibir um brinde.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Feche a atividade
        finish();
    }
}