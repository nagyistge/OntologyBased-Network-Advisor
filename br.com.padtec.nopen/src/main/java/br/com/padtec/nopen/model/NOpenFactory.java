package br.com.padtec.nopen.model;

import br.com.padtec.common.factory.FactoryUtil;
import br.com.padtec.okco.core.application.OKCoUploader;

public class NOpenFactory {

	public static void createOTNTech(OKCoUploader repository)
	{
		String otnURI = repository.getNamespace()+"OTN";		
		String techURI = repository.getNamespace()+ConceptEnum.TECHNOLOGY.toString();
		
		String poukURI = repository.getNamespace()+"POUk";
		String odukURI = repository.getNamespace()+"ODUk";
		String otukURI = repository.getNamespace()+"OTUk";
		String layerURI = repository.getNamespace()+ConceptEnum.LAYER.toString();
		
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), otnURI,techURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), poukURI, layerURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), odukURI, layerURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), otukURI, layerURI);
		
		String techToLayerURI = repository.getNamespace()+RelationEnum.COMPONENTOF_TECH_LAYER.toString();
				
		FactoryUtil.createInstanceRelation(repository.getBaseModel(),otnURI, techToLayerURI, poukURI);
		FactoryUtil.createInstanceRelation(repository.getBaseModel(),otnURI, techToLayerURI, odukURI);
		FactoryUtil.createInstanceRelation(repository.getBaseModel(),otnURI, techToLayerURI, otukURI);
	}	
	
	public static void createMEFTech(OKCoUploader repository)
	{
		String mefURI = repository.getNamespace()+"MEF";		
		String techURI = repository.getNamespace()+ConceptEnum.TECHNOLOGY.toString();
		
		String menURI = repository.getNamespace()+"MEN";
		String subscribersURI = repository.getNamespace()+"Subscribers";
		String layerURI = repository.getNamespace()+ConceptEnum.LAYER.toString();
		
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), mefURI,techURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), menURI, layerURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), subscribersURI, layerURI);
		
		String techToLayerURI = repository.getNamespace()+RelationEnum.COMPONENTOF_TECH_LAYER.toString();
				
		FactoryUtil.createInstanceRelation(repository.getBaseModel(),mefURI, techToLayerURI, menURI);
		FactoryUtil.createInstanceRelation(repository.getBaseModel(),mefURI, techToLayerURI, subscribersURI);
	}
	
	public static void createEquipment(OKCoUploader repository){
		String eq1URI = repository.getNamespace()+"Equipment1";
		String eq2URI = repository.getNamespace()+"Equipment2";
		String eq3URI = repository.getNamespace()+"Equipment3";
		String equipmentURI = repository.getNamespace()+ConceptEnum.EQUIPMENT.toString();
		
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), eq1URI,equipmentURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), eq2URI,equipmentURI);
		FactoryUtil.createInstanceIndividual(repository.getBaseModel(), eq3URI,equipmentURI);

	}
}
