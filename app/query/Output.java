package query;

import com.google.common.collect.*;
import models.Article;
import models.Conference;
import models.KeywordEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

public class Output {
    public static String createCSV(Map<Article, Set<KeywordEntity>> _resultSet) {
        String filename = "";

        Map<KeywordEntity,Set<Article>> articlesByKeywordEntity = Maps.newHashMap();
        BiMap<Article,Integer> columnNumberByArticle = HashBiMap.create();

        Integer colNumber = 0;

            for (Article a: _resultSet.keySet()) {
                if (!columnNumberByArticle.containsKey(a)) {
                     columnNumberByArticle.put(a, colNumber++);
                }
                for (KeywordEntity k : _resultSet.get(a))   {
                    if(!articlesByKeywordEntity.containsKey(k) && k.isBuzzword()) {
                       Set<Article> h = Sets.newHashSet();
                       articlesByKeywordEntity.put(k, h);
                    }
                    if(k.isBuzzword())
                       articlesByKeywordEntity.get(k).add(a);
                }
            }
        StringBuilder bw = new StringBuilder();
        try{
            // CSV file creation

            bw.append("keyword,");
            for(int i = 0, count = columnNumberByArticle.size(); i < count; i++){
                bw.append(columnNumberByArticle.inverse().get(i).getUri());
                bw.append(",");
            }
            bw.append("total");
            bw.append("\n");
            for (KeywordEntity kE : articlesByKeywordEntity.keySet())
            {
                Integer c = 0;
                bw.append(kE.getKeywordRepresentation());
                bw.append(",");
                for(int i = 1, count = columnNumberByArticle.size(); i <= count; i++){
                    if(articlesByKeywordEntity.get(kE).contains(columnNumberByArticle.inverse().get(i))) {
                        bw.append(1);
                        bw.append(",");
                        c+=1;
                    }
                    else {
                        bw.append(0);
                        bw.append(",");
                    }
                }
                bw.append(c);
                bw.append("\n");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return bw.toString();
    }
}
