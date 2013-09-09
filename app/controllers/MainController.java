package controllers;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import logic.*;
import models.Article;
import models.Conference;
import models.KeywordEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.lib.MultiMap;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.Routes;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import query.Output;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;

public class MainController extends Controller {
    protected static final Annotator annotator = new AnnotatorImpl();
    protected static final Categorizer categorizer = new CategorizerImpl();
    protected static final Linker linker = new LinkerImpl();
    
    public static Result index() {
        return ok(views.html.index.render("Hello from Java"));
    }

    //@BodyParser.Of(BodyParser.Json.class)
    public static Result process() {
        String text = "";
        System.out.println(request().body().asFormUrlEncoded());
        if (request().body().asFormUrlEncoded().containsKey("text"))
            text = request().body().asFormUrlEncoded().get("text")[0].toString();
        if (text.isEmpty()) {
            Logger.error("No index argument provided.");
            return internalServerError("Please provide a valid input argument");
        }

        ObjectNode result = Json.newObject();
        try {
            result.put("text",text);
            response().setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            return ok(result);
        } catch (Exception e) {
            result.put("error",e.getMessage());
            Logger.error(e.toString());
            response().setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            return internalServerError(result);
        }

    }

    public static Result conferences() {
        Set<Conference> conferences = Conference.all();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Conference c : conferences) {
            sb.append(i).append(",").append(c.getYear()).append(",").append(c.getTitle()).append(",").append(c.getUri());
            sb.append("\n");
            i+=1;
        }
        response().setContentType("text/csv");
        response().setHeader("Content-disposition","attachment; filename="+"conferences.csv");
        return ok(sb.toString());
    }

    public static Result articleLinks() {
        Multimap<String, String> articleToArticleMap = linker.getArticlesLinks("app/assets/xml/linksfile.nt");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String article : articleToArticleMap.keys()) {
            for (String otherArticle : articleToArticleMap.get(article)) {
                sb.append(i).append(",").append(article).append(",").append(otherArticle).append("\n");
                i+=1;
            }
        }
        response().setContentType("text/csv");
        response().setHeader("Content-disposition","attachment; filename="+"conferences.csv");
        return ok(sb.toString());
    }

    public static Result buildConference() {
        String conference = request().getQueryString("conference");
        if (Strings.isNullOrEmpty(conference)) {
            Logger.error("No conference argument provided.");
            return badRequest("Please provide a valid input argument");
        }
        String conferenceLabel = StringUtils.substring(conference,conference.lastIndexOf("/"));
        conferenceLabel = StringUtils.strip(conferenceLabel, ":./");

        Map<Article, Set<KeywordEntity>> keArticleSetMap = linker.getKeywordEntitiesByArticle(conference);
        String csv = Output.createCSV(keArticleSetMap);
        response().setContentType("text/csv");
        response().setHeader("Content-disposition","attachment; filename="+conferenceLabel+".csv");
        return ok(csv);
    }

    public static void registerOutChannelSomewhere(Chunks.Out<String> out, Set<Conference> conferences) {
        Table<Conference, Article, Set<KeywordEntity>> resultTable = HashBasedTable.create();
        out.write("annotating conferences...\n");
        for (Conference conference : conferences) {
            out.write(String.format("annotating %s\n",conference.getTitle()));
            Map<Article,Set<KeywordEntity>> keywordEntitiesByArticleMap = annotator.annotateConference(conference);
            //Logger.debug(keywordEntitiesByArticleMap.toString());
            resultTable.putAll(categorizer.categorizeConference(conference, keywordEntitiesByArticleMap));
        }
        out.write("loading into store...\n");

        //Integer buzzwords = 0;
        //Integer numOfArticles = 0;
        //for (Conference c : conferences) {
        //    numOfArticles += resultTable.row(c).size();
        //    for (Article a : resultTable.row(c).keySet()) {
        //        Set<KeywordEntity> keywordEntities = resultTable.get(c,a);
        //        for (KeywordEntity ke : keywordEntities) {
        //            if (ke.isBuzzword().booleanValue()==Boolean.TRUE) {
        //                buzzwords+=1;
        //                System.out.println(ke.getKeywordRepresentation());
        //            }
        //        }
        //    }
        //}

        linker.link(resultTable);

        //out.write(String.format("done and found %s buzzwords in %s articles",buzzwords,numOfArticles));
        out.write("done");
        out.close();
    }

    public static Result reloadData() {
        final Set<Conference> conferences = Conference.all();
//
        // Prepare a chunked text stream
        Chunks<String> chunks = new StringChunks() {

         // Called when the stream is ready
         public void onReady(Chunks.Out<String> out) {
                registerOutChannelSomewhere(out, conferences);
            }

        };

        // Serves this stream with 200 OK
        return ok(chunks);
    }

    public static Result mainRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("mjsRoutes",
                        // Routes
                        routes.javascript.MainController.process()
                )
        );
    }


}
