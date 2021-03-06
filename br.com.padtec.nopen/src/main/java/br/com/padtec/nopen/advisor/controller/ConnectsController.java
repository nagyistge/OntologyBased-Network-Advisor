package br.com.padtec.nopen.advisor.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.padtec.advisor.core.application.AdvisorComponents;

@Controller
public class ConnectsController {

	@RequestMapping(value = "/autoConnects", method = RequestMethod.POST)
	public String autoConnects(HttpServletRequest request)
	{

		ArrayList<String[]> listInstancesCreated = new ArrayList<String[]>();
		listInstancesCreated = AdvisorComponents.connects.autoConnect();
		
		String returnMessage;
		if(listInstancesCreated.size()>0){
			returnMessage = "Reference Points connected:<br>";
		}else{
			returnMessage = "No reference points connected.";
		}
		
		request.getSession().setAttribute("loadOk", returnMessage);
		
		return VisualizationController.connects(request);
			
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/do_connects")
	public @ResponseBody String do_connects(@RequestParam("rp_src") String rp_src,@RequestParam("rp_trg") String rp_trg, @RequestParam("rp_type") String rp_type, HttpServletRequest request) 
	{
		try {
			AdvisorComponents.connects.connects(rp_src, rp_trg, rp_type);
			
			/*
			 * Verify the new possible connects
			 * */			
			String hashAllowed = new String();			
			ArrayList<String[]> possibleConnections;
			
			AdvisorComponents.connects.setEquipmentsWithRPs();

			for (String connections : AdvisorComponents.connects.getConnectsBetweenEqsAndRps()) {
				String src = connections.split("#")[0];
				String trg = connections.split("#")[1];

				possibleConnections = AdvisorComponents.connects.getPossibleConnectsTuples(src);
				if(!possibleConnections.isEmpty() && !hashAllowed.contains(src))
					hashAllowed += src+"#";

				possibleConnections = AdvisorComponents.connects.getPossibleConnectsTuples(trg);
				if(!possibleConnections.isEmpty() && !hashAllowed.contains(trg))
					hashAllowed += trg+"#";
			}
			
			for (String equipWithRP : AdvisorComponents.connects.getEquipmentsWithRps()) {
				String equip = equipWithRP.split("#")[0];
				String rp = equipWithRP.split("#")[1];

				if(!equip.isEmpty()){
					if(!AdvisorComponents.connects.getPossibleConnectsTuples(rp).isEmpty() && !hashAllowed.contains(rp)){
						hashAllowed += equip+"#";
					}
				}
			}
			
			return hashAllowed;
		} catch (Exception e) {
			e.printStackTrace();
			return "false";
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/get_possible_connections")
	public @ResponseBody String get_possible_connections(@RequestParam("rp") String rp, HttpServletRequest request) {
		/*
		 * [0] = RP's name
		 * [1] = Connection type
		 * */
		ArrayList<String[]> list = AdvisorComponents.connects.getPossibleConnectsTuples(rp);

		String hashEquipIntIn = "";

		for(String[] line : list){
			hashEquipIntIn += line[0].substring(line[0].indexOf("#")+1)+"#"+line[1]+";";
		}

		return hashEquipIntIn;
	}	
}
