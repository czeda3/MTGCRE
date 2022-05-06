package com.mtgcre;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/***
 *  An object of a SearchEngine is the main element of a search. It contains all the needed data as:
 *      documents (map of distinct text objects those can be returned individually as a result for a search <title of document, document>)
 *      stoplist (a list of words which will be removed from the documents, as it wouldn't help narrow down the search)
 *      inverseIndexes (a structure which stores the term-document matrix in a space efficient way)
 *      glossaries (list off glossary entries from the rulebook)
 *      specificRulings (if an exact rule number is entered in a search, it's matching rule will be stored here)
 *      specificGlossaries (if an exact glossary word is entered in a search, it's matching glossary entry will be stored here)
 */
public class SearchEngine {

    private final HashMap<String, Document> documents;
    private final ArrayList<String> stoplist;
    private final HashMap<String, IndexData> inverseIndexes; /** Name of index, data of it */
    private final ArrayList<GloassaryData> glossaries;
    private final ArrayList<Document> specificRulings;
    private final ArrayList<Document> specificGlossaries;


    public SearchEngine(BufferedReader compRules, BufferedReader compRulesGlossary, BufferedReader stopList){
        this.documents = new HashMap<>();
        this.inverseIndexes = new HashMap<>();
        this.stoplist = loadStoplist(stopList);
        this.glossaries = new ArrayList<>();
        this.specificRulings = new ArrayList<>();
        this.specificGlossaries = new ArrayList<>();

        this.buildDocuments(compRules);
        this.buildInverseIndexes();
        this.loadGlossary(compRulesGlossary);
    }

    /**
     * Processes a search as:
     *      Clears specificRulings and specificGlossaries to be loaded again if needed
     *      Collects all the documents which has a chance to appear in the answers
     *      For each of these documents it calculates a value with COSINE MEASURE, which shows how good of an answer is it to the question
     *      Keeps only the top X most relevant documents
     *      Orders them in reverse order
     * @param question string
     * @return getFinalResults() with the final answers list
     */
    public ArrayList<Document> retrieval(String question){
        this.specificRulings.clear();
        this.specificGlossaries.clear();

        /* PROCESS QUESTION */
        QuestionData questionData = this.processQuestion(question);
        ArrayList<String> possibleDocuments = new ArrayList<>();

        /* DOCUMENT COLLECTING */
        for (String questionIndex : questionData.getW_i().keySet()) {
            possibleDocuments.addAll(Objects.requireNonNull(this.inverseIndexes.get(questionIndex)).getW_ij().keySet());
        }


        LinkedHashMap<String, Double> unSortedResults = new LinkedHashMap<>();
        LinkedHashMap<String, Double> reverseSortedResults = new LinkedHashMap<>();

        /* CALCULATING COSINE MEASURE */
        for (String doc : possibleDocuments) {
            double sum_W_ij_W_ik = 0.0;
            double sum_W_ij = 0.0;
            double sum_W_ik = 0.0;

            for (String questionIndex : questionData.getW_i().keySet()) {
                if (inverseIndexes.get(questionIndex).getW_ij().containsKey(doc)) {
                    sum_W_ij_W_ik += questionData.getW_i().get(questionIndex) * inverseIndexes.get(questionIndex).getW_ij().get(doc);
                    sum_W_ij += Math.pow(questionData.getW_i().get(questionIndex),2);
                    for (String docIndex : documents.get(doc).getIndexes()){
                        sum_W_ik += Math.pow(inverseIndexes.get(docIndex).getW_ij().get(doc),2);
                    }
                }
            }

            double weight = sum_W_ij_W_ik / (Math.sqrt(sum_W_ij * sum_W_ik));
            unSortedResults.put(doc, weight);
        }

        /* SORT */
        unSortedResults
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(20)
                .forEachOrdered(x -> reverseSortedResults.put(x.getKey(), x.getValue()));

        return getFinalResults(reverseSortedResults);
    }

    /***
     *  Creates the final array of result as it contains:
     *      any number specific rulin match + the most relevant search answers + any number of glossary match
     * @param results search results
     * @return arraylist of all the results
     */
    private ArrayList<Document> getFinalResults(LinkedHashMap<String, Double> results) {
        ArrayList<Document> finalResults = new ArrayList<>();
        if (specificRulings.size() == 0 && results.size() == 0 && specificGlossaries.size() == 0){
            return null;
        }

        if (specificRulings.size() > 0){
            finalResults.addAll(specificRulings);
        }
        for (String result : results.keySet()){
            Document document = this.documents.get(result);
            finalResults.add(document);
        }
        if (specificGlossaries.size() > 0){
            finalResults.addAll(specificGlossaries);
        }
        return finalResults;
    }

