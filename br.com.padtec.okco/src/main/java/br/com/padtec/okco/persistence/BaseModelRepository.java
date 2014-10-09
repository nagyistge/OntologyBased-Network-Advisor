package br.com.padtec.okco.persistence;

import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;

public interface BaseModelRepository {
	
	public abstract void readBaseOntModel(String inputFileName);
	public abstract void readBaseOntModel(InputStream in);
	public abstract OntModel getBaseOntModel();
	public abstract String getBaseOntModelAsString();
	public abstract String getNameSpace();
	public abstract OntModel clone(OntModel ontModel);	
	public abstract void saveBaseOntModel(String path);
	public abstract void saveBaseOntModel();
	public abstract void printOutBaseOntModel();	
}