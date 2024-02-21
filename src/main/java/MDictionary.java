import org.javatuples.Pair;


public class MDictionary {
    private Pair<String,Integer>[] array;
    private int capacity;
    public int wordCounter;
    private MorfologyTool mt;
    private String lastWord;


    public MDictionary() {
        this.capacity = 400;
        this.wordCounter = 0;
        this.array = new Pair[capacity];
        mt = new MorfologyTool();
    }

    /**
     *Empties the dictionary and frees memory for dictionary collections
     **/
    public void empty()
    {
        array = new Pair[capacity];
        wordCounter=0;
        lastWord = "";
    }

    /**
     *The method resets the number of occurrences of concepts in the dictionary
    **/
    public void reset()
    {
        for (int i = 0; i < array.length; i++){
            if (array[i] != null) {
                array[i] = Pair.with(array[i].getValue0(), 0);
            }
        }
    }

    /**
    *Adding a concept to the dictionary based on the word and hash key number
     * @param W word
     * @param h word hash
    **/
    private int add(String W, int h)
    {
        if(wordCounter >= array.length) resizeArray();
        boolean added = false;
        int findedIndex = find(W,h);
        if(findedIndex == -1){
            while(!added){
                if(h>=array.length) h=0;
                if(array[h]==null){
                    array[h]=Pair.with(W,0);
                    added=true;
                    wordCounter++;
                }
                else h++;
            }
            return h;
        }
        return findedIndex;
    }

    /**
     *Adding a concept to the dictionary based on the word
     * @param W word
    **/
    public int add(String W)
    {
        return add(W, hash(W));
    }

    /**
     *Provides the key for a given word
     * @param W word
    **/
    private int hash(String W)
    {
        int hash = 7;
        for (int i = 0; i < W.length(); i++) {
            hash = hash * 31 + W.charAt(i);
        }
        return Math.abs(hash % array.length);
    }

    /**
     *The method returns the hash of the word if the word is found, otherwise it returns -1
     * @param W word
     * @param h word hash
    **/
    private int find(String W, int h)
    {
        if(h>=array.length) h=0;
        int indexTosave = h;
        while(array[h]!=null){
            if (array[h].getValue0().equals(W)) {
                return h;
            } else if (h == indexTosave - 1) {
                return -1;
            } else h++;
            if(h>=array.length) h=0;
        }
        return -1;
    }

    /**
     *The method returns the hash of the word if the word is found and increases the number of occurrences by n, otherwise it returns -1
     * @param W word
     * @param n occurences to add
     **/
    public int findAndAdd(String W, int n)
    {
        int h = hash(W);
        if(h>=array.length) h=0;
        int indexTosave = h;
        while(array[h]!=null){
            if (array[h].getValue0().equals(W)) {
                array[h] = Pair.with (W,array[h].getValue1()+n);
                return h;
            } else if (h == indexTosave - 1) {
                return -1;
            } else h++;
            if(h>=array.length) h=0;
        }
        return -1;
    }
    /**
     * USED TO FILES ONLY
     * The method returns the hash of the word if the word is found and increases the number of occurrences by n, otherwise it returns -1
     * @param W word
     * @param n occurences to add
     **/
    public int findAndAddFromFile(String W, int n)
    {
        if(W.length() <= 2 ) return -1;
        String temp = lastWord + " " + W;
        lastWord = W;
        W = mt.getConcept(W);
        temp = mt.getConcept(temp);
        int currentIndex=hash(temp);
        int counter = 0;
        int lastIndex = currentIndex;
        if(currentIndex>=array.length) currentIndex=0;
        while(array[currentIndex]!=null){
            if (array[currentIndex].getValue0().equals(temp)) {
                array[currentIndex] = Pair.with(temp, array[currentIndex].getValue1() + n);
                return currentIndex;
            }
            currentIndex++;
            if(currentIndex == lastIndex) counter++;
            if(counter >= 2) return -1;
            if(currentIndex>=array.length){
                currentIndex=0;
            }
        }

        currentIndex=hash(W);
        counter = 0;
        lastIndex = currentIndex;
        if(currentIndex>=array.length) currentIndex=0;
        while(array[currentIndex]!=null){
            if (array[currentIndex].getValue0().equals(W)) {
                array[currentIndex] = Pair.with(W, array[currentIndex].getValue1() + n);
                return currentIndex;
            }
            currentIndex++;
            if(currentIndex == lastIndex) counter++;
            if(counter >= 2) return -1;
            if(currentIndex>=array.length){
                currentIndex=0;
            }
        }
        return -1;
    }

    /**
     *The method returns the hash of the word if found, otherwise it returns -1
    **/
    public int find(String W)
    {
        return find(mt.getConcept(W), hash(W));
    }

    /**
     * Returns words in the dictionary
     */
    public String[] getWords() {
        String[] toReturn = new String[wordCounter];
        boolean allWords = false;
        int counter=0,counterToReturn=0;
        while(!allWords){
            if(array[counter]!=null) {
                toReturn[counterToReturn] = array[counter].getValue0();
                counterToReturn++;
            }
            if(counterToReturn==toReturn.length) allWords=true;
            counter++;
        }
        return toReturn;
    }

    /**
     * Returns the words that appeared in the document
     * @return array of appeared words
     */
    public String[] getAppearedWords() {
        int sum = 0;
        for (int i = 0; i < capacity || sum>= wordCounter; i++) {
            if(array[i] != null && array[i].getValue1() > 0){
                sum++;
            }
        }
        String[] toReturn = new String[sum];
            int counter = 0;
            for (int i = 0; i < capacity; i++) {
                if (array[i] != null && array[i].getValue1() > 0) {
                    toReturn[counter++] = array[i].getValue0();
                }
            }
        return toReturn;
    }

    /**
     * Returns the concepts that occurred and the number of occurrences
     * @return array of Pairs
     */
    public Pair<String, Integer>[] getAppearedWordsWithCount() {
        Pair<String, Integer>[] appearedWordsWithCount = (Pair<String, Integer>[]) new Pair[wordCounter];
        int index = 0;
        for (Pair<String, Integer> objects : array) {
            if (objects != null) {
                appearedWordsWithCount[index++] = objects;
            }
        }
        return appearedWordsWithCount;
    }
    /**
     * Expands the array twice if the array runs out of space
     */
    private void resizeArray(){
        Pair<String, Integer>[] newArray = new Pair[array.length*2];
        for (int i = 0; i < array.length; i++) {
            newArray[i]=array[i];
        }
        array=newArray;
    }
}
