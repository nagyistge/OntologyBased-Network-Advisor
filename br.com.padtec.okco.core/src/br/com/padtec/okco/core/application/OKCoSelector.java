package br.com.padtec.okco.core.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.padtec.common.dto.DataPropertyValue;
import br.com.padtec.common.dto.DtoCompleteClass;
import br.com.padtec.common.dto.DtoDefinitionClass;
import br.com.padtec.common.dto.DtoGetPrevNextSpecProperty;
import br.com.padtec.common.dto.DtoInstance;
import br.com.padtec.common.dto.DtoInstanceRelation;
import br.com.padtec.common.dto.DtoPropertyAndSubProperties;
import br.com.padtec.common.dto.DtoStatus;
import br.com.padtec.common.factory.DtoFactoryUtil;
import br.com.padtec.common.queries.DtoQueryUtil;
import br.com.padtec.common.queries.QueryUtil;
import br.com.padtec.common.types.OntCardinalityEnum;
import br.com.padtec.okco.core.exception.OKCoNameSpaceException;

import com.hp.hpl.jena.rdf.model.InfModel;

public class OKCoSelector {
	
	protected OKCoUploader repository; 
	
	public OKCoSelector(OKCoUploader repository)
	{
		this.repository = repository;
	}	
	
	//Keeps the list of individuals that were modified
	public List<String> modifiedIndividualsURIs = new ArrayList<String>();
	
	//Keeps the individual that was selected with other useful informations
	public DtoInstance individualSelected;
	public DtoDefinitionClass definitionClassSelected;
	public List<DtoCompleteClass> completeClassesFromSelected = new ArrayList<DtoCompleteClass>();
	public List<DtoPropertyAndSubProperties> relationSpecializationsFromSelected = new ArrayList<DtoPropertyAndSubProperties>();	
		
	/**
	 * Select a particular individual to be used later on.
	 * 
	 * @param individualURI
	 * @return
	 */
	public DtoInstance selectIndividual(String individualURI)
	{
		individualSelected = DtoQueryUtil.getIndividualByName(repository.getInferredModel(), individualURI, true, true, true);
		return individualSelected;
	}
	
	public DtoInstance selectIndividual(String individualURI, boolean loadCardinalityDefinitions)
	{
		individualSelected = DtoQueryUtil.getIndividualByName(repository.getInferredModel(), individualURI, true, true, true);		
		if(loadCardinalityDefinitions)
		{
			getSomeDefinitionsFromSelected();
			getMinDefinitionsFromSelected();
			getMaxDefinitionsFromSelected();
			getExactDefinitionsFromSelected();
		}		
		return individualSelected;
	}
	
	/**
	 * Select a individual to be used later on.
	 * 
	 * @param newSelectedIndividual
	 */
	public void selectIndividual(DtoInstance newSelectedIndividual)
	{
		individualSelected = newSelectedIndividual;
	}
	
	/**
	 * Select a particular cardinality definition to be used later on.
	 * 
	 * @param uriProperty
	 * @return
	 */
	public DtoDefinitionClass selectDefinitionFromSelected(String uriProperty)
	{
		/** SOME */
		List<DtoDefinitionClass> someList = getSomeDefinitionsFromSelected();		
		definitionClassSelected = DtoFactoryUtil.getDefinitionFrom(someList, uriProperty);
		
		/** MIN */		
		List<DtoDefinitionClass> minList = getMinDefinitionsFromSelected();
		if(definitionClassSelected == null) definitionClassSelected = DtoFactoryUtil.getDefinitionFrom(minList, uriProperty);
		
		/** MAX */
		List<DtoDefinitionClass> maxList = getMaxDefinitionsFromSelected();
		if(definitionClassSelected == null) definitionClassSelected = DtoFactoryUtil.getDefinitionFrom(maxList, uriProperty);
		
		/** EXACTLY */
		List<DtoDefinitionClass> exactList = getExactDefinitionsFromSelected();
		if(definitionClassSelected == null)	definitionClassSelected = DtoFactoryUtil.getDefinitionFrom(exactList, uriProperty);
		
		return definitionClassSelected;
	}
			
