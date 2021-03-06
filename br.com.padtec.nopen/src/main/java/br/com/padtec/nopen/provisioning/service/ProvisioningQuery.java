package br.com.padtec.nopen.provisioning.service;

import java.util.ArrayList;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ProvisioningQuery {

	//Create query string
	static String prefix = "PREFIX ont: <" + ProvisioningComponents.provisioningRepository.getNamespace() + "> " + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
	
	
	public static boolean isConnectedPort (String port) {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		//normal query
		String queryString = prefix +
				"SELECT ?port WHERE { ont:" + port + " ont:vertical_links_to|ont:horizontal_links_to ?port . }";
				
		System.out.println(queryString);
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		//get result and put on label variable
		if (results.hasNext()) {
			return true;
		}
		
		//inverse query
		queryString = prefix +
				"SELECT ?port WHERE { ?port ont:vertical_links_to|ont:horizontal_links_to ont:" + port + " . }";
			
		System.out.println(queryString);
		
		query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, ontModel);
		results = qe.execSelect();
		
		//get result and put on label variable
		if (results.hasNext()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean askToOWL(String subject, ArrayList<String> predicates, String object) {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		String queryString = prefix +
				"ASK { ont:" + subject + " ont:" + predicates.get(0);
		
		for(int i = 1; i < predicates.size(); i++) {
			queryString += "/ont:" + predicates.get(i);
		}
		
		queryString += " ont:" + object + " . }" ;
				
		System.out.println(queryString);
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();
		
		//get result and put on label variable
		if(results.hasNext()) {
			QuerySolution row = results.next();
		    String result = row.get("Result").toString();
		    
		   if(result.equals("True")) {
			   return true;
		   }
		}
		
		return false;
	}
	
	public static ArrayList<String> getAllLayersFromOWL() {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		//create query string
		String queryString = prefix + "SELECT DISTINCT ?label WHERE { ?layer rdf:type ont:Layer_Type . ?layer rdfs:label ?label . }";
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		ArrayList<String> layers = new ArrayList<String>();
		
		//get result and put on label variable
		while (results.hasNext()) {
			QuerySolution row = results.next();
			String label = row.get("label").toString();
			label = label.replace("@en", "");
			label = label.replace("@EN", "");
			
			System.out.println("LABEL: " + label);
			
			layers.add(label);
		}
		
		return layers;
		
	}
	
	public static ArrayList<String> getTopLayersFromOWL() {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		//create query string
		String queryString = prefix + "SELECT DISTINCT ?label WHERE { ?layer rdf:type ont:Layer_Type . ?layer rdfs:label ?label . FILTER NOT EXISTS { ?layer ont:is_client ?x . } }";
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		ArrayList<String> topLayers = new ArrayList<String>();
		
		//get result and put on label variable
		while (results.hasNext()) {
			QuerySolution row = results.next();
			String label = row.get("label").toString();
			label = label.replace("@en", "");
			label = label.replace("@EN", "");
			topLayers.add(label);
		}
		
		return topLayers;
		
	}
	
	public static ArrayList<String> getBottomLayersFromOWL() {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		//create query string
		String queryString = prefix + "SELECT DISTINCT ?label WHERE { ?layer rdf:type ont:Layer_Type . ?layer rdfs:label ?label . FILTER NOT EXISTS { ?layer ont:INV.is_client ?x . } }";
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		ArrayList<String> bottomLayers = new ArrayList<String>();
		
		//get result and put on label variable
		while (results.hasNext()) {
			QuerySolution row = results.next();
			String label = row.get("label").toString();
			label = label.replace("@en", "");
			label = label.replace("@EN", "");
			bottomLayers.add(label);
		}
		
		return bottomLayers;
		
	}
	
	
	/**
	 * Procedure to get objects from a specific subject individual of OWL
	 * @param subject
	 * @param predicates
	 * @return
	 */
	public static ArrayList<String> getObjectFromOWL (String subject, ArrayList<String> predicates) {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		String queryString = prefix +
				"SELECT ?object WHERE { ont:" + subject + " ont:" + predicates.get(0);
		
		for(int i = 1; i < predicates.size(); i++) {
			queryString += "/ont:" + predicates.get(i);
		}
		
		queryString += 
					" ?object . " +
					" ?object rdfs:label ?label . " +
				"} ORDER BY ?label ";
				
		System.out.println(queryString);
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		ArrayList<String> objects = new ArrayList<String>();
		
		//get result and put on label variable
		while (results.hasNext()) {
			QuerySolution row = results.next();
		    String object = row.get("object").toString();
		    
		    objects.add(object);
		}
		
		return objects;
		
	}
	
	
	/**
	 * Procedure to get label from a individual
	 * @param model
	 * @param subject
	 * @return
	 */
	public static String getLabelFromOWL (String subject) {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		
		//create query string
		String queryString = prefix + "SELECT ?label WHERE { ont:" + subject + " rdfs:label ?label . }";
		
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		String label = "";
		
		//get result and put on label variable
		while (results.hasNext()) {
			QuerySolution row = results.next();
			label = row.get("label").toString();
			label = label.replace("@en", "");
			label = label.replace("@EN", "");
		}
		
		return label;
		
	}
	
	
	/**
	 * Procedure to get predicate from owl model
	 * @param sourceType
	 * @param targetType
	 * @return
	 * @author Lucas Bassetti
	 */
	public static String getPredicateFromOWL(String sourceType, String targetType) {
		
		OntModel ontModel = ProvisioningComponents.provisioningRepository.getBaseModel();
		String namespace = ProvisioningComponents.provisioningRepository.getNamespace();
		
		String queryString = prefix +
						"SELECT DISTINCT * WHERE { " +
							"?predicate rdfs:domain ?domain . " +
							"?predicate rdfs:range ?range . " +
							"ont:" + sourceType + " rdfs:subClassOf*/owl:intersectionOf*/rdf:rest*/rdf:first*/rdfs:subClassOf* ?domain . " +
							"ont:" + targetType + " rdfs:subClassOf*/owl:intersectionOf*/rdf:rest*/rdf:first*/rdfs:subClassOf* ?range . " +
						 "}";
		
//		System.out.println(queryString);
		
		//execute query string
		Query query = QueryFactory.create(queryString); 
		
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		ResultSet results = qe.execSelect();

		//get predicate from query result
		String predicate = "";
		boolean pred = true;
		
		while (results.hasNext()) {
			QuerySolution row = results.next();
		    
		    RDFNode predicateNode = row.get("?predicate");
		    String domain = row.get("?domain").toString().replace(namespace, "");
		    String range = row.get("?range").toString().replace(namespace, "");
		    
		    if(sourceType.equals(domain) && targetType.equals(range)) {
		    	predicate = predicateNode.toString();
		    	break;
		    }
		    else if(sourceType.equals(domain) || targetType.equals(range)) {
		    	predicate = predicateNode.toString();
		    	pred = false;
		    }
		    else if (pred){
		    	predicate = predicateNode.toString();
		    }
		}
		
//		System.out.println("source: " + sourceType);
//	    System.out.println("target: " + targetType);
//		System.out.println(predicate);
		
		return predicate;
	}
	
	
}
