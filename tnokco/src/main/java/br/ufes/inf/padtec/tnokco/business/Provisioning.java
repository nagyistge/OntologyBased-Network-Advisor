package br.ufes.inf.padtec.tnokco.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mindswap.pellet.exceptions.InconsistentOntologyException;

import br.com.padtec.common.appication.UploadApp;
import br.com.padtec.common.dto.DtoInstanceRelation;
import br.com.padtec.common.exceptions.OKCoExceptionInstanceFormat;
import br.com.padtec.common.queries.QueryUtil;
import br.ufes.inf.nemo.padtec.processors.BindsProcessor;
import br.ufes.inf.padtec.tnokco.controller.HomeController;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Statement;


public class Provisioning {


	//public static HashMap<HashMap<String, String>, HashMap<String,String>> values = new HashMap<HashMap<String,String>, HashMap<String,String>>();
	HashMap<String, HashMap<String, String>> equipmentsReleations = new HashMap<String, HashMap<String,String>>();
	HashMap<String, String> equipmentRP = new HashMap<String, String>();
	HashMap<String, String> equipmentOut = new HashMap<String, String>();
	private static ArrayList<Equipment> equipmentsList= new ArrayList<Equipment>();
	public static OntModel Model= HomeController.Model;
	public static InfModel InfModel = HomeController.InfModel;
	static List<String> equipments = QueryUtil.getIndividualsURI(HomeController.InfModel, HomeController.NS+"Equipment");
	public static ArrayList<String[]> connections; 
	public static ArrayList<String[]> binds; 
	public static String relation= "site_connects";
	public static HashMap<String, List<String>> ind_class= new HashMap<String, List<String>>();
	public static ArrayList<String[]> triples_g800 = new ArrayList<String[]>();

	static Provisioning instance = new Provisioning();


	static public Provisioning getInstance(){

		return instance;
	}

	public static void main(String[] args) {
		Provisioning rp= new Provisioning();
	}


