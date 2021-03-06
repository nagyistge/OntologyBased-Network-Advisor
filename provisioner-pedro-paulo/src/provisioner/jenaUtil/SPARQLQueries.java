package provisioner.jenaUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import provisioner.domain.IntBinds;
import provisioner.domain.Interface;
import br.com.padtec.common.queries.QueryUtil;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQLQueries {
	public static List<Interface> getInterfacesAndEquipMappingPorts(OntModel model, boolean forOutputInterface, boolean forSourceComponent, HashMap<String, Interface> interfaces){
		String interfaceType = "";
		String componentType = "";
		if(forSourceComponent){
			componentType = "Source";
		}else{
			componentType = "Sink";
		}
		if(forOutputInterface){
			interfaceType = "Output";
		}else{
			interfaceType = "Input";
		}
		
		System.out.println("\nExecuting getEquipmentMappingPorts()...");
		List<Interface> result = new ArrayList<Interface>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t?equipment rdf:type ns:Equipment .\n"
				+ "\t?equipment ns:componentOf ?interface .\n"
				+ "\t?interface rdf:type ns:" + interfaceType +"_Interface .\n"
				+ "\t?interface ns:maps ?mappedPort .\n"
				+ "\t?tf ns:componentOf ?mappedPort .\n"
				+ "\t?tf rdf:type ?tfType .\n"
				+ "\tFILTER( ?tfType IN (ns:AF_" + componentType + ", ns:TF_" + componentType + ", ns:Matrix_" + componentType + ")).\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode equipment = row.get("equipment");	
		    RDFNode interface_ = row.get("interface");
		    @SuppressWarnings("unused")
			RDFNode mappedPort = row.get("mappedPort");
		    if(QueryUtil.isValidURI(equipment.toString()) && QueryUtil.isValidURI(interface_.toString()))
		    {
		    	System.out.println("- equipment URI: "+equipment.toString()); 
		    	System.out.println("- interface_ URI: "+interface_.toString()); 
		    	Interface newInt = interfaces.get(interface_.toString());
		    	//Interface newInt = new Interface(interface_.toString(), equipment.toString());
//		    	result.add(interface_.toString());
//		    	result.add(equipment.toString());		    	 
		    	result.add(newInt);
		    }
		}
		return result;
	}
	
	public static String EquipWithPMofInterface(OntModel model, String interfaceURI){
		System.out.println("\nExecuting isEquipBindedWithPMEquip()...");
		
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ " ?equip ns:componentOf <" + interfaceURI + "> .\n"
				+ "	?equip ns:componentOf ?pm .\n"
				+ "	?pm rdf:type ns:Physical_Media .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode equip = row.get("equip");	
		    if(QueryUtil.isValidURI(equip.toString()))
		    {
		    	System.out.println("- equip URI: "+equip.toString()); 
		    	//result.add(equip.toString()); 
		    	return equip.toString();
		    }
		}
		
		return "";
	}
	
	public static List<String> getInterfacesFromTopLayer(OntModel model, String interfaceTypeURI, String srcOrSink) throws Exception{
		System.out.println("\nExecuting getInterfacesFromLayer()...");
		
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n"
				+ "?serverLayer ns:client_of ?clientLayer . \n"
				+ "}";
		
		List<String> clientLayers = new ArrayList<String>();	
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode clientLayer = row.get("clientLayer");	
		    if(QueryUtil.isValidURI(clientLayer.toString()))
		    {
		    	System.out.println("- clientLayer URI: "+clientLayer.toString()); 
		    	clientLayers.add(clientLayer.toString());		    	
		    }
		}
		
		List<String> allLayers = QueryUtil.getIndividualsURI(model, model.getNsPrefixURI("")+"Layer_Network");
		allLayers.removeAll(clientLayers);
		
		if(allLayers.size() != 1){
			throw new Exception("Something went wrong. More than one top layer were found.");
		}
		String topLayer = allLayers.get(0);
		
		queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n"
				+ "?intfc rdf:type <" + interfaceTypeURI + "> .\n"
				+ "?intfc ns:maps ?tfPort .\n"
				+ "?tf ns:componentOf ?tfPort .\n"
				+ "?tf rdf:type ?tfType .\n"
				+ "FILTER ( ?tfType  IN (ns:AF_"+srcOrSink+", ns:TF_"+srcOrSink+", ns:Matrix_"+srcOrSink+", ns:Physical_Media) ) .\n"
				+ "?tf ?tfRel <" + topLayer + "> .\n"
				+ "FILTER ( ?tfRel IN (ns:defines, ns:adapts_to, ns:hasLayer) ) .\n"
				+ "}";
		
		List<String> result = new ArrayList<String>();	
		query = QueryFactory.create(queryString); 		
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode intfc = row.get("intfc");	
		    if(QueryUtil.isValidURI(intfc.toString()))
		    {
		    	System.out.println("- intfc URI: "+intfc.toString()); 
		    	result.add(intfc.toString());		    	
		    }
		}
		
		return result;
	}
	
	public static String equipBindingEquipWithPM(OntModel model, String interfaceFromURI){
		System.out.println("\nExecuting isEquipBindedWithPMEquip()...");
		
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "?equip1 ns:componentOf <" + interfaceFromURI + "> .\n"
				+ "?equip1 ns:componentOf ?int2 .\n"
				+ "?int2 ns:maps ?tf_port .\n"
				+ "?tf_port ns:binds ?pm_port .\n"
				+ "?pm ns:componentOf ?pm_port .\n"
				+ "?pm rdf:type ns:Physical_Media .\n"
				+ "?equip2 ns:componentOf ?pm .\n"
				+ "?equip2 rdf:type ns:Equipment .\n"
				+ "}";
		
		
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode equip2 = row.get("equip2");	
		    if(QueryUtil.isValidURI(equip2.toString()))
		    {
		    	System.out.println("- equip URI: "+equip2.toString()); 
		    	//result.add(equip.toString()); 
		    	return equip2.toString();
		    }
		}
		
		return "";
	}
	
	public static List<String> getEquipmentWithPhysicalMedia(OntModel model){
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
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
	
	//public static List<String> getMappingInterfaceFrom(OntModel model, String tfURI, boolean isSource){
	public static List<Interface> getMappingInterfaceFrom(OntModel model, String tfURI, HashMap<String, Interface> interfaces, HashMap<String, Interface> bindedInterfaces){
		String tf_type = "";
		//if(isSource){
			tf_type  = "Output";
		//}else{
		//	tf_type = "Input";
		//}
		System.out.println("\nExecuting getMappedPort()...");
		List<Interface> result = new ArrayList<Interface>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ " <" + tfURI + "> ns:componentOf ?port .\n"
				+ "	?interface ns:maps ?port .\n"
				+ "	?interface rdf:type ns:" + tf_type + "_Interface.\n"
				+ "?equip ns:componentOf ?interface .\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		
		//List<String> bindedInterfaces = Queries.getBindedInterfaces(model);
		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
			RDFNode interface_ = row.get("interface");	
			RDFNode equip = row.get("equip");	
			
			Interface newInt = interfaces.get(interface_.toString());
			//Interface newInt = new Interface(interface_.toString(), equip.toString());
	    	
		    if(QueryUtil.isValidURI(interface_.toString()) && QueryUtil.isValidURI(equip.toString()) && !bindedInterfaces.containsKey(newInt.getInterfaceURI()))
		    {
		    	System.out.println("- interface URI: "+interface_.toString()); 
//		    	result.add(interface_.toString());
		    	System.out.println("- equip URI: "+equip.toString()); 
//		    	result.add(equip.toString());
		    	result.add(newInt);
		    }
		}
		return result;
	}
	
	public static String getMappedPort(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getMappedPort()...");
		String queryString = ""
				+ QueryUtil.PREFIXES
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
		    	return port.toString(); 
		    }
		}
		return "";
	}
	
	public static String getMappedTFFrom(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getMappedTF()...");
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT *\n"
				+ "WHERE {\n"
				+ "\t<" + interfaceURI + "> ns:maps ?port .\n"
				+ "\t?tf ns:componentOf ?port .\n"
				+ "?tf rdf:type ns:Transport_Function .\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode tf = row.get("tf");	
		    if(QueryUtil.isValidURI(tf.toString()))
		    {
		    	System.out.println("- tf URI: "+tf.toString()); 
		    	return tf.toString(); 
		    }
		}
		return "";
	}
	
	public static List<String> getLayersAdaptedFromAF(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
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
	
	public static List<Interface> getInterfacesToProvision(OntModel model, String interfaceFromURI, boolean isSource, HashMap<String, Interface> interfaces, HashMap<String, Interface> bindedInterfaces) throws Exception{
		System.out.println("\nExecuting getInterfacesToProvision()...");
		
		String intType = "";
		String relName1 = "";
		String relName2 = "";
		String tgtTFtype = "";
		String srcOrSk = "";
		
		List<String> tfTypes = getTfTypesMappedByInterface(model, interfaceFromURI);
		
		intType = "Input";
		if(isSource){
			//intType = "Input";
			srcOrSk = "Source";
		}else{
			//intType = "Output";
			srcOrSk = "Sink";
		}
		String ns = model.getNsPrefixURI("");
		if(tfTypes.contains(ns+"Termination_Function")){
			relName1 = "ns:adapts_from, ns:hasLayer";
			relName2 = "defines";
			tgtTFtype = "ns:AF_" + srcOrSk;
			if(isSource){
				tgtTFtype += ", ns:Matrix_" + srcOrSk;
			}			
		}else if(tfTypes.contains(ns+"Adaptation_Function")){
			relName1 = "ns:defines";
			relName2 = "adapts_from";
			tgtTFtype += "ns:TF_" + srcOrSk;
			if(!isSource){
				relName1 += ", ns:adapts_from, ns:hasLayer";
				tgtTFtype += ", ns:AF_Source" + ", ns:Matrix_" + srcOrSk;
			}
		}else if(tfTypes.contains(ns+"Matrix")){
			if(isSource){
				relName1 = "ns:adapts_from";
				tgtTFtype += "ns:AF_" + srcOrSk;				
			}else{
				relName1 = "ns:defines";
				tgtTFtype += "ns:TF_" + srcOrSk;
			}
			relName2 = "hasLayer";
		}else if(tfTypes.contains(ns+"Physical_Media")){
			relName1 = "ns:adapts_from";
			relName2 = "hasLayer";
			tgtTFtype += "ns:TF_" + srcOrSk;
		}else{
			throw new Exception("Something went wrong. The interface is does not mapping neither Termination_Function, Adaptation_Function, Matrix, and Physical_Media");
		}
		
		List<Interface> result = new ArrayList<Interface>();				
		String queryString = "";
		boolean isInterfaceInTheLastLayer = isInterfaceInTheLastLayer(model, interfaceFromURI);
		if(isInterfaceInTheLastLayer){
			queryString = ""
					+ QueryUtil.PREFIXES
					+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
					+ "SELECT DISTINCT *\n"
					+ "WHERE {\n"
					+ "	?equipTo ns:componentOf ?intTo .\n"
					+ "	?intTo rdf:type ns:" + intType + "_Interface .\n"
					+ "	?intTo ns:maps ?port .\n"
					+ "	?pm ns:componentOf ?port .\n"
					+ "	?pm rdf:type ns:Physical_Media .\n"
					+ "}";
		}else if(tfTypes.contains(ns+"Physical_Media")){
			queryString = ""
					+ QueryUtil.PREFIXES
					+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
					+ "SELECT DISTINCT *\n"
					+ "WHERE {\n"
					+ "?intTo ns:maps ?portTo .\n"
					+ "?intTo rdf:type ?intToType .\n"
					+ "?equipTo ns:componentOf ?intTo .\n"
					+ "?equipTo rdf:type ns:Equipment . \n"
					+ "FILTER(?intToType IN (ns:" + intType + "_Interface) ) .\n"
					+ "?tfTo ns:componentOf ?portTo .\n"
					+ "?tfTo ns:defines ?layer .\n"
					+ "?layer ns:Layer_Network.isLast \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> .\n"
					+ "}";
		}else{
			queryString = ""
					+ QueryUtil.PREFIXES
					+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
					+ "SELECT DISTINCT *\n"
					+ "WHERE {\n"
					+ "	<" + interfaceFromURI + "> ns:maps ?portFrom .\n"
					+ "	?tfFrom ns:componentOf ?portFrom .\n"
					+ "	?tfFrom ns:" + relName2 + " ?layer .\n"
					+ "	?tfTo ?tfToRel ?layer .\n"
					+ " ?tfTo ns:componentOf ?portTo .\n"
					+ "	?tfTo rdf:type ?tfToType .\n"
					+ "	?intTo ns:maps ?portTo .\n"
					+ " ?equipTo ns:componentOf ?intTo . 	\n"
					+ " ?intTo rdf:type ns:Input_Interface .\n"
					+ "	FILTER( ?tfToRel IN (" + relName1 + ") ) .\n"
					+ "	FILTER ( ?tfToType IN (" + tgtTFtype + ") )\n"
					+ "}";
		}
		
		//List<String> bindedInterfaces = getBindedInterfaces(model);
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode equipTo = row.get("equipTo");	
		    RDFNode intTo = row.get("intTo");
		    Interface newInt = interfaces.get(intTo.toString());
		    //Interface newInt = new Interface(intTo.toString(), equipTo.toString());
		    if(QueryUtil.isValidURI(equipTo.toString()) && QueryUtil.isValidURI(intTo.toString()) && !bindedInterfaces.containsKey(newInt.getInterfaceURI()))
		    {
	    		System.out.println("- intTo URI: "+intTo.toString());
    			
//			    	result.add(intTo.toString());
		    	System.out.println("- equipTo URI: "+equipTo.toString()); 
//			    	result.add(equipTo.toString());
		    	result.add(newInt);		    				    			    			
		    }
		}
		
		return result;
	}
	
	public static List<Interface> getBindedInterfaces(OntModel model, HashMap<String, Interface> interfaces){
		System.out.println("\nExecuting getBindedInterfaces()...");
		List<Interface> result = new ArrayList<Interface>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
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
		    if(QueryUtil.isValidURI(int1.toString()) && !result.contains(int1.toString()))
		    {
		    	System.out.println("- int1 URI: "+int1.toString()); 
		    	Interface newInt = interfaces.get(int1.toString());
		    	//Interface newInt = new Interface(int1.toString());
		    	result.add(newInt);
//		    	result.add(int1.toString()); 
		    }
		    RDFNode int2 = row.get("int2");	
		    if(QueryUtil.isValidURI(int2.toString()) && !result.contains(int2.toString()))
		    {
		    	System.out.println("- int2 URI: "+int2.toString());
		    	Interface newInt = interfaces.get(int2.toString());
		    	//Interface newInt = new Interface(int2.toString());
		    	result.add(newInt);
//		    	result.add(int2.toString()); 
		    }
		}
		return result;
	}
	
	public static List<IntBinds> getIntBinds(OntModel model, HashMap<String, Interface> interfaces){
		System.out.println("\nExecuting getBindedInterfaces()...");
		List<IntBinds> result = new ArrayList<IntBinds>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n"
//				+ "	{\n"
				+ "		?int1 ns:maps ?port1 .\n"
				+ "		?int2 ns:maps ?port2 .\n"
				+ "		?port1 ns:binds ?port2 .\n"
//				+ "	}\n"
//				+ "	UNION\n"
//				+ "	{\n"
//				+ "		?int1 ns:maps ?port1 .\n"
//				+ "		?int2 ns:maps ?port2 .\n"
//				+ "		?port1 ns:INV.binds ?port2 .\n"
//				+ "	}\n" 
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode int1 = row.get("int1");	
		    RDFNode int2 = row.get("int2");	
		    if(QueryUtil.isValidURI(int1.toString()) && QueryUtil.isValidURI(int2.toString()))
		    {
		    	System.out.println("- int1 URI: "+int1.toString()); 
		    	System.out.println("- int2 URI: "+int2.toString());
		    	Interface interfaceFrom = interfaces.get(int1.toString());
		    	Interface interfaceTo = interfaces.get(int2.toString());
		    	IntBinds newIntBinds = new IntBinds(interfaceFrom, interfaceTo);
		    	result.add(newIntBinds); 
		    }
		}
		return result;
	}
	
	public static List<Interface> getIntPaths(OntModel model, HashMap<String, Interface> interfaces){
		System.out.println("\nExecuting getBindedInterfaces()...");
		List<Interface> result = new ArrayList<Interface>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n"
				+ "?int1 ns:path ?int2 .\n" 
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode int1 = row.get("int1");	
		    RDFNode int2 = row.get("int2");	
		    if(QueryUtil.isValidURI(int1.toString()) && QueryUtil.isValidURI(int2.toString()))
		    {
		    	System.out.println("- int1 URI: "+int1.toString()); 
		    	System.out.println("- int2 URI: "+int2.toString());
		    	Interface interfaceFrom = interfaces.get(int1.toString());
		    	interfaceFrom.setAlreadyProvisioned(true);
		    	Interface interfaceTo = interfaces.get(int2.toString());
		    	interfaceTo.setAlreadyProvisioned(true);
		    	if(!result.contains(interfaceFrom)) result.add(interfaceFrom);
		    	if(!result.contains(interfaceTo)) result.add(interfaceTo);
		    }
		}
		return result;
	}
	
	public static List<IntBinds> getInternalIntBinds(OntModel model, HashMap<String, Interface> interfaces){
		System.out.println("\nExecuting getBindedInterfaces()...");
		List<IntBinds> result = new ArrayList<IntBinds>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT ?int1 ?int2 \n"
				+ "WHERE {\n"
//				+ "	{\n"
//				+ "		?int1 ns:maps ?port1 .\n"
//				+ "		?tf1 ns:componentOf ?port1 .\n"
//				+ "		?tf1 ns:tf_binds+ ?tf2 .\n"
//				+ "		?tf2 ns:componentOf ?port2 .\n"
//				+ "		?int2 ns:maps ?port2 .\n"
//				+ "		?equipment ns:componentOf ?int1 .\n"
//				+ "		?equipment ns:componentOf ?int2 .\n "
//				+ "	}\n"
//				+ "	UNION\n"
//				+ "	{\n"
				+ "		?int1 ns:maps ?port1 .\n"
				+ "		?tf1 ns:componentOf ?port1 .\n"
				+ "		?tf2 ns:componentOf ?port2 .\n"
				+ "		?int2 ns:maps ?port2 .\n"
				+ "		?equipment ns:componentOf ?int1 .\n"
				+ "		?equipment ns:componentOf ?int2 .\n"
				+ "		?int1 rdf:type ns:Input_Interface .\n"
				+ "		?int2 rdf:type ns:Output_Interface . \n"
//				+ "	}\n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
		    RDFNode int1 = row.get("int1");	
		    RDFNode int2 = row.get("int2");	
		    if(QueryUtil.isValidURI(int1.toString()) && QueryUtil.isValidURI(int2.toString()))
		    {
		    	System.out.println("- int1 URI: "+int1.toString()); 
		    	System.out.println("- int2 URI: "+int2.toString());
		    	Interface interfaceFrom = interfaces.get(int1.toString());
		    	Interface interfaceTo = interfaces.get(int2.toString());
		    	IntBinds newIntBinds = new IntBinds(interfaceFrom, interfaceTo);
		    	result.add(newIntBinds); 
		    }
		}
		return result;
	}
	
	public static List<String> getServerLayersFromTF(OntModel model, String interfaceURI){
		System.out.println("\nExecuting getEquipmentWithPhysicalMedia()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
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
	
	public static List<String> getTfTypesMappedByInterface(OntModel model, String interfaceURI){
		System.out.println("\nExecuting isInterfaceMappedByTF()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n"
				+ "	<" + interfaceURI + "> ns:maps ?port .\n"
				+ "	?tf ns:componentOf ?port .\n"
				+ "	?tf rdf:type ?tfType . \n"
				+ "}";
		
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
			RDFNode tfType = row.get("tfType");	
			if(QueryUtil.isValidURI(tfType.toString()))
		    {
		    	System.out.println("- tfType URI: "+tfType.toString()); 
		    	result.add(tfType.toString()); 
		    }
		}
		return result;
	}
	
	public static boolean isInterfaceInTheLastLayer(OntModel model, String interfaceURI){
		System.out.println("\nExecuting isInterfaceInTheLastLayer()...");
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "ASK\n"
				+ "WHERE {\n"
				+ "	<" + interfaceURI + "> ns:maps ?port .\n"
				+ "	?tf ns:componentOf ?port .\n"
				+ "	?tf ns:defines ?layer . \n"
				+ "	?layer ns:Layer_Network.isLast \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . \n"
				+ "}";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		boolean results = qe.execAsk();		
		
		return results;
	}
	
	/** 
	 * Return true if an interface is mapping a port from a Termination Function Source
	 * 
	 * @param model: jena.ontology.InfModel 
	 * 
	 * @author Freddy Brasileiro
	 */
	static public boolean isInterfaceSource(InfModel model, String interfaceUri) 
	{		
		System.out.println("\nExecuting isInterfaceSource()...");
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "ASK\n"
				+ "WHERE {\n"
				+ "<" + interfaceUri + "> ns:maps ?port .\n"
				+ "?tf ns:componentOf ?port .\n"
				+ "?tf rdf:type ?tfType .\n"
				+ "FILTER( ?tfType IN (ns:AF_Source, ns:TF_Source, ns:Matrix_Source) ) .\n"
				+ "}\n";
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		boolean exist = qe.execAsk();		
		
		if(exist){
			return exist;
		}
		
		queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "ASK\n"
				+ "WHERE {\n"
				+ "<" + interfaceUri + "> ns:maps ?port .\n"
				+ "?tf ns:componentOf ?port .\n"
				+ "?tf rdf:type ?tfType .\n"
				+ "?port rdf:type ?portType .\n"
				+ "FILTER( ?tfType IN (ns:Physical_Media) ) .\n"
				+ "FILTER( ?portType IN (ns:Input)) . \n"
				+ "}\n";
		query = QueryFactory.create(queryString);
		
		qe = QueryExecutionFactory.create(query, model);
		exist = qe.execAsk();
		
		return exist;
	}
	
	public static List<String> getLastBindedTFFrom(OntModel model, String tfURI, boolean isSource){
		System.out.println("\nExecuting getLastBindedTFFrom()...");
		List<String> result = new ArrayList<String>();				
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n";
				
		if(isSource){
			queryString += ""
					+ "	{\n"
					+ "		<" + tfURI + "> ns:tf_binds*/ns:tf_binds ?tf2 .\n"
//					+ "		<" + tfURI + "> (ns:componentOf/ns:binds/^ns:componentOf)* ?tf2 .\n"
//					+ "		?tf2 rdf:type ?tf2_type .\n"
//					+ "		FILTER (?tf2_type IN (ns:AF_Source, ns:TF_Source) ) .\n"
					+ "	}\n"
					+ "	UNION\n"
					+ "	{\n"
//					+ "		<" + tfURI + "> (ns:componentOf/ns:binds/^ns:componentOf)* ?tf2 .\n"
//					+ "		?tf2 rdf:type ns:Physical_Media .\n"
//					+ "		?tf2 ns:componentOf ?port2 .\n"
//					+ "		?port2 ^ns:binds ?port3 .\n"
//					+ "		?port3 ^ns:componentOf ?tf3 .\n"
//					+ "		?tf3 rdf:type ns:TF_Sink .\n"
//					+ "		?tf3 (ns:componentOf/^ns:binds/^ns:componentOf)* ?tf4 .\n"
//					+ "		FILTER ( <" + tfURI + "> NOT IN (?tf2, ?tf3, ?tf4) ) .\n"			
					+ "		<" + tfURI + "> ns:tf_binds*/ns:tf_binds ?tf2 .\n"
					+ "		?tf2 rdf:type ns:Physical_Media . \n"
					+ "		?tf2 ns:INV.tf_binds ?tf3 .\n"
					+ "		?tf3 rdf:type ns:TF_Sink .\n"
					+ "		?tf3 ns:INV.tf_binds*/ns:INV.tf_binds ?tf4 .\n"
					+ "	}\n";					
		}else{
			queryString += ""
					+ "	{\n"
//					+ "		?tf2 (ns:componentOf/ns:binds/^ns:componentOf)* <" + tfURI + "> .\n"
//					+ "		?tf2 rdf:type ns:AF_Sink .\n"
//					+ "		?tf2 ns:componentOf ?port2 .\n"
//					+ "		?port2 ns:binds ?port3 .\n"
//					+ "		?tf3 ns:componentOf ?port3 .\n"
//					+ "		?tf3 rdf:type ns:AF_Source .\n"
//					+ "		?tf3 (ns:componentOf/ns:binds/^ns:componentOf)* ?tf4\n"
//					+ "		FILTER (?tf4 NOT IN (<" + tfURI + ">) ) ."
					+ "		<" + tfURI + "> ns:INV.tf_binds*/ns:INV.tf_binds ?tf2 .\n"
					+ "		?tf2 rdf:type ns:AF_Sink . \n"
					+ "		?tf2 ns:tf_binds ?tf3 .\n"
					+ "		?tf3 rdf:type ns:AF_Source . \n"
					+ "		?tf3 ns:tf_binds*/ns:tf_binds ?tf4 .\n"
					+ "	}\n"
					+ "	UNION\n"
					+ "	{\n"
					+ "		<" + tfURI + "> ns:INV.tf_binds*/ns:INV.tf_binds ?tf2 .\n"
//					+ "		?tf2 (ns:componentOf/ns:binds/^ns:componentOf)* <" + tfURI + "> .\n"
//					+ "		?tf2 rdf:type ?tf2_type .\n"
//					+ "		FILTER (?tf2_type IN (ns:AF_Sink, ns:TF_Sink) ) .\n"
					+ "	}\n";					
		}
		queryString += "}\n";
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		
		List<String> quebraDeAsa = new ArrayList<String>();
		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
			RDFNode tf2 = row.get("tf2");
			RDFNode tf3 = row.get("tf3");
			RDFNode tf4 = row.get("tf4");
			if(QueryUtil.isValidURI(tf2.toString()))
		    {
		    	if(!result.contains(tf2.toString())){
		    		System.out.println("- tf2 URI: "+tf2.toString()); 
			    	result.add(tf2.toString());
		    	}		    	 
		    }
			if(tf3 != null){
				if(QueryUtil.isValidURI(tf3.toString()))
			    {
			    	if(!result.contains(tf3.toString())){
			    		System.out.println("- tf3 URI: "+tf3.toString()); 
				    	result.add(tf3.toString());
				    	if(!quebraDeAsa.contains(tf3.toString())) quebraDeAsa.add(tf3.toString());
			    	}		    	 
			    }
			}
			if(tf4 != null){
				if(QueryUtil.isValidURI(tf4.toString()))
			    {
			    	if(!result.contains(tf4.toString())){
			    		System.out.println("- tf4 URI: "+tf4.toString()); 
				    	result.add(tf4.toString());
				    	if(!quebraDeAsa.contains(tf4.toString())) quebraDeAsa.add(tf4.toString());
			    	}		    	 
			    }
			}
		}
		
		for (String tf : quebraDeAsa) {
			List<String> newResult = getLastBindedTFFrom(model, tf, !isSource);
			for (String newTf : newResult) {
				if(!result.contains(newTf)) result.add(newTf);
			}
		}
		
		return result;
	}
	
	public static List<Interface> getInterfacesMappingMatrixes(OntModel model, boolean isSource, HashMap<String, Interface> interfaces){
		System.out.println("\nExecuting getLastBindedTFFrom()...");
		List<Interface> result = new ArrayList<Interface>();				
		
		String portType;
		if(isSource){
			portType = "Output";					
		}else{
			portType = "Input";					
		}
		String queryString = ""
				+ QueryUtil.PREFIXES
				+ "PREFIX ns: <" + model.getNsPrefixURI("") + ">\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE {\n"
				+ "	?interface1 ns:maps ?port . \n"
				+ "	?port rdf:type ns:Matrix .\n"
				+ "	?interface1 ns:path ?interface2 .\n"
				+ "	?interface1 rdf:type ns:" + portType + "_Interface . \n"
				+ "}";
				
		Query query = QueryFactory.create(queryString); 		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();		
		
		while (results.hasNext()) 
		{			
			QuerySolution row = results.next();
			RDFNode interface1 = row.get("interface1");
			if(QueryUtil.isValidURI(interface1.toString()))
		    {
		    	if(!result.contains(interface1.toString())){
		    		System.out.println("- interface1 URI: "+interface1.toString());
		    		Interface newInt = interfaces.get(interface1.toString());
			    	result.add(newInt);
		    	}		    	 
		    }			
		}
		
		return result;
	}
}
