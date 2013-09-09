package models;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import query.Queries;

import java.util.Set;

public class Conference {
    protected String uri;
    protected Integer year;
    protected String title;

    public Conference() {
        this.uri = "";
        this.year = 0;
        this.title = "";
    }

    public Conference(String uri, String title, Integer year) {
        this.uri = uri;
        this.title = title;
        this.year = year;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static Set<Conference> all() {
        Set<Conference> response = Sets.newHashSet();
        Query query = QueryFactory.create(Queries.queryConferences); //s2 = the query above
        //System.out.println(query.toString());
        QueryExecution qExe = QueryExecutionFactory.sparqlService(Queries.sparqlService, query);
        ResultSet results = qExe.execSelect();
        //ResultSetFormatter.out(System.out, results, query) ;
        while (results.hasNext()) {
            QuerySolution row= results.next();
            RDFNode thing= row.get("conference");
            Literal year= row.getLiteral("year");
            Literal title = row.getLiteral("title");
            response.add(new Conference(thing.toString(),title.getString(),year.getInt()));
        }
        return response;
    }
}