	public Provisioning(){
		BindsProcessor.initValues();

		//		HashMap<String, String> tf1= new HashMap<String, String>();
		//		HashMap<String, String> hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Source_Input");
		//		tf1.put("OUTPUT", "Termination_Source_Output");
		//		hashrp.put("RP", "Source_A-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_A-FEP");
		//		hashrp.put("RP_BINDING", "Source_A-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_A-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_A-FEP_from");		
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Matrix_Input");
		//		tf1.put("OUTPUT", "Termination_Source_Output");
		//		hashrp.put("RP", "Source_M-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_M-FEP");
		//		hashrp.put("RP_BINDING", "Source-M-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_M-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_M-FEP-from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Subnetwork_Input");
		//		tf1.put("OUTPUT", "Termination_Source_Output");
		//		hashrp.put("RP", "Source_SN-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_SN-FEP");
		//		hashrp.put("RP_BINDING", "Source-SN-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_SN-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_SN-FEP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Layer_Processor_Source_Input");
		//		tf1.put("OUTPUT", "Termination_Source_Output");
		//		hashrp.put("RP", "Source_LPF-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_LPF-FEP");
		//		hashrp.put("RP_BINDING", "Source_LPF-FEP Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_LPF-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_LPF-FEP-from");	
		//		values.put(tf1, hashrp);
		//
		//
		//		tf1.put("INPUT", "Adaptation_Sink_Input");
		//		tf1.put("OUTPUT", "Termination_Sink_Output");
		//		hashrp.put("RP", "Sink_A-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_A-FEP");
		//		hashrp.put("RP_BINDING", "Sink_A-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_A-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_A-FEP_from");		
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Matrix_Input");
		//		tf1.put("OUTPUT", "Termination_Sink_Output");
		//		hashrp.put("RP", "Sink_M-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_M-FEP");
		//		hashrp.put("RP_BINDING", "Sink_M-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_M-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_M-FEP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Subnetwork_Input");
		//		tf1.put("OUTPUT", "Termination_Sink_Output");
		//		hashrp.put("RP", "Sink_SN-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_SN-FEP");
		//		hashrp.put("RP_BINDING", "Sink_SN-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_SN-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_SN-FEP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Layer_Processor_Sink_Input");
		//		tf1.put("OUTPUT", "Termination_Sink_Output");
		//		hashrp.put("RP", "Sink_LPF-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_LPF-FEP");
		//		hashrp.put("RP_BINDING", "Sink_LPF-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_LPF-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_LPF-FEP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Physical_Media_Input");
		//		tf1.put("OUTPUT", "Termination_Source_Output");
		//		hashrp.put("RP", "Source_PM-FEP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_PM-FEP");
		//		hashrp.put("RP_BINDING", "Sink_PM-FEP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_PM-FEP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_PM-FEP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Termination_Source_Input");
		//		tf1.put("OUTPUT", "Adaptation_Source_Output");
		//		hashrp.put("RP", "Source_AP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_AP");
		//		hashrp.put("RP_BINDING", "Source_AP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_AP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_AP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Termination_Sink_Input");
		//		tf1.put("OUTPUT", "Adaptation_Sink_Output");
		//		hashrp.put("RP", "Sink_AP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_AP");
		//		hashrp.put("RP_BINDING", "Sink_AP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_AP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_AP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Source_Input");
		//		tf1.put("OUTPUT", "Layer_Processor_Source_Output");
		//		hashrp.put("RP", "Source_LP-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_L-FP");
		//		hashrp.put("RP_BINDING", "Source_L-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_L-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_L-FP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Source_Input");
		//		tf1.put("OUTPUT", "Layer_Processor_Sink_Output");
		//		hashrp.put("RP", "Sink_LP-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_L-FP");
		//		hashrp.put("RP_BINDING", "Sink_L-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_L-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_L-FP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Source_Input");
		//		tf1.put("OUTPUT", "Adaptation_Source_Output");
		//		hashrp.put("RP", "Unidirectional_A-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_A-FP");
		//		hashrp.put("RP_BINDING", "A-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_A-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_A-FP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Source_Input");
		//		tf1.put("OUTPUT", "Subnetwork_Output");
		//		hashrp.put("RP", "Source_SN-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_SN-FP");
		//		hashrp.put("RP_BINDING", "Source_SN-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_SN-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_SN-FP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Sink_Input");
		//		tf1.put("OUTPUT", "Subnetwork_Output");
		//		hashrp.put("RP", "Source_SN-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_SN-FP");
		//		hashrp.put("RP_BINDING", "Sink_SN-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_SN-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_SN-FP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Source_Input");
		//		tf1.put("OUTPUT", "Matrix_Output");
		//		hashrp.put("RP", "Source_SN-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_So_M-FP");
		//		hashrp.put("RP_BINDING", "Source_M-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_So_M-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_So_M-FP_from");	
		//		values.put(tf1, hashrp);
		//
		//		tf1= new HashMap<String, String>();
		//		hashrp= new HashMap<String, String>();
		//		tf1.put("INPUT", "Adaptation_Sink_Input");
		//		tf1.put("OUTPUT", "Matrix_Output");
		//		hashrp.put("RP", "Source_M-FP");
		//		hashrp.put("RP_RELATION", "is_represented_by_Sk_M-FP");
		//		hashrp.put("RP_BINDING", "Sink_M-FP_Binding");
		//		hashrp.put("RP_BINDING_REL_IN", "binds_Sk_M-FP_to");
		//		hashrp.put("RP_BINDING_REL_OUT", "binds_Sk_M-FP_from");	
		//		values.put(tf1, hashrp);		
	}



