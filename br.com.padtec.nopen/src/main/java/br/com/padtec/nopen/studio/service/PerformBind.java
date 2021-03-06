package br.com.padtec.nopen.studio.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import br.com.padtec.common.factory.FactoryUtil;
import br.com.padtec.common.queries.QueryUtil;
import br.com.padtec.common.util.Util;
import br.com.padtec.nopen.model.ConceptEnum;
import br.com.padtec.nopen.model.DtoJointElement;
import br.com.padtec.nopen.model.RelationEnum;
import br.com.padtec.nopen.service.ContainerStructure;
import br.com.padtec.nopen.service.NOpenComponents;
import br.com.padtec.nopen.service.NOpenLog;
import br.com.padtec.nopen.service.util.NOpenQueryUtil;
import br.com.padtec.okco.core.application.OKCoUploader;

public class PerformBind {
	private static PerformBind instance = new PerformBind();
	
	public static boolean applyBindsWithoutVerification(DtoJointElement dtoSourceElement, DtoJointElement dtoTargetElement, OKCoUploader repository) throws Exception {
		String nameSource = dtoSourceElement.getName();
		String typeSource = dtoSourceElement.getType();
		String nameTarget = dtoTargetElement.getName();
		String idSource = dtoSourceElement.getId();
		String idTarget = dtoTargetElement.getId();
		String typeTarget = dtoTargetElement.getType();
		String property =  RelationEnum.binds.toString();
		
		
		if(typeTarget.equals(ConceptEnum.Output_Card.toString()) || typeTarget.equals(ConceptEnum.Input_Card.toString())){
			String idPort = Util.generateUUID();
			String typePort = typeSource + "_" + typeTarget.substring(0, typeTarget.indexOf("_"));
			String relation = RelationEnum.componentOf.toString();
			
			//cria portas
			FactoryUtil.createInstanceIndividual(
					repository.getBaseModel(), 
					repository.getNamespace() + idPort, 
					repository.getNamespace() + typePort,
					false
				);
			
			FactoryUtil.createInstanceIndividual(
					repository.getBaseModel(), 
					repository.getNamespace() + idTarget, 
					repository.getNamespace() + typeTarget,
					false
				);
			
			//specific componentOf between port and tf
			ArrayList<String> specificPropertyURIs = QueryUtil.getRelationsBetweenClasses(NOpenComponents.nopenRepository.getBaseModel(), NOpenComponents.nopenRepository.getNamespace() + typeSource, NOpenComponents.nopenRepository.getNamespace() + typePort, NOpenComponents.nopenRepository.getNamespace() + relation);
			String specificComponentOf = specificPropertyURIs.get(0);
			specificComponentOf = specificComponentOf.substring(specificComponentOf.indexOf("#")+1);
			
			FactoryUtil.createInstanceRelation(
					repository.getBaseModel(), 
					repository.getNamespace() + idSource, 
					repository.getNamespace() + specificComponentOf,
					repository.getNamespace() + idPort,
					false,
					false,
					false
				);
			
			
			List<String> superTypes = QueryUtil.getAllSupertypesURIs(NOpenComponents.nopenRepository.getBaseModel(), NOpenComponents.nopenRepository.getNamespace() + typePort);
			String superTypePort = superTypes.get(0).substring(superTypes.get(0).indexOf("#")+1);
			specificPropertyURIs = QueryUtil.getRelationsBetweenClasses(NOpenComponents.nopenRepository.getBaseModel(), NOpenComponents.nopenRepository.getNamespace() + typeTarget, NOpenComponents.nopenRepository.getNamespace() + superTypePort, NOpenComponents.nopenRepository.getNamespace() + property);
			String specificBinds = specificPropertyURIs.get(0);
			specificBinds = specificBinds.substring(specificBinds.indexOf("#")+1);
			
			
			FactoryUtil.createInstanceRelation(
					repository.getBaseModel(), 
					repository.getNamespace() + idTarget, 
					repository.getNamespace() + specificBinds,
					repository.getNamespace() + idPort,
					false,
					false,
					false
				);
			
			//create the relation between tf and input/output card

			superTypes = QueryUtil.getTopSupertypesURIs(NOpenComponents.nopenRepository.getBaseModel(), NOpenComponents.nopenRepository.getNamespace() + typeSource);
			String superTypeTf = superTypes.get(0).substring(superTypes.get(0).indexOf("#")+1);
			
			specificPropertyURIs = QueryUtil.getRelationsBetweenClasses(NOpenComponents.nopenRepository.getBaseModel(), NOpenComponents.nopenRepository.getNamespace() + typeTarget, NOpenComponents.nopenRepository.getNamespace() + superTypeTf, NOpenComponents.nopenRepository.getNamespace() + RelationEnum.is_interface_of.toString());
			String specificInterfaceOf = specificPropertyURIs.get(0);
			specificInterfaceOf = specificInterfaceOf.substring(specificInterfaceOf.indexOf("#")+1);

			
			FactoryUtil.createInstanceRelation(
					repository.getBaseModel(), 
					repository.getNamespace() + idTarget, 
					repository.getNamespace() + specificInterfaceOf,
					repository.getNamespace() + idSource,
					false,
					false,
					false
			);
			
			
			//create the componentOf relation between target and the card 
			
			//List<String> card = QueryUtil.getIndividualsURIAtObjectPropertyRange(repository.getBaseModel(), repository.getNamespace() + idSource, repository.getNamespace() + RelationEnum.INV_componentOf.toString(), repository.getNamespace() + ConceptEnum.Card.toString());
			//String cardId = null;
			/*if(!card.isEmpty()){
				cardId = card.get(0).substring(card.get(0).indexOf("#")+1);
			}else{
				ArrayList<String> relationsToGetCard = new ArrayList<String>();
				relationsToGetCard.add(RelationEnum.INV_A_CardLayer_TrailTerminationFunction.toString());
				relationsToGetCard.add(RelationEnum.INV_A_Card_CardLayer.toString());
				ArrayList<String> result = QueryUtil.endOfGraph(repository.getBaseModel(), idSource, relationsToGetCard);
				cardId = result.get(0).substring(result.get(0).indexOf("#")+1);
			}
			*/
			
			specificPropertyURIs = QueryUtil.getRelationsBetweenClasses(NOpenComponents.nopenRepository.getBaseModel(), NOpenComponents.nopenRepository.getNamespace() + ConceptEnum.Card.toString(), NOpenComponents.nopenRepository.getNamespace() + typeTarget, NOpenComponents.nopenRepository.getNamespace() + RelationEnum.componentOf.toString());
			String specificInterfaceComponentOf = specificPropertyURIs.get(0);
			specificInterfaceComponentOf = specificInterfaceComponentOf.substring(specificInterfaceComponentOf.indexOf("#")+1);
			
		/*	FactoryUtil.createInstanceRelation(
					repository.getBaseModel(), 
					repository.getNamespace() + cardId, 
					repository.getNamespace() + specificInterfaceComponentOf,
					repository.getNamespace() + idTarget
			);*/
			
			return true;
			
		} else {
			//Binds entre TFs 
			//First, verify if the ports can have a RP between them
			//if so, create the ports, RP and the relations between them
			
			String typeOutput = typeSource + "_Output";
			String outputId = Util.generateUUID();
			
			String relation_source = null;
			HashMap<String, String> source_componentOfs = new HashMap<String, String>();
			
			String typeInput = typeTarget + "_Input";
			String inputId = Util.generateUUID();
			String relation_target = null;
			HashMap<String, String> target_componentOfs = new HashMap<String, String>();
			
			HashSet<String> rpsBetweenPorts = discoverRPBetweenPorts( typeOutput, typeInput, NOpenComponents.nopenRepository);
			String rpTypeURI = rpsBetweenPorts.iterator().next();
			String rpType = rpTypeURI.substring(rpTypeURI.indexOf("#")+1);
			String rpId = Util.generateUUID();
			
			String rpTypeInNOpen = NOpenComponents.nopenRepository.getNamespace() + rpType;
			String typeOutputNOpen = NOpenComponents.nopenRepository.getNamespace() + typeOutput;
			String typeInputNOpen = NOpenComponents.nopenRepository.getNamespace() + typeInput;
			String propertyOutputRPInNOpen = NOpenComponents.nopenRepository.getNamespace() + RelationEnum.links_output.toString();
			String propertyInputRPInNOpen = NOpenComponents.nopenRepository.getNamespace() + RelationEnum.links_input.toString();
			
			ArrayList<String> relationOutRpInNOpen = QueryUtil.getRelationsBetweenClasses(NOpenComponents.nopenRepository.getBaseModel(), rpTypeInNOpen, typeOutputNOpen, propertyOutputRPInNOpen);
			ArrayList<String> relationInRpInNOpen = QueryUtil.getRelationsBetweenClasses(NOpenComponents.nopenRepository.getBaseModel(), rpTypeInNOpen, typeInputNOpen, propertyInputRPInNOpen);
			
			String relationRpOut = relationOutRpInNOpen.get(0);
			relationRpOut = relationRpOut.substring(relationRpOut.indexOf("#") + 1);
			
			String relationInRp = relationInRpInNOpen.get(0);
			relationInRp = relationInRp.substring(relationInRp.indexOf("#") + 1);
			
			// create reference point
			FactoryUtil.createInstanceIndividual(
					repository.getBaseModel(), 
					repository.getNamespace() + rpId, 
					repository.getNamespace() + rpType,
					false
				);
			
			//create output
			FactoryUtil.createInstanceIndividual(
					repository.getBaseModel(), 
					repository.getNamespace() + outputId, 
					repository.getNamespace() + typeOutput,
					false
				);

			//create input
			FactoryUtil.createInstanceIndividual(
					repository.getBaseModel(), 
					repository.getNamespace() + inputId, 
					repository.getNamespace() + typeInput,
					false
				);
			
			//create relation between output and reference point
			FactoryUtil.createInstanceRelation(
					repository.getBaseModel(), 
					repository.getNamespace() + rpId, 
					repository.getNamespace() + relationRpOut,
					repository.getNamespace() + outputId,
					false,
					false,
					false
				);
			
			//create relation between input and reference point
			FactoryUtil.createInstanceRelation(
					repository.getBaseModel(), 
					repository.getNamespace() + rpId, 
					repository.getNamespace() + relationInRp,
					repository.getNamespace() + inputId,
					false,
					false,
					false
				);
			
							
			source_componentOfs = NOpenQueryUtil.getAllComponentOFRelations(typeSource, NOpenComponents.nopenRepository.getBaseModel()); 
			target_componentOfs = NOpenQueryUtil.getAllComponentOFRelations(typeTarget, NOpenComponents.nopenRepository.getBaseModel()); 

			if ((source_componentOfs.containsKey(NOpenComponents.nopenRepository.getNamespace() + typeSource + "_Output")) && (target_componentOfs.containsKey(NOpenComponents.nopenRepository.getNamespace() + typeTarget + "_Input"))) {
				relation_source = source_componentOfs.get(NOpenComponents.nopenRepository.getNamespace() + typeSource + "_Output");
				relation_target = target_componentOfs.get(NOpenComponents.nopenRepository.getNamespace() + typeTarget + "_Input");
				
				relation_source = relation_source.replace("http://www.menthor.net/nOpenModel.owl#", repository.getNamespace());
				relation_target = relation_target.replace("http://www.menthor.net/nOpenModel.owl#", repository.getNamespace());
				
				//create the relation between tf and output
				FactoryUtil.createInstanceRelation( 
						repository.getBaseModel(), 
						repository.getNamespace() + idSource, 
						relation_source,
						repository.getNamespace() + outputId,
						false,
						false,
						false
						);		
				
				NOpenLog.appendLine(repository.getName()+":  Output "+outputId+" created at "+ typeSource + ": "+nameSource);
				
				
				//create the relation between tf and input
				FactoryUtil.createInstanceRelation(
						repository.getBaseModel(), 
						repository.getNamespace() + idTarget, 
						relation_target,
						repository.getNamespace() + inputId,
						false,
						false,
						false
				);
				
				
				NOpenLog.appendLine(repository.getName()+":  Input "+inputId+" created at "+ typeTarget + ": "+nameTarget);

				NOpenLog.appendLine("Success: Binds successfully made between (" + typeSource + "::" + nameSource +", " + typeTarget + "::" + nameTarget + ")");

				return true;
			}	
		}

		
		return false;
	}
	
