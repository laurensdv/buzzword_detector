package logic;

import com.google.common.collect.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import models.Article;
import models.Conference;
import models.KeywordEntity;
import query.Queries;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategorizerImpl implements Categorizer {

    public static Set<String> buzzwordwhitelistStatic;
    public static Set<String> buzzwordwhitelistDynamic;


    public static Set<String> dbpediaBuzzwords() {
        Set<String> response = Sets.newHashSet();
        Query query = QueryFactory.create(Queries.queryDbPediaBuzzwords); //s2 = the query above
        //System.out.println(query.toString());
        QueryExecution qExe = QueryExecutionFactory.sparqlService(Queries.sparqlService, query);
        ResultSet results = qExe.execSelect();
        //ResultSetFormatter.out(System.out, results, query) ;
        while (results.hasNext()) {
            QuerySolution row= results.next();
            RDFNode thing= row.get("s");
            response.add(thing.toString());
        }
        return response;
    }

    public CategorizerImpl() {
        // build buzzword lists
        buzzwordwhitelistStatic= ImmutableSet.of("Alignment", "At the end of the day", "Break through the clutter", "Bring to the table", "Buzzword", "Clear goal", "Countless", "Disruptive innovation", "Diversity", "Empowerment", "Exit strategy", "Face time", "Generation X", "Globalization", "Grow", "Impact", "Leverage", "Milestone", "Moving forward", "On the runway", "Organic growth", "Outside the box", "Paradigm", "Paradigm shift", "Proactive", "Push the envelope", "Reach out", "Robust", "Sea change", "Spin-up", "Streamline", "Survival strategy", "Sustainability", "Synergy", "Wellness", "Win-win", "Accountable talk", "Higher-order thinking", "Invested in", "Run like a business", "Student engagement", "Analytics", "Ballpark figure", "Bandwidth", "Business-to-Business", "B2B", "Business-to-Consumer", "B2C", "Best of Breed", "Best place to work", "Best practices", "Bizmeth", "Boil the ocean", "Brand", "Brick-and-mortar", "Business process outsourcing", "Buzzword compliant", "Building Capabilities", "Client-centric", "Cloud Computing", "Close the loop", "Co-opetition", "Core competency", "Customer-centric", "Deep dive", "Downsizing", "Drinking the Kool-Aid", "Eating your own dogfood", "Enable", "Entitlement", "Enterprise", "Event horizon", "Eyeballs", "Free value", "Fulfilment issues", "Granular", "Guard rails", "Herding cats", "Holistic approach", "Hyperlocal", "Knowledge Process Outsourcing", "Leverage", "Logistics", "Long Tail", "Low Hanging Fruit", "Make it pop", "Mindshare", "Mission Critical", "Management Visibility", "New economy", "Next generation", "Offshoring", "Pain point", "Peel back the onion", "Return on Investment", "Reverse fulfilment", "Rightshoring", "Seamless integration", "Serum", "Share options", "Shoot", "Solution", "SOX", "Sustainability", "Tee off", "Touchpoint", "Value-added", "Visibility", "4G", "Aggregator", "Ajax", "Algorithm", "Benchmarking", "Back-end", "Beta", "Big data", "Bleeding edge", "Blog", "Bring your own Device", "Bricks-and-clicks", "Clickthrough", "Cloud", "Collaboration", "Content management", "Content Management System", "CMS", "Convergence", "Cross-platform", "Datafication", "Data science", "Deep dive", "Design pattern", "DevOps", "Digital divide", "Digital Remastering", "Digital Rights Management", "DRM", "Digital signage", "Disruptive Technologies", "Document management", "Dot-bomb", "E-learning", "Engine", "Enterprise Content Management", "Enterprise Service Bus", "Framework", "Folksonomy", "Fuzzy logic", "HTML5", "Immersion", "Information superhighway", "Information highway", "Innovation", "Mashup", "Mobile", "Modularity", "Nanotechnology", "Netiquette", "Next Generation", "PaaS", "Pizzazz", "Podcasting", "Portal", "Real-time", "Responsive", "SaaS", "Scalability", "Skeuomorphic", "Social bookmarking", "Social software", "Spam", "Struts", "Sync-up", "Systems Development Life-Cycle", "Tagging", "Think outside the box", "Transmedia", "User generated content", "Virtualization", "Vlogging", "Vortal", "Web 2.0", "Webinar", "Weblog", "Web services", "Wikiality", "Workflow", "Pandering", "Big society", "Information society", "Political capital", "Socialist", "Stakeholder", "Truthiness", "Warfighter", "Plutocracy");
        buzzwordwhitelistDynamic = dbpediaBuzzwords();
    }

    public Table<Conference, Article, Set<KeywordEntity>> categorizeConference(Conference conference,
                                                                               Map<Article, Set<KeywordEntity>> keywordEntitiesByArticleMap) {

        Table<Conference, Article, Set<KeywordEntity>> table = HashBasedTable.create();

        // for each article..
        for(Map.Entry<Article, Set<KeywordEntity>> e : keywordEntitiesByArticleMap.entrySet()){

            Article art = e.getKey();
            Set<KeywordEntity> keywordentityset = e.getValue();

            // filtering step over all keywordentities found
            for(KeywordEntity ke : keywordentityset) {
                for(String bwFromList : buzzwordwhitelistStatic) {
                    if(bwFromList.equalsIgnoreCase(ke.getKeywordRepresentation())) {
                        // buzzword found
                        ke.setBuzzword(true);
                    }
                }
                for(String bwFromList : buzzwordwhitelistDynamic) {
                    if(bwFromList.equalsIgnoreCase(ke.getUri())) {
                        // buzzword found
                        ke.setBuzzword(true);
                    }
                }
            }
            table.put(conference, art, keywordentityset);
        }

        return table;

    }

//    @Override
//    public Table<Conference, Article, Set<KeywordEntity>> categorizeConference(Conference conference, Map<Article, Set<KeywordEntity>> keywordEntitiesByArticleMap) {
//        Table<Conference, Article, Set<KeywordEntity>> categorizedTable = HashBasedTable.create();
//        for (Article a : keywordEntitiesByArticleMap.keySet()) {
//            categorizedTable.put(conference,a, keywordEntitiesByArticleMap.get(a));
//        }
//        return categorizedTable;
//    }
}
