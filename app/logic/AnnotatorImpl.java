package logic;

import akka.dispatch.ExecutionContexts;
import akka.japi.Function;
import akka.util.Timeout;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import models.Article;
import models.Conference;
import models.KeywordEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import play.Logger;
import play.api.libs.ws.WS;
import play.libs.F;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Http.RequestBody;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static akka.dispatch.Futures.traverse;
import static akka.dispatch.Futures.future;

public class AnnotatorImpl implements Annotator {

    // CLESA web service configuration information
    public final static String SERVICE_URL = "http://km.aifb.kit.edu/services/wifi-annotation/";
    public final static String PARAM_TEXT = "doc";
    public final static String PARAM_INPUT_LANGUAGE = "lang";
    final protected Timeout timeout = new Timeout(Duration.create(600, "seconds"));
    protected static final ExecutionContext ec = ExecutionContexts.global();

    @Override
    public Set<KeywordEntity> annotateArticle(Article article) {
        /** Use DBpedia miner web service **/
        String language = "en";
        final Set<KeywordEntity> resultset = Sets.newHashSet();

        String[] paramName = { PARAM_TEXT, PARAM_INPUT_LANGUAGE };
        String[] paramValue = { article.getContent(), language };

        String httpResponse = httpGet(SERVICE_URL, paramName, paramValue);
//			System.out.println("Http Response:");
//			System.out.println(httpResponse);


        /** Begin abstract parsing (finding entities) **/
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();



            DefaultHandler handler = new DefaultHandler() {

                KeywordEntity ke =  new KeywordEntity();

                public void startElement(String uri, String localName,String qName,
                                         Attributes attributes) throws SAXException {

                    if (qName.equalsIgnoreCase("annotation")) {
                        ke = new KeywordEntity();
                        ke.setKeywordRepresentation(attributes.getValue("displayName"));

                    }
                    else if(qName.equalsIgnoreCase("description")) {
                        ke.setUri(attributes.getValue("URL"));
                        resultset.add(ke);
                    }

                }

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {
                }

            };

            saxParser.parse(new ByteArrayInputStream(httpResponse.getBytes("UTF-8")), handler);

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        return resultset;

    }

    public static String httpPost(String urlRequest, String[] paramName, String[] paramVal) {
        HttpClient client = new DefaultHttpClient();
        HttpPost method = new HttpPost(urlRequest);
        InputStream rstream = null;
        BasicHttpParams params = new BasicHttpParams();
        // Add POST parameters
        for (int i = 0; i < paramName.length; i++) {
            params.setParameter(paramName[i], paramVal[i]);
        }
        method.setParams(params);

        // Send POST request
        try {
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Logger.error("Method failed: " + response.getStatusLine());
            }
            // Get the response body
            rstream = response.getEntity().getContent();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return getResponse(rstream);
    }

    protected static String getResponse(InputStream rstream) {
        String response = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Logger.error(e.getMessage());
        }
        return response;
    }

    public static String httpGet(String urlRequest, String[] paramName, String[] paramVal) {
        HttpClient client = new DefaultHttpClient();

        String url =  urlRequest;
        for (int i = 0; i < paramName.length; i++) {
                url+= "?"+URLEncoder.encode(paramName[i])+"="+URLEncoder.encode(paramVal[i])+"&";
            }
        //Logger.debug(url);
        HttpGet method = new HttpGet(url);
        InputStream rstream = null;
        // Send GET request
        try {
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Logger.error("Method failed: " + response.getStatusLine());
            }
            rstream = response.getEntity().getContent();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return getResponse(rstream);
    }

    @Override
    public Map<Article, Set<KeywordEntity>> annotateConference(Conference conference) {
        final Map<Article, Set<KeywordEntity>> keywordEntitiesByArticleMap = Maps.newHashMap();
        Set<Article> articles = Article.articlesByConference(conference);
        Future<Iterable<Boolean>> futureResult = traverse(articles,
                new Function<Article, Future<Boolean>>() {
                    public Future<Boolean> apply(final Article article) {
                        return future(new Callable<Boolean>() {
                            public Boolean call() {
                                Set<KeywordEntity> entities = annotateArticle(article);
                                keywordEntitiesByArticleMap.put(article,entities);
                                return Boolean.TRUE;
                            }
                        }, ec);
                    }
                }, ec);
        //for (Article article : articles) {
        //    Set<KeywordEntity> entities = annotateArticle(article);
        //    keywordEntitiesByArticleMap.put(article,entities);
        //}
        try{
            Await.result(futureResult,timeout.duration());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return keywordEntitiesByArticleMap;
    }
}