import logic.CategorizerImpl;
import models.Article;
import models.Conference;
import org.junit.Test;

import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

public class QueriesTest {
    @Test
    public void testConferences() {
        assertThat( Conference.all().size()).isGreaterThan(1);
    }

    @Test
    public void testArticles() {
        Conference c = new Conference("http://data.semanticweb.org/conference/eswc/2013/proceedings", "ESWC", 2013);
        assertThat( Article.articlesByConference(c).size()).isGreaterThan(1);
    }

    @Test
    public void testDbPediaBuzzWords() {
        Set<String> buzzWords = CategorizerImpl.dbpediaBuzzwords();
        assertThat(buzzWords.size()).isEqualTo(66);
    }
}
