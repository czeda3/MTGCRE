package com.mtgcre;
import java.util.ArrayList;

/***
 *  A document object contains a rule's:
 *      title (paragraph id, eg. 702.4g)
 *      text
 *      indexes (indexes of the document's text)
 *      examples (any eample that belongs to a rule in a list)
 */
public class Document {

    private String title;
    private final String text;
    private ArrayList<String> indexes;
    private final ArrayList<String> examples;

    public Document(String _text) {
        this.text = _text;
        this.examples = new ArrayList<>();
    }

    /***
     *  Prints out all the data of this object
     */
    public void printDocument(){
        System.out.println(this.getTitle());
        System.out.println(this.getText());
        for (String example: this.getExamples()) {
            System.out.println(example);
        }
        System.out.println();
        System.out.println();
    }

    /** GETTERS & SETTERS **/

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getText() {
        return text;
    }
    public ArrayList<String> getIndexes() {
        return indexes;
    }
    public void setIndexes(ArrayList<String> indexes) {
        this.indexes = indexes;
    }
    public ArrayList<String> getExamples() {
        return examples;
    }
}