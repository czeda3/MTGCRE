package com.mtgcre;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/***
 *  This activity is where the user can enter the question, for which he/she gets the answer.
 *
 */
public class Search extends AppCompatActivity {
    public static final String EXTRA_QUESTION = "EXTRA_QUESTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setTitle("MTG Comprehensive Rules Engine");

        createButtonSearch();
        try{
            /* If there are no answers for the question, a message pops */
            Intent intent = getIntent();
            String tryagain = intent.getStringExtra(ShowRules.EXTRA_TRYAGAIN);
            if (!tryagain.isEmpty()){
                new AlertDialog.Builder(Search.this)
                        .setMessage(tryagain)
                        .show();
            }
        }catch (Exception ignored){}
    }

    /***
     *  Creates a button for the search.
     *  Will open the ShowRules activity in openShowRulesActivity()
     */
    private void createButtonSearch() {
        Button searchRule = findViewById(R.id.find_rule);
        searchRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openShowRulesActivity();
            }
        });
    }

    /***
     *  Opens the ShowRules activity to show the answers for the question
     */
    private void openShowRulesActivity() {
        EditText questionField = findViewById(R.id.question);
        String question = questionField.getText().toString();

        Intent intent = new Intent(this, ShowRules.class);
        intent.putExtra(EXTRA_QUESTION, question);
        startActivity(intent);
    }
}