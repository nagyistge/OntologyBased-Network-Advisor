package br.com.padtec.common.okco.features.test;
import java.util.ArrayList;

import br.com.padtec.common.dto.simple.SimpleDtoClass;
import br.com.padtec.common.dto.simple.SimpleDtoInstance;
import br.com.padtec.common.dto.simple.SimpleDtoRelation;
import br.com.padtec.common.okco.features.OKCoFeatures;
import br.com.padtec.common.okco.features.OKCoResultFromFile;

public class TestMethodThree {

	public static void main(String[] args) {

		String inputFileName = "C://Users//fabio_000//Desktop//OntologiasOWL//assassinato.owl";
		ArrayList<String> setInstances = new ArrayList<String>();
		setInstances.add("http://www.semanticweb.org/ontologies/2013/8/ontology.owl#fabio");

		OKCoFeatures o = new OKCoFeatures();
		OKCoResultFromFile dto = o.completeIncompleteness(setInstances, inputFileName, "HERMIT", "REGULAR");
		
		if(dto.ListErrors.size() > 0)
		{
			for (String error : dto.ListErrors) {
				System.out.println("- " + error);
			}
			
		} else {
			
			for (SimpleDtoInstance i : dto.ListInstances) {
				
				System.out.println("----------------- " + i.Name + " -----------------");
				System.out.println("- " + i.Namespace);
				System.out.println("- Classes Belong: ");
				for (String string : i.ListClassesBelong) {
					System.out.println("    - " + string);
				}
				System.out.println("- Same instances: ");
				for (String string : i.ListSameInstances) {
					System.out.println("    - " + string);
				}
				System.out.println("- Dife instances: ");
				for (String string : i.ListDiferentInstances) {
					System.out.println("    - " + string);
				}
				System.out.println("- Classes definitions: ");
				for (SimpleDtoClass def : i.ListImcompletenessClassDefinitions) {
					System.out.println("   - " + def.TopClass);
					for (String string : def.SubClassesToClassify) {
						System.out.println("      - " + string);
					}
				}
				System.out.println("- Relation definitions: ");
				for (SimpleDtoRelation def : i.ListImcompletenessRelationDefinitions) {
					System.out.println("   - " + def.SourceClass + " -> " + def.Relation + " (" + def.KindProperty + "-" + def.RelationType + ") " + def.TargetClass + " (" + def.Cardinality + ")" );
					
				}
			}
			
		}
		
		
	}

}