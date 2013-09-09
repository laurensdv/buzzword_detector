package logic;

import com.google.common.collect.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import controllers.Assets;
import de.uni_leipzig.simba.controller.PPJoinController;
import models.Article;
import models.Conference;
import models.KeywordEntity;
import org.springframework.core.io.ClassPathResource;

import java.net.URL;
import java.util.Map;
import java.util.Set;

public class LinkerImpl implements Linker{
    protected final Table<Conference, Article, Set<KeywordEntity>> data = HashBasedTable.create();

    //@Override
    //public void link(Table<Conference, Article, Set<KeywordEntity>> keywordEntitiesByArticleByConferenceTable) {
    //    cacheTable.putAll(keywordEntitiesByArticleByConferenceTable);
    //}

    @Override
    public void link(Table<Conference, Article, Set<KeywordEntity>> keywordEntitiesByArticleByConferenceTable)
    {
        data.putAll(keywordEntitiesByArticleByConferenceTable);//set the data to the Table data
        //prepareLinkingDataFile("app/assets/xml/data.csv");
        //linkingProcess("app/assets/xml/csv_articles_articles.xml");
    }

    private void linkingProcess(String specFilePath)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = classLoader.getResource(specFilePath).getPath();

        PPJoinController controller = new PPJoinController();
        controller.run(path);
    }

    @Override
    public Multimap<String, String> getArticlesLinks(String linksFilePath) //"/home/mofeed/Desktop/linksfile.nt"
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = classLoader.getResource(linksFilePath).getPath();
        Model model = logic.io.Reader.readModel(path.toString());

        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  SELECT ?s ?o WHERE  { ?s ?p ?o}";
        Query query = QueryFactory.create(queryString);
        QueryExecution exec = QueryExecutionFactory.create(query, model);
        ResultSet rs = exec.execSelect();
        Multimap<String, String> resultStrings = ArrayListMultimap.create();

        while(rs.hasNext())
        {
            QuerySolution sol = rs.next();
            resultStrings.put(sol.get("?s").asResource().getURI(), sol.get("?o").asResource().getURI());
        }
        return resultStrings;
    }

    private void prepareLinkingDataFile(String csvDataFilePath)
    {
        Multimap<String, String> article_words = ArrayListMultimap.create();//to fill the articles with theri buzzwords

        //Map<Conference,Map<Article,Set<KeywordEntity>>> dataMap= data.rowMap();

        for (Conference conference : data.rowKeySet())
        {
            Map<Article,Set<KeywordEntity>> article_data = data.row(conference);
            for(Article article : article_data.keySet() )
            {
                String article_uri =article.getUri();
                for(KeywordEntity ke : article_data.get(article))
                {
                    String word = ke.getKeywordRepresentation();
                    article_words.put(article_uri, word);
                }
            }
        }
        logic.io.WriterFile.write(article_words,csvDataFilePath);
    }


    @Override
    public Map<Article, Set<KeywordEntity>> getKeywordEntitiesByArticle(String conferenceUri) {
        for (Conference c : data.rowKeySet()) {
            if (c.getUri().equalsIgnoreCase(conferenceUri)) {
                return data.row(c);
            }
        }
        return Maps.newHashMap();
    }
}
