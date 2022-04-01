package com.mtgcre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/***
 *   Activity that initializes the search engine. Loads in the documents and does the weight calculations.
 */
public class InitActivity extends AppCompatActivity {
    private static SearchEngine searchEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        Objects.requireNonNull(getSupportActionBar()).hide();

        new Handler().postDelayed(new Runnable() {
            /***
             *  Creates a loading screen until the processes are done for the search engine calculation.
             *  Starts the search activity when it is finished.
             */
            @Override
            public void run() {
                initEngine();
                startActivity(new Intent(InitActivity.this, Search.class));
                finish();
            }
        }, 100);
    }

    /***
     *  Loads in all three documents that are needed for the Comprehensive Rule search,
     *  then initializes the search engine based on those.
     */
    private void initEngine() {
        /* Comprehensive Rules */
        InputStream iStream_1 = this.getResources().openRawResource(R.raw.mtg_cr);
        BufferedReader compRules = new BufferedReader(new InputStreamReader(iStream_1));

        /* Comprehensive Rules Glossary */
        InputStream iStream_2 = this.getResources().openRawResource(R.raw.mtg_cr_glossary);
        BufferedReader compRulesGlossary = new BufferedReader(new InputStreamReader(iStream_2));

        /* Time Stoplist */
        InputStream iStream_3 = this.getResources().openRawResource(R.raw.time_stoplist);
        BufferedReader stopList = new BufferedReader(new InputStreamReader(iStream_3));

        searchEngine = new SearchEngine(compRules, compRulesGlossary, stopList);
    }

    public static SearchEngine getSearchEngine() {
        return searchEngine;
    }
}