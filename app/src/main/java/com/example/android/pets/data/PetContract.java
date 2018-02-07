package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by VALDIR on 07/02/2018.
 */

public final class PetContract {

    private static final String TAG = "PetContract";

    //Para evitar que alguém instanciem acidentalmente a classe do contrato,
    // Faremos um construtor privado


    private  PetContract() {}

    /* Classe interna que define o conteúdo da tabela */
    public static abstract class PetsEntry implements BaseColumns{

        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_WEIGHT = "weight";

        /** Possiveis valores para GENDER
         * 0 - Desconhecido, 1 - Masculino, 2 - Feminino
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        

    }





}