	// get all equipments from specific site
	public static ArrayList<Equipment> getEquipmentsFromSite(String site){
		equipments = new ArrayList<String>();
		equipments =  QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, site, HomeController.NS+"has_equipment", HomeController.NS+"Equipment");
		return getEquipments();
	}

	public static String nameRelation="";
	public static ArrayList<String[]> siteConnects= new ArrayList<String[]>();
	public static List<String> getSitesAndRelations(){
		ind_class= new HashMap<String, List<String>>();
		nameRelation="site_connects";
		ArrayList<String[]> siteConnects= new ArrayList<String[]>();
		List<String> sites= QueryUtil.getIndividualsURI(InfModel, "Site");
		for (String site : sites) {
			Individual indSite = Model.getIndividual(site);
			List<String> siteTarget= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, indSite.getNameSpace(), "site_connects", "Site");
			for (String target : siteTarget) {
				String[] relation= new String[2];
				relation[0]=site;
				relation[1]=target;
				siteConnects.add(relation);
			}

		}
		return sites;
	}

	public static ArrayList<Equipment> getAllEquipmentsandConnections(){

		equipments = QueryUtil.getIndividualsURI(HomeController.InfModel, HomeController.NS+"Equipment");
		return getEquipments();
	}

	public static ArrayList<Equipment> getEquipments(){
		Model = HomeController.Model;
		InfModel = HomeController.InfModel;
		HashMap<String, String> hashInputEquipment= new HashMap<String, String>();
		//		inferInterfaceConnections();
		//equipments = HomeController.Search.GetInstancesFromClass(Model, InfModel, HomeController.NS+"Equipment");
		Equipment e = null;
		Individual individual;
		ArrayList<Equipment> equips= new ArrayList<Equipment>();
		for (String equipment: equipments) {
			Individual indeq= Model.getIndividual(equipment);
			List<String> inpInt= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, equipment, HomeController.NS+"componentOf", HomeController.NS+"Input_Interface");
			for (String string : inpInt) {
				Individual ind= Model.getIndividual(string);
				hashInputEquipment.put(ind.getLocalName(), indeq.getLocalName());
			}
		}


		for (String equipment: equipments) {
			List<String> outInts= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, equipment, HomeController.NS+"componentOf", HomeController.NS+"Output_Interface");
			Individual ind= Model.getIndividual(equipment);
			e = new Equipment(ind.getLocalName());

			for (String out_int : outInts) {
				individual= Model.getIndividual(out_int);
				InterfaceOutput outputInt = new InterfaceOutput();
				outputInt.setName(individual.getLocalName());
				e.addOut(outputInt);
				String inputcon= null;
				if(!QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, out_int, HomeController.NS+"interface_binds", HomeController.NS+"Input_Interface").isEmpty()){
					inputcon= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, out_int, HomeController.NS+"interface_binds", HomeController.NS+"Input_Interface").get(0);
				}
				if(inputcon!=null){
					outputInt.setConnected(true);
					ArrayList<String> binds = new ArrayList<String>();
					binds.add(individual.getLocalName());
					Individual indiv= Model.getIndividual(inputcon);
					binds.add(indiv.getLocalName());
					Individual equipmentEl = Model.getIndividual(HomeController.NS+hashInputEquipment.get(indiv.getLocalName()));
					Equipment equip = new Equipment(equipmentEl.getLocalName());
					e.putBinds(binds, equip);
				}

			}
			equips.add(e); 
		}

		equipments = QueryUtil.getIndividualsURI(HomeController.InfModel, HomeController.NS+"Equipment");
		return equips;
	}

	public static ArrayList<String[]> getPossibleConnects(String rp){
		InfModel = HomeController.InfModel;
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		List<String> classes_from_rp=QueryUtil.getClassesURI(InfModel,HomeController.NS+rp);
		//String binding=null;
		//String input = null;
		
		ArrayList<String> relations = new ArrayList<String>();
		ArrayList<String> rp_sink = new ArrayList<String>();
		ArrayList<String> rp_so = new ArrayList<String>();
		if(classes_from_rp.contains(HomeController.NS+"Source_PM-FEP")){
			relations.add("INV.binding_is_represented_by");
			relations.add("is_binding");
			relations.add("INV.componentOf");
			relations.add("componentOf");
			relations.add("INV.is_binding");
			relations.add("binding_is_represented_by");
			rp_sink = QueryUtil.query_EndOfGraph(rp, relations, InfModel);
			for(int i = 0; i < rp_sink.size(); i++){
				if(!QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+rp,HomeController.NS+"has_forwarding", HomeController.NS+"Reference_Point").contains(rp_sink.get(i))){
					String[] tuple = new String[2];
					tuple[0] =rp_sink.get(i);
					tuple[1] = "pm_nc";
					result.add(tuple);
				}
			}
			System.out.println();
		}else{
			relations.add("INV.binding_is_represented_by");
			relations.add("is_binding");
			relations.add("INV.componentOf");
			relations.add("componentOf");
			relations.add("INV.is_binding");
			relations.add("binding_is_represented_by");
			rp_so = QueryUtil.query_EndOfGraph(rp, relations, InfModel);
			relations.add("has_forwarding");
			relations.add("INV.binding_is_represented_by");
			relations.add("is_binding");
			relations.add("INV.componentOf");
			relations.add("componentOf");
			relations.add("INV.is_binding");
			relations.add("binding_is_represented_by");
			rp_sink = QueryUtil.query_EndOfGraph(rp, relations, InfModel);
			for(int i = 0; i < rp_sink.size(); i++){
				if(!QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+rp,HomeController.NS+"has_forwarding", HomeController.NS+"Reference_Point").contains(rp_sink.get(i))){
					String[] tuple = new String[2];
					tuple[0]= rp_sink.get(i);
					for(int j = 0; j < rp_so.size(); j++){
						if(rp_so.get(j).equals(HomeController.NS+"Source_PM-FEP")|| rp_so.get(j).equals(HomeController.NS+"Source_Path_FEP")){
							tuple[1]="nc";
						}else{
							tuple[1]="trail";
							result.add(tuple);
						}
					}
				}
			}
			System.out.println();
		}
		return result;
	}


	public static void connects(String rp, String rp_2, String type) throws InconsistentOntologyException, OKCoExceptionInstanceFormat{
		HomeController.InfModel = HomeController.Model;
		ArrayList<Statement> stmts = new ArrayList<Statement>();
		Individual forwarding;
		Individual te;
		if(type.equals("pm_nc")){
			forwarding = HomeController.Model.createIndividual(HomeController.NS+rp+"_fw_"+rp_2,HomeController.Model.getResource(HomeController.NS+"PM_NC_Forwarding"));
			te = HomeController.Model.createIndividual(HomeController.NS+rp+"_ate_"+rp_2,HomeController.Model.getResource(HomeController.NS+"Unidirectional_PM_NC"));
			stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"is_represented_by_Uni_Access_Transport_Entity"), te));
			stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"Forwarding_from_Uni_PM_NC"), HomeController.Model.getIndividual(HomeController.NS+rp)));
			stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"Forwarding_to_Uni_PM_NC"), HomeController.Model.getIndividual(HomeController.NS+rp_2)));	
			
			
		}else{
			if(type.equals("nc")){
				forwarding = HomeController.Model.createIndividual(HomeController.NS+rp+"_fw_"+rp_2,HomeController.Model.getResource(HomeController.NS+"Path_NC_Forwarding"));
				te = HomeController.Model.createIndividual(HomeController.NS+rp+"_ate_"+rp_2,HomeController.Model.getResource(HomeController.NS+"Unidirectional_Path_NC"));
				stmts = new ArrayList<Statement>();
				stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"is_represented_by_Uni_Path_NC"), te));
				stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"Forwarding_from_Uni_Path_NC"), HomeController.Model.getIndividual(HomeController.NS+rp)));
				stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"Forwarding_to_Uni_Path_NC"), HomeController.Model.getIndividual(HomeController.NS+rp_2)));	
			}else{
				forwarding = HomeController.Model.createIndividual(HomeController.NS+rp+"_fw_"+rp_2,HomeController.Model.getResource(HomeController.NS+"AP_Forwarding"));
				te = HomeController.Model.createIndividual(HomeController.NS+rp+"_ate_"+rp_2,HomeController.Model.getResource(HomeController.NS+"Unidirectional_Access_Transport_Entity"));
				stmts = new ArrayList<Statement>();
				stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"is_represented_by_Uni_Access_Transport_Entity"), te));
				stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"Forwarding_from_Uni_Access_Transport_Entity"), HomeController.Model.getIndividual(HomeController.NS+rp)));
				stmts.add(HomeController.Model.createStatement(forwarding, HomeController.Model.getProperty(HomeController.NS+"Forwarding_to_Uni_Access_Transport_Entity"), HomeController.Model.getIndividual(HomeController.NS+rp_2)));	
				
			}
			
		}
		stmts.add(HomeController.Model.createStatement(HomeController.Model.getIndividual(HomeController.NS+rp), HomeController.Model.getProperty(HomeController.NS+"has_forwarding"), HomeController.Model.getIndividual(HomeController.NS+rp_2)));
		HomeController.Model.add(stmts);
		
		HomeController.UpdateAddIntanceInLists(forwarding.getNameSpace()+forwarding.getLocalName());
		HomeController.UpdateAddIntanceInLists(te.getNameSpace()+te.getLocalName());
		
		HomeController.InfModel = HomeController.Model;

	}

	// type = 0 for output type =1 for input
	public static String getRPFromInterface(String eq_interface, Integer type){
		String value="maps_output";
		String target="Output";
		if(type==1){
			value="maps_input";
			target="Input";
		}

		if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, eq_interface, HomeController.NS+value, HomeController.NS+target).size()>0){
			String port= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, eq_interface, HomeController.NS+value, HomeController.NS+target).get(0);
			if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, port, HomeController.NS+"INV.is_binding", HomeController.NS+"Binding").size()>0){
				String binding = QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, port, HomeController.NS+"INV.is_binding", HomeController.NS+"Binding").get(0);
				if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, binding, HomeController.NS+"binding_is_represented_by", HomeController.NS+"Directly_Bound_Reference_Point").size()>0){
					return QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, binding, HomeController.NS+"binding_is_represented_by", HomeController.NS+"Directly_Bound_Reference_Point").get(0);					
				}
			}
		}
		return "";
	}

	public static ArrayList<Equipment> getEquipmentsConnectionsBinds(){
		return getAllEquipmentsandConnections();

	}

	public static boolean bindsInterfaces(String output, String input){
		Individual ind= Model.getIndividual(output);

		return true;
	}

	public static List<String> getAllSitesAndConnections(){
		connections = new ArrayList<String[]>();
		List<String> sites = QueryUtil.getIndividualsURI(HomeController.InfModel, HomeController.NS+"Site");
		for (String site : sites) {
			if(!QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, site, HomeController.NS+"site_connects", HomeController.NS+"Site").isEmpty()){
				List<String>targets=QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, site, HomeController.NS+"site_connects", HomeController.NS+"Site");
				for (String target : targets) {
					String[] connection = new String[2];
					connection[0]=site;
					connection[1]=target;
					connections.add(connection);
				}
			}
		}

		return sites;
	}

	public static List<String> getAllG800(){
		List<String> allIndividuals=QueryUtil.getIndividualsURIFromAllClasses(InfModel);
		ArrayList<String> copy = new ArrayList<String>();

		for (String ind : allIndividuals) {
			List<String> classesFromIndividual= QueryUtil.getClassesURI(InfModel,ind);
			if((classesFromIndividual.contains(HomeController.NS+"Input_Interface")  || classesFromIndividual.contains(HomeController.NS+"Output_Interface") || classesFromIndividual.contains(HomeController.NS+"Site") || classesFromIndividual.contains(HomeController.NS+"Equipment"))){
				copy.add(ind);
			}
		}
		for (String string : copy) {
			allIndividuals.remove(string);
		}
		setRelationsG800(allIndividuals);
		return allIndividuals;
	}
	public static List<String> getG800FromEquipment(String equipment){
		Provisioning.triples_g800 =  new ArrayList<String[]>();

		List<String> g800s  = QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+equipment, HomeController.NS+"componentOf", HomeController.NS+"Transport_Function");
		List<String> outInt = QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+equipment, HomeController.NS+"componentOf", HomeController.NS+"Output_Interface");
		List<String> inpInt = QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+equipment, HomeController.NS+"componentOf", HomeController.NS+"Input_Interface");

		for (String interface_out : outInt) {
			try {
				g800s.add(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+interface_out, HomeController.NS+interface_out+"maps_output", HomeController.NS+interface_out+"Output").get(0));
			} catch (Exception e) {				
			}			
		}
		for (String interface_inp : inpInt) {
			try {
				g800s.add(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, HomeController.NS+interface_inp, HomeController.NS+"maps_input", HomeController.NS+"Input").get(0));
			} catch (Exception e) {				
			}			
		}
		setRelationsG800(g800s);
		return g800s;
	}
	public static void setRelationsG800(List<String> g800_elements){

		ind_class = new HashMap<String, List<String>>();
		List<String> classesFromIndividual;
		for (String g800 : g800_elements) {
			classesFromIndividual= QueryUtil.getClassesURI(InfModel,g800);
			ind_class.put(g800, classesFromIndividual);
			
			// Get instance relations
			List<DtoInstanceRelation> rel = new ArrayList<DtoInstanceRelation>();
			List<String> propertiesURIList = QueryUtil.getPropertiesURI(HomeController.InfModel, g800);
			for(String propertyURI: propertiesURIList){
				DtoInstanceRelation dtoItem = new DtoInstanceRelation();
			    dtoItem.Property = propertyURI;
			    List<String> ranges = QueryUtil.getRangeURIs(UploadApp.getInferredModel(), propertyURI);
			    if(ranges.size()>0) dtoItem.Target = ranges.get(0);
			    else dtoItem.Target = "";
			    rel.add(dtoItem);
			}
			
			for (DtoInstanceRelation dtoInstanceRelation : rel) {
				String[] triple = new String[3];
				triple[0]=g800;
				triple[1]=dtoInstanceRelation.Property;
				triple[2]=dtoInstanceRelation.Target;
				triples_g800.add(triple);
			}

		}
	}

	public static void bindsSpecific(Individual a, Individual b, String tipo_out,
			String tipo_inp) {
		// TODO Auto-generated method stub
		HashMap<String, String> key = new HashMap<String, String>();
		key.put("INPUT", tipo_inp);
		key.put("OUTPUT", tipo_out);
		try{
			HashMap<String, String> value = BindsProcessor.values.get(key);
			OntClass ClassImage = Model.getOntClass(HomeController.NS+value.get("RP"));
			Individual rp = Model.createIndividual(HomeController.NS+a.getLocalName()+"rp"+b.getLocalName(),ClassImage);
			HomeController.Model=Model;
			Individual binding= Model.createIndividual(HomeController.NS+a.getLocalName()+"binding"+b.getLocalName(),Model.getResource(HomeController.NS+value.get("RP_BINDING")));
			ArrayList<Statement> stmts = new ArrayList<Statement>();
			stmts.add(HomeController.Model.createStatement(binding, Model.getProperty(HomeController.NS+value.get("RP_RELATION")), rp));
			stmts.add(HomeController.Model.createStatement(binding, Model.getProperty(HomeController.NS+value.get("RP_BINDING_REL_IN")), b));
			stmts.add(HomeController.Model.createStatement(binding, Model.getProperty(HomeController.NS+value.get("RP_BINDING_REL_OUT")), a));
			HomeController.Model.add(stmts);
		}catch(Exception e){
			e = new Exception("not bound");
		}
	}


	public static void inferInterfaceConnections(){
		HashMap<String, String> int_port = new HashMap<String, String>();
		List<String> inters = QueryUtil.getIndividualsURI(InfModel, HomeController.NS+"Input_Interface");
		for (String inter : inters) {
			List<String> port_inp =QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, inter, HomeController.NS+"maps_input", HomeController.NS+"Input");
			if(port_inp.size()>0){
				int_port.put(port_inp.get(0), inter);
			}
		}
		inters = QueryUtil.getIndividualsURI(InfModel, HomeController.NS+"Output_Interface");
		for (String inter : inters) {
			List<String> port_inp =QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, inter, HomeController.NS+"maps_output", HomeController.NS+"Output");
			if(port_inp.size()>0){
				int_port.put(port_inp.get(0), inter);
			}
		}

		List<String> outs = QueryUtil.getIndividualsURI(InfModel, HomeController.NS+"Output");
		for (String out : outs) {
			List<String> inputs  = QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, out, HomeController.NS+"binds", HomeController.NS+"Input");
			if(inputs.size()>0){
				String interfac_input= int_port.get(inputs.get(0));
				String interfac_output= int_port.get(out);
				Individual a = null,b=null;
				if(interfac_input!=null)
					a = HomeController.Model.getIndividual(interfac_input);
				if(interfac_output!=null)
					b = HomeController.Model.getIndividual(interfac_output);
				ObjectProperty rel = HomeController.Model.getObjectProperty(HomeController.NS+"interface_binds");
				if(a!=null && b!=null){
					Statement stmt = HomeController.Model.createStatement(b, rel, a);
					HomeController.Model.add(stmt);
				}
			}
		}
		HomeController.InfModel = HomeController.Model;
	}

	private static String[] getTriplePM(String value, String pm) {
		// TODO Auto-generated method stub

		String[] result = new String[3];

		if(value.equals("input")){
			if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, pm, HomeController.NS+"componentOf", HomeController.NS+"Physical_Media_Input").size()>0){
				String port= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, pm, HomeController.NS+"componentOf", HomeController.NS+"Physical_Media_Input").get(0);
				result[0]=port;
				if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, port, HomeController.NS+"INV.binds", HomeController.NS+"Termination_Source_Output").size()>0){
					String tf_out= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, port, HomeController.NS+"INV.binds", HomeController.NS+"Termination_Source_Output").get(0);
					if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, tf_out, HomeController.NS+"INV.maps_output", HomeController.NS+"Output_Interface").size()>0){
						String out_int= (QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, tf_out, HomeController.NS+"INV.maps_output", HomeController.NS+"Output_Interface").get(0));
						result[1]=out_int;
						if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, out_int, HomeController.NS+"INV.componentOf", HomeController.NS+"Equipment").size()>0){
							result[2]= (QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, out_int, HomeController.NS+"INV.componentOf", HomeController.NS+"Equipment").get(0));
						}
					}
				}
			}}else{
				if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, pm, HomeController.NS+"componentOf", HomeController.NS+"Physical_Media_Output").size()>0){
					String port= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, pm, HomeController.NS+"componentOf", HomeController.NS+"Physical_Media_Output").get(0);
					result[0]=port;
					if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, port, HomeController.NS+"binds", HomeController.NS+"Termination_Sink_Input").size()>0){
						String tf_in= QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, port, HomeController.NS+"binds", HomeController.NS+"Termination_Sink_Input").get(0);
						if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, tf_in, HomeController.NS+"INV.maps_input", HomeController.NS+"Input_Interface").size()>0){
							String inp_int= (QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, tf_in, HomeController.NS+"INV.maps_input", HomeController.NS+"Input_Interface").get(0));
							result[1]=inp_int;
							if(QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, inp_int, HomeController.NS+"INV.componentOf", HomeController.NS+"Equipment").size()>0){
								result[2]= (QueryUtil.getIndividualsURIAtObjectPropertyRange(InfModel, inp_int, HomeController.NS+"INV.componentOf", HomeController.NS+"Equipment").get(0));
							}
						}
					}
				}
			}
		return result ;


	} 


	public static ArrayList<String[]> getAllPhysicalMediaAndBinds(){

		List<String> pms = QueryUtil.getIndividualsURI( HomeController.InfModel, HomeController.NS+"Physical_Media");
		ArrayList<String[]> triples = new ArrayList<String[]>();

		for (String pm : pms) {
			String[] triple = new String[7];
			String[] triple_aux = new String[3];	
			triple_aux= getTriplePM("input", pm);
			if(triple_aux[0]!=null)
				triple[1]= triple_aux[0].split("#")[1];
			if(triple_aux[1]!=null)
				triple[0]= triple_aux[1].split("#")[1];
			if(triple_aux[2]!=null)
				triple[2]= triple_aux[2].split("#")[1];

			triple[3]=pm.split("#")[1];

			triple_aux= getTriplePM("output", pm);
			if(triple_aux[0]!=null){
				triple[4]= triple_aux[0].split("#")[1];
			}
			if(triple_aux[1]!=null){
				triple[5]= triple_aux[1].split("#")[1];
			}
			if(triple_aux[2]!=null)
				triple[6]= triple_aux[2].split("#")[1];

			triples.add(triple);
		}

		return triples;
	}




}
