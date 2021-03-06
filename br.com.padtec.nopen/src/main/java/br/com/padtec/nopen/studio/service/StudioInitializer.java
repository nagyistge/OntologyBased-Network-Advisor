package br.com.padtec.nopen.studio.service;

import java.io.InputStream;
import java.util.Date;

import br.com.padtec.common.util.PerformanceUtil;
import br.com.padtec.nopen.model.RelationEnum;
import br.com.padtec.nopen.service.NOpenComponents;
import br.com.padtec.nopen.service.util.NOpenUtilities;

public class StudioInitializer {

	public static String uploadEquipStudioTBox(boolean runReasoner)
	{
		Date beginDate = new Date();
		InputStream s = StudioInitializer.class.getResourceAsStream("/model/StudioLight.owl");
		
		String msg = NOpenUtilities.uploadTBOx(s, runReasoner, StudioComponents.studioRepository);
		
		PerformanceUtil.printExecutionTime("Studio: TBox uploaded.", beginDate);		
		
		BuildBindStructure.createBindStructure(NOpenComponents.nopenRepository.getNamespace() + RelationEnum.binds.toString());
				
		PerformanceUtil.printExecutionTime("Studio: Binding structure created.", beginDate);	
		
		return msg;
	}	
	
}
