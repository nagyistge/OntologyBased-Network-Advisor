package br.com.padtec.common.util;
import java.util.ArrayList;
import java.util.List;

import br.com.padtec.common.queries.QueryUtil;
import br.com.padtec.common.dto.DtoDefinitionClass;
import br.com.padtec.common.dto.EnumRelationTypeCompletness;
import br.com.padtec.common.util.Instance;
import br.com.padtec.common.util.ManagerInstances;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
//import com.hp.hpl.jena.sparql.function.CastXSD.Instance;

public class Search 
{	 	
 	public ArrayList<DtoDefinitionClass> GetModelDefinitionsInInstances(List<Instance> listAllInstances,	InfModel InfModel) {

 		System.out.println("\nSearch: Getting model definitions in instances()...");
		ArrayList<DtoDefinitionClass> resultListDefinitions = new ArrayList<DtoDefinitionClass>();
		
		for (Instance instance : listAllInstances) 
		{
			for (String cls : instance.ListClasses) 
			{
				//DtoDefinitionClass aux = DtoDefinitionClass.getDtoWithSourceAndRelationAndTarget(resultListDefinitions, cls);
				
				DtoDefinitionClass aux = null;
				if(aux == null && ! cls.contains("Thing"))	//doesn't exist yet
				{
					//relations URI some values
					ArrayList<DtoDefinitionClass> dtoSomeRelationsList = new ArrayList<DtoDefinitionClass>();
					for(String[] triple: QueryUtil.getTuplesSomeValuesFrom(InfModel,cls)){
						DtoDefinitionClass dto = new DtoDefinitionClass();
						dto.Source = triple[0];
						dto.Relation = triple[1];
						dto.Target = triple[2];
						dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
						dto.TypeCompletness = EnumRelationTypeCompletness.SOME;
					}
					
					//relations URI minimum cardinality values
					ArrayList<DtoDefinitionClass> dtoMinRelationsList = new ArrayList<DtoDefinitionClass>();
					for(String[] triple: QueryUtil.getTuplesMinQualifiedCardinality(InfModel,cls)){
						DtoDefinitionClass dto = new DtoDefinitionClass();
						dto.Source = triple[0];
						dto.Relation = triple[1];
						dto.Cardinality = triple[2];
						dto.Target = triple[3];
						dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
						dto.TypeCompletness = EnumRelationTypeCompletness.MIN;
					}
					
					//relations URI maximum cardinality values
					ArrayList<DtoDefinitionClass> dtoMaxRelationsList = new ArrayList<DtoDefinitionClass>();
					for(String[] triple: QueryUtil.getTuplesMaxQualifiedCardinality(InfModel,cls)){
						DtoDefinitionClass dto = new DtoDefinitionClass();
						dto.Source = triple[0];
						dto.Relation = triple[1];
						dto.Cardinality = triple[2];
						dto.Target = triple[3];
						dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
						dto.TypeCompletness = EnumRelationTypeCompletness.MAX;
					}	
					
					//relations URI exact cardinality values
					ArrayList<DtoDefinitionClass> dtoExactlyRelationsList = new ArrayList<DtoDefinitionClass>();
					for(String[] triple: QueryUtil.getTuplesQualifiedCardinality(InfModel,cls)){
						DtoDefinitionClass dto = new DtoDefinitionClass();
						dto.Source = triple[0];
						dto.Relation = triple[1];
						dto.Cardinality = triple[2];
						dto.Target = triple[3];
						dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
						dto.TypeCompletness = EnumRelationTypeCompletness.EXACTLY;
					}						
					
					resultListDefinitions.addAll(dtoSomeRelationsList);
					resultListDefinitions.addAll(dtoMinRelationsList);
					resultListDefinitions.addAll(dtoMaxRelationsList);
					resultListDefinitions.addAll(dtoExactlyRelationsList);
				}			
				
			}		
			
		}		
		
		return resultListDefinitions;
	}
	
