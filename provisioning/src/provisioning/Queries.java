package provisioning;

import java.util.ArrayList;
import java.util.List;

import br.com.padtec.common.queries.QueryUtil;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Queries {
	public static List<String> getEquipmentMappingPorts(OntModel model, boolean forOutput){
		String portType = "";
		String tfType = "";
		if(forOutput){
			portType = "Output";
			tfType = "TF_Source";
		}else{
			portType = "Input";
			tfType = "TF_Sink";
		}
		
		System.out.println("\nExecuting getEquipmentMappingPorts()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t?equipment rdf:type ns:Equipment .\n"
				+ "\t?equipment ns:componentOf ?interface .\n"
				+ "\t?interface rdf:type ns:" + portType +"_Interface .\n"
				+ "\t?interface ns:maps ?mappedPort .\n"
				+ "\t?mappedPort ns:INV.componentOf ?tf .\n"
				+ "\t?tf rdf:type ns:" + tfType + " .\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode equipment = row.get("equipment");	
		    @SuppressWarnings("unused")
			RDFNode interface_ = row.get("interface");
		    @SuppressWarnings("unused")
			RDFNode mappedPort = row.get("mappedPort");
		    if(QueryUtil.isValidURI(equipment.toString()))
		    {
		    	System.out.println("- Class URI: "+equipment.toString()); 
		    	result.add(equipment.toString()); 
		    }
		}
		return result;
	}
	
	public static boolean isEquipBindedWithPMEquip(OntModel model, String equipURI, boolean isSource){
		System.out.println("\nExecuting isEquipBindedWithPMEquip()...");
		
		String interfaceType = "";
		if(isSource){
			interfaceType = "Output";
		}else{
			interfaceType = "Input";
		}
		
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "ASK\n"
				+ "WHERE {\n"
				+ "<" + equipURI + "> ns:componentOf ?eq1_intfc .\n"
				+ "?eq1_intfc ns:maps ?eq1_tf .\n"
				+ "?eq1_intfc rdf:type ns:" + interfaceType + "_Interface .\n"
				+ "?eq1_tf ns:binds ?pm_port .\n"
				+ "?pm ns:componentOf ?pm_port .\n"
				+ "?pm rdf:type ns:Physical_Media .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		boolean results = qe.execAsk();	
		
		return results;
	}
	
	public static List<String> getAvailableInterfacesFromEquipment(OntModel model, String equipURI, boolean forOutput){
		String portType = "";
		if(forOutput){
			portType = "Output";
		}else{
			portType = "Input";
		}
		
		System.out.println("\nExecuting getEquipmentMappingPorts()...");
		
		List<String> bindedInterfaces = getBindedInterfaces(model);
		List<String> result = new ArrayList<String>();
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t<" + equipURI + "> ns:componentOf ?int1 .\n"
				+ "\t?int1 rdf:type ns:" + portType + "_Interface ."
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode int1 = row.get("int1");	
		   if(QueryUtil.isValidURI(int1.toString()) && !bindedInterfaces.contains(int1.toString()))
		    {
		    	System.out.println("- Class URI: "+int1.toString()); 
		    	result.add(int1.toString()); 
		    }
		}
		
		return result;
	}
	
	public static List<String> getEquipmentWithPhysicalMedia(OntModel model){
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t?equipment rdf:type ns:Equipment .\n"
				+ "\t?equipment ns:componentOf ?pm .\n"
				+ "\t?pm rdf:type ns:Physical_Media .\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode equipment = row.get("equipment");	
		    @SuppressWarnings("unused")
			RDFNode pm = row.get("pm");
		    if(QueryUtil.isValidURI(equipment.toString()))
		    {
		    	System.out.println("- Class URI: "+equipment.toString()); 
		    	result.add(equipment.toString()); 
		    }
		}
		return result;
	}
	
	public static List<String> getMappedPort(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getMappedPort()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t<" + interfaceURI + "> ns:maps ?port .\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode port = row.get("port");	
		    if(QueryUtil.isValidURI(port.toString()))
		    {
		    	System.out.println("- port URI: "+port.toString()); 
		    	result.add(port.toString()); 
		    }
		}
		return result;
	}
	
	public static List<String> getLayersAdaptedFromAF(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ " <" + interfaceURI + "> ns:maps ?port .\n"
				+ "	?tf ns:componentOf ?port .\n"
				+ "	?tf ns:adapts_to ?layer .\n "
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode layer = row.get("layer");	
		    if(QueryUtil.isValidURI(layer.toString()))
		    {
		    	System.out.println("- layer URI: "+layer.toString()); 
		    	result.add(layer.toString()); 
		    }
		}
		return result;
	}
	
	public static List<String> getCompatibleEquipment(OntModel model, String layerURI, boolean isSource, boolean isTF){
		String intType = "";
		String relName = "";
		String from = "";
		if(isTF){
			relName = "adapts_from";
			from += "AF_";
		}else{
			relName = "defines";
			from += "TF_";
		}
		if(isSource){
			intType = "Input";
			from += "Source";
		}else{
			intType = "Output";
			from += "Sink";
		}
		
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = "";
		if(QueryUtil.hasDataPropWithValue(model, layerURI, model.getNsPrefixURI("")+"Layer_Network.isLast", "\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>")){
			queryString = ""
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
					+ "PREFIX ns: <http://nemo.inf.ufes.br/NewProject.owl#>\n"
					+ "SELECT DISTINCT *\n"
					+ "WHERE {\n"
					+ "	?eq ns:componentOf ?int .\n"
					+ "	?int rdf:type ns:" + intType + "_Interface .\n"
					+ "	?int ns:maps ?port .\n"
					+ "	?pm ns:componentOf ?port .\n"
					+ "	?pm rdf:type ns:Physical_Media .\n"
					+ "}";
		}else{
			queryString = ""
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
					+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
					+ "SELECT DISTINCT *\n"
					+ "WHERE {\n"
					+ "	?eq ns:componentOf ?int .\n"
					+ " ?int rdf:type ns:" + intType + "_Interface . \n"
					+ "	?int ns:maps ?port .\n"
					+ "	?tf ns:componentOf ?port .\n"
					+ "	?tf ns:" + relName + " <" + layerURI + "> . \n"
					+ " ?tf rdf:type ns:" + from + " . \n"
					+ "}";
		}
		
		List<String> bindedInterfaces = getBindedInterfaces(model);
		
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode eq = row.get("eq");	
		    RDFNode intfc = row.get("int");
		    if(QueryUtil.isValidURI(eq.toString()) && !bindedInterfaces.contains(intfc.toString()))
		    //if(QueryUtil.isValidURI(eq.toString()))
		    {
		    	System.out.println("- layer URI: "+eq.toString()); 
		    	if(!result.contains(eq.toString())){
		    		result.add(eq.toString());
		    	}		    	 
		    }
		}
		
		return result;
	}
	
	public static List<String> getBindedInterfaces(OntModel model){
		System.out.println("\nExecuting getBindedInterfaces()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "?int1 ns:maps ?port1 .\n"
				+ "	?int2 ns:maps ?port2 .\n"
				+ "	?port1 ns:binds ?port2 .\n" 
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode int1 = row.get("int1");	
		    if(QueryUtil.isValidURI(int1.toString()))
		    {
		    	System.out.println("- int1 URI: "+int1.toString()); 
		    	result.add(int1.toString()); 
		    }
		    RDFNode int2 = row.get("int2");	
		    if(QueryUtil.isValidURI(int2.toString()))
		    {
		    	System.out.println("- int2 URI: "+int2.toString()); 
		    	result.add(int2.toString()); 
		    }
		}
		return result;
	}
	
	public static List<String> getServerLayersFromTF(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "<" + interfaceURI + "> ns:maps ?port .\n"
				+ "	?tf ns:componentOf ?port .\n"
				+ "	?tf ns:defines ?serverLayer . \n" 
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode serverLayer = row.get("serverLayer");	
		    if(QueryUtil.isValidURI(serverLayer.toString()))
		    {
		    	System.out.println("- serverLayer URI: "+serverLayer.toString()); 
		    	result.add(serverLayer.toString()); 
		    }
		}
		return result;
	}
	
	public static boolean isInterfaceMappedByTF(OntModel model, String interfaceURI){
		System.out.println("\nExecuting isInterfaceMappedByTF()...");
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "ASK\n"
				+ "WHERE {\n"
				+ "	<" + interfaceURI + "> ns:maps ?port .\n"
				+ "	?tf ns:componentOf ?port .\n"
				+ "	?tf rdf:type ns:Termination_Function . \n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		boolean results = qe.execAsk();		
		
		return results;
	}
	
	public static List<String> getLastBindedEquipmentFrom(OntModel model, String fromEquipURI){
		System.out.println("\nExecuting getBindedEquipmentFrom()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t{\n"
				+ "\t\t<" + fromEquipURI + "> ns:eq_binds*/ns:eq_binds ?equip2 .\n"
				+ "\t}\n"
				+ "\tUNION\n"
				+ "\t{\n"
				+ "\t\t?equip2 ns:eq_binds*/ns:eq_binds <" + fromEquipURI + "> .\n"
				+ "\t}\n"
				+ "}\n";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
			RDFNode equip2 = row.get("equip2");	
			if(QueryUtil.isValidURI(equip2.toString()))
		    {
		    	System.out.println("- Equipment URI: "+equip2.toString()); 
		    	result.add(equip2.toString()); 
		    }
		}
		return result;
	}
	
	
}
