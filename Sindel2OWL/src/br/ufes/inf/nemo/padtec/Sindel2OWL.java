package br.ufes.inf.nemo.padtec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import br.ufes.inf.nemo.padtec.DtoSindel.DtoResultSindel;
import br.ufes.inf.nemo.padtec.processors.AttributeProcessor;
import br.ufes.inf.nemo.padtec.processors.BindsProcessor;
import br.ufes.inf.nemo.padtec.processors.ClientProcessor;
import br.ufes.inf.nemo.padtec.processors.CompositionProcessor;
import br.ufes.inf.nemo.padtec.processors.ConnectsProcessor;
import br.ufes.inf.nemo.padtec.processors.ElementsProcessor;
import br.ufes.inf.nemo.padtec.processors.MapsProcessor;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


public class Sindel2OWL {
	
	public OntModel model;
	public String ClassNS;
	public String IndNS = new String("http://www.semanticweb.org/individuous.owl#");//nao se deve usar esse NS, animal de orelha

	public static String warning = new String();
	public static String error = new String();
	public static HashMap<String,String> hashIndividuals = new HashMap<String,String>();
	public static HashMap<String,String> hashSindelxG800 = new HashMap<String,String>();

	private DtoResultSindel DtoSindel;
	public DtoResultSindel getDtoSindel() {
		return DtoSindel;
	}

	private static int cont = 0;
	public static int getCont(){
		return cont++;
	}

	private OntModel owlReference;
	
	public Sindel2OWL(OntModel owl, String individualsPrefixName) {
		this(owl);
		IndNS += individualsPrefixName;
	}
	
	public Sindel2OWL(OntModel owl) {
		//save owl reference
//		owlReference = owl;

		//Getting owl namespace
		ClassNS = owl.getNsPrefixURI("");
		this.IndNS = this.ClassNS;
//		
//		//Transforming a Model to an OntModel
//		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
//		model.add(owlReference);
		model = owl;
		
		warning = new String();
		error = new String();
		hashIndividuals = new HashMap<String,String>();
		hashSindelxG800 = new HashMap<String,String>();
	}

	private void intializeHashs(){
		hashSindelxG800.put("so-tf","Termination_Source");
		hashSindelxG800.put("sk-tf","Termination_Sink");
		
		hashSindelxG800.put("so-af","Adaptation_Source");
		hashSindelxG800.put("sk-af","Adaptation_Sink");
		
		hashSindelxG800.put("so-lpf","Layer_Processor_Function_Source");
		hashSindelxG800.put("sk-lpf","Layer_Processor_Function_Sink");
		
		hashSindelxG800.put("so-matrix","Source_Matrix");
		hashSindelxG800.put("sk-matrix","Sink_Matrix");
		
		hashSindelxG800.put("so-sn","Source_Subnetwork");
		hashSindelxG800.put("sk-sn","Sink_Subnetwork");
		
		hashSindelxG800.put("pm","Physical_Media");
	}
	
