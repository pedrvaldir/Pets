package com.example.android.pets;

import android.content.ContentProvider;

/**
 * Created by VALDIR on 15/03/2018.
 */
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

public class PetProvider extends ContentProvider {

    /** Tag para as mensagens de log */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** Código URI Matcher para o conteúdo URI para a mesa de animais de estimação */
    private static final int PETS = 100;

    /** Código URI Matcher para o conteúdo URI para um único animal de estimação na mesa de animais de estimação */
    private static final int PET_ID = 101;

    /**
     * Objeto UriMatcher para combinar um URI de conteúdo com um código correspondente.
     * A entrada passada para o construtor representa o código para retornar para o URI da raiz.
     * É comum usar NO_MATCH como entrada para este caso.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Inicializador estático. Isso é executado pela primeira vez, qualquer coisa é chamada dessa classe.
    static {
        //As chamadas para addURI () vão aqui, para todos os padrões de URI de conteúdo que o provedor
        //deve reconhecer. Todos os caminhos adicionados ao UriMatcher têm um código correspondente para retornar
        // quando é encontrada uma partida.

        // O URI de conteúdo do formulário "content: //com.example.android.pets/pets" será mapeado para o
        //código inteiro {@link #PETS}. Este URI é usado para fornecer acesso a linhas MÚLTIPLAS
        // da tabela de  animais
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // O URI de conteúdo do formulário "content: //com.example.android.pets/pets/#" irá mapear para o
        // código inteiro {@link #PET_ID}. Este URI é usado para fornecer acesso a uma única linha
        // da tabela de animais.
        //
        // Neste caso, o curinga "#" é usado onde "#" pode ser substituído por um número inteiro.
        // Por exemplo, "conteúdo: //com.example.android.pets/pets/3" corresponde, mas
        // "conteúdo: //com.example.android.pets/pets" (sem um número no final) não corresponde.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /** Objeto auxiliar de banco de dados*/
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Obter banco de dados legível
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Este cursor manterá o resultado da consulta
        Cursor cursor;

        // descubra se o URI Matcher pode combinar o URI com um código específico
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Para o código PETS, consulte a tabela de animais de estimação diretamente com o dado
                // projeção, seleção, argumentos de seleção e ordem de classificação. O cursor
                // pode conter várias linhas da tabela de animais de estimação.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:
                // Para o código PET_ID, extraia a ID do URI.
                // Para um exemplo de URI, como "conteúdo: //com.example.android.pets/pets/3",
                // a seleção será "_id =?" e o argumento da seleção será um
                // String array contendo a ID real de 3 neste caso.
                //
                // Para cada "?" Na seleção, precisamos ter um elemento na seleção
                // argumentos que preencherão o "?". Uma vez que temos 1 ponto de interrogação na
                // seleção, temos 1 String nos argumentos de seleção 'String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Isso executará uma consulta na tabela de animais de estimação onde o _id é igual a 3 para retornar um
                // Cursor contendo essa linha da tabela.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Definir notificação URI no Cursor,
        // então sabemos para o conteúdo de URI que o Cursor foi criado.
        // Se os dados neste URI mudar, então sabemos que precisamos atualizar o Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Retornar o cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     *Insira um animal de estimação no banco de dados com os valores de conteúdo fornecidos. Retornar o novo conteúdo URI
            * para essa linha específica no banco de dados.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        //Verifique se o nome não é nulo
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        //Verifique se o gênero é válido
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        //Se o peso for fornecido, verifique se é maior ou igual a 0 kg
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // Não é necessário verificar a raça, qualquer valor é válido (incluindo nulo).

        //Obter banco de dados gravável
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // nsira o novo animal de estimação com os valores dados
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        // Se o ID for -1, então a inserção falhou. Registre um erro e volte nulo.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notificar todos os ouvintes que os dados mudaram para o URI do conteúdo do animal de estimação
        getContext().getContentResolver().notifyChange(uri, null);

        // Retornar o novo URI com o ID (da linha recém-inserida) anexado no final
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // Para o código PET_ID, extraia o ID do URI,
                // então sabemos qual linha para atualizar. A seleção será "_id =?" e seleção
                // argumentos serão uma matriz de Cadeia de caracteres que contém a identificação real.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Atualize animais de estimação no banco de dados com os valores de conteúdo fornecidos. Aplique as alterações nas linhas
            * especificado nos argumentos de seleção e seleção (que podem ser 0 ou 1 ou mais animais de estimação).
            * Retorna o número de linhas que foram atualizadas com sucesso.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Se a chave {@link PetEntry # COLUMN_PET_NAME} estiver presente,
        // verifique se o valor do nome não é nulo.
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // Se a chave {@link PetEntry # COLUMN_PET_GENDER} estiver presente,
        // verifique se o valor de gênero é válido.
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // Se a chave {@link PetEntry # COLUMN_PET_WEIGHT} estiver presente,
        // verifique se o valor do peso é válido.
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Verifique se o peso é maior ou igual a 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // Não é necessário verificar a raça, qualquer valor é válido (incluindo nulo).
        // Se não houver valores para atualizar, então não tente atualizar o banco de dados
        if (values.size() == 0) {
            return 0;
        }

        // Caso contrário, obtenha banco de dados gravável para atualizar os dados
        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        // Execute a atualização no banco de dados e obtenha o número de linhas afetadas
        int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        // Se 1 ou mais linhas foram atualizadas, notifique todos os ouvintes que os dados no
        // URI dado mudou
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Retorna o número de linhas atualizadas
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Obter banco de dados gravável
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Rastreie o número de linhas que foram excluídas
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Excluir uma única linha dada pela ID no URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Se 1 ou mais linhas foram excluídas, notifique a todos os ouvintes que os dados no
        // URI dado mudou
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Retorna o número de linhas excluídas
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
