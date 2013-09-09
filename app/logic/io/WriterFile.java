package logic.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

public class WriterFile 
{
	public static void write(Multimap<String, String> data ,String filePath)
	{
		try {
            // Assume default encoding.
            FileWriter fileWriter = new FileWriter(filePath);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =  new BufferedWriter(fileWriter);
            bufferedWriter.write("article,label\n");
            for (String article: data.keySet()) 
            {
            	List<String> words = (List<String>) data.get(article);
            	for (String word : words) 
            	{
    				bufferedWriter.write(article+","+word+"\n");
				}
			}
            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) 
        {
            System.out.println("Error writing to file '" + filePath + "'");
            
        }
     
	}
}
