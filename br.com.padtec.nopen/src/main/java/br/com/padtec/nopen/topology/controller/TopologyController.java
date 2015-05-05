package br.com.padtec.nopen.topology.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.padtec.nopen.service.util.NOpenFileUtil;
import br.com.padtec.nopen.topology.service.TopologyExporter;
import br.com.padtec.nopen.topology.service.TopologyImporter;


@Controller
public class TopologyController {

	@RequestMapping("/network-topology")
	public String networkTopologyRequest() {
		return "network-topology/network-topology";
	}
	
	/**
	 * Procedure to export a Topology in a XML file.
	 * @param tnodeArray
	 * @param tlinkArray
	 * @param uuid
	 * @return
	 */
	@RequestMapping(value = "/exportTopology", method = RequestMethod.POST)
	protected @ResponseBody String exportTopology(@RequestParam("tnodeArray") String tnodeArray,@RequestParam("tlinkArray") String tlinkArray, @RequestParam("uuid") String uuid){
		
		TopologyExporter topology = new TopologyExporter();
		String xml = topology.exportTopology(tnodeArray, tlinkArray, uuid);
		
		return xml;
	}
	
	/**
	 * Procedure to import a XML file Topology.
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/importTopology", method = RequestMethod.POST)
	protected @ResponseBody String importTopology(HttpServletRequest request){
			
		TopologyImporter topology = new TopologyImporter();
		String json = topology.importTopology(request);
		
		return json;
	}
	
	/**
	 * Procedure to check if a Topology file exist.
	 * @param filename
	 * @return
	 */
	@RequestMapping(value = "/checkTopologyFile", method = RequestMethod.POST)
	protected @ResponseBody String checkTopologyFile(@RequestParam("filename") String filename){

		filename = NOpenFileUtil.replaceSlash(filename + "/" + filename + ".json");
		if(NOpenFileUtil.checkTopologyFileExist(filename)){
			return "exist";
		}
		
		return null;
	}
	
	/**
	 * Procedure to save a JSON Topology file.
	 * @param filename
	 * @param graph
	 */
	@RequestMapping(value = "/saveTopology", method = RequestMethod.POST)
	protected @ResponseBody void saveTopology(@RequestParam("filename") String filename, @RequestParam("graph") String graph){
		
		NOpenFileUtil.createTopologyRepository(filename);
		filename = NOpenFileUtil.replaceSlash(filename + "/" + filename);
		
		try {
			File file = NOpenFileUtil.createTopologyJSONFile(filename);
			NOpenFileUtil.writeToFile(file, graph);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Procedure to get all Topology saved files.
	 * @return
	 */
	@RequestMapping(value = "/getAllTopologies", method = RequestMethod.GET)
	protected @ResponseBody String getAllTopologies(){
			
		String[] topologies = NOpenFileUtil.getAllTopplogyJSONFileNames();
		return NOpenFileUtil.parseStringToJSON("topology", topologies);
		
	}
	
	/**
	 * Procedure to open a JSON Topology file.
	 * @param filename
	 * @return
	 */
	@RequestMapping(value = "/openTopology", method = RequestMethod.POST)
	protected @ResponseBody String openTopology(@RequestParam("filename") String filename){
		
		filename = NOpenFileUtil.replaceSlash(filename + "/" + filename + ".json");	
		return NOpenFileUtil.openTopologyJSONFileAsString(filename);
		
	}
	
	/**
	 * Procedure to get all Equipment templates.
	 * @return
	 */
	@RequestMapping(value = "/getAllTemplateEquipment", method = RequestMethod.GET)
	protected @ResponseBody String getAllTemplateEquipment(){
		 
		String[] equipments = NOpenFileUtil.getAllTemplateJSONFileNames();
		return NOpenFileUtil.parseStringToJSON("equipment", equipments);
		 
	}
	
	/**
	 * Procedure to march a Equipment to a Node.
	 * @param idNode
	 * @param idEquipment
	 */
	@RequestMapping(value = "/matchEquipmentToNode", method = RequestMethod.POST)
	protected @ResponseBody void matchEquipmentToNode(@RequestParam("idNode") String idNode, @RequestParam("idEquipment") String idEquipment){
		
		System.out.println("idNode: " + idNode);
		System.out.println("idEquipment: " + idEquipment);
		
		// ManagerTopology managerTopology = new ManagerTopology();
		// managerTopology.matchEquipmentToNode(idNode, idEquipment);
	}	
	
	/**
	 * Procedure to create a Connection.
	 * @param source
	 * @param target
	 */
	@RequestMapping(value = "/createConnection", method = RequestMethod.POST)
	protected @ResponseBody void createConnection(@RequestParam("source") String source, @RequestParam("target") String target){
		
		System.out.println("Source: " + source);
		System.out.println("Target: " + target);
		
		// ManagerTopology managerTopology = new ManagerTopology();
		// managerTopology.createConnection(source, target);
		
	}
	
}