    /***
     *  Creates a QuestionData object with all the needed data of from the question
     * @param question string which was entered
     * @return QuestionData object
     */
    private QuestionData processQuestion(String question){
        ArrayList<String> temp = this.tokenizeDocument(question);

        this.checkForSpecificRuling(temp);
        this.checkForSpecificGlossaryEntry(temp);
        temp = this.removeSpecialCharacters(temp);
        temp = this.tokenizeDocument(temp);
        temp = this.removeStopWords(temp);
        temp = this.stemIndexes(temp);

        /** build weights ***/
        return new QuestionData(temp, this);
    }

    /***
     *  Checks if an Array of Strings contains any entry which is an exact rule title (eg. 702.4g)
     *  If any found, it is added to the specificRulings list
     *  @param temp list to be checked
     */
    private void checkForSpecificRuling(ArrayList<String> temp) {
        String titleHolder;
        for (String token : temp){
            if (token.trim().length() > 2){
                if (token.replaceAll("\\.", "").trim().matches("[0-9]+[a-zA-Z]?")){
                    titleHolder = token.replaceAll("\\.", "").trim();
                    titleHolder = titleHolder.substring(0,3) + "." + titleHolder.substring(3);
                    if (titleHolder.matches("^.*\\d$")){
                        titleHolder = titleHolder + ".";
                        //titleHolder = titleHolder.substring(0,3) + "." + titleHolder.substring(3) + ".";
                    }

                    if (documents.containsKey(titleHolder)){
                        this.specificRulings.add(documents.get(titleHolder));
                    }
                }
            }
        }
    }

    /***
     *  Checks if an Array of Strings contains any entry which is the glossaries of te rules book
     *  If any found, it is added to the specificGlossaries list
     *  @param temp list to be checked
     */
    private void checkForSpecificGlossaryEntry(ArrayList<String> temp){
        for (GloassaryData gloassaryData : glossaries) {
            if (temp.containsAll(gloassaryData.getTitleTokens())){
                Document document = new Document(gloassaryData.getText());
                document.setTitle(gloassaryData.getTitle());
                specificGlossaries.add(document);
            }
        }
    }

