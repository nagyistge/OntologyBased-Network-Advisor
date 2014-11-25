package br.com.padtec.common.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mindswap.pellet.exceptions.InconsistentOntologyException;

import br.com.padtec.common.dto.DataPropertyValue;
import br.com.padtec.common.dto.DtoCompleteClass;
import br.com.padtec.common.dto.DtoDefinitionClass;
import br.com.padtec.common.dto.DtoInstance;
import br.com.padtec.common.dto.DtoInstanceRelation;
import br.com.padtec.common.dto.DtoPropertyAndSubProperties;
import br.com.padtec.common.exceptions.OKCoExceptionInstanceFormat;
import br.com.padtec.common.factory.DtoFactoryUtil;
import br.com.padtec.common.factory.FactoryUtil;
import br.com.padtec.common.queries.DtoQueryUtil;
import br.com.padtec.common.queries.OntModelAPI;
import br.com.padtec.common.queries.QueryUtil;
import br.com.padtec.common.types.OntCardinalityEnum;
import br.com.padtec.common.types.OntPropertyEnum;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;


public class CompleterApp {
	
	public static List<DtoInstance> ListAllInstances;	
	public static List<DtoDefinitionClass> ModelDefinitions;
	public static List<String> ListModifiedInstances = new ArrayList<String>();
	
	// Save the new instances before commit in views (completePropertyObject and completePropertyData)

	//Instances to add in relation
	public static ArrayList<DtoInstance> listNewInstancesRelation;
	//DataValues to add in relation
	public static ArrayList<DataPropertyValue> listNewDataValuesRelation;
	//Dto selected
	public static DtoDefinitionClass dtoSelected;
	//Instance selected
	public static DtoInstance instanceSelected;
	//Specialization - Complete classes for instance class
	public static ArrayList<DtoCompleteClass> ListCompleteClsInstaceSelected;
	//Specialization - Property and subProperties
	public static ArrayList<DtoPropertyAndSubProperties> ListSpecializationProperties;
	
	public static void clear() 
	{
		ListModifiedInstances.clear();
		ListAllInstances=null;	
	}
		
	public static void updateModifiedList()
	{
		for (DtoInstance i : ListAllInstances) 
		{
			String s = i.ns + i.name;
			if (ListModifiedInstances.contains(s)) i.setModified(true);			
		}
	}
	
	//Check the validity of this method
	public static void updateLists() throws InconsistentOntologyException, OKCoExceptionInstanceFormat 
	{	
		System.out.println("Updating Lists()...");
		InfModel inferredModel = UploadApp.getInferredModel();
		OntModel Model = UploadApp.getBaseModel();
    	// Refresh list of instances
    	ListAllInstances = DtoQueryUtil.getIndividuals(inferredModel);
    	//Get model definitions on list of instances	    	
	  	ModelDefinitions = DtoQueryUtil.getClassDefinitions(inferredModel);			
		// Organize data (Update the list of all instances)			
    	UpdateInstanceAndRelations(Model, UploadApp.getBaseRepository().getNameSpace(), ListAllInstances, ModelDefinitions);			
		UpdateInstanceSpecialization(ListAllInstances, Model, inferredModel, UploadApp.getBaseRepository().getNameSpace());			
    }
	
	//Check the validity of this method
	public static void updateAddingToLists(String instanceURI) throws InconsistentOntologyException, OKCoExceptionInstanceFormat
	{							
		System.out.println("Updating and Adding to Lists()...");
		InfModel inferredModel = UploadApp.getInferredModel();
		OntModel Model = UploadApp.getBaseModel();
	    //Get model definitions on list of instances	    	
		List<DtoDefinitionClass> intanceDefinitions = DtoQueryUtil.getClassDefinitions(inferredModel, instanceURI);
		ModelDefinitions.addAll(intanceDefinitions);			
		// Organize data (Update the list of all instances)			
		UpdateInstanceAndRelations(Model, UploadApp.getBaseRepository().getNameSpace(), ListAllInstances, intanceDefinitions);			
		UpdateInstanceSpecialization(ListAllInstances, Model, inferredModel, UploadApp.getBaseRepository().getNameSpace());			
	}
	

	public static DtoInstance getInstance(List<DtoInstance> listInstances, String instanceName) {		
		
		for (DtoInstance instance : listInstances) {
			System.out.println("Comparing: "+instance.ns + instance.name);
			System.out.println("With: "+instanceName);
			if((instance.ns + instance.name).equals(instanceName))
			{
				return instance;
			}
		}
		
		return null;
	}
	
