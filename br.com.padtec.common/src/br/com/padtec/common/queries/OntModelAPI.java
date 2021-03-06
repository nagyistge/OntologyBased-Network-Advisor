package br.com.padtec.common.queries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

public class OntModelAPI {
	
	/** 
	 * Return the URI of all properties of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<String> getPropertiesURI(OntModel model) 
	{		
		List<String> lista = new ArrayList<String>();
		ExtendedIterator<OntProperty> i = model.listOntProperties().filterKeep(new Filter<OntProperty>()				
		{
            @Override
            public boolean accept(OntProperty o){
                return o.isURIResource();
            }
        });
		while(i.hasNext()) {
			Resource val = (Resource) i.next();
			lista.add(val.getURI());
		}
		return lista;
	}
	
	/** 
	 * Return all properties of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<OntProperty> getProperties(OntModel model)
	{
		ExtendedIterator<OntProperty> i = model.listOntProperties().filterKeep(new Filter<OntProperty>()				
		{
            @Override
            public boolean accept(OntProperty o){
                return o.isURIResource();
            }
        });
		return i.toList();
	}
	
	/** 
	 * Return the URI of all classes of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<String> getClassesURI(OntModel model) 
	{		
		List<String> lista = new ArrayList<String>();
		ExtendedIterator<OntClass> i = model.listClasses().filterKeep(new Filter<OntClass>()				
		{
            @Override
            public boolean accept(OntClass o){
                return o.isURIResource();
            }
        });		
		while(i.hasNext()) {
			Resource val = (Resource) i.next();			
			lista.add(val.getURI());		
		}		
		return lista;
	}
	
	/** 
	 * Return all classes of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<OntClass> getClasses(OntModel model) 
	{		
		ExtendedIterator<OntClass> i = model.listClasses().filterKeep(new Filter<OntClass>()				
		{
            @Override
            public boolean accept(OntClass o){
                return o.isURIResource();
            }
        });		
		return i.toList();
	}
	
	/** 
	 * Return all individuals of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<Individual> getIndividuals(OntModel model) 
	{		
		ExtendedIterator<Individual> i = model.listIndividuals().filterKeep(new Filter<Individual>()				
		{
            @Override
            public boolean accept(Individual o){
                return o.isURIResource();
            }
        });		
		return i.toList();
	}
	
	/** 
	 * Return all individuals URI of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<String> getIndividualsURI(OntModel model) 
	{		
		List<String> lista = new ArrayList<String>();
		ExtendedIterator<Individual> i = model.listIndividuals().filterKeep(new Filter<Individual>()				
		{
            @Override
            public boolean accept(Individual o){
                return o.isURIResource();
            }
        });		
		while(i.hasNext()) {
			Resource val = (Resource) i.next();			
			lista.add(val.getURI());
		}		
		return lista;
	}
	
	/** 
	 * Return all individuals of a given class of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @param ontclass: jena.ontology.OntClass
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<Individual> getIndividuals(OntModel model, OntClass ontclass) 
	{		
		ExtendedIterator<Individual> i = model.listIndividuals(ontclass).filterKeep(new Filter<Individual>()				
		{
            @Override
            public boolean accept(Individual o){
                return o.isURIResource();
            }
        });
		return i.toList();
	}
	
	
	/** 
	 * Return the URI of all individuals of a given class of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @param ontclass: jena.ontology.OntClass
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<String> getIndividualsURI(OntModel model, OntClass ontclass) 
	{		
		List<String> lista = new ArrayList<String>();
		ExtendedIterator<Individual> i = model.listIndividuals(ontclass).filterKeep(new Filter<Individual>()				
		{
            @Override
            public boolean accept(Individual o){
                return o.isURIResource();
            }
        });	
		while(i.hasNext()) {
			Resource val = (Resource) i.next();			
			lista.add(val.getURI());			
		}		
		return lista;
	}

	/** 
	 * Return the URI of all individuals of a given class URI of the ontology. This method is performed using the Jena API.
	 * 
	 * @param model: jena.ontology.OntModel 
	 * @param classURI: OntClass URI
	 * @return
	 * 
	 * @author John Guerson
	 */
	static public List<String> getIndividualsURI(OntModel model, String classURI) 
	{
		OntClass ontclass = model.getOntClass(classURI);
		if(ontclass!=null) return getIndividualsURI(model,ontclass); 
		else return new ArrayList<String>();		
	}	
	
	/**
	 * Clone the base OntModel passed as argument. 
	 * 
	 * @param model: OntModel 
	 * 
	 * @author John Guerson
	 */
	static public OntModel clone(OntModel model)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		model.write(out, "RDF/XML");
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        OntModel newModel = ModelFactory.createOntologyModel();
        newModel.read(in,null);        
        return newModel;
	}
	

}