	public void run(String sindelCode){
		intializeHashs();
		BindsProcessor.resetPortsAndRPsToBindsSpecifically();
		
		String[] s_parts;
		s_parts = sindelCode.split("!");
		for (String s_elements : s_parts) {
			String[] element = s_elements.split("#");
			if(element.length != 2){
				continue;
			}
			if(element[0].equalsIgnoreCase("elements")){
				ElementsProcessor.processElements(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("component_of")){
				CompositionProcessor.processCompositions(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("binds")){
				BindsProcessor.processBinds(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("connects")){
				ConnectsProcessor.processConnects(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("client")){
				ClientProcessor.processClient(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("maps")){
				MapsProcessor.processMaps(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("str_location")){
				AttributeProcessor.processLocationTFString(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("geo_location")){
				AttributeProcessor.processLocationTFGeolocalization(model, ClassNS, IndNS, element[1]);
			}else if(element[0].equalsIgnoreCase("tf_type")){
				AttributeProcessor.processTypeTF(model, ClassNS, IndNS, element[1]);
			}
		}

		
		ArrayList<ArrayList<Individual>> portsAndRPsToBindsSpecifically = BindsProcessor.getPortsAndRPsToBindsSpecifically();
		for (ArrayList<Individual> portsAndBinds : portsAndRPsToBindsSpecifically) {
			Individual port1 = portsAndBinds.get(0);
			Individual port2 = portsAndBinds.get(1);
			Individual rp = portsAndBinds.get(2);
			Individual binding = portsAndBinds.get(3);
			
			ArrayList<String> listInstancesCreated = new ArrayList<String>();
			listInstancesCreated.add(rp.getNameSpace()+rp.getLocalName());
			listInstancesCreated.add(port1.getNameSpace()+port1.getLocalName());
			listInstancesCreated.add(port2.getNameSpace()+port2.getLocalName());
			
			BindsProcessor.bindPorts(rp, port1, port2, binding, ClassNS, model, listInstancesCreated);
		}
	//	createDisjointness();
		createDtoSindel();
//		createModelImports();
	}

	private void createModelImports() {
		model.remove(owlReference);

		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		Ontology ontology = model.createOntology(IndNS);
		Ontology ontITUGx = ontModel.createOntology(ClassNS.replaceAll("#", ""));

		ontology.addImport(ontITUGx);
	}

	private void createDtoSindel() {
		DtoSindel = new DtoResultSindel();

		DtoSindel.warning = warning;
		DtoSindel.error = error;
		DtoSindel.model = model;
	}

	private void createDisjointness(){
		//Make all individuals different
		ExtendedIterator<Individual> individuals = model.listIndividuals();
		ArrayList<Resource> lst = new ArrayList<Resource>();

		while (individuals.hasNext()) {
			Resource individual = (Resource) individuals.next();
			if (individual.getNameSpace().equals(IndNS)){
				lst.add(individual);
			}
		}
		model.createAllDifferent(model.createList(lst.toArray(new RDFNode[]{})));
	}


//	public static void main(String[] args) {
//		String sindelCode = "elements#input:in_ODUj_Client_AF4,in_ODUj_Client_AF3,in_ODUj_Client_AF2,in_ODUj_Client_AF1;af:ODUj_Client_AF4,ODUj_Client_AF3,ODUj_Client_AF2,ODUj_Client_AF1;tf:ODUj_TTF4,ODUj_TTF3,ODUj_TTF2,ODUj_TTF1;output:out_ODUj_TTF4,out_ODUj_TTF3,out_ODUj_TTF2,out_ODUj_TTF1;!binds#ODUj_Client_AF1:ODUj_TTF1,ODUj_Client_AF2:ODUj_TTF2,ODUj_Client_AF3:ODUj_TTF3,ODUj_Client_AF4:ODUj_TTF4!connects#!maps#!client#!component_of#ODUj_Client_AF1:in_ODUj_Client_AF1;ODUj_Client_AF2:in_ODUj_Client_AF2;ODUj_Client_AF3:in_ODUj_Client_AF3;ODUj_Client_AF4:in_ODUj_Client_AF4;ODUj_TTF1:out_ODUj_TTF1;ODUj_TTF2:out_ODUj_TTF2;ODUj_TTF3:out_ODUj_TTF3;ODUj_TTF4:out_ODUj_TTF4!str_location!geo_location!tf_type!";
//		try{
//			Model model = FileManager.get().loadModel("g800.owl");
//			Sindel2OWL so = new Sindel2OWL(model);
//			so.run(sindelCode);
//
//			DtoResultSindel dtoSindel = so.getDtoSindel();
//
//			File arquivo;   
//			arquivo = new File("arquivoSaida.owl");  // Chamou e nomeou o arquivo txt.  
//			try{
//				FileOutputStream fos = new FileOutputStream(arquivo);  // Perceba que estamos instanciando uma classe aqui. A FileOutputStream. Pesquise sobre ela!  
//				dtoSindel.model.write(fos, "RDF/XML");
//				fos.close();  // Fecha o arquivo. Nunca esquecer disso.  
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			System.out.println("SUCESSAGEM");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
}
