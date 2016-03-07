/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package authorship.verification;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author Souvick
 */
public class AuthorshipVerification {

    String op;
    static String input_path, output_path;
    Read_and_Write rw = new Read_and_Write();
    String content;
    private Properties prop;
    private StanfordCoreNLP pipeline;
    //LexicalizedParser lp = LexicalizedParser.loadModel("W:\\study\\PG course\\project\\PAN\\JU_CSE_PAN\\library\\stanford-corenlp-full-2014-08-27\\stanford-corenlp-3.4.1-models\\edu\\stanford\\nlp\\models\\lexparser\\englishPCFG.ser.gz");
    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf= tlp.grammaticalStructureFactory();
    private int MAX=200;
    private String yn;
    
    String[] punctuationToWordRatio = new String[100000];
     String[] commaRatio = new String[100000];
     String[] semicolonRatio = new String[100000];
     String[] colonRatio = new String[100000];
     String[] stopRatio = new String[100000];
     String[] questionRatio = new String[100000];
     String[] exclamationRatio = new String[100000];
     String[] slashRatio = new String[100000];
     String[] dashRatio = new String[100000];
     String[] shortsentencebytotal = new String[100000];
     String[] longsentencebytotal = new String[100000];
     String[] uniqueword = new String[100000];
    
     public AuthorshipVerification(){
        prop = new Properties();
        try{
            prop.setProperty("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            prop.setProperty("ssplit.eolonly", "true");
            this.pipeline = new StanfordCoreNLP(prop);
        }
        catch(Exception e){
            System.out.println("Error in setting properties file:"+e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        AuthorshipVerification av=new AuthorshipVerification();
        input_path= args[2]; //input path from command line argument
        output_path= args[4]; //output path from command line argument
        av.takeInputAllLanguage(input_path);
        Weka weka= new Weka();
        weka.createFeatureFile(output_path+"\\test_file.txt", output_path+"\\feature_file.txt");
    }
    
    void takeInputAllLanguage(String input_path) throws FileNotFoundException, IOException
    {   
        //redirect to each language folder
        File dir = new File(input_path);
        String[] children = dir.list();
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        }
        else {
            op="";
            for (int i=0; i<children.length; i++) {
                String subdir = children[i];
                String new_addr=input_path+"\\"+subdir;
                takeInputSingleLanguage(new_addr);
            }
        }
    }
    
    void takeInputSingleLanguage(String input_path) throws FileNotFoundException, IOException
    {
        String language=null;
        File dir = new File(input_path);
        String[] children = dir.list();
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        }
        else {
            for (int i=0; i<children.length; i++) {
                String file_name = children[i];
                String check=file_name.substring(file_name.length()-4);
                if(check.equals("json")){
                    ReadJSON rj= new ReadJSON();
                    language= rj.readJson(input_path+"\\"+file_name);
                    System.out.println(language);
                }
                else if(check.equals(".txt")){
                     yn= rw.read_file(input_path+"\\"+file_name);
                }
            }
            for (int i=0; i<children.length; i++) {
                String file_name = children[i];
                String check=file_name.substring(file_name.length()-4);
                if(check.equals("json"))
                    continue;
                else if(check.equals(".txt"))
                    continue;
                else
                    takeInput(language, input_path+"\\"+file_name);
            }
        }
    }
   
    void takeInput(String language, String input_path) throws FileNotFoundException, IOException
    {   
        //Read the contents of .txt files
        int i;
        File dir = new File(input_path);
        String[] children = dir.list();
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        }
        else {
            for (i=0; i<children.length; i++) {
                String file_name = children[i];
                String adrs=input_path+"\\"+file_name;
                content = rw.read_file(adrs).toLowerCase();
                handle_files(content, adrs);
            }
            StringTokenizer tokenizer = new StringTokenizer(op,"\t");
            for(i=0; i<children.length; i++){
                punctuationToWordRatio[i]=tokenizer.nextToken();
                commaRatio[i]=tokenizer.nextToken();
                semicolonRatio[i]=tokenizer.nextToken();
                colonRatio[i]=tokenizer.nextToken();
                stopRatio[i]=tokenizer.nextToken();
                questionRatio[i]=tokenizer.nextToken();
                exclamationRatio[i]=tokenizer.nextToken();
                slashRatio[i]=tokenizer.nextToken();
                dashRatio[i]=tokenizer.nextToken();
                shortsentencebytotal[i]=tokenizer.nextToken();
                longsentencebytotal[i]=tokenizer.nextToken();
                uniqueword[i]=tokenizer.nextToken();
            }
            op = language+"\t";
            op+= getAverage(punctuationToWordRatio, i) +"\t";
            op+= getAverage(commaRatio, i)+"\t";
            op+= getAverage(semicolonRatio, i)+"\t";
            op+= getAverage(colonRatio, i)+"\t";
            op+= getAverage(stopRatio, i)+"\t";
            op+= getAverage(questionRatio, i)+"\t";
            op+= getAverage(exclamationRatio, i)+"\t";
            op+= getAverage(slashRatio, i)+"\t";
            op+= getAverage(dashRatio, i)+"\t";
            op+= getAverage(shortsentencebytotal, i)+"\t";
            op+= getAverage(longsentencebytotal, i)+"\t";
            op+= getAverage(uniqueword, i)+"\t";
            
            String auth_id=input_path.substring(input_path.length()-6);
            op+= checkAnswer(auth_id)+"\n";
            System.out.println(op);
            rw.write_file(op, output_path+"\\test_file.txt");
            op=" ";
    }
}
    void handle_files(String content, String adrs) throws FileNotFoundException
    {
        int sentencecounter[] = new int[MAX];
        double longsentbytotal,shortsentbytotal;
        String tagged_output,word,pos,wordfreq,posfreq = null,posseqfreq,bigrams,trigrams;
        
        POStag pt=new POStag();
        tagged_output = pt.postag(content);
        pos = pt.getPOSTags(tagged_output);
        
        RootWords rt=new RootWords();
        word = rt.getWords(tagged_output);
        
        WordFrequency wf= new WordFrequency();
        wordfreq = wf.getWordFrequency_toString(word);
        float uniqueword=wf.getUniqueWordRatio(word);
        //System.out.println(wordfreq);
        
        POSfrequency pf= new POSfrequency();
        posfreq = pf.getPosFrequency_toString(pos);
        System.out.println(posfreq);
        
        POSsequence seq= new POSsequence();
        posseqfreq= seq.getPosSeqFrequency_toString(pos);
        
        Bigrams bg= new Bigrams();
        bigrams = bg.getBigrams_toString(word);
        
        Trigrams tg= new Trigrams();
        trigrams = tg.getTrigrams_toString(word);
        
        SentenceRatio st= new SentenceRatio();
        sentencecounter = st.countSentences(content,sentencecounter);
        longsentbytotal = st.getLongSentenceRatio(sentencecounter);
        shortsentbytotal = st.getShortSentenceRatio(sentencecounter);
         
        Punctuation p=new Punctuation();
        String punc_out= p.countPunctuationRatio(content);
         
        
        //op +=   posfreq + "\t";
        //op +=  posseqfreq+ "\t"; 
        //op +=  bigrams+ "\t";
        //op +=  trigrams+ "\t";
        op +=  punc_out+"\t";
        op +=  shortsentbytotal+ "\t";
        op +=  longsentbytotal + "\t";
        op +=  uniqueword+"\t";
        
        POSAtSentenceStart pass= new POSAtSentenceStart();
        String beginofsent = pass.getBeginningofSentence(content);
        //System.out.println(beginofsent);
        
        //String posatbeginnning= pass.getPOSAtSentenceStart(tagged_output);
        //System.out.println(posatbeginnning);
        
          
    }
    
    char checkAnswer(String auth_id)
    {
        char answer=yn.charAt(yn.indexOf(auth_id)+6);
        return answer;
    }
    
    float getAverage(String[] array, int count)
    {
        int i;
        float sum=0, result;
        for(i=0;i<count-1;i++){
            sum+= Float.parseFloat(array[i]);
            System.out.println("sum="+sum);
        }
        result=sum/(i);
        System.out.println("unk="+Float.parseFloat(array[i]));
        result= result-(Float.parseFloat(array[i]));
        return result;
    }
}