	public void setSelectedToModified()
	{
		setIsModified(getSelectedIndividualURI());
	}
	
	public void setIsModified(String individualURI)
	{
		if(!modifiedIndividualsURIs.contains(individualURI)) modifiedIndividualsURIs.add(individualURI);
	}
	
	public void clearModified()
	{
		modifiedIndividualsURIs.clear();
	}
	
	public void clearSelected() 
	{		
		definitionClassSelected=null;
		individualSelected=null;
		completeClassesFromSelected.clear();		
		relationSpecializationsFromSelected.clear();		
	}

	public List<String> getModifiedIndividuals()
	{
		return modifiedIndividualsURIs;
	}
		
	/**
	 * The selected cardinality definition.
	 * 
	 * @return
	 */
	public DtoDefinitionClass getSelectedClassDefinition()
	{
		return definitionClassSelected;
	}
		
	/**
	 * The selected individual
	 */
	public DtoInstance getSelectedIndividual()
	{
		return individualSelected;
	}
	
	/**
	 * The selected individual URI if any. Otherwise it returns null.
	 * 
	 * @return
	 */
	public String getSelectedIndividualURI()
	{
		if(individualSelected!=null) return individualSelected.ns+individualSelected.name;
		else return null;
	}
			
	/**
	 * List of all SOME cardinality restrictions from the selected individual.
	 * 
	 * @return
	 */
	public List<DtoDefinitionClass> getSomeDefinitionsFromSelected()
	{
		if(individualSelected!=null) 
		{
			if(individualSelected.ListSome.size()==0) setDefinitionsInSelected(OntCardinalityEnum.SOME);
			return individualSelected.getSomeDefinitionWithNoRepetition();
		}		
		else return new ArrayList<DtoDefinitionClass>();
	}
	
	/**
	 * List of all MAX cardinality restrictions from the selected individual.
	 * 
	 * @return
	 */
	public List<DtoDefinitionClass> getMaxDefinitionsFromSelected()
	{
		if(individualSelected!=null) 
		{
			if(individualSelected.ListMax.size()==0) setDefinitionsInSelected(OntCardinalityEnum.MAX);
			return individualSelected.getMaxDefinitionWithNoRepetition();
		}
		else return new ArrayList<DtoDefinitionClass>();
	}
	
	/**
	 * List of all MIN cardinality restrictions from the selected individual.
	 * 
	 * @return
	 */
	public List<DtoDefinitionClass> getMinDefinitionsFromSelected()
	{
		if(individualSelected!=null) 
		{
			if(individualSelected.ListMin.size()==0) setDefinitionsInSelected(OntCardinalityEnum.MIN);
			return individualSelected.getMinDefinitionWithNoRepetition();
		}
		else return new ArrayList<DtoDefinitionClass>();
	}
	
	/**
	 * List of all EXACT cardinality restrictions from the selected individual.
	 * 
	 * @return
	 */
	public List<DtoDefinitionClass> getExactDefinitionsFromSelected()
	{
		if(individualSelected!=null) 
		{
			if(individualSelected.ListExactly.size()==0) setDefinitionsInSelected(OntCardinalityEnum.EXACTLY);
			return individualSelected.getExactDefinitionWithNoRepetition();
		}
		else return new ArrayList<DtoDefinitionClass>();
		
	}
	
	/**
	 * List of all relations of the selected individual.
	 * 
	 * @return
	 */
	public List<DtoInstanceRelation> getRelationsFromSelected()
	{
		if(individualSelected!=null) 
		{
			return DtoQueryUtil.getRelationsFrom(repository.getInferredModel(),getSelectedIndividualURI());			
		}
		else return new ArrayList<DtoInstanceRelation>();
	}
	
