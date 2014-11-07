package br.com.padtec.common.dto;

import java.util.ArrayList;
import java.util.List;

import br.com.padtec.common.util.Instance;
import br.com.padtec.common.dto.TupleInstanceExist;

public class DtoViewSelectInstance {
	
	public Instance instance;
	public ArrayList<TupleInstanceExist > listSameShow;
	public ArrayList<TupleInstanceExist > listDifferentShow;
	
	public DtoViewSelectInstance(Instance i, List<Instance> listAllInstances)
	{
		this.instance = i;
		this.listDifferentShow = new ArrayList<TupleInstanceExist>();
		this.listSameShow = new ArrayList<TupleInstanceExist>();
		
		String uri = i.ns + i.name;
		
		//Create all instances using - false
		for (Instance instance : listAllInstances) 
		{	
			if(!(uri.equals(instance.ns + instance.name)))
			{
				this.listSameShow.add(new TupleInstanceExist(instance.ns, instance.name, false));
				this.listDifferentShow.add(new TupleInstanceExist(instance.ns, instance.name, false));
			}
			
		}
		
		//Setting true
		for (String iName : i.ListSameInstances) 
		{
			TupleInstanceExist t = TupleInstanceExist.getTuple(iName, listSameShow);
			if(!(t == null))
			{
				t.exist = true;
			}
		}
		
		//Setting true
		for (String iName : i.ListDiferentInstances) 
		{
			TupleInstanceExist t = TupleInstanceExist.getTuple(iName, listDifferentShow);
			if(!(t == null))
			{
				t.exist = true;
			}
		}
	}

}