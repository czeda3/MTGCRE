package com.mtgcre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/***
 *  A QuestionData object contains data of the question as:
 *      post (pairs of index in the question and it's occurrence)
 *      w_i (pairs of i index and it's weight in the question)
 */
public class QuestionData {
    private final HashMap<String, Integer> posts;
    private final HashMap<String, Double> w_i;


    public QuestionData(ArrayList<String> questionArrayList, SearchEngine searchEngine){
        posts = new HashMap<>();
        w_i = new HashMap<>();
        this.createPosts(questionArrayList);
        this.createWeights(searchEngine);
    }

    /***
     *  Creates the post map for the question
     * @param questionArrayList the question in a tokenized form
     */
    private void createPosts(ArrayList<String> questionArrayList) {
        ArrayList<String> indexes = new ArrayList<>();
        for (String str : questionArrayList) {
            if (!indexes.contains(str)) {
                this.posts.put(str, Collections.frequency(questionArrayList, str));
                indexes.add(str);
            }
        }
    }

    /***
     *  Creates the weights map
     * @param searchEngine is the main engine that controls the actual search
     */
    private void createWeights(SearchEngine searchEngine) {
        this.posts.forEach(
                (key,value)-> weightCalculator(key, value, searchEngine)
        );
    }

    /***
     * Calculates a weight for an index with the TF-IDF formula
     * @param key index as a word
     * @param value index's occurrences in the question
     * @param searchEngine is the main engine that controls the actual search
     */
    private void weightCalculator(String key, Integer value, SearchEngine searchEngine){
        if (searchEngine.getInverseIndexes().containsKey(key)){
            int f_ij = value;
            Double m = (double) searchEngine.getDocuments().size() + 1;

            IndexData indexData = searchEngine.getInverseIndexes().get(key);
            Double F_i = (double) indexData.getW_ij().size() + 1;

            double y = f_ij*Math.log10(m/F_i);
            w_i.put(key, y);
        }
    }

    /** GETTERS & SETTERS **/
    public HashMap<String, Double> getW_i() {
        return w_i;
    }
}