	/**
	 * List of all the relation specializations (relations and sub-relations) from the selected individual.
	 * 
	 * @return
	 */
	public List<DtoPropertyAndSubProperties> getRelationSpecializationsFromSelected()
	{
		if(individualSelected!=null) 
		{
			if(individualSelected.ListSpecializationProperties.size()==0) setRelationSpecializationsInSelected();
			relationSpecializationsFromSelected = individualSelected.getSpecializationProperties();
			return relationSpecializationsFromSelected;
		}
		else return new ArrayList<DtoPropertyAndSubProperties>();
	}
	
	/**
	 * List of all classes that are complete from the selected individual.
	 * 
	 * @return
	 */
	public List<DtoCompleteClass> getCompleteClassesFromSelected()
	{
		if(individualSelected!=null) 
		{
			if(individualSelected.ListCompleteClasses.size()==0) setClassSpecializationsInSelected();
			completeClassesFromSelected = individualSelected.getCompleteClasses();
			return completeClassesFromSelected;
		}
		else return new ArrayList<DtoCompleteClass>();
	}
		
	/**
	 * List of individuals at the range of the cardinality restriction selected.
	 * 
	 * @return
	 */
	public List<DtoInstance> getIndividualsAtClassDefinitionRangeSelected()
	{
		InfModel model = repository.getInferredModel();
		List<DtoInstance> dtoIndividuals = DtoQueryUtil.getIndividualsAtObjectPropertyRange(model, getSelectedIndividualURI(), definitionClassSelected.Relation, definitionClassSelected.Target);
		return dtoIndividuals;
	}
	
	/**
	 * List of data values at the range of the cardinality restriction selected.
	 * 
	 * @return
	 */
	public List<DataPropertyValue> getDataValuesAtClassDefinitionRangeSelected()
	{
		List<DataPropertyValue> result = new ArrayList<DataPropertyValue>();		
		InfModel model = repository.getInferredModel();
		List<String> individualsList = QueryUtil.getIndividualsURIAtDataTypePropertyRange(model, getSelectedIndividualURI(), definitionClassSelected.Relation, definitionClassSelected.Target);
		//List<String> individualsList = QueryUtil.getIndividualsURIAtObjectPropertyRange(model, getSelectedIndividualURI(), definitionClassSelected.Relation, definitionClassSelected.Target);
		for(String individualURI: individualsList)
		{
			DataPropertyValue data = new DataPropertyValue();
			data.value = individualURI.split("\\^\\^")[0];
			data.classValue = definitionClassSelected.Target;
			data.existInModel = true;
			result.add(data);
		}
		return result;
	}
		
	/** 
	 * Return the list of all individuals from the ontology.
	 * It returns also all the classes of an individual as well as all the other individuals different and the same as this one.
	 *  
	 * @throws OKCoNameSpaceException
	 * 
	 * @author John Guerson
	 * 
	 * @param model
	 * @param clsEager Defines when the classes of an individual must be got eagerly 
	 * @param diffFromEager Defines when the "different from individuals" of an individual must be got eagerly
	 * @param sameAsEager Defines when the "same as individuals" of an individual must be got eagerly
	 */
	public List<DtoInstance> getIndividuals(Boolean classesEager, Boolean diffFromEager, Boolean sameAsEager)
	{
		return DtoQueryUtil.getIndividuals(repository.getInferredModel(), classesEager, diffFromEager, sameAsEager);
	}

	/**
	 * Get the Property from Selected alongside the next and previous properties.
	 */
	public DtoGetPrevNextSpecProperty getPropertyWithNextAndPreviousFromSelected(String propertyURI)
	{
		DtoPropertyAndSubProperties dto = DtoFactoryUtil.getPropertyFrom(relationSpecializationsFromSelected, propertyURI);
		if(dto == null) return null;			
		boolean haveNext = false;
		boolean havePrev = false;
		DtoPropertyAndSubProperties dtoNext =  DtoFactoryUtil.getPropertyFrom(relationSpecializationsFromSelected, relationSpecializationsFromSelected.get(relationSpecializationsFromSelected.indexOf(dto)+1).Property);
		DtoPropertyAndSubProperties dtoPrev=  DtoFactoryUtil.getPropertyFrom(relationSpecializationsFromSelected, relationSpecializationsFromSelected.get(relationSpecializationsFromSelected.indexOf(dto)-1).Property);
		if(dtoNext != null) haveNext = true;
		if(dtoPrev != null) havePrev = true;
		DtoGetPrevNextSpecProperty data = new DtoGetPrevNextSpecProperty(individualSelected.name, individualSelected.ns, dto, haveNext, havePrev);				  
		return data;
	}
	