	public static void UpdateInstanceAndRelations(OntModel model, String ns, List<DtoInstance> listInstances, List<DtoDefinitionClass> dtoRelationsList)
	{		
		System.out.println("\nManager Instances: updating instance and relations()...");
		for (DtoDefinitionClass dto : dtoRelationsList)
		{			
			List<String> listInstancesOfDomain =QueryUtil.getIndividualsURI(model, dto.Source);
			if(listInstancesOfDomain.size() > 0)	//Check if are need to create
			{
				for (String instanceName : listInstancesOfDomain)
				{					
					//---SOME---//
					
					if(dto.TypeCompletness.equals(OntCardinalityEnum.SOME))
					{
						boolean existInstanceTarget = QueryUtil.existsIndividualsAtPropertyRange(model, instanceName, dto.Relation, dto.Target);
						if(existInstanceTarget)
						{
							//Do nothing
							
						} else {
							
							//Check if individual already exist in list
							DtoInstance instance = getInstance(listInstances, instanceName);
							if(instance == null)
							{
								ArrayList<String> listClasses = new ArrayList<String>();
								listClasses.add(dto.Source);
								instance = new DtoInstance(ns, instanceName.replace(ns, ""), listClasses, QueryUtil.getIndividualsURIDifferentFrom(model, instanceName), QueryUtil.getIndividualsURISameAs(model, instanceName), true);
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListSome);
								if(!existDto)
								{
									instance.ListSome.add(dto);
								}
								listInstances.add(instance);
				
							} else {
								
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListSome);
								if(!existDto)
								{
									instance.ListSome.add(dto);
								}								
							}
						}
					}
					
					//---MIN---//
					
					if(dto.TypeCompletness.equals(OntCardinalityEnum.MIN))
					{
						int quantityInstancesTarget = QueryUtil.countIndividualsURIAtPropertyRange(model, instanceName, dto.Relation, dto.Target);
						if (quantityInstancesTarget < Integer.parseInt(dto.Cardinality))	//Min restriction
						{
							DtoInstance instance = getInstance(listInstances, instanceName);
							if(instance == null)
							{
								ArrayList<String> listClasses = new ArrayList<String>();
								listClasses.add(dto.Source);
								instance = new DtoInstance(ns, instanceName.replace(ns, ""), listClasses, QueryUtil.getIndividualsURIDifferentFrom(model, instanceName), QueryUtil.getIndividualsURISameAs(model, instanceName),true);
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListMin);
								if(!existDto)
								{
									instance.ListMin.add(dto);
								}
								listInstances.add(instance);
				
							} else {
								
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListMin);
								if(!existDto)
								{
									instance.ListMin.add(dto);
								}	
							}
						}
					}
					
					//---MAX---//
					
