package logic;

import com.google.common.collect.Table;
import models.Article;
import models.Conference;
import models.KeywordEntity;

import java.util.Map;
import java.util.Set;

public interface Categorizer {
    public Table<Conference, Article,Set<KeywordEntity>> categorizeConference(Conference conference, Map<Article,Set<KeywordEntity>> keywordEntitiesByArticleMap);
}
