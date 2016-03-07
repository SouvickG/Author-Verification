/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package authorship.verification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class Weka {
    public void createFeatureFile(String inputFileLoc, String outputFileLoc) throws Exception {
        //ATTRIBUTES
        Attribute attr[]= new Attribute[50];
	//Nominal
	FastVector myNomVals = new FastVector(4);
	myNomVals.addElement("Dutch");
        myNomVals.addElement("English");
        myNomVals.addElement("Greek");
        myNomVals.addElement("Spanish");
	attr[1] = new Attribute("language", myNomVals);
				
        //Numeric
	attr[2] = new Attribute("comma_ratio");
	attr[3] = new Attribute("semicolon_ratio");
        attr[4] = new Attribute("colon_ratio");
        attr[5] = new Attribute("stop_ratio");
        attr[6] = new Attribute("question_ratio");
        attr[7] = new Attribute("exclamation_ratio");
        attr[8] = new Attribute("slash_ratio");
        attr[9] = new Attribute("dash_ratio");
        attr[10] = new Attribute("shortsentence_ratio");
        attr[11] = new Attribute("longsentence_ratio");
        attr[12] = new Attribute("uniqueword_ratio");
		
        //Class
        FastVector classValue = new FastVector(2);
	classValue.addElement("Y");
        classValue.addElement("N");
        attr[13] = new Attribute("answer", classValue);
                
	//Create dataset
	FastVector attrs = new FastVector();
		
	attrs.addElement(attr[1]);
	attrs.addElement(attr[2]);
	attrs.addElement(attr[3]);
        attrs.addElement(attr[4]);
        attrs.addElement(attr[5]);
	attrs.addElement(attr[6]);
	attrs.addElement(attr[7]);
        attrs.addElement(attr[8]);
        attrs.addElement(attr[9]);
	attrs.addElement(attr[10]);
	attrs.addElement(attr[11]);
        attrs.addElement(attr[12]);
        attrs.addElement(attr[13]);
                
	Instances dataset = new Instances("my_dataset", attrs, 0);
		
	//Add instances
        FileInputStream fstream = new FileInputStream(inputFileLoc);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                
        String lines[] = new String[236600];
	String str;
        int c = 0;
        while((str = br.readLine())!=null)
            lines[c++] = str;
        //int no_of_words = lines[0].split("\t").length-2;
        ArrayList label = new ArrayList();
        for(int i = 0;i<c;i++)
        {
            String arr[] = lines[i].split("\t");
            if(!label.contains(arr[arr.length-1]))
                label.add(arr[arr.length-1]);
        }
        for(int i = 0;i<c;i++){
            Instance example = new Instance(4);
            String arr[] = lines[i].split("\t");
            for(int j = 1;j<arr.length;j++)
                example.setValue(attr[j], arr[j]);
            dataset.add(example);
        }
		
        //Save dataset
        String file = outputFileLoc;
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File(file));
        saver.writeBatch();
    }
}
