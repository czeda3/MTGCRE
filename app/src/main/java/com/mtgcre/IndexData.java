package com.mtgcre;

import java.util.Collections;
import java.util.HashMap;

/***
 *  An IndexData object contains data about the indexes in the documents as:
 *      word (which is the index)
 *      posts (pairs of <document in which the index is in, how many time it occurs in there>)
 *      w_ij (pairs as the i index's weight in document j <document, weight>)
 */
public class IndexData {

    private final String word;
    private final HashMap<String, Integer> posts;
    private final HashMap<String, Double> w_ij;


    public IndexData(String _word, HashMap<String, Document> documentHashMap){
        this.word = _word;
        posts = new HashMap<>();
        w_ij = new HashMap<>();
        this.createPosts(documentHashMap);
        this.createWeights(documentHashMap);
    }

    /***
     *  Creates the posts for the index based on the given document set
     * @param documentHashMap all the engine's documents
     */
    private void createPosts(HashMap<String, Document> documentHashMap) {
        for (Document document : documentHashMap.values()) {
            if (document.getIndexes().contains(this.word)) {
                this.posts.put(document.getTitle(), Collections.frequency(document.getIndexes(), this.word));
            }
        }
    }

    /***
     *  Calculates the weights for the index for every document it appears in
     */
    private void createWeights(HashMap<String, Document> documentHashMap) {
        this.posts.forEach(
                (key,value)-> weightCalculator(key, value, documentHashMap)
        );
    }

    private void weightCalculator(String key, Integer value, HashMap<String, Document> documentHashMap){
        /** TF_IDF **/
        int f_ij = value;
        Double m = (double) documentHashMap.size();
        Double F_i = (double) this.posts.size();
        double y = f_ij*Math.log(m/F_i);
        w_ij.put(key, y);
    }

    /** GETTERS & SETTERS **/

    public HashMap<String, Double> getW_ij() {
        return w_ij;
    }
}
