import org.javatuples.Pair;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SearchEngine {

    public SearchEngine(){
        prepareProgram();
    }
    private void prepareProgram(){
        if (new File("indices/rev/").exists() && Objects.requireNonNull(new File("indices/rev/").listFiles()).length > 0) {
            try {
                for (String temp : Objects.requireNonNull(new File("indices/rev/").list())) {
                    new PrintWriter("indices/rev/" + temp).close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Reading file with breaking it into words
     * @param file file location
     * @return files
     */
    public String[] readFile(File file) {
        String[] splitedFile = null;
        StringBuilder allFile = new StringBuilder();
        String temp;
        try {
            BufferedReader bf = new BufferedReader(new FileReader(file));
            while((temp = bf.readLine()) != null){
                allFile.append(temp+" ");
            }
            splitedFile = allFile.toString().split("[\\P{L}|\\r\\n\\s]");
            bf.close();
        } catch (IOException e) {
            e.getMessage();
        }

        for (int i = 0; i < splitedFile.length; i++) {
            temp=splitedFile[i].toLowerCase();
            if(!temp.equals(splitedFile[i])) splitedFile[i]=temp;
        }

        return splitedFile;
    }
    /**
     * Reading profiles with breaking it into words
     * @return mainArr
     */
    public String[] readProfiles() {
        File folder = new File("profiles");
        String[] mainArr = null;
        boolean wasFirst = false;
        for (File fileEntry : folder.listFiles()) {
            if(wasFirst){
                mainArr=merge(mainArr,readProfile(fileEntry.toString()));
            }else {
                mainArr=readProfile(fileEntry.toString());
                wasFirst=true;
            }
        }
        return mainArr;
    }

    private String[] merge(String[] t1, String[] t2) {
        String[] toReturn = new String[t1.length+t2.length];
        for (int i = 0; i < t1.length; i++) {
            toReturn[i] = t1[i];
        }

        for (int i = 0; i < t2.length; i++) {
            toReturn[i+t1.length] = t2[i];
        }
//        System.arraycopy(t1,0,toReturn,0,t1.length);
//        System.arraycopy(t2,0,toReturn,t1.length,t2.length);
        return toReturn;
    }

    public String[] readProfile(String profileName) {
        String[] toReturn = null;
        int line;
        StringBuilder allFile = new StringBuilder();
        try {
            FileReader fr = new FileReader(profileName);
            while ((line = fr.read()) != -1) {
                allFile.append((char)line);
            }
            toReturn = allFile.toString().split(System.lineSeparator());
            fr.close();

        }catch (IOException e){
            e.getMessage();
        }
        return toReturn;
    }

    /**
     * Making index file from txt file - filename.idx(word : occurence in file) and inverted index word.idx(filename : occurence in file)
     * @param fileEntry files to make index
     * @param wordsL words with occurences as Pair array
     */
    public void makeIndex(File fileEntry, Pair<String, Integer>[] wordsL) {

        File location = new File("indices");
        if (!location.exists()) {
            location.mkdir();
        }
        location = new File(location+"/rev/");
        if (!location.exists()) {
            location.mkdir();
        }
        StringBuilder sb;
        for (Pair<String, Integer> word : wordsL){
            if(word.getValue1()>0) {
                location = new File("indices/rev/", word.getValue0() + ".idx");
                updateIndex(location, fileEntry.getName() + ":" + word.getValue1()+"\n");
            }
        }
        sb = new StringBuilder();
        location = new File("indices/"+fileEntry.getName().substring(0,fileEntry.getName().length()-4)+".idx");
        for (Pair<String, Integer> word : wordsL){
            if(word.getValue1()>0)
           sb.append(word.getValue0()+":"+word.getValue1()+"\n");
        }
        sb.delete(sb.length()-1,sb.length());
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(location,false))){
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateIndex(File fileEntry, String word) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileEntry,true))){
            bw.write(word);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return files containing word
     * @param word searched word
     * @return array containing files
     */
    public String[] getDocsContainingWord(String word) {
        String[] file = null;
        String[] toReturn;
        try {
            file = Files.readAllLines(Path.of("indices/rev/" + word + ".idx")).toArray(new String[]{});
        }catch (Exception e){
            e.printStackTrace();
        }
        toReturn= new String[file.length];
        for (int i = 0; i < file.length; i++) {
            toReturn[i]=file[i].split(":")[0];
        }
        return toReturn;
    }

    /**
     * Return files containing words
     * @param words searched words
     * @return array containing files
     */
    public String[] getDocsContainingWords(String[] words) {
        File[] fileIndices = new File("indices/").listFiles(File::isFile);
        String[] workingArr = new String[fileIndices.length];
        String[] tempSplit;
        int arrCounter = 0,sum;
        MDictionary md = new MDictionary();
        for(String word : words){
            md.add(word);
        }
        for (File filename : fileIndices){
            sum = 0;
            try(BufferedReader bf = new BufferedReader(new FileReader(filename.getAbsolutePath()))){
                while(bf.ready()){
                    tempSplit = bf.readLine().split(":");
                    if(md.find(tempSplit[0]) != -1)
                        sum++;
                }
                if(sum >= words.length)
                    workingArr[arrCounter++] = filename.getName().substring(0,filename.getName().length()-3)+"txt";
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        String[] toReturn = new String[arrCounter];
        for (int i = 0; i < arrCounter; i++) {
            toReturn[i] = workingArr[i];
        }
        return toReturn;
    }

    /**
     * Returns n files containing the most searched words
     * @param words array of searched words
     * @param n how many files to return
     * @return array of files with descending order with n limit
     */
    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {
        File[] fileIndices = new File("indices/").listFiles(File::isFile);
        Pair<String, Integer>[] files = new Pair[fileIndices.length];
        String[] toReturn;
        int counter = 0;
        MDictionary md = new MDictionary();
        for (String word : words){
            md.add(word);
        }

        for (File filename : fileIndices){
            files[counter] = Pair.with(filename.getName(), 0);
            try(BufferedReader bf = new BufferedReader(new FileReader(filename.getAbsolutePath()))){
                while(bf.ready()){
                    toReturn = bf.readLine().split(":");
                    if(md.find(toReturn[0]) != -1)
                        files[counter] = Pair.with(filename.getName(), files[counter].getValue1() + 1);
                }
                counter++;

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        files = sort(files);
        toReturn = new String[Math.min(n, counter)];
        for (int i = 0; i < toReturn.length; i++) {
            if (files[i] != null) {
                toReturn[i] = files[i].getValue1()+"."+files[i].getValue0();
            }
        }

        return toReturn;
    }

    /**
     * Returning the n documents with the highest match to the selected profile
     * @param n how many files should be returned
     * @return array of Pair with files limited by n param
     */
    public Pair<String,Double>[] getDocsClosestToProfile(int n, String profileName){
        File[] fileIndices = new File("indices/").listFiles(File::isFile);
        Pair<String, Double>[] workingArr = new Pair[fileIndices.length];
        double sum;
        int counter = 0;
        String[] tempSplit;
        String[] keywords = readProfile("profiles/"+profileName+".txt");
        MDictionary md = new MDictionary();
        for(String word: keywords)
        {
            md.add(word);
        }

        for(File filename : fileIndices){
            sum = 0;
            try(BufferedReader bf = new BufferedReader(new FileReader(filename.getAbsolutePath()))){
                while(bf.ready()){
                    tempSplit = bf.readLine().split(":");
                    if(md.find(tempSplit[0]) != -1)
                        sum += Math.log10(Integer.parseInt(tempSplit[1]));
                }
                workingArr[counter++] = new Pair<>("files/"+filename.getName(), Math.floor(((sum/keywords.length)*100) * 100) / 100);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        workingArr = sort(workingArr);
        Pair<String, Double>[] toReturn = new Pair[Math.min(n, workingArr.length)];
        System.arraycopy(workingArr, 0, toReturn, 0, Math.min(n, workingArr.length));
        return toReturn;
    }
    /**
     * Returns a descending sorted array (using merge sort) of String and T (could be Int or Double)
     * @param pairs array of word and occurences
     * @return pairs
     */
    private static <T extends Comparable<T>> Pair<String, T>[] sort(Pair<String, T>[] pairs) {
        if (pairs.length == 1)
            return pairs;

        Pair<String, T>[] arr1 = sort(split(pairs, 0, pairs.length / 2));
        Pair<String, T>[] arr2 = sort(split(pairs, pairs.length / 2, pairs.length));

        return merge(arr1, arr2);
    }

    private static <T extends Comparable<T>> Pair<String, T>[] merge(Pair<String, T>[] arr1, Pair<String, T>[] arr2) {
        Pair<String, T>[] toReturn = new Pair[arr1.length + arr2.length];
        int i = 0, l = 0, r = 0;
        while (l < arr1.length && r < arr2.length) {
            if (arr1[l].getValue1().compareTo(arr2[r].getValue1()) > 0) {
                toReturn[i] = arr1[l];
                i++;
                l++;
            } else {
                toReturn[i] = arr2[r];
                i++;
                r++;
            }
        }

        while (l < arr1.length) {
            toReturn[i] = arr1[l];
            i++;
            l++;
        }
        while (r < arr2.length) {
            toReturn[i] = arr2[r];
            i++;
            r++;
        }
        return toReturn;
    }

    private static <T> Pair<String, T>[] split(Pair<String, T>[] arr, int from, int to) {
        Pair<String, T>[] temp = new Pair[to - from];
        int counter = 0;
        for (int i = from; i < to; i++) {
            temp[counter++] = arr[i];
        }
        return temp;
    }
}
