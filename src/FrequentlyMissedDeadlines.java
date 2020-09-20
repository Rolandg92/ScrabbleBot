import javax.jnlp.DownloadService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class FrequentlyMissedDeadlines implements BotAPI {

    // The public API of Bot must not change
    // This is ONLY class that you can edit in the program
    // Rename Bot to the name of your team. Use camel case.
    // Bot may not alter the state of the game objects
    // It may only inspect the state of the board and the player objects

    private PlayerAPI me;
    private OpponentAPI opponent;
    private BoardAPI board;
    private UserInterfaceAPI info;
    private DictionaryAPI dictionary;
    private int turnCount;
    private boolean containsBlank = false;
    private boolean needExchange = false;


    private String[] strings;
    private int t;
    private int[] i;
    private LinkedList<String> unique = new LinkedList<>();
    private TreeMap<Integer, ArrayList<Coordinates>> squareCoords;
    LinkedList<String> legalWords;
    LinkedList<String> allWords = new LinkedList<>();
    private String tmpCommand = "";
    private String previousFrame = "";

    FrequentlyMissedDeadlines(PlayerAPI me, OpponentAPI opponent, BoardAPI board, UserInterfaceAPI ui, DictionaryAPI dictionary) {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.info = ui;
        this.dictionary = dictionary;
        turnCount = 0;
    }

    public String getCommand() {
        // Add your code here to input your commands
        // Your code must give the command NAME <botname> at the start of the game
        String command = "";
        switch (turnCount) {
            case 0:
                command = "NAME FrequentlyMissedDeadlines";
                break;
            default:
                command = botsWord();//method return string
                break;
        }
        turnCount++;
        return command;
    }


    //Initial method to work out which word to play
    public String botsWord() {

        String currentFrame = me.getFrameAsString().replaceAll("[^a-zA-Z0-9_]", "");

        if(currentFrame.contains("_")){
            currentFrame = currentFrame.replaceAll("_", "E");
            containsBlank = true;
        }

        String[] frameArray = currentFrame.trim().split("");
        boolean r_wrds = false;
        String wordToPlay = "";

        if (board.isFirstPlay()) {
            allWords = botsFirstTurn(frameArray);  //calling a method only used on first turn
            legalWords = dictionaryChecker(allWords);   //calling dictionary check method

            if (r_wrds = legalWords.isEmpty()) {
                return "X " + currentFrame;
            }else{
                wordToPlay = highestScoringWord(legalWords);
            }
        }

        String cmd = "PASS"; //add by G

        if(!board.getSquareCopy(7,7).isOccupied() && !r_wrds){
            if(wordToPlay.length()>4){
                wordToPlay = blankInFrame(wordToPlay); //change word arrangment if blank was used w_rd o
                return "D8 A " + wordToPlay;
            }else{
                wordToPlay = blankInFrame(wordToPlay); //change word arrangment if blank was used w_rd o
                return "G8 A " + wordToPlay;
            }
        }

        if(board.getSquareCopy(7,7).isOccupied()){
            cmd =  findViableSquare();
            tmpCommand = cmd;
        }

        return cmd;
    }

    //play first word using as many tiles as possible
    public LinkedList<String> botsFirstTurn(String[] botFrame){
        LinkedList<String> modifiedTotal = new LinkedList<>();  //shorten list holding permutations that could possibly be words
        LinkedList<String> total = new LinkedList<>();  //initial list to hold all permutations

        for (int z = 0; z < botFrame.length; z++) {
            total.addAll(outer(z + 1, botFrame));    //initial set of permutations with order doesnt matter
        }

        while (!total.isEmpty()) {
            String tmp = total.pop();
            modifiedTotal.addAll(genPermutations(tmp)); //second set of permutations on each of the above where order does matter
        }
        return modifiedTotal;
    }

    //Generate all permutations of available letters
    public LinkedList<String> genPermutations(String letters) {
        LinkedList<String> perms = new LinkedList<>();
        perms.add("");
        for (int l = 0; l < letters.length(); l++) {
            LinkedList<String> newPerms = new LinkedList<>();
            String letter = letters.substring(l, l + 1);
            for (String perm : perms) {
                for (int i = 0; i <= l; i++) {
                    newPerms.add(perm.substring(0, i) + letter + perm.substring(i, l));
                }
            }
            perms = newPerms;
        }

        return perms;
    }

    //second constructor for our permutation methods
    private FrequentlyMissedDeadlines(int t, String... strings) {
        this.strings = strings;
        this.t = t;
        this.i = new int[t];
        for(int x = 0 ; x < this.t ; x++ ) {
            i[x] = x;
        }
    }

    private boolean permutate() {
        return permutate(this.t - 1);
    }

    private boolean permutate(int c) {
        if(c < 0) {
            return false;
        }
        this.i[c]++;
        int m = this.strings.length - (this.t - c - 1);
        if(this.i[c] >= m) {
            if(permutate(c - 1)) {
                this.i[c] = this.i[c - 1] + 1;
            } else {
                return false;
            }
        }
        return true;
    }

    //adds strings to create permutations before adding to list
    private void createList() {
        StringBuilder sb = new StringBuilder();
        for(int x = 0 ; x < this.t ; x++ ) {
            sb.append(this.strings[i[x]]);
        }
        if(sb.toString().length()>1) {
            this.unique.add(sb.toString());
        }
    }

    private static LinkedList<String> outer(int z, String[] strings) {
        FrequentlyMissedDeadlines p = new FrequentlyMissedDeadlines(z, strings);
        int c = 0;
        boolean running = true;
        while(running && c < 200) {
            p.createList();
            running = p.permutate();
            c++;
        }
        return p.unique;
    }

    //find which words are actual words
    public LinkedList<String> dictionaryChecker(LinkedList<String> allPerms) {
        LinkedList<String> words = new LinkedList<>();
        ArrayList<Word> tmpList = new ArrayList<>();

        while (!allPerms.isEmpty()) {
            String tmp = allPerms.pop();

            Word tmpWord = new Word(0, 1, true, tmp.toUpperCase());
            tmpList.add(0, tmpWord);

            if (dictionary.areWords(tmpList)) {
                words.add(tmp);
            }
            tmpList.remove(0);
        }
        return words;
    }
    //=====================================================Roland Change====================================================
    //find which words are actual words with board tile
    public LinkedList<String> dictionaryChecker(LinkedList<String> allPerms, char tileOnBoardInWord, int gridcoord) {
        LinkedList<String> words = new LinkedList<>();
        ArrayList<Word> tmpList = new ArrayList<>();
        String s = Character.toString(tileOnBoardInWord);

        while (!allPerms.isEmpty()) {
            String tmp = allPerms.pop();

            Word tmpWord = new Word(0, 1, true, tmp.toUpperCase());
            tmpList.add(0, tmpWord);

            int tilesBeforeBoardTile=0;
            int tilesAfterBoardTile = 0;
            for(int i=0;i<tmp.length();i++){
                if(tmp.charAt(i)== tileOnBoardInWord){
                    tilesBeforeBoardTile += i;
                    tilesAfterBoardTile = tmp.length() - i;
                    break;
                }
            }

            //check its a word , word contains tile from board, word will fit on board with tiles before and after tile on board
            if (dictionary.areWords(tmpList) && tmp.contains(s) && gridcoord-tilesBeforeBoardTile>=1 && gridcoord+tilesAfterBoardTile<=15) {
                words.add(tmp);
            }
            tmpList.remove(0);
        }

        if(words.size()==0)
            needExchange = true;

        return words;
    }

    //Calculate which word has the highest total value tile wise
    //find the word and removes it from the list and returns the word
    public String highestScoringWord(LinkedList<String> allWordsList) {
        int max = 0;
        int index = 0;
        int pointVal = 0;

        String currentHighestWord;
        String highestWord = "X";
        if(!allWordsList.isEmpty()){
            currentHighestWord = allWordsList.get(0);
            highestWord = currentHighestWord;
            max = pointValue(currentHighestWord);
        }

        for(int i = 1; i < allWordsList.size() && !allWordsList.isEmpty(); i++){
            currentHighestWord = allWordsList.get(i);
            pointVal = pointValue(currentHighestWord);

            if(max < pointVal){
                max = pointVal;
                highestWord = currentHighestWord;
                index = i;
            }
        }
        allWordsList.remove(index);
        return highestWord;
    }

    //Method to assign point value to each tile
    public int pointValue(String word) {
        int points = 0;
        for (int i = 0; i < word.length(); i++) {
            String tile = String.valueOf(word.charAt(i));
            int tileValue = 1;

            if ("DG".contains(tile)) {
                tileValue = 2;
            } else if ("BCMP".contains(tile)) {
                tileValue = 3;
            } else if ("FHVWY".contains(tile)) {
                tileValue = 4;
            } else if ("K".contains(tile)) {
                tileValue = 5;
            } else if ("JX".contains(tile)) {
                tileValue = 8;
            } else if ("QZ".contains(tile)) {
                tileValue = 10;
            } else if ("_".contains(tile)) {
                tileValue = 0;
            }

            points += tileValue;
        }
        return points;        //Return assigned point value of tile
    }


    //initializez and populates squareCoords TreeMap
    public void coordsOfOccupiedSquares(){
        int r, c;
        squareCoords = null;
        squareCoords = new TreeMap<>();
        for(int z = 1; z <= 10; z++){
            if(!(z == 6 || z == 7 || z == 9)){
                squareCoords.put(z, new ArrayList());
            }
        }
        int pointValue;
        Square temp_square;
        Coordinates coords;

        for(r = 0; r < 15; r++){
            for(c = 0; c < 15; c++){
                temp_square = board.getSquareCopy(r , c);
                coords = new Coordinates(r, c);
                if(temp_square.isOccupied()){
                    //=========================================================Changes Roland==========================================================
                    //So we don't get an error with blanks on the board since their value is BLANK_VALUE not an int
                    if(!temp_square.getTile().isBlank()) {
                        pointValue = temp_square.getTile().getValue();
                        squareCoords.get(pointValue).add(coords);
                        //=========================================================End Change==========================================================
                    }
                }
            }
        }
    }
    //get array of squares based on given point value
    //starts with the highest point tile and goes down
    private ArrayList<Coordinates> getCoordsList(int x){
        if(x == 6 || x == 7 || x == 9){
            return null;
        }
        ArrayList<Coordinates> tempList;
        if(squareCoords != null){
            return squareCoords.get(x);
        }
        return null;
    }
    public String findViableSquare(){
        coordsOfOccupiedSquares();
        String cmd = "PASS";
        Coordinates coords;
        ArrayList<Coordinates> tempList;
        if(squareCoords.size() > 1){

            for(int i = 10; i > 0 && cmd.equals("PASS"); i--){
                tempList = getCoordsList(i);

                if(tempList != null){

                    for(int j = 0; j < tempList.size() && cmd.equals("PASS"); j++){
                        coords = tempList.get(j);
                        cmd = processSquare(coords);
                    }
                }
            }
        }

        return cmd;
    }

    // takes the tile and find the right tiles to place to form a word and return the command to execute the action
    public String processSquare(Coordinates crds){
        int row = crds.getRow();
        int col = crds.getCol();
        //holds the empty squares on the board, left right up down
        int[] free_sqrs = new int[4];
        final int LEFT= 0;
        final int RIGHT = 1;
        final int UP = 2;
        final int DOWN = 3;
        boolean sentinel = true;
        String direction = "";
        String completeWrd = "";
        String cmd = "PASS";
        char col_char = 'x';
        int wrd_row = -1;
        String frame = me.getFrameAsString().replaceAll("[^a-zA-Z0-9_]", "");

        //=========================================================Changes Roland==========================================================
        String tmpFrame = frame;// using for exchange

        if(frame.contains("_")){
            frame = frame.replaceAll("_", "E");
        }

        //=========================================================End Changes==========================================================
        Square tempSquare = board.getSquareCopy(row, col);

        free_sqrs[LEFT] = getLeft(row, col);
        free_sqrs[RIGHT] = getRight(row, col);
        free_sqrs[UP] = getUp(row, col);
        free_sqrs[DOWN] = getDown(row, col);

        if((free_sqrs[LEFT] == 0 || free_sqrs[RIGHT] == 0) && (free_sqrs[UP] != 0 && free_sqrs[DOWN] != 0)){
            direction = "D";// stores direction of the word, used later on to form the command

            //=========================================================Changes Roland==========================================================
            //finds a word with a tile from board
            char boardTile = tempSquare.getTile().getLetter();
            frame = frame + boardTile;
            String[] frameArray = frame.trim().split("");

            allWords =  botsFirstTurn(frameArray );
            legalWords = dictionaryChecker(allWords, boardTile, row);   //calling dictionary check method

            if(needExchange) {

                if(previousFrame.equals(tmpFrame)){
                    return "PASS";
                }
                needExchange = false;
                previousFrame = tmpFrame;
                return "X " + tmpFrame;
            }

            // this will loop if the sentinel is true
            //sentinel is set to false if any of the legal words cant be places on the board
            //or if the there is less than 1 valid word in the list of legal words
            while(sentinel){
                completeWrd = highestScoringWord(legalWords);
                int tilesBeforeBoardTile=0;
                int tilesAfterBoardTile = 0 ;

                for(int i=0;i<completeWrd.length();i++){
                    if(completeWrd.charAt(i)== boardTile){
                        tilesBeforeBoardTile += i;
                        tilesAfterBoardTile = completeWrd.length() - i;
                        break;
                    }
                }

                if((tilesBeforeBoardTile <= free_sqrs[UP] && tilesAfterBoardTile <= free_sqrs[DOWN])){

                    col_char = (char)('A' + col);
                    wrd_row = row - tilesBeforeBoardTile;
                }

                completeWrd = blankInFrame(completeWrd); //changes word arrangment if blank was used w_rd o

                if(col_char != 'x' && wrd_row != -1){
                    cmd = col_char +""+ (wrd_row + 1) + " " + direction + " " + completeWrd;

                    sentinel = false;// valid command set to false and exit loop
                }
                sentinel = sentinel && legalWords.size() > 1;
            }

        } else if((free_sqrs[UP] == 0 || free_sqrs[DOWN] == 0) && (free_sqrs[LEFT] != 0 && free_sqrs[RIGHT] != 0)){
            direction = "A";

            //finds a word with a tile from board
            //String currentFrame = me.getFrameAsString().replaceAll("[^a-zA-Z0-9_]", "");
            char boardTile = tempSquare.getTile().getLetter();
            frame= frame + boardTile;
            String[] frameArray = frame.trim().split("");

            allWords =  botsFirstTurn(frameArray );
            legalWords = dictionaryChecker(allWords, boardTile,col);   //calling dictionary check method

            if(needExchange) {
                if(previousFrame.equals(tmpFrame)){
                    return "PASS";
                }
                needExchange = false;
                previousFrame = tmpFrame;
                return "X " + tmpFrame;
            }

            // this will loop if the sentinel is true
            //sentinel is set to false if any of the legal words cant be places on the board
            //or if the there is less than 1 valid word in the list of legal words
            while(sentinel){
                //gets the highest score word in the list
                completeWrd = highestScoringWord(legalWords);
                int tilesBeforeBoardTile=0;
                int tilesAfterBoardTile = 0;

                for(int i=0;i<completeWrd.length();i++){
                    if(completeWrd.charAt(i)== boardTile){
                        tilesBeforeBoardTile += i;
                        tilesAfterBoardTile = completeWrd.length() - i;
                        break;
                    }
                }

                if(tilesBeforeBoardTile <= free_sqrs[LEFT] && tilesAfterBoardTile <= free_sqrs[RIGHT]){

                    col_char = (char)((col - tilesBeforeBoardTile/*completeWrd.length()-1 - col*/) + 'A'); //*********************************?
                    wrd_row = row;
                }
                completeWrd = blankInFrame(completeWrd); //change word arrangment if blank was used w_rd o

                if(col_char != -1 && wrd_row != -1){
                    cmd = col_char +""+ (wrd_row + 1) + " " + direction + " " + completeWrd;

                    sentinel = false;// valid command set to false and exit loop
                }
                sentinel = sentinel && legalWords.size() > 1;
            }
        }

        if(tmpCommand.equalsIgnoreCase(cmd)) {
            needExchange = true;
        }

        return cmd;
    }

    //checks if blank was in frame and replace with the correct command
    public String blankInFrame(String word){
        if(containsBlank){
            if(word.contains("E")) {
                word = word.replaceFirst("E", "_");
                word = word + " E";
                containsBlank = false;
            }
        }
        return word;
    }

    private int getLeft(int row, int col){
        int count = 0;
        boolean sentinel = false;
        for(int x = col - 1; x >= 0 && !sentinel; x--){
            sentinel = board.getSquareCopy(row , x).isOccupied();
            if(!sentinel){
                count++;
            }
            else{
                if(count>0){
                    count--;
                }
            }
        }
        return count;
    }

    //4 get methods to get the available squares around a tile
    private int getRight(int row, int col){
        int count = 0;
        boolean sentinel = false;
        for(int x = col + 1; x < 15 && !sentinel; x++){
            sentinel = board.getSquareCopy(row, x).isOccupied();
            if(!sentinel){
                count++;
            }
            else{
                if(count>0){
                    count--;
                }
            }
        }
        return count;
    }

    private int getUp(int row, int col){
        int count = 0;
        boolean sentinel = false;
        for(int x = row - 1; x >= 0 && !sentinel; x--){
            sentinel = board.getSquareCopy(x, col).isOccupied();
            if(!sentinel){
                count++;
            }
            else{
                if(count>0){
                    count--;
                }
            }
        }
        return count;
    }

    private int getDown(int row, int col){
        int count = 0;
        boolean sentinel = false;
        for(int x = row + 1; x < 15 && !sentinel; x++){
            sentinel = board.getSquareCopy(x, col).isOccupied();
            if(!sentinel){
                count++;
            }
            else{
                if(count>0){
                    count--;
                }
            }
        }
        return count;
    }
}
