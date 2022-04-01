package com.mtgcre;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/***
 *  This activity shows the results of the search in a scrollable view
 */
public class ShowRules extends AppCompatActivity {
    public static final String EXTRA_CLICKEDITEM = "EXTRA_CLICKEDITEM";
    public static final String EXTRA_TRYAGAIN = "EXTRA_TRYAGAIN";

    private ArrayList<Document> answers;

    /***
     *  Gets the question from the Search activity.
     *  Performs the search with the question.
     *  Wraps the answer objects into single texts.
     *  Shows the wrapped answers in a scrollable and clickable view.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_rules);

        Intent intent = getIntent();
        SearchEngine searchEngine = InitActivity.getSearchEngine();
        String question = intent.getStringExtra(Search.EXTRA_QUESTION);

        /* SEARCH */
        answers = searchEngine.retrieval(question);
        if (answers == null){
            Intent newSearch = new Intent(this, Search.class);
            newSearch.putExtra(EXTRA_TRYAGAIN, "Please, specify the search a bit more!");
            startActivity(newSearch);
            this.finish();
            return;
        }

        /* WRAP */
        ArrayList<String> wrapAnswers = new ArrayList<>();
        for (Document document : answers) {
            String title = document.getTitle();
            String text = document.getText();
            wrapAnswers.add(title+"\n"+text);
        }

        /* SHOW */
        ListView answersList = findViewById(R.id.answer_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ShowRules.this, android.R.layout.simple_list_item_1, wrapAnswers);
        answersList.setAdapter(adapter);
        answersList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            /***
             *  Opens the ShowDeeprules activity with passing the clicked rule's title when any is clicked
             *  Opens that activity only if not a glossary element was clicked
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                if (searchEngine.getDocuments().get(answers.get(position).getTitle()) != null){
                    Intent itemIntent = new Intent(view.getContext(), ShowDeepRules.class);
                    itemIntent.putExtra(EXTRA_CLICKEDITEM, answers.get(position).getTitle());
                    startActivity(itemIntent);
                }
            }
        });
    }
}