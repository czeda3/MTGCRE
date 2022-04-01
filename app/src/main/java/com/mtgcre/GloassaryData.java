package com.mtgcre;

import java.util.ArrayList;

/***
 *  A GlossaryData object contains a glossary entry from the rulebook as:
 *      title (name of the glossary entry)
 *      text
 *      titleTokens (string tokens of the title)
 */
public class GloassaryData {

    private final String title;
    private final String text;
    private ArrayList<String> titleTokens;

    public GloassaryData(String _title, String _text){
        title = _title;
        text = _text;
    }

    /** GETTERS & SETTERS **/

    public String getTitle() {
        return title;
    }
    public String getText() {
        return text;
    }
    public ArrayList<String> getTitleTokens() {
        return titleTokens;
    }
    public void setTitleTokens(ArrayList<String> titleTokens) {
        this.titleTokens = titleTokens;
    }
}
