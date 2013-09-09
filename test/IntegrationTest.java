import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import logic.*;
import models.Article;
import models.Conference;
import models.KeywordEntity;
import org.junit.*;

import play.Logger;
import play.test.*;
import play.libs.F.*;
import query.Output;

import java.util.Map;
import java.util.Set;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


public class IntegrationTest {

    /**
     * This integration test uses Solenium to test the app with a browser
     */   
    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                assertThat(browser.pageSource()).contains("Hello from Java");
            }
        });
    }

    @Test
    public void deploy() {
        Annotator annotator = new AnnotatorImpl();
        Categorizer categorizer = new CategorizerImpl();
        Linker linker = new LinkerImpl();
        Set<Conference> conferences = Conference.all();
        Table<Conference, Article, Set<KeywordEntity>> resultTable = HashBasedTable.create();

        for (Conference conference : conferences) {
            Map<Article,Set<KeywordEntity>> keywordEntitiesByArticleMap = annotator.annotateConference(conference);
            //Logger.debug(keywordEntitiesByArticleMap.toString());
//          assertThat(keywordEntitiesByArticleMap.values().size()).isGreaterThan(0);
            resultTable.putAll(categorizer.categorizeConference(conference, keywordEntitiesByArticleMap));
        }

        Integer buzzwords = 0;
        Integer numOfArticles = 0;
        for (Conference c : conferences) {
                  numOfArticles += resultTable.row(c).size();
                  for (Article a : resultTable.row(c).keySet()) {
                      Set<KeywordEntity> keywordEntities = resultTable.get(c,a);
                      for (KeywordEntity ke : keywordEntities) {
                          if (ke.isBuzzword().booleanValue()==Boolean.TRUE) {
                             buzzwords+=1;
                             System.out.println(ke.getKeywordRepresentation());
                          }
                  }
            }
        }
        assertThat(buzzwords).isGreaterThan(0);
        System.out.println(String.format("Found %s buzzwords in %s articles",buzzwords,numOfArticles));
        linker.link(resultTable);
        for (Conference c : conferences) {
            Map<Article, Set<KeywordEntity>> keyArticleSetMap = linker.getKeywordEntitiesByArticle(c.getUri());
            //assertThat(keyArticleSetMap.size()).isGreaterThan(0);
            System.out.println(Output.createCSV(keyArticleSetMap));

        }

        //linker.getArticlesLinks("app/assets/xml/linksfile.nt");
        System.out.println(linker);
    }
  
}