    /***
     *  Builds the documents from the read in BufferedReader (rule book file):
     *      Eliminates rows from the file if it will not be a document
     *      Sets title for the document based on the text
     *      Creates indexes:
     *          Tokenizes
     *          Removes special characters
     *          Tokenizes (it is necessary because from the previous step, new tokens could have been created in a single token)
     *          Removes stoplist words
     *          Stems
     *      Adds examples
     * @param compRules
     */
    private void buildDocuments(BufferedReader compRules){
        try {
            String line = compRules.readLine();
            boolean add;
            while (line != null) {
                if (line.trim().length()>4){
                    if (!Character.isDigit(line.charAt(4))) {
                        line = compRules.readLine();
                        continue;
                    }
                }

                if (line.trim().length() != 0){
                    add = true;
                    Document new_doc = new Document(line);
                    new_doc.setIndexes(tokenizeDocument(new_doc.getText()));
                    new_doc.setTitle(new_doc.getIndexes().remove(0));
                    new_doc.setIndexes(removeSpecialCharacters(new_doc.getIndexes()));
                    new_doc.setIndexes(tokenizeDocument(new_doc.getIndexes()));
                    if (new_doc.getIndexes().size() < 5) add = false;
                    //if (new_doc.indexes.size() == 5) System.out.println(new_doc.indexes);
                    new_doc.setIndexes(removeStopWords(new_doc.getIndexes()));
                    new_doc.setIndexes(stemIndexes(new_doc.getIndexes()));
                    line = compRules.readLine();
                    if (line != null && line.trim().length()>6){
                        while (line.startsWith("Example")){
                            new_doc.getExamples().add(line);
                            line = compRules.readLine();
                            if (line.length() == 0) break;
                        }
                    }
                    if (add) this.documents.put(new_doc.getTitle(),new_doc);
                } else {
                    line = compRules.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     *  Builds the inverse index structure to represent the term-document matrix and spare a lot of space.
     */
    private void buildInverseIndexes(){
        ArrayList<String> allIndexes = new ArrayList<>();
        for (Document document : this.documents.values()) {
            allIndexes.addAll(document.getIndexes());
        }
        Set<String> distinctIndexes = new LinkedHashSet<>(allIndexes);

        /** Cut on the indexes **/
        //distinctIndexes.remove("abil");

        for (String index : distinctIndexes) {
            IndexData indexdata = new IndexData(index, this.documents);
            inverseIndexes.putIfAbsent(index, indexdata);
        }
    }

    /***
     *  Tokenizes a string.
     * @param text  string to be tokenized
     * @return list of the tokens
     */
    private ArrayList<String> tokenizeDocument(String text){
        ArrayList<String> templist = new ArrayList<>();

        StringTokenizer stringTokenizer = new StringTokenizer(text);
        while (stringTokenizer.hasMoreTokens()) {
            templist.add(stringTokenizer.nextToken().trim().toLowerCase(Locale.ROOT));
        }
        return templist;
    }

    /***
     *  Tokenizes all string elements in a list.
     * @param arrayList list to be tokenized
     * @return  list of the tokens
     */
    private ArrayList<String> tokenizeDocument(ArrayList<String> arrayList){
        ArrayList<String> templist = new ArrayList<>();

        for (String item : arrayList ){
            StringTokenizer stringTokenizer = new StringTokenizer(item);
            while (stringTokenizer.hasMoreTokens()) {
                templist.add(stringTokenizer.nextToken().trim().toLowerCase(Locale.ROOT));
            }
        }
        return templist;
    }

    /***
     *  Stems a list of tokens with the Porter Stemming algorithm
     * @param arrayList tokens created from text of documents
     * @return list of stemmed tokens
     */
    private ArrayList<String> stemIndexes(ArrayList<String> arrayList){
        ArrayList<String> temp = new ArrayList<>();

        for (String item : arrayList) {

            Stemmer stemmer = new Stemmer();
            item = item.toLowerCase(Locale.ROOT);

            for (int c = 0; c < item.length(); c++) stemmer.add(item.charAt(c));
            stemmer.stem();
            temp.add(stemmer.toString());
        }
        return temp;
    }

    /***
     *  Filters out the words which are in the stoplist.
     * @param arrayList list of indexes
     * @return filtered list
     */
    private ArrayList<String> removeStopWords(ArrayList<String> arrayList){
        ArrayList<String> templist = new ArrayList<>();

        String buffer = "";
        for (String item : arrayList){
            if (!this.stoplist.contains(item) && !containsNumber(item) && !buffer.toLowerCase(Locale.ROOT).equals("see")) {
                templist.add(item);
            }
            buffer = item;
        }
        return templist;
    }

    /***
     *  Removes special characters from a list of strings.
     * @param arrayList list of strings
     * @return list that only contains strings with alphanumeric characters
     */
    private ArrayList<String> removeSpecialCharacters(ArrayList<String> arrayList){
        ArrayList<String> temp = new ArrayList<>();
        for (String item : arrayList ){
            temp.add(item.replaceAll("[^a-zA-Z0-9]", " "));
        }
        return temp;
    }

    /***
     *  Loads the stoplist. Words which will be removed from indexes list
     * @param stoplist read BufferedReader
     * @return list of words
     */
    private ArrayList<String> loadStoplist(BufferedReader stoplist) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            String line = stoplist.readLine();
            while (line != null) {
                if (line.trim().length() != 0){
                    temp.add(line.toLowerCase(Locale.ROOT).trim());
                }
                line = stoplist.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    /***
     *  Checks if a string contains at least one number
     * @param string string
     * @return  boolean
     *
     *  TODO it can be just a regex check
     */
    private boolean containsNumber(String string) {
        char[] chars = string.toCharArray();
        for(char c : chars){
            if(Character.isDigit(c)){
                return true;
            }
        }
        return false;
    }

    /***
     *  Creates the glossaries to the glossary list
     * @param compRulesGlossary read in the InitActivity
     */
    private void loadGlossary(BufferedReader compRulesGlossary){
        try {
            String line = compRulesGlossary.readLine();
            while (line != null) {
                if (line.trim().length() != 0){
                    String title = line.toLowerCase(Locale.ROOT);
                    StringBuilder text = new StringBuilder();
                    line = compRulesGlossary.readLine();
                    while (line != null && line.trim().length() != 0){
                        text.append(" ");
                        text.append(line);
                        line = compRulesGlossary.readLine();
                    }
                    GloassaryData gloassaryData = new GloassaryData(title, text.toString());
                    gloassaryData.setTitleTokens(tokenizeDocument(title));
                    this.glossaries.add(gloassaryData);
                } else {
                    line = compRulesGlossary.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     *  Prints the frequency of all indexes in the whole document set in the engine
     *  Ordered in reverse.
     */
    public void frequency() {
        ArrayList<String> list = new ArrayList<>();
        HashMap<String, Integer> words = new HashMap<>();

        for (Document document : this.documents.values()) {
            list.addAll(document.getIndexes());
        }
        for (String string: list) {
            if (words.containsKey(string)){
                words.put(string, words.get(string) + 1);
            }else{
                words.put(string, 1);
            }
        }

        HashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        words
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        System.out.println("Reverse Sorted Map: " + reverseSortedMap);
        System.out.println("Reverse Sorted Map size: " + reverseSortedMap.size());

    }

    /** GETTERS & SETTERS **/

    public HashMap<String, Document> getDocuments() {
        return documents;
    }
    public HashMap<String, IndexData> getInverseIndexes() {
        return inverseIndexes;
    }
}