	/**
	 * Update and check specialization class for all instances one by one
	 */
	private void setClassSpecializationsInSelected()
	{
		InfModel model = repository.getInferredModel();
		
		// ------ Complete classes list ------//		
		ArrayList<DtoCompleteClass> completeClassesList = new ArrayList<DtoCompleteClass>();
		DtoCompleteClass dto = null;		
		if(individualSelected.ListClasses.size() == 1 && individualSelected.ListClasses.get(0).contains("Thing"))	//Case thing
		{
			//Case if the instance have no class selected - only Thing
			dto = new DtoCompleteClass();
			dto.CompleteClass = individualSelected.ListClasses.get(0);			
			for (String subClas : QueryUtil.getClassesURI(model)) 
			{
				if(subClas != null) dto.AddMember(subClas);
			}
			completeClassesList.add(dto);			
		} else {			
			for (String cls : individualSelected.ListClasses)
			{					
				HashMap<String,List<String>> map = QueryUtil.getCompleteClassesURI(cls, individualSelected.ListClasses, model);
				for(String completeClassURI: map.keySet())
				{
					DtoCompleteClass dtoCompleteClass = new DtoCompleteClass();
					dtoCompleteClass.setCompleteClass(completeClassURI);
					dtoCompleteClass.addAllMember(map.get(completeClassURI));
					completeClassesList.add(dtoCompleteClass);
				}											
			}
		}		
		individualSelected.ListCompleteClasses = completeClassesList;		
	}

