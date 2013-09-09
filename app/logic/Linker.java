package logic;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import models.Article;
import models.Conference;
import models.KeywordEntity;

import java.util.Map;
import java.util.Set;

public interface Linker {
    // Input is a table with columns: conference URI -> article URI -> ref to set of KeywordEntities
    public void link(Table<Conference, Article,Set<KeywordEntity>> keywordEntitiesByArticleByConferenceTable);

    public Map<Article, Set<KeywordEntity>> getKeywordEntitiesByArticle(String conferenceUri);

    public Multimap<String, String> getArticlesLinks(String linksFilePath);
}
