package br.com.padtec.nopen.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;

import br.com.padtec.common.dto.CardinalityDef;
import br.com.padtec.common.dto.RelationDef;
import br.com.padtec.common.queries.DtoQueryUtil;
import br.com.padtec.common.queries.QueryUtil;
import br.com.padtec.nopen.model.RelationEnum;
import br.com.padtec.nopen.studio.service.StudioComponents;
import br.com.padtec.okco.core.application.OKCoUploader;

public class ContainerStructure {
	
	private OKCoUploader repository = NOpenComponents.nopenRepository ;
	private static ContainerStructure instance = new ContainerStructure();
	
	private static HashMap<String,String> containerStructure = new HashMap<String,String>();

	public static HashMap<String, String> getContainerStructure() {
		return containerStructure;
	}

	public static void setContainerStructure(
			HashMap<String, String> containerStructure) {
		ContainerStructure.containerStructure = containerStructure;
	}

	public OKCoUploader getRepository() {
		return repository;
	}

	public static ContainerStructure getInstance() {
		return instance;
	}
	
	public static boolean verifyContainerRelation(String sourceURI, String tipo_source, String targetURI, String tipo_target) throws Exception{

		String property = RelationEnum.componentOf.toString();
		String propertyURI = StudioComponents.studioRepository.getNamespace() + property;
		String tipo_targetURI = StudioComponents.studioRepository.getNamespace() + tipo_target;
		
		Integer numberOfRelations = QueryUtil.getNumberOfOccurrences(StudioComponents.studioRepository.getBaseModel(), sourceURI, propertyURI, tipo_targetURI ); // <- n�o est� atualizando
		
		String key = NOpenComponents.nopenRepository.getNamespace() + tipo_source + NOpenComponents.nopenRepository.getNamespace() + property + NOpenComponents.nopenRepository.getNamespace() + tipo_target; 
		String cardinality = ContainerStructure.getContainerStructure().get(key);
		
		if(cardinality != null ){
			Integer cardinality_target = Integer.parseInt(cardinality);
			if( ((numberOfRelations < cardinality_target) || (cardinality_target == -1)) ){
				return true;
			}
		}
		
		return false;
	}
	
	public static void buildContainerStructure(String relationURI){
		//Create a mapping with all componentOf relations of the model and their cardinalities
		HashMap<String, RelationDef> possibleInstants = new HashMap<String, RelationDef>();
		possibleInstants = DtoQueryUtil.getPossibleInstantiationsOfRelation(NOpenComponents.nopenRepository.getBaseModel(), relationURI);
		
		Iterator<Entry<String, RelationDef>> it = possibleInstants.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        RelationDef relationdef = (RelationDef) pairs.getValue();
	        CardinalityDef card = relationdef.getCardOnRange();
	        Integer valueUp = card.getUpperBound();
	        ContainerStructure.containerStructure.put((String) pairs.getKey(), Integer.toString(valueUp));
	    }
	}

	public static boolean isTargetOfComponentOfRelation(String type) {
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX ont: <http://www.menthor.net/nOpenModel.owl#> "
				+ "ASK "
				+ "WHERE { "
				+ "?subject rdfs:subPropertyOf ont:componentOf . "
				+ "?subject rdfs:range ont:" + type + " . "
				+ "}" ;
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, instance.repository.getBaseModel());
		boolean result = qe.execAsk();			
		return result;
	}
}