	/**
	 * 	Update and check specialization class for all instances one by one
	 */
	private void setRelationSpecializationsInSelected() 
	{
		InfModel model = repository.getInferredModel();	
		
		ArrayList<DtoPropertyAndSubProperties> completePropertiesList = new ArrayList<DtoPropertyAndSubProperties>();
		DtoPropertyAndSubProperties dtoP = null;		
		//Get instance relations
		//List<DtoInstanceRelation> instanceListRelations = new ArrayList<DtoInstanceRelation>();
		List<DtoInstanceRelation> instanceListRelations = QueryUtil.getPropertiesAndIndividualsURI(repository.getInferredModel(), individualSelected.ns + individualSelected.name);
		
		List<String> propertiesURIList = QueryUtil.getPropertiesURI(repository.getInferredModel(), individualSelected.ns + individualSelected.name);
		/*
		for(String propertyURI: propertiesURIList){
			DtoInstanceRelation dtoItem = new DtoInstanceRelation();
		    dtoItem.Property = propertyURI;			      
		    List<String> ranges = QueryUtil.getRangeIndividualURI(model, individualSelected.ns + individualSelected.name, propertyURI);
		    //List<String> ranges = QueryUtil.getRangeURIs(UploadApp.getInferredModel(), propertyURI);
		    if (ranges!=null && ranges.size()>0) dtoItem.Target = ranges.get(0);
		    else dtoItem.Target = "";
		    instanceListRelations.add(dtoItem);
		}
		*/
		for (DtoInstanceRelation dtoInstanceRelation : instanceListRelations) 
		{	
			List<String> subPropertiesWithDomainAndRange = QueryUtil.getSubPropertiesURIExcluding(model,individualSelected.ns + individualSelected.name, dtoInstanceRelation.Property, dtoInstanceRelation.Target, propertiesURIList);
			if(subPropertiesWithDomainAndRange.size() > 0)
			{
				dtoP = new DtoPropertyAndSubProperties();
				dtoP.Property = dtoInstanceRelation.Property;
				String target = dtoInstanceRelation.Target;
				if(target != null && !target.equals(""))
				{
					dtoP.iTargetNs = target.split("#")[0] + "#";
					dtoP.iTargetName = target.split("#")[1];
				}
				dtoP.propertyType = QueryUtil.getPropertyURIType(model, dtoInstanceRelation.Property);				
				for (String sub : subPropertiesWithDomainAndRange) 
				{
					boolean ok = true;					
					List<String> distointSubPropOfProp = QueryUtil.getPropertiesURIDisjointWith(model,sub);
					for (String disjointrop : distointSubPropOfProp) 
					{						
						for (DtoInstanceRelation dtoWithRelation : instanceListRelations) 
						{
							if(dtoWithRelation.Property.equals(disjointrop)) // instance have this sub relation
							{
								ok = false;
								break;
							}
						}
					}					
					for (DtoInstanceRelation dtoWithRelation : instanceListRelations) 
					{					
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
				if(dtoP.SubProperties.size() > 0) completePropertiesList.add(dtoP);
			}			
		}		
		individualSelected.ListSpecializationProperties = completePropertiesList;	
	}

	private void setDefinitionsInSelected(OntCardinalityEnum typeCompletness)
	{
		InfModel model = repository.getInferredModel();
		
		for (String classURI : individualSelected.ListClasses) 
		{
			List<DtoDefinitionClass> definitions;
			if(typeCompletness.equals(OntCardinalityEnum.SOME)){
				definitions = DtoQueryUtil.getSomeCardinalityDefinitionsFrom(model, classURI);
			}else{
				definitions = DtoQueryUtil.getCardinalityDefinitionsFrom(model, classURI, typeCompletness);
			}			
			
			for (DtoDefinitionClass def : definitions) {
				switch (typeCompletness) {
					case SOME:
						if(!individualSelected.ListSome.contains(def))
						{
							def.status = getStatus(individualSelected,def);
							individualSelected.ListSome.add(def);
						}
						break;
					case MIN:
						if(!individualSelected.ListMin.contains(def))
						{
							def.status = getStatus(individualSelected,def);
							individualSelected.ListMin.add(def);
						}
						break;
					case MAX:
						if(!individualSelected.ListMax.contains(def))
						{
							def.status = getStatus(individualSelected,def);
							individualSelected.ListMax.add(def);
						}
						break;
					case EXACTLY:
						if(!individualSelected.ListExactly.contains(def))
						{
							def.status = getStatus(individualSelected,def);
							individualSelected.ListExactly.add(def);
						}
						break;
					case COMPLETE:
						
						break;
				}				
			}
		}		
	}
	
	private DtoStatus getStatus(DtoInstance dtoIndividual, DtoDefinitionClass dtoDefinition)
	{
		if(dtoDefinition.TypeCompletness==OntCardinalityEnum.MIN)
		{
			Integer number = Integer.parseInt(dtoDefinition.Cardinality);
			List<String> individuals = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), dtoIndividual.uri, dtoDefinition.Relation);
			if(individuals.size()<number) return DtoStatus.NOT_SATISFIED;
			else return DtoStatus.SATISFIED;
		}
		if(dtoDefinition.TypeCompletness==OntCardinalityEnum.SOME)
		{			
			List<String> individuals = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), dtoIndividual.uri, dtoDefinition.Relation);
			if(individuals.size()==0) return DtoStatus.NOT_SATISFIED;
			else return DtoStatus.SATISFIED;
		}
		if(dtoDefinition.TypeCompletness==OntCardinalityEnum.EXACTLY)
		{			
			Integer number = Integer.parseInt(dtoDefinition.Cardinality);
			List<String> individuals = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), dtoIndividual.uri, dtoDefinition.Relation);
			if(individuals.size()!=number) return DtoStatus.NOT_SATISFIED;
			else return DtoStatus.SATISFIED;
		}
		return DtoStatus.NOT_SATISFIED;
	}
	
}