 	public ArrayList<DtoDefinitionClass> GetModelDefinitionsInInstances(String instanceURI, OntModel model, InfModel InfModel, List<Instance> listAllInstances, ManagerInstances manager) {

		Instance Instance = manager.getInstance(listAllInstances, instanceURI); // GET INTANCE on MODEL
		List<String> listInstancesDto = QueryUtil.getIndividualsURIFromAllClasses(InfModel);		
		for (String dto : listInstancesDto) {
			
			if(dto.equals(instanceURI))
			{				
				String nameSpace = dto.split("#")[0] + "#";
				String name = dto.split("#")[1];
				
				if (Instance == null)
				{					
					Instance = new Instance(nameSpace, name, QueryUtil.getClassesURI(InfModel, instanceURI), QueryUtil.getIndividualsURIDifferentFrom(InfModel, dto), QueryUtil.getIndividualsURISameAs(InfModel, dto),true);
					
				} else {
					
					//Update classes
					Instance.ListClasses = QueryUtil.getClassesURI(InfModel, instanceURI);
				}
			}
		}
	
		ArrayList<DtoDefinitionClass> resultListDefinitions = new ArrayList<DtoDefinitionClass>();

		for (String cls : Instance.ListClasses) 
		{
			DtoDefinitionClass aux = DtoDefinitionClass.getDtoWithSourceAndRelationAndTarget(resultListDefinitions, cls);
			if(aux == null && ! cls.contains("Thing"))	//don't exist yet
			{
				//relations URI some values
				ArrayList<DtoDefinitionClass> dtoSomeRelationsList = new ArrayList<DtoDefinitionClass>();
				for(String[] triple: QueryUtil.getTuplesSomeValuesFrom(InfModel,cls)){
					DtoDefinitionClass dto = new DtoDefinitionClass();
					dto.Source = triple[0];
					dto.Relation = triple[1];
					dto.Target = triple[2];
					dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
					dto.TypeCompletness = EnumRelationTypeCompletness.SOME;
				}
				
				//relations URI minimum cardinality values
				ArrayList<DtoDefinitionClass> dtoMinRelationsList = new ArrayList<DtoDefinitionClass>();
				for(String[] triple: QueryUtil.getTuplesMinQualifiedCardinality(InfModel,cls)){
					DtoDefinitionClass dto = new DtoDefinitionClass();
					dto.Source = triple[0];
					dto.Relation = triple[1];
					dto.Cardinality = triple[2];
					dto.Target = triple[3];
					dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
					dto.TypeCompletness = EnumRelationTypeCompletness.MIN;
				}
				
				//relations URI maximum cardinality values
				ArrayList<DtoDefinitionClass> dtoMaxRelationsList = new ArrayList<DtoDefinitionClass>();
				for(String[] triple: QueryUtil.getTuplesMaxQualifiedCardinality(InfModel,cls)){
					DtoDefinitionClass dto = new DtoDefinitionClass();
					dto.Source = triple[0];
					dto.Relation = triple[1];
					dto.Cardinality = triple[2];
					dto.Target = triple[3];
					dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
					dto.TypeCompletness = EnumRelationTypeCompletness.MAX;
				}	
				
				//relations URI exact cardinality values
				ArrayList<DtoDefinitionClass> dtoExactlyRelationsList = new ArrayList<DtoDefinitionClass>();
				for(String[] triple: QueryUtil.getTuplesQualifiedCardinality(InfModel,cls)){
					DtoDefinitionClass dto = new DtoDefinitionClass();
					dto.Source = triple[0];
					dto.Relation = triple[1];
					dto.Cardinality = triple[2];
					dto.Target = triple[3];
					dto.PropertyType = QueryUtil.getPropertyURIType(InfModel, dto.Relation);
					dto.TypeCompletness = EnumRelationTypeCompletness.EXACTLY;
				}						
				
				
				resultListDefinitions.addAll(dtoSomeRelationsList);
				resultListDefinitions.addAll(dtoMinRelationsList);
				resultListDefinitions.addAll(dtoMaxRelationsList);
				resultListDefinitions.addAll(dtoExactlyRelationsList);
			}			
			
		}	
		
		//Add to list of intances
		//listAllInstances.add(Instance);
		
		//return
		return resultListDefinitions;
	}
 		
}
