import org.javatuples.Pair;

import java.io.File;

public class SearchApplication {
    public static void main(String[] args) {
        SearchEngine se = new SearchEngine();
        String[] keyWords = se.readProfiles();
        MDictionary mDict = new MDictionary();

        for(String word : keyWords) {
            mDict.add(word);
        }
        System.out.println("---- Readed profiles -- In dictionary ----");
        System.out.println("              "+keyWords.length+"                 "+mDict.getWords().length);

        String[] words;
        File folder = new File("files");
        for (final File fileEntry : folder.listFiles()) {
            words = se.readFile(fileEntry);
            mDict.reset();
            for (String word : words) {
                mDict.findAndAddFromFile(word,1);
            }
            se.makeIndex(fileEntry, mDict.getAppearedWordsWithCount());
       }

        // wyświetl pliki zawierające dane słowo
        String word = "rakieta";
        System.out.println("--------- Files containing " + word + " --------");
        for(String file : se.getDocsContainingWord(word))
            System.out.println(file);

//         wyświetl pliki zawierające wszystkie słowa
        words = new String[] {"armia", "artyleria", "front", "wojsko"};
        System.out.print("--------- Files containing ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        String[] strings = se.getDocsContainingWords(words);
        for(String file : strings)
            System.out.println(file);

        // wyświetl pliki zawierające najwięcej z podanych słów
        words = new String[] {"armia", "artyleria", "front", "generał", "wojsko", "broń", "bitwa", "atakować"};
        System.out.print("--------- Files containing max of: ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        for(String file : se.getDocsWithMaxMatchingWords(words, 6))
            System.out.println(file);

        // wyświetl pliki zawierające się najbliżej danego słowa
        String profileName = "militaria";
        System.out.println("-------- Files closest to the profile: '"+ profileName + "' --------");
        Pair<String, Double> [] files = se.getDocsClosestToProfile(10, profileName);
        for(Pair<String, Double> pair : files)
            System.out.println(pair.getValue0() + ": " + pair.getValue1());

    }
}
