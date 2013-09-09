package logic;

import models.Article;
import models.Conference;
import models.KeywordEntity;

import java.util.Map;
import java.util.Set;

public interface Annotator {
    public Set<KeywordEntity> annotateArticle(Article article);
    public Map<Article, Set<KeywordEntity>> annotateConference(Conference conference);
}
