package com.mtgcre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/***
 *  An activity that shows all rules in that subsection as the rule that was clicked.
 *  Based on the number of it as Section.subsection (eg. 107.4)
 */
public class ShowDeepRules extends AppCompatActivity {

    /***
     *  First it gets the title of the clicked rule.
     *  From the documents, if it is in the same subcategory as the clicked rule, adds it to a list.
     *  Converts the list to a ListView to be scrollable through an adapter
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_deep_rules);


        Intent intent = getIntent();
        String clickedRule = intent.getStringExtra(ShowRules.EXTRA_CLICKEDITEM);
        getSupportActionBar().setTitle("Rules related to "+clickedRule);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        clickedRule = getOnlyRuleNumber(clickedRule);

        SearchEngine se = InitActivity.getSearchEngine();
        ListView deepRulesListView = findViewById(R.id.deep_rule);
        ArrayList<String> deepRulesArrayList = new ArrayList<>();

        for(Map.Entry<String, Document> entry : se.getDocuments().entrySet()){
            if (getOnlyRuleNumber(entry.getValue().getTitle()).equals(clickedRule)){
                deepRulesArrayList.add(entry.getValue().getText());
            }
        }
        Collections.sort(deepRulesArrayList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ShowDeepRules.this, android.R.layout.simple_list_item_1, deepRulesArrayList);
        deepRulesListView.setClickable(false);
        deepRulesListView.setAdapter(adapter);

    }

    /***
     *  Gets only the numbers (section,subsection) from a rule title (paragraph name)
     * @param str A rule's paragraph's title as a string
     * @return a string that contains only the numbers
     */
    private String getOnlyRuleNumber(String str){
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))){
                break;
            }else{
                temp.append(str.charAt(i));
            }
        }
        temp = new StringBuilder(temp.toString().replaceAll("\\.", ""));
        return temp.toString().trim();
    }
}