					if(dto.TypeCompletness.equals(OntCardinalityEnum.MAX))
					{
						int quantityInstancesTarget = QueryUtil.countIndividualsURIAtPropertyRange(model, instanceName, dto.Relation, dto.Target);
						if (quantityInstancesTarget > Integer.parseInt(dto.Cardinality))	//Max restriction
						{
							DtoInstance instance = getInstance(listInstances, instanceName);
							if(instance == null)
							{
								ArrayList<String> listClasses = new ArrayList<String>();
								listClasses.add(dto.Source);
								instance = new DtoInstance(ns, instanceName.replace(ns, ""), listClasses, QueryUtil.getIndividualsURIDifferentFrom(model, instanceName), QueryUtil.getIndividualsURISameAs(model, instanceName),true);
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListMax);
								if(!existDto)
								{
									instance.ListMax.add(dto);
								}
								listInstances.add(instance);
				
							} else {
								
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListMax);
								if(!existDto)
								{
									instance.ListMax.add(dto);
								}	
							}
						}
					}
					
					//---EXACLTY---//
					
					if(dto.TypeCompletness.equals(OntCardinalityEnum.EXACTLY))
					{
						int quantityInstancesTarget =QueryUtil.countIndividualsURIAtPropertyRange(model, instanceName, dto.Relation, dto.Target);
						if (quantityInstancesTarget != Integer.parseInt(dto.Cardinality))	//Exactly restriction
						{
							DtoInstance instance = getInstance(listInstances, instanceName);
							if(instance == null)
							{
								ArrayList<String> listClasses = new ArrayList<String>();
								listClasses.add(dto.Source);
								instance = new DtoInstance(ns, instanceName.replace(ns, ""), listClasses, QueryUtil.getIndividualsURIDifferentFrom(model, instanceName), QueryUtil.getIndividualsURISameAs(model, instanceName),true);
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListExactly);
								if(!existDto)
								{
									instance.ListExactly.add(dto);
								}
								listInstances.add(instance);
				
							} else {
								
								boolean existDto = DtoDefinitionClass.existDto(dto, instance.ListExactly);
								if(!existDto)
								{
									instance.ListExactly.add(dto);
								}	
							}
						}
					}
					
					//---COMPLETE---//
				}
			}			
		}	
	}
	
	public static void UpdateInstanceSpecialization(List<DtoInstance> listAllInstances, OntModel model, InfModel infModel, String ns) {
		
		System.out.println("\nManager Instances: updating instance specialization()...");
		//update and check specialization class for all instances one by one		
		
		for (DtoInstance instanceSelected : listAllInstances) 
		{			
			// ------ Complete classes list ------//
			
			ArrayList<DtoCompleteClass> ListCompleteClsInstaceSelected = new ArrayList<DtoCompleteClass>();
			DtoCompleteClass dto = null;
			
			if(instanceSelected.ListClasses.size() == 1 && instanceSelected.ListClasses.get(0).contains("Thing"))	//Case thing
			{
				//Case if the instance have no class selected - only Thing
				dto = new DtoCompleteClass();
				dto.CompleteClass = instanceSelected.ListClasses.get(0);
				for (String subClas : OntModelAPI.getClassesURI(model)) {
					if(subClas != null)
						dto.AddMember(subClas);
				}
				ListCompleteClsInstaceSelected.add(dto);
				
			} else {
				
				for (String cls : instanceSelected.ListClasses)
				{					
					HashMap<String,List<String>> map = QueryUtil.getCompleteClassesURI(cls, instanceSelected.ListClasses, infModel);
					for(String completeClassURI: map.keySet()){
						DtoCompleteClass dtoCompleteClass = new DtoCompleteClass();
						dtoCompleteClass.setCompleteClass(completeClassURI);
						dtoCompleteClass.addAllMember(map.get(completeClassURI));
						ListCompleteClsInstaceSelected.add(dtoCompleteClass);
					}											
				}
			}
			
			instanceSelected.ListCompleteClasses = ListCompleteClsInstaceSelected;			
			
			// ------ Complete properties list ------//
			
			ArrayList<DtoPropertyAndSubProperties> ListSpecializationProperties = new ArrayList<DtoPropertyAndSubProperties>();
			DtoPropertyAndSubProperties dtoP = null;
			
			//Get instance relations
			List<DtoInstanceRelation> instanceListRelations = new ArrayList<DtoInstanceRelation>();
			List<String> propertiesURIList = QueryUtil.getPropertiesURI(UploadApp.getInferredModel(), instanceSelected.ns + instanceSelected.name);
			for(String propertyURI: propertiesURIList){
				DtoInstanceRelation dtoItem = new DtoInstanceRelation();
			    dtoItem.Property = propertyURI;			      
			    List<String> ranges = QueryUtil.getRangeURIs(UploadApp.getInferredModel(), propertyURI);;
			    if (ranges!=null && ranges.size()>0) dtoItem.Target = ranges.get(0);
			    else dtoItem.Target = "";
			    instanceListRelations.add(dtoItem);
			}
			
			for (DtoInstanceRelation dtoInstanceRelation : instanceListRelations) 
			{			
				List<String> subPropertiesWithDomainAndRange = QueryUtil.getSubPropertiesURIExcluding(infModel,instanceSelected.ns + instanceSelected.name, dtoInstanceRelation.Property, dtoInstanceRelation.Target, propertiesURIList);

				if(subPropertiesWithDomainAndRange.size() > 0)
				{
					dtoP = new DtoPropertyAndSubProperties();
					dtoP.Property = dtoInstanceRelation.Property;
					dtoP.iTargetNs = dtoInstanceRelation.Target.split("#")[0] + "#";
					dtoP.iTargetName = dtoInstanceRelation.Target.split("#")[1];
					dtoP.propertyType = QueryUtil.getPropertyURIType(infModel, dtoInstanceRelation.Property);
					
					for (String sub : subPropertiesWithDomainAndRange) 
					{
						boolean ok = true;
						
						List<String> distointSubPropOfProp = QueryUtil.getPropertiesURIDisjointWith(infModel,sub);
						for (String disjointrop : distointSubPropOfProp) {
							
							for (DtoInstanceRelation dtoWithRelation : instanceListRelations) {
								if(dtoWithRelation.Property.equals(disjointrop)) // instance have this sub relation
								{
									ok = false;
									break;
								}
							}
						}
						
						for (DtoInstanceRelation dtoWithRelation : instanceListRelations) {
						
							if(dtoWithRelation.Property.equals(sub)) // instance have this sub relation
							{
								ok = false;
								break;
							}
						}						
						
						
						if(ok == true)
						{
							dtoP.SubProperties.add(sub);
						}
					}
					
					if(dtoP.SubProperties.size() > 0)
						ListSpecializationProperties.add(dtoP);
				}			
			}
			
			instanceSelected.ListSpecializationProperties = ListSpecializationProperties;						
		}
		
	}
	
	public static OntModel ClassifyInstanceAuto(OntModel model, InfModel infModel, DtoInstance instance) {
		
		/* Check the subclasses are disjoint and complete */
		for (DtoCompleteClass dto : instance.ListCompleteClasses) 
		{
			boolean isDisjoint = true;
			for (String subCls : dto.Members)
			{
				for (String subCls2 : dto.Members) 
				{
					if(! subCls.equals(subCls2))
					{
						boolean result = QueryUtil.isClassesURIDisjoint(infModel, subCls, subCls2); /* Return true if subCls is disjoint of subCls2 */
						if(result == true)
						{
							//Not disjoint
							isDisjoint = false;
							
						} else {
							
							//isDisjoint = true;
						}
					}
				}
				
				if(isDisjoint == false)
				{
					break;
				}
			}
			
			if(isDisjoint == true && dto.Members.size() > 0 )
			{
				//Classify random
				model = FactoryUtil.createIndividualOfClass(model, instance.ns + instance.name, dto.Members.get(0));
			}
		}
		
		
		return model;
		
	}

	public static OntModel CompleteInstanceAuto(DtoInstance instance, String modelNameSpace, OntModel model, InfModel infModel, List<DtoInstance> ListAllInstances)
	{
		//Classify instance classes
		model = ClassifyInstanceAuto(model, infModel, instance);
		
		//complete relations
		for (DtoDefinitionClass dto : instance.ListSome) 
		{
			if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
			{
				//create the the new instance
				String instanceName = dto.Target.split("#")[1] + "-" + (QueryUtil.getIndividualsURI(infModel, dto.Target).size() + 1);
				ArrayList<String> listSame = new ArrayList<String>();		  
				ArrayList<String> listDif = new ArrayList<String>();
				ArrayList<String> listClasses = new ArrayList<String>();
				DtoInstance newInstance = new DtoInstance(modelNameSpace, instanceName, listClasses, listDif, listSame, false);
				
				model = DtoFactoryUtil.createIndividual(model, newInstance, instance.ns + instance.name, dto);
			}
		}
		for (DtoDefinitionClass dto : instance.ListMin) 
		{
			if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
			{
				int quantityInstancesTarget = QueryUtil.countIndividualsURIAtPropertyRange(infModel, instance.ns + instance.name, dto.Relation, dto.Target);
				
				ArrayList<String> listDif = new ArrayList<String>();
				while(quantityInstancesTarget < Integer.parseInt(dto.Cardinality))
				{
					//create the the new instance
					String instanceName = dto.Target.split("#")[1] + "-" + (quantityInstancesTarget + 1);
					ArrayList<String> listSame = new ArrayList<String>();		  
					ArrayList<String> listClasses = new ArrayList<String>();
					DtoInstance newInstance = new DtoInstance(modelNameSpace, instanceName, listClasses, listDif, listSame, false);
					
					model = DtoFactoryUtil.createIndividual(model, newInstance, instance.ns + instance.name, dto);				
					listDif.add(newInstance.ns + newInstance.name);
					quantityInstancesTarget ++;
				}
			}
					
		}
		for (DtoDefinitionClass dto : instance.ListExactly) 
		{
			if(dto.PropertyType.equals(OntPropertyEnum.OBJECT_PROPERTY))
			{
				int quantityInstancesTarget = QueryUtil.countIndividualsURIAtPropertyRange(infModel, instance.ns + instance.name, dto.Relation, dto.Target);
				
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
						DtoInstance newInstance = new DtoInstance(modelNameSpace, instanceName, listClasses, listDif, listSame, false);
						
						model = DtoFactoryUtil.createIndividual(model, newInstance, instance.ns + instance.name, dto);				
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
		
		
		return model;
	}
}
