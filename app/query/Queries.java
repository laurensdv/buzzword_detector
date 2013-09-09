package query;

public class Queries {
    public final static String sparqlService = "http://km.aifb.kit.edu/projects/synctech/virtuoso/sparql";

    public final static String dbPediaSparqlService = "http://www.dbpedia.org/sparql";

    public final static String queryDbPediaBuzzwords = "SELECT DISTINCT ?s ?label from <http://dbpedia.org> { ?s  <http://purl.org/dc/terms/subject> <http://dbpedia.org/resource/Category:Buzzwords> .}";

    public final static String queryDbPediaKeywords = "SELECT DISTINCT ?o from <http://data.semanticweb.org/conference> { ?s <http://purl.org/dc/elements/1.1/subject> ?o .}";

    public final static String queryConferences =
            "PREFIX  swrc:    <http://swrc.ontoware.org/ontology#>\n" +
            "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#>\n" +
            "\n" +
            "SELECT DISTINCT ?conference ?year ?title\n" +
            "FROM <http://data.semanticweb.org/conference>\n"+
            "WHERE\n" +
            "  { ?conference a swrc:Proceedings .\n" +
            "    ?conference swrc:year ?year .\n" +
            "    ?conference swc:relatedToEvent ?event .\n" +
            "    ?event rdfs:label ?title\n"+
            "  }\n" +
            "LIMIT   25\n" +
            "";

    public final static String queryArticlesByConference =
                    "PREFIX  swrc:    <http://swrc.ontoware.org/ontology#>\n" +
                    "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#>\n" +
                    "\n" +
                    "SELECT DISTINCT ?article ?abstract\n" +
                    "FROM <http://data.semanticweb.org/conference>\n"+
                    "WHERE\n" +
                    "  { ?s swc:hasPart ?article .\n" +
                    "    ?article swrc:abstract ?abstract .\n" +
                    "  }\n" +
                    "LIMIT 50\n" +
                    "";
}
