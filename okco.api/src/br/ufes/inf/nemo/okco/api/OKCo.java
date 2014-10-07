package br.ufes.inf.nemo.okco.api;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import org.mindswap.pellet.exceptions.InconsistentOntologyException;

import br.com.padtec.common.queries.InfModelQueryUtil;
import br.com.padtec.common.queries.OntPropertyEnum;
import br.com.padtec.okco.domain.DtoCompleteClass;
import br.com.padtec.okco.domain.DtoDefinitionClass;
import br.com.padtec.okco.domain.FactoryInstances;
import br.com.padtec.okco.domain.HermitReasonerImpl;
import br.com.padtec.okco.domain.ManagerInstances;
import br.com.padtec.okco.domain.OntologyReasoner;
import br.com.padtec.okco.domain.PelletReasonerImpl;
import br.com.padtec.okco.domain.Search;
import br.com.padtec.okco.domain.exceptions.OKCoExceptionInstanceFormat;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;


/**Class for externals the OKCo's functionalities
 * @author F�bio Coradini
 /* @version 1.0
 * @since Release 01
 */
public class OKCo {
	
	/**List all incompleteness of an OWL file
     * @author F�bio Coradini
     * @param  pathOwlFile String - owl file path.
     * @param  optionReasoner String - For use Pellet reasoner "PELLET". For use Hermit reasoner "HERMIT". For no reasoner "NONE".
     * @return DtoResultInstances - Result operation - List of all incompleteness of an OWL file and possible errors in the process.
    */
	public DtoResultInstances listFileIncompleteness(String pathOwlFile, String reasonerOption)
	{
		DtoResultInstances dtoResult = new DtoResultInstances();
		try {
						
			OntologyReasoner Reasoner;
			
			//Select reasoner
			if(reasonerOption.equals("HERMIT"))
			{
				Reasoner = new HermitReasonerImpl();
				  
			} else if(reasonerOption.equals("PELLET")) {
				
				Reasoner = new PelletReasonerImpl();
				
			} else if(reasonerOption.equals("NONE")) {
				
				Reasoner = null;
				
			} else {
				
				  dtoResult.ListErrors.add("ERROR: Please select an available reasoner.");
				  return dtoResult;
			}		
			
			InputStream in = FileManager.get().open(pathOwlFile);
			if (in == null) {
				dtoResult.ListErrors.add("ERROR:  File: " + pathOwlFile + " not found.");
			    return dtoResult;
			}
			
			//Create model
			OntModel model = null;
			model = ModelFactory.createOntologyModel();
			
			model.read(in,null);		
			String ns = model.getNsPrefixURI("");		  
			if(ns == null)
			{
				dtoResult.ListErrors.add("ERROR: Please select an owl file with defined namespace.");
				return dtoResult;
			}
			
			Search search = new Search();
		  	FactoryInstances factoryInstance = new FactoryInstances(search);
		  	ManagerInstances managerInstances = new ManagerInstances(search, factoryInstance, model);
		  	
		  	//Call reasoner
		  	InfModel infModel;
		  	if(Reasoner == null)
		  	{
		  		infModel = model;
		  	} else {
		  		infModel = Reasoner.run(model);	
		  	}
		  	
		  	//get instances
		  	
		  	ArrayList<br.com.padtec.okco.domain.Instance> ListAllInstances = managerInstances.getAllInstances(model, infModel, ns);
	  	  
		  	// Gets definitions on model
		  	ArrayList<DtoDefinitionClass> ModelDefinitions = search.GetModelDefinitionsInInstances(ListAllInstances, infModel);			
			
			
			// Organize data (Update the list of all instances)
			
	    	managerInstances.UpdateInstanceAndRelations(ListAllInstances, ModelDefinitions, model, infModel, ns);
			managerInstances.UpdateInstanceSpecialization(ListAllInstances, model, infModel, ns);
			
			//build the return instances
			for (br.com.padtec.okco.domain.Instance i : ListAllInstances) 
			{
				//build list incompleteness relations
				ArrayList<InstanceRelationDefinition> ListImcompletenessRelationDefinitions = new ArrayList<InstanceRelationDefinition>();
				for (DtoDefinitionClass dto : i.ListSome) {
					
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "SOME";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}
				for (DtoDefinitionClass dto : i.ListMin) {
									
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "MIN";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}
				for (DtoDefinitionClass dto : i.ListMax) {
					
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "MAX";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}
				for (DtoDefinitionClass dto : i.ListExactly) {
					
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "EXACTLY";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}				
				
				//build list incompleteness classes
				ArrayList<InstanceClassDefinition> ListImcompletenessClassDefinitions = new ArrayList<InstanceClassDefinition>();
				for (DtoCompleteClass dto : i.ListCompleteClasses) {
					if(dto.Members.size() > 0)
					{
						InstanceClassDefinition classDefinition = new InstanceClassDefinition();
						classDefinition.TopClass = dto.CompleteClass;
						for (String cls : dto.Members) {
							classDefinition.SubClassesToClassify.add(cls);
						}
						ListImcompletenessClassDefinitions.add(classDefinition);
					}
				}				
				
				Instance newInstance = new Instance(i.ns, i.name, i.ListClasses, i.ListDiferentInstances, i.ListSameInstances, ListImcompletenessRelationDefinitions, ListImcompletenessClassDefinitions);
				dtoResult.ListInstances.add(newInstance);
			}		
			
		} catch (OKCoExceptionInstanceFormat e) {

			String error = "ERROR: Entity format error: " + e.getMessage();
			dtoResult.ListErrors.add(error);
			return dtoResult;
			
		} catch (InconsistentOntologyException e) {

			String error = "INCONSISTENCY: " + e.toString() + ".";
			dtoResult.ListErrors.add(error);
			return dtoResult;
			
		}
		
		return dtoResult;
	}
		
