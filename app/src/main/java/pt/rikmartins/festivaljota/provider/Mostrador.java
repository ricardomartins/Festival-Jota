/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pt.rikmartins.festivaljota.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class Mostrador {
    public static final String AUTORIDADE = "pt.rikmartins.festivaljota";
    
	public static final String NOME_BASEDADOS = "noticias.db";
	public static final int VERSAO_BASEDADOS = 1;
	
	public static final String NOME_TABELA_NOTICIAS = "noticias";
	public static final String NOME_TABELA_SINCRONISMO = "sincronismo";

    // This class cannot be instantiated
    private Mostrador() {}
    
    /**
     * Noticias table
     */
    public static final class Noticias implements BaseColumns {
        // This class cannot be instantiated
        private Noticias() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTORIDADE + "/noticias");

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI_CATEGORIA = Uri.parse("content://" + AUTORIDADE + "/noticias/categoria");

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI_ID = Uri.parse("content://" + AUTORIDADE + "/noticias/id");

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI_ACTUALIZAR = Uri.parse("content://" + AUTORIDADE + "/actualizar/noticias");

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI_ACTUALIZAR_FORCAR = Uri.parse("content://" + AUTORIDADE + "/actualizar/noticias/forcar");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.rikmartins.festivaljota";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rikmartins.festivaljota";


        /**
         * O grupo a que a notícia pertence
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_CATEGORIA = "grupo";

        /**
         * O título da notícia
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_TITULO = "titulo";

        /**
         * O subtítulo da notícia
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_SUBTITULO = "subtitulo";

        /**
         * O texto da notícia
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_TEXTO = "texto";

        /**
         * O endereço da notícia completa
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_ENDERECO = "endereco";
        
        /**
         * O endereço da imagem associada à notícia
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_ENDERECOIMAGEM = "enderecoimagem";

        /**
         * A imagem associada à notícia
         * <P>Tipo: BLOB</P>
         */
        public static final String COLUNA_IMAGEM = "imagem";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUNA_ORDEM = "ordem";

        /**
         * The default sort order for this table
         */
        public static final String ORDEM_DEFEITO = COLUNA_ORDEM;
    }
    
    public static final class Sincronismos implements BaseColumns {
        // This class cannot be instantiated
        private Sincronismos() {}

        /**
         * O grupo a que a notícia pertence
         * <P>Tipo: TEXT</P>
         */
        public static final String COLUNA_HORA = "hora";
    }
}
