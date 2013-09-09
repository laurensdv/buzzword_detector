package models;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import query.Queries;

import java.util.Set;

public class Article {
    protected final String uri;
    protected final String content;

    public Article() {
        this.uri = "";
        this.content = "";
    }

    public Article(String uri, String content) {
        this.uri = uri;
        this.content = content;
    }

    public Article(String uri) {
        this.uri = uri;
        this.content = "";
    }

    public String getUri() {
        return uri;
    }

    public String getContent() {
        return content;
    }

    public static Set<Article> articlesByConference(Conference conference) {
        Set<Article> response = Sets.newHashSet();
        ParameterizedSparqlString queryStr = new ParameterizedSparqlString(Queries.queryArticlesByConference);
        Model model = ModelFactory.createDefaultModel();

        RDFNode conferenceUri = model.createResource(conference.getUri());
        queryStr.setParam("s",conferenceUri);
        Query query = QueryFactory.create(queryStr.toString());
        //System.out.println(queryStr);
        QueryExecution qExe = QueryExecutionFactory.sparqlService(Queries.sparqlService, query);
        ResultSet results = qExe.execSelect();
        //ResultSetFormatter.out(System.out, results, query) ;
        while (results.hasNext()) {
            QuerySolution row= results.next();
            RDFNode thing= row.get("article");
            Literal content= row.getLiteral("abstract");
            response.add(new Article(thing.toString(),content.toString()));
        }
        return response;
    }
}