	/**Complete all relation incompleteness of an OWL file
     * @author F�bio Coradini
     * @param  pathOwlFile String - owl file content.
     * @param  optionReasoner String - For use Pellet reasoner "PELLET". For use Hermit reasoner "HERMIT".
     * @param  strength String - Strength for complete file. "FULL" for completions that do not make domain assumptions over instances (the creation of new individuals or data) and "REGULAR" there are others that require assumptions (classification of individuals). 
     * @return DtoResultFile - Result operation - OWL file completed and possible errors in the process.
    */
	public DtoResultFile completeIncompleteness(String pathOwlFile, String reasonerOption, String strength)
	{
		DtoResultFile dtoResult = new DtoResultFile();
		
		try {
			
			OntologyReasoner Reasoner;
			
			//Select reasoner
			if(reasonerOption.equals("HERMIT"))
			{
				Reasoner = new HermitReasonerImpl();
				  
			} else if(reasonerOption.equals("PELLET")) {
				
				Reasoner = new PelletReasonerImpl();
				
			} else if(reasonerOption.equals("NONE")) {
				
				Reasoner = null;
				
			} else {
				
				dtoResult.ListErrors.add("ERROR: Please select an available reasoner.");
				return dtoResult;
			}	
			
			//Check strength
			if(! (strength.equals("FULL") || strength.equals("REGULAR")) )
			{
				dtoResult.ListErrors.add("ERROR: Please select an available strength.");
				return dtoResult;
			}
			
			InputStream in = FileManager.get().open(pathOwlFile);
			if (in == null) {
				dtoResult.ListErrors.add("ERROR: File: " + pathOwlFile + " not found.");
			    return dtoResult;
			}
			
			//Create model
			OntModel model = ModelFactory.createOntologyModel();
			
			model.read(in,null);		
			String ns = model.getNsPrefixURI("");		  
			if(ns == null)
			{
				dtoResult.ListErrors.add("ERROR: Please select owl file with defined namespace.");
				return dtoResult;
			}
			
			Search search = new Search();
		  	FactoryInstances factoryInstance = new FactoryInstances(search);
		  	ManagerInstances managerInstances = new ManagerInstances(search, factoryInstance, model);
		  	
		  	//Call reasoner
		  	InfModel infModel;
		  	if(Reasoner == null)
		  	{
		  		infModel = model;
		  	} else {
		  		infModel = Reasoner.run(model);	
		  	}

		  	
		  	/*--------------------------------------------------------------------------------------------- //
												Update List instances
			//--------------------------------------------------------------------------------------------- */
		  	
		  	ArrayList<br.com.padtec.okco.domain.Instance> ListAllInstances = managerInstances.getAllInstances(model, infModel, ns);
	  	  
		  	// Gets definitions on model
		  	ArrayList<DtoDefinitionClass> ModelDefinitions = search.GetModelDefinitionsInInstances(ListAllInstances, infModel);			
			
			
			// Organize data (Update the list of all instances)
			
	    	managerInstances.UpdateInstanceAndRelations(ListAllInstances, ModelDefinitions, model, infModel, ns);
			managerInstances.UpdateInstanceSpecialization(ListAllInstances, model, infModel, ns);
			
			/*--------------------------------------------------------------------------------------------- //
												Complete instances
			//--------------------------------------------------------------------------------------------- */
			
			//Complete the selected instances
			
			for (br.com.padtec.okco.domain.Instance instance : ListAllInstances) 
			{
				if(strength.equals("FULL"))
				{
					//Classify instance classes
					model = managerInstances.ClassifyInstanceAuto(instance, model, infModel);
				}
				
				for (DtoDefinitionClass dto : instance.ListSome) 
				{
					if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
					{
						//create the the new instance
						String instanceName = dto.Target.split("#")[1] + "-" + (InfModelQueryUtil.getIndividualsURI(infModel, dto.Target).size() + 1);
						ArrayList<String> listSame = new ArrayList<String>();		  
						ArrayList<String> listDif = new ArrayList<String>();
						ArrayList<String> listClasses = new ArrayList<String>();
						br.com.padtec.okco.domain.Instance newInstance = new br.com.padtec.okco.domain.Instance(ns, instanceName, listClasses, listDif, listSame, false);
						
						model = managerInstances.CreateInstanceAuto(instance.ns + instance.name, dto, newInstance, model, infModel, ListAllInstances);
					}
				}
				for (DtoDefinitionClass dto : instance.ListMin) 
				{
					if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
					{
						int quantityInstancesTarget = search.CheckExistInstancesTargetCardinality(infModel, instance.ns + instance.name, dto.Relation, dto.Target, dto.Cardinality);
						
						ArrayList<String> listDif = new ArrayList<String>();
						while(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
						{
							//create the the new instance
							String instanceName = dto.Target.split("#")[1] + "-" + (quantityInstancesTarget + 1);
							ArrayList<String> listSame = new ArrayList<String>();		  
							ArrayList<String> listClasses = new ArrayList<String>();
							br.com.padtec.okco.domain.Instance newInstance = new br.com.padtec.okco.domain.Instance(ns, instanceName, listClasses, listDif, listSame, false);
							
							model = managerInstances.CreateInstanceAuto(instance.ns + instance.name, dto, newInstance, model, infModel, ListAllInstances);				
							listDif.add(newInstance.ns + newInstance.name);
							quantityInstancesTarget ++;
						}
					}
							
				}
				for (DtoDefinitionClass dto : instance.ListExactly) 
				{
					if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
					{
						int quantityInstancesTarget = search.CheckExistInstancesTargetCardinality(infModel, instance.ns + instance.name, dto.Relation, dto.Target, dto.Cardinality);
						
						// Case 1 - same as min
						if(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
						{
							ArrayList<String> listDif = new ArrayList<String>();
							while(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
							{
								//create the the new instance
								String instanceName = dto.Target.split("#")[1] + "-" + (quantityInstancesTarget + 1);
								ArrayList<String> listSame = new ArrayList<String>();		  
								ArrayList<String> listClasses = new ArrayList<String>();
								br.com.padtec.okco.domain.Instance newInstance = new br.com.padtec.okco.domain.Instance(ns, instanceName, listClasses, listDif, listSame, false);
								
								model = managerInstances.CreateInstanceAuto(instance.ns + instance.name, dto, newInstance, model, infModel, ListAllInstances);				
								listDif.add(newInstance.ns + newInstance.name);
								quantityInstancesTarget ++;
							}
						}
						
						// Case 2 - more individuals than necessary
						if(quantityInstancesTarget > Integer.parseInt(dto.Cardinality))
						{
													
						}
					}
				}
			}			
			
			/*--------------------------------------------------------------------------------------------- //
											Update List instances
			//--------------------------------------------------------------------------------------------- */

			ListAllInstances = managerInstances.getAllInstances(model, infModel, ns);
		  	  
		  	// Gets definitions on model
		  	ModelDefinitions = search.GetModelDefinitionsInInstances(ListAllInstances, infModel);		
			
			// Organize data (Update the list of all instances)
			
	    	managerInstances.UpdateInstanceAndRelations(ListAllInstances, ModelDefinitions, model, infModel, ns);
			managerInstances.UpdateInstanceSpecialization(ListAllInstances, model, infModel, ns);
			
			/*--------------------------------------------------------------------------------------------- //
											Build the return instances
			//--------------------------------------------------------------------------------------------- */
			
			for (br.com.padtec.okco.domain.Instance i : ListAllInstances) 
			{
				//build list incompleteness relations
				ArrayList<InstanceRelationDefinition> ListImcompletenessRelationDefinitions = new ArrayList<InstanceRelationDefinition>();
				for (DtoDefinitionClass dto : i.ListSome) {
					
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "SOME";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}
				for (DtoDefinitionClass dto : i.ListMin) {
									
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "MIN";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}
				for (DtoDefinitionClass dto : i.ListMax) {
					
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "MAX";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}
				for (DtoDefinitionClass dto : i.ListExactly) {
					
					InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
					relationDef.SourceClass = dto.Source;
					relationDef.Relation = dto.Relation;
					relationDef.Cardinality = dto.Cardinality;
					relationDef.RelationType = dto.PropertyType.toString();
					relationDef.TargetClass = dto.Target;
					relationDef.KindProperty = "EXACTLY";
					ListImcompletenessRelationDefinitions.add(relationDef);
				}				
				
				//build list incompleteness classes
				ArrayList<InstanceClassDefinition> ListImcompletenessClassDefinitions = new ArrayList<InstanceClassDefinition>();
				for (DtoCompleteClass dto : i.ListCompleteClasses) {
					if(dto.Members.size() > 0)
					{
						InstanceClassDefinition classDefinition = new InstanceClassDefinition();
						classDefinition.TopClass = dto.CompleteClass;
						for (String cls : dto.Members) {
							classDefinition.SubClassesToClassify.add(cls);
						}
						ListImcompletenessClassDefinitions.add(classDefinition);
					}
				}				
				
				Instance newInstance = new Instance(i.ns, i.name, i.ListClasses, i.ListDiferentInstances, i.ListSameInstances, ListImcompletenessRelationDefinitions, ListImcompletenessClassDefinitions);
				dtoResult.ListInstances.add(newInstance);		
			}
			
			/*--------------------------------------------------------------------------------------------- //
												Write OWL return
			//--------------------------------------------------------------------------------------------- */
			
			StringWriter writer = new StringWriter();
			model.write(writer,"RDF/XML");
			String owltext = writer.toString();			
			dtoResult.owlFile = owltext;
			
		} catch (OKCoExceptionInstanceFormat e) {

			String error = "ERROR: Entity format error: " + e.getMessage();
			dtoResult.ListErrors.add(error);
			return dtoResult;
			
		} catch (InconsistentOntologyException e) {

			String error = "INCONSISTENCY: Ontology have inconsistence:" + e.toString() + ".";
			dtoResult.ListErrors.add(error);
			return dtoResult;			
		}
		
		return dtoResult;
	
	}

	/**Complete all relation incompleteness of an OWL file
     * @author F�bio Coradini
     * @param  setInstances ArrayList<String> - list instances to complete.
     * @param  pathOwlFile String - owl file content.
     * @param  optionReasoner String - For use Pellet reasoner "PELLET". For use Hermit reasoner "HERMIT".
     * @param  strength String - Strength for complete file. "FULL" for completions that do not make domain assumptions over instances (the creation of new individuals or data) and "REGULAR" there are others that require assumptions (classification of individuals).
     * @return DtoResultFile - Result operation - OWL file completed and possible errors in the process.
    */
	public DtoResultFile completeIncompleteness(ArrayList<String> setInstances, String pathOwlFile, String reasonerOption, String strength)
	{
		DtoResultFile dtoResult = new DtoResultFile();
		
		try {
			
			OntologyReasoner Reasoner;
			
			//Select reasoner
			if(reasonerOption.equals("HERMIT"))
			{
				Reasoner = new HermitReasonerImpl();
				  
			} else if(reasonerOption.equals("PELLET")) {
				
				Reasoner = new PelletReasonerImpl();
				
			} else if(reasonerOption.equals("NONE")) {
				
				Reasoner = null;
				
			} else {
				
				dtoResult.ListErrors.add("ERROR: Please select an available reasoner.");
				return dtoResult;
			}
			
			//Check strength
			if(! (strength.equals("FULL") || strength.equals("REGULAR")) )
			{
				dtoResult.ListErrors.add("ERROR: Please select an available strength.");
				return dtoResult;
			}			
			
			
			InputStream in = FileManager.get().open(pathOwlFile);
			if (in == null) {
				dtoResult.ListErrors.add("ERROR: File: " + pathOwlFile + " not found.");
			    return dtoResult;
			}
			
			//Create model
			OntModel model = ModelFactory.createOntologyModel();
			
			model.read(in,null);		
			String ns = model.getNsPrefixURI("");		  
			if(ns == null)
			{
				dtoResult.ListErrors.add("ERROR: Please select an owl file with defined namespace.");
				return dtoResult;
			}
			
			Search search = new Search();
		  	FactoryInstances factoryInstance = new FactoryInstances(search);
		  	ManagerInstances managerInstances = new ManagerInstances(search, factoryInstance, model);
		  	
		  	//Call reasoner
		  	InfModel infModel;
		  	if(Reasoner == null)
		  	{
		  		infModel = model;
		  	} else {
		  		infModel = Reasoner.run(model);	
		  	}
		  	
		  	/*--------------------------------------------------------------------------------------------- //
												Update List instances
			//--------------------------------------------------------------------------------------------- */
		  	
		  	ArrayList<br.com.padtec.okco.domain.Instance> ListAllInstances = managerInstances.getAllInstances(model, infModel, ns);
		  	  
		  	// Gets definitions on model
		  	ArrayList<DtoDefinitionClass> ModelDefinitions = search.GetModelDefinitionsInInstances(ListAllInstances, infModel);			
			
			// Organize data (Update the list of all instances)
			
	    	managerInstances.UpdateInstanceAndRelations(ListAllInstances, ModelDefinitions, model, infModel, ns);
			managerInstances.UpdateInstanceSpecialization(ListAllInstances, model, infModel, ns);
			
			/*--------------------------------------------------------------------------------------------- //
												Complete instances
			//--------------------------------------------------------------------------------------------- */
			
			//Complete the selected instances
			
			for (br.com.padtec.okco.domain.Instance instance : ListAllInstances) 
			{
				if(setInstances.contains(instance.ns + instance.name))
				{
					if(strength.equals("FULL"))
					{
						//Classify instance classes
						model = managerInstances.ClassifyInstanceAuto(instance, model, infModel);
					}
					
					for (DtoDefinitionClass dto : instance.ListSome) 
					{
						if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
						{
							//create the the new instance
							String instanceName = dto.Target.split("#")[1] + "-" + (InfModelQueryUtil.getIndividualsURI(infModel, dto.Target).size() + 1);
							ArrayList<String> listSame = new ArrayList<String>();		  
							ArrayList<String> listDif = new ArrayList<String>();
							ArrayList<String> listClasses = new ArrayList<String>();
							br.com.padtec.okco.domain.Instance newInstance = new br.com.padtec.okco.domain.Instance(ns, instanceName, listClasses, listDif, listSame, false);
							
							model = managerInstances.CreateInstanceAuto(instance.ns + instance.name, dto, newInstance, model, infModel, ListAllInstances);
						}
					}
					for (DtoDefinitionClass dto : instance.ListMin) 
					{
						if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
						{
							int quantityInstancesTarget = search.CheckExistInstancesTargetCardinality(infModel, instance.ns + instance.name, dto.Relation, dto.Target, dto.Cardinality);
							
							ArrayList<String> listDif = new ArrayList<String>();
							while(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
							{
								//create the the new instance
								String instanceName = dto.Target.split("#")[1] + "-" + (quantityInstancesTarget + 1);
								ArrayList<String> listSame = new ArrayList<String>();		  
								ArrayList<String> listClasses = new ArrayList<String>();
								br.com.padtec.okco.domain.Instance newInstance = new br.com.padtec.okco.domain.Instance(ns, instanceName, listClasses, listDif, listSame, false);
								
								model = managerInstances.CreateInstanceAuto(instance.ns + instance.name, dto, newInstance, model, infModel, ListAllInstances);				
								listDif.add(newInstance.ns + newInstance.name);
								quantityInstancesTarget ++;
							}
						}
								
					}
					for (DtoDefinitionClass dto : instance.ListExactly) 
					{
						if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
						{
							int quantityInstancesTarget = search.CheckExistInstancesTargetCardinality(infModel, instance.ns + instance.name, dto.Relation, dto.Target, dto.Cardinality);
							
							// Case 1 - same as min
							if(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
							{
								ArrayList<String> listDif = new ArrayList<String>();
								while(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
								{
									//create the the new instance
									String instanceName = dto.Target.split("#")[1] + "-" + (quantityInstancesTarget + 1);
									ArrayList<String> listSame = new ArrayList<String>();		  
									ArrayList<String> listClasses = new ArrayList<String>();
									br.com.padtec.okco.domain.Instance newInstance = new br.com.padtec.okco.domain.Instance(ns, instanceName, listClasses, listDif, listSame, false);
									
									model = managerInstances.CreateInstanceAuto(instance.ns + instance.name, dto, newInstance, model, infModel, ListAllInstances);				
									listDif.add(newInstance.ns + newInstance.name);
									quantityInstancesTarget ++;
								}
							}
							
							// Case 2 - more individuals than necessary
							if(quantityInstancesTarget > Integer.parseInt(dto.Cardinality))
							{
														
							}
						}
					}
				}
			}			
			
			/*--------------------------------------------------------------------------------------------- //
											Update List instances
			//--------------------------------------------------------------------------------------------- */

			ListAllInstances = managerInstances.getAllInstances(model, infModel, ns);
		  	  
		  	// Gets definitions on model
		  	ModelDefinitions = search.GetModelDefinitionsInInstances(ListAllInstances, infModel);			
			
			// Organize data (Update the list of all instances)
			
	    	managerInstances.UpdateInstanceAndRelations(ListAllInstances, ModelDefinitions, model, infModel, ns);
			managerInstances.UpdateInstanceSpecialization(ListAllInstances, model, infModel, ns);
			
			/*--------------------------------------------------------------------------------------------- //
											Build the return instances
			//--------------------------------------------------------------------------------------------- */
			
			for (br.com.padtec.okco.domain.Instance i : ListAllInstances) 
			{
				if(setInstances.contains(i.ns + i.name))
				{
					//build list incompleteness relations
					ArrayList<InstanceRelationDefinition> ListImcompletenessRelationDefinitions = new ArrayList<InstanceRelationDefinition>();
					for (DtoDefinitionClass dto : i.ListSome) {
						
						InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
						relationDef.SourceClass = dto.Source;
						relationDef.Relation = dto.Relation;
						relationDef.Cardinality = dto.Cardinality;
						relationDef.RelationType = dto.PropertyType.toString();
						relationDef.TargetClass = dto.Target;
						relationDef.KindProperty = "SOME";
						ListImcompletenessRelationDefinitions.add(relationDef);
					}
					for (DtoDefinitionClass dto : i.ListMin) {
										
						InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
						relationDef.SourceClass = dto.Source;
						relationDef.Relation = dto.Relation;
						relationDef.Cardinality = dto.Cardinality;
						relationDef.RelationType = dto.PropertyType.toString();
						relationDef.TargetClass = dto.Target;
						relationDef.KindProperty = "MIN";
						ListImcompletenessRelationDefinitions.add(relationDef);
					}
					for (DtoDefinitionClass dto : i.ListMax) {
						
						InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
						relationDef.SourceClass = dto.Source;
						relationDef.Relation = dto.Relation;
						relationDef.Cardinality = dto.Cardinality;
						relationDef.RelationType = dto.PropertyType.toString();
						relationDef.TargetClass = dto.Target;
						relationDef.KindProperty = "MAX";
						ListImcompletenessRelationDefinitions.add(relationDef);
					}
					for (DtoDefinitionClass dto : i.ListExactly) {
						
						InstanceRelationDefinition relationDef = new InstanceRelationDefinition();
						relationDef.SourceClass = dto.Source;
						relationDef.Relation = dto.Relation;
						relationDef.Cardinality = dto.Cardinality;
						relationDef.RelationType = dto.PropertyType.toString();
						relationDef.TargetClass = dto.Target;
						relationDef.KindProperty = "EXACTLY";
						ListImcompletenessRelationDefinitions.add(relationDef);
					}				
					
					//build list incompleteness classes
					ArrayList<InstanceClassDefinition> ListImcompletenessClassDefinitions = new ArrayList<InstanceClassDefinition>();
					for (DtoCompleteClass dto : i.ListCompleteClasses) {
						if(dto.Members.size() > 0)
						{
							InstanceClassDefinition classDefinition = new InstanceClassDefinition();
							classDefinition.TopClass = dto.CompleteClass;
							for (String cls : dto.Members) {
								classDefinition.SubClassesToClassify.add(cls);
							}
							ListImcompletenessClassDefinitions.add(classDefinition);
						}
					}				
					
					Instance newInstance = new Instance(i.ns, i.name, i.ListClasses, i.ListDiferentInstances, i.ListSameInstances, ListImcompletenessRelationDefinitions, ListImcompletenessClassDefinitions);
					dtoResult.ListInstances.add(newInstance);
				}
			}
			
			/*--------------------------------------------------------------------------------------------- //
												Write OWL return
			//--------------------------------------------------------------------------------------------- */
			
			StringWriter writer = new StringWriter();
			model.write(writer,"RDF/XML");
			String owltext = writer.toString();			
			dtoResult.owlFile = owltext;
			
		} catch (OKCoExceptionInstanceFormat e) {

			String error = "ERROR: Entity format error: " + e.getMessage();
			dtoResult.ListErrors.add(error);
			return dtoResult;
			
		} catch (InconsistentOntologyException e) {

			String error = "INCONSISTENCY: " + " Ontology have inconsistency:" + e.toString() + ".";
			dtoResult.ListErrors.add(error);
			return dtoResult;
			
		}
		
		return dtoResult;
	
	}
	
}