	/*
	 * this method apply binds between tfs and between ports and tfs. 
	 * creates an output for the id_source 
	 * creates an input for the id_target and the component of 
	 * discover the rp for the binds and the component of
	 * 
	 */
	public synchronized static boolean applyBinds(DtoJointElement dtoSourceElement, DtoJointElement dtoTargetElement, String flag, OKCoUploader repository) throws Exception {
			if(flag.equals("ITU")){
				StudioComponents.studioRepository.getReasoner().run(StudioComponents.studioRepository.getBaseModel());
			}
			boolean result = false;
			String nameSource = dtoSourceElement.getName();
			String nameTarget = dtoTargetElement.getName();

			if(canCreateBind(dtoSourceElement, dtoTargetElement, repository)){
				result = applyBindsWithoutVerification(dtoSourceElement, dtoTargetElement, repository);
			}else {
				NOpenLog.appendLine("Error: The Transport Function " + nameSource + "cannot be bound to " + nameTarget);
				throw new Exception("Error: Unexpected bind between " + nameSource + "and " + nameTarget);
			}
		return result;

	}
	
	/*
	 * given two ports discover the rp between them.
	 */
	public static HashSet<String> discoverRPBetweenPorts(String type_output, String type_input, OKCoUploader repository){
		HashSet<String> rp = new HashSet<String>();
		rp = NOpenQueryUtil.discoverRPBetweenPorts(type_output, type_input, repository.getBaseModel());
		
		return rp;
	}
	
	
	/*
	 * verify if the source's layer is client of the target's layer.
	 */
	static boolean isClient(String sourceId, String targetId, OKCoUploader repository){ 

		String tgtClassURI = repository.getNamespace() + ConceptEnum.Card_Layer.toString();
		String relationDownURI = repository.getNamespace() + RelationEnum.intermediates_down_Transport_Function_Card_Layer.toString();
		String relationUpURI = repository.getNamespace() + RelationEnum.intermediates_up_Transport_Function_Card_Layer.toString();
		
		String sourceURI = repository.getNamespace() + sourceId;
		String targetURI = repository.getNamespace() + targetId;
		
		//verifica se o tf_source tem a rela��o de intermediates_down e se o tf_target tem a rela��o de intermediates_up
		boolean tfSourceHasIntermediatesDownRelation = QueryUtil.hasTargetIndividualFromClass(repository.getBaseModel(), sourceURI, relationDownURI, tgtClassURI );
		boolean tfSourceHasIntermediatesUpRelation = QueryUtil.hasTargetIndividualFromClass(repository.getBaseModel(), sourceURI, relationUpURI, tgtClassURI );
		
		boolean tfTargetHasIntermediatesUpRelation = QueryUtil.hasTargetIndividualFromClass(repository.getBaseModel(), targetURI, relationUpURI, tgtClassURI );
		boolean tfTargetHasIntermediatesDownRelation = QueryUtil.hasTargetIndividualFromClass(repository.getBaseModel(), targetURI, relationDownURI, tgtClassURI );
		
		if((!tfSourceHasIntermediatesUpRelation && !tfSourceHasIntermediatesDownRelation) || (!tfTargetHasIntermediatesUpRelation && !tfTargetHasIntermediatesDownRelation)){
			return true;
		}
		else if((tfSourceHasIntermediatesUpRelation && tfTargetHasIntermediatesDownRelation) || (tfSourceHasIntermediatesDownRelation && tfTargetHasIntermediatesUpRelation)){
			//pega o card_layer do tf_source e o card_layer do tf_target
			String cardLayerSource;
			String cardLayerTarget; 
			if(tfSourceHasIntermediatesUpRelation){
				cardLayerSource = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), sourceURI, relationUpURI).get(0);
			} else{
				cardLayerSource = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), sourceURI, relationDownURI).get(0);
			}
			if(tfTargetHasIntermediatesUpRelation){
				cardLayerTarget = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), targetURI, relationUpURI).get(0);
			} else{
				cardLayerTarget = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), targetURI, relationDownURI).get(0);
			}
			
			
			//pega a camada do card_layer do tf_source e a camada do card_layer do tf_target
			String layerSource = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), cardLayerSource, repository.getNamespace() + RelationEnum.instantiates_Card_Layer_Layer_Type.toString()).get(0);
			String layerTarget = QueryUtil.getIndividualsURIAtPropertyRange(repository.getBaseModel(), cardLayerTarget, repository.getNamespace() + RelationEnum.instantiates_Card_Layer_Layer_Type.toString()).get(0);
			
			//pega as rela��es entre as camadas
			ArrayList<String> relationsBetweenLayerSourceAndLayerTarget = new ArrayList<String>();
			relationsBetweenLayerSourceAndLayerTarget = QueryUtil.getRelationsBetweenIndividuals(repository.getBaseModel(), layerSource, layerTarget);
			
			//se entre a camada do tf_source e a camada do tf_target existir a rela��o de is_client, ent�o retorna true
			if(relationsBetweenLayerSourceAndLayerTarget.contains(repository.getNamespace() + RelationEnum.INV_is_client_Layer_Type_Layer_Type.toString())){
				return true;
			}
		}
		return false;
	}
	
	public static boolean canCreateBind(DtoJointElement dtoSourceElement, DtoJointElement dtoTargetElement, OKCoUploader repository ) throws Exception{
		String nameSource = dtoSourceElement.getName();
		String typeSource = dtoSourceElement.getType();
		String idSource = dtoSourceElement.getId();
		String nameTarget = dtoTargetElement.getName();
		String typeTarget = dtoTargetElement.getType();
		String idTarget = dtoTargetElement.getId();
		String sourceURI = repository.getNamespace() + dtoSourceElement.getId();
		String targetURI = repository.getNamespace() + dtoTargetElement.getId();
		
		if(typeTarget.equals(ConceptEnum.Output_Card.toString()) || typeTarget.equals(ConceptEnum.Input_Card.toString())){ //rela��o entre tf e porta de card
			String property = RelationEnum.binds.toString();
			String propertyURI = repository.getNamespace() + property;
			String typePort = typeSource + "_" + typeTarget.substring(0, typeTarget.indexOf("_"));

			String key = NOpenComponents.nopenRepository.getNamespace() + typeTarget + NOpenComponents.nopenRepository.getNamespace() + property + NOpenComponents.nopenRepository.getNamespace() + typePort; 
			String cardinality = BuildBindStructure.getInstance().getBindsTuple().get(key);
			if(cardinality == null){
				NOpenLog.appendLine("Error: The Transport Function " + nameSource + " cannot be bound to " + nameTarget + " because the relation between " + dtoSourceElement.getType() + " and " + dtoTargetElement.getType() + " does not exist.");
				throw new Exception("Error: Unexpected relation between " + nameSource + " and " + nameTarget + " because there is no \"binds\" relation between " + typeSource + " and " + typeTarget);
			}else{
				Integer numberOfAlreadyBoundPorts = QueryUtil.getNumberOfOccurrences(repository.getBaseModel(), targetURI, propertyURI, typeSource );
				Integer cardinalityInputTarget = Integer.parseInt(cardinality);
				if(cardinalityInputTarget == -1 || numberOfAlreadyBoundPorts <= cardinalityInputTarget){
					return true;

				}

			}
		}
		else{ //ent�o, o binds � entre TFs

			String typeOutput = typeSource + "_Output";
			String typeInput = typeTarget + "_Input";
			String property = RelationEnum.binds.toString();
			String propertyURI = repository.getNamespace() + property;
			Integer numberOfAlreadyBoundPorts = QueryUtil.getNumberOfOccurrences(repository.getBaseModel(), sourceURI, propertyURI, typeTarget );
			String key = NOpenComponents.nopenRepository.getNamespace() + typeSource + NOpenComponents.nopenRepository.getNamespace() + property + NOpenComponents.nopenRepository.getNamespace() + typeTarget; 
			String cardinality = BuildBindStructure.getInstance().getBindsTuple().get(key);
			if(cardinality == null){
				NOpenLog.appendLine("Error: The Transport Function " + nameSource + " cannot be bound to " + nameTarget + " because the relation between " + dtoSourceElement.getType() + " and " + dtoTargetElement.getType() + " does not exist.");
				throw new Exception("Error: Unexpected relation between " + nameSource + " and " + nameTarget + " because there is no \"binds\" relation between " + typeSource + " and " + typeTarget);
			}
			Integer cardinalityInputTarget = Integer.parseInt(cardinality);
			
			HashSet<String> rpsBetweenPorts = new HashSet<String>();
			rpsBetweenPorts = discoverRPBetweenPorts( typeOutput, typeInput, NOpenComponents.nopenRepository);
			boolean isClient = false;
			isClient = isClient(idSource, idTarget, repository);
			if(rpsBetweenPorts.size() > 0){
				if((numberOfAlreadyBoundPorts < cardinalityInputTarget) || (cardinalityInputTarget == -1)){
					if(isClient){
						return true;
					} else{
						NOpenLog.appendLine("Error: The Transport Function " + nameSource + " cannot be bound to " + nameTarget + " because the source layer is not client of target layer.");
						throw new Exception("Error: Unexpected relation between " + nameSource + " and " + nameTarget + " because the source layer is not client of target layer.");
					}
				} else {
					NOpenLog.appendLine("Error: The Transport Function " + nameSource + " cannot be bound to " + nameTarget + " because the cardinality of the relation is already maximum.");
					throw new Exception("Error: Unexpected relation between " + nameSource + " and " + nameTarget + " because the cardinality of the relation is already maximum.");
				}
			}
			else{
				NOpenLog.appendLine("Error: The Transport Function " + nameSource + " cannot be bound to " + nameTarget + " because there is no Reference Point between " + dtoSourceElement.getType() + " and " + dtoTargetElement.getType() + " . ");
				throw new Exception("Error: Unexpected relation between " + nameSource + " and " + nameTarget + "  because there is no Reference Point between " + dtoSourceElement.getType() + " and " + dtoTargetElement.getType() + " . ");
			}
		}
		return false;
		
	}
	
	public static void applyEquipmentBinds(DtoJointElement dtoSourceElement, DtoJointElement dtoTargetElement) throws Exception{
				
		String rangeClassName = ConceptEnum.Transport_Function.toString();
		String sourceIndividualId = dtoSourceElement.getId();
		String property = RelationEnum.is_interface_of.toString();
		String targetIndividualId = dtoTargetElement.getId();
		
		//first, we need to get the transport functions connected to the card's ports
		String[] tfSource = NOpenQueryUtil.getIndividualsNamesAtObjectPropertyRange(StudioComponents.studioRepository.getBaseModel(), sourceIndividualId, property, rangeClassName);
		String[] tfTarget = NOpenQueryUtil.getIndividualsNamesAtObjectPropertyRange(StudioComponents.studioRepository.getBaseModel(), targetIndividualId, property, rangeClassName);
		//then we need to know their specific classes
		List<String> typeTfSource = QueryUtil.getClassesURIFromIndividual(StudioComponents.studioRepository.getBaseModel(), StudioComponents.studioRepository.getNamespace() + tfSource[0]);
		List<String> typeTfTarget = QueryUtil.getClassesURIFromIndividual(StudioComponents.studioRepository.getBaseModel(), StudioComponents.studioRepository.getNamespace() + tfTarget[0]);
		
		Iterator<String> iterator = typeTfSource.iterator();
		while(iterator.hasNext()){
			String type = iterator.next();
			if(QueryUtil.hasSubClass(StudioComponents.studioRepository.getBaseModel(), type) && QueryUtil.SubClass(StudioComponents.studioRepository.getBaseModel(), type).size() > 1){
				iterator.remove();
			}
		}
		iterator = typeTfTarget.iterator();
		while(iterator.hasNext()){
			String type = iterator.next();
			if(QueryUtil.hasSubClass(StudioComponents.studioRepository.getBaseModel(), type) && QueryUtil.SubClass(StudioComponents.studioRepository.getBaseModel(), type).size() > 1){
				iterator.remove();
			}
		}
		
		//if there is no transport function we cannot make the connection
		if(tfSource.length == 0 || tfTarget.length == 0){
			NOpenLog.appendLine("Error: The Transport Function " + dtoSourceElement.getName() + " cannot be bound to " + dtoTargetElement.getName() + "because the equipments are not defined in ITUStudio.");
			throw new Exception("Error: Unexpected relation between " + dtoSourceElement.getName() + " and " + dtoTargetElement.getName() + "because the equipments are not defined in ITUStudio. ");
		}
		
		String finalTypeTfSource = typeTfSource.get(0).substring(typeTfSource.get(0).indexOf("#")+1);
		String finalTypeTfTarget = typeTfTarget.get(0).substring(typeTfTarget.get(0).indexOf("#")+1);

		//then we create joint elements with the transport functions to call the binds procedure
		DtoJointElement newSource = new DtoJointElement();
		newSource.setId(tfSource[0]);
		newSource.setName(tfSource[0]);
		newSource.setType(finalTypeTfSource);
		DtoJointElement newTarget = new DtoJointElement();
		newTarget.setId(tfTarget[0]);
		newTarget.setName(tfTarget[0]);
		newTarget.setType(finalTypeTfTarget);
		System.out.println();
		applyBinds(newSource, newTarget, "Equipment", StudioComponents.studioRepository);
	}
	
	public static boolean canCreateEquipmentBinds(DtoJointElement dtoSourceElement, DtoJointElement dtoTargetElement, OKCoUploader repository) throws Exception{
		String rangeClassName = ConceptEnum.Transport_Function.toString();
		String sourceIndividualId = dtoSourceElement.getId();
		String property = RelationEnum.is_interface_of.toString();
		String targetIndividualId = dtoTargetElement.getId();
		String[] tfSource = NOpenQueryUtil.getIndividualsNamesAtObjectPropertyRange(repository.getBaseModel(), sourceIndividualId, property, rangeClassName);
		String[] tfTarget = NOpenQueryUtil.getIndividualsNamesAtObjectPropertyRange(repository.getBaseModel(), targetIndividualId, property, rangeClassName);
		if(tfSource == null || tfTarget == null){
			NOpenLog.appendLine("Error: The Transport Function " + dtoSourceElement.getName() + " cannot be bound to " + dtoTargetElement.getName() + "because the equipments are not defined in ITUStudio.");
			throw new Exception("Error: Unexpected relation between " + dtoSourceElement.getName() + " and " + dtoTargetElement.getName() + "because the equipments are not defined in ITUStudio. ");
		}
		System.out.println();
		DtoJointElement newSource = new DtoJointElement();
		newSource.setId(Util.generateUUID());
		newSource.setName(tfSource[0]);
		newSource.setType(rangeClassName);
		DtoJointElement newTarget = new DtoJointElement();
		newTarget.setId(Util.generateUUID());
		newTarget.setName(tfTarget[0]);
		newTarget.setType(rangeClassName);
		System.out.println();
		boolean result = canCreateBind(newSource, newTarget, repository);
		return result;
		
	}

	public static PerformBind getInstance(){
		return instance;
	}
	
}
