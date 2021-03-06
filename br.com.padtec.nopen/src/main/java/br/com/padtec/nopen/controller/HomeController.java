package br.com.padtec.nopen.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.padtec.common.factory.FactoryUtil;
import br.com.padtec.nopen.provisioning.service.ProvisioningComponents;
import br.com.padtec.nopen.service.NOpenInitializer;
import br.com.padtec.nopen.service.NOpenReasoner;
import br.com.padtec.nopen.service.NOpenRegister;
import br.com.padtec.nopen.studio.service.StudioComponents;

@Controller
public class HomeController {

	@RequestMapping(method = RequestMethod.GET, value="/init")
	public String index(HttpServletRequest request) throws Exception 
	{		
		if(StudioComponents.studioRepository.getBaseModel() == null){
			NOpenInitializer.uploadTBoxes();
			NOpenRegister.registerDefaultTechnologies();
			NOpenReasoner.runInference(true);	
		}

		//		OntModel model = ProvisioningComponents.provisioningRepository.getBaseModel();
		//		HashMap<String, RelationDef> t = DtoQueryUtil.getPossibleInstantiationsOfRelation(model, model.getNsPrefixURI("")+"componentOf");

		return "welcome";
	}

	@RequestMapping("/home")
	public String homeRequest(HttpServletRequest request) {		
		return "welcome";
	}

	@RequestMapping("/info")
	public String aboutRequest(HttpServletRequest request) {		
		return "about";
	}

	@RequestMapping("/questions")
	public String faqRequest(HttpServletRequest request) {		
		return "faq";
	}		

	@RequestMapping("/advisor")
	public String advisorRequest() {
		return "redirect:welcome.htm";
	}

	/** Get the base model from studio as a string text */	
	@RequestMapping(method = RequestMethod.GET, value="/getStudioOntology")
	public String getStudioOntology(HttpSession session, HttpServletRequest request) throws IOException 
	{	     
		if(StudioComponents.studioRepository.isBaseModelUploaded())
		{
			request.getSession().removeAttribute("loadOk");
			request.getSession().setAttribute("ontology", StudioComponents.studioRepository.getBaseModelAsString());
			request.getSession().setAttribute("name", "Studio");
			return "ontology";
		}else{				
			request.getSession().removeAttribute("ontology");
			request.getSession().removeAttribute("name");
			request.getSession().setAttribute("loadOk", "false");
			return "welcome";
		}
	}

	/** Get the base model from provisioning as a string text */	
	@RequestMapping(method = RequestMethod.GET, value="/getProvisioningOntology")
	public String getProvisioningOntology(HttpSession session, HttpServletRequest request) throws IOException 
	{	     
		if(ProvisioningComponents.provisioningRepository.isBaseModelUploaded())
		{
			request.getSession().removeAttribute("loadOk");
			request.getSession().setAttribute("ontology", ProvisioningComponents.provisioningRepository.getBaseModelAsString());
			request.getSession().setAttribute("name", "Provisioning");
			return "ontology";
		}else{				
			request.getSession().removeAttribute("ontology");
			request.getSession().removeAttribute("name");
			request.getSession().setAttribute("loadOk", "false");
			return "welcome";
		}
	}

	//==========================================================================

	@RequestMapping("/dashboard")
	public String editorRequest() {
		return "dashboard/dashboard";
	}

	@RequestMapping("/hello")
	public String showMessage(HttpServletRequest request) {
		System.out.println("from controller");
		return "hello";
	}

	@RequestMapping(value = "/node_added", method = RequestMethod.GET)
	public @ResponseBody String nodeAdded(@RequestParam("id") String id, @RequestParam("stencil") String stencil) {
		System.out.println("id: "+id);
		System.out.println("stencil: "+stencil);
		return "OK";
	}

	@RequestMapping(value = "/printJSON", method = RequestMethod.POST)
	public @ResponseBody String printJSON(@RequestParam("json") String json) {
		System.out.println("JSON: \n"+json);
		return "OK";
	}

	@RequestMapping(value = "/requestSomething", method = RequestMethod.GET)
	public @ResponseBody String requestSomething() {
		String something = "var rect = new joint.shapes.basic.Rect({\r\n" + 
				"				position : {\r\n" + 
				"					x : 100,\r\n" + 
				"					y : 100\r\n" + 
				"				},\r\n" + 
				"				size : {\r\n" + 
				"					width : 70,\r\n" + 
				"					height : 30\r\n" + 
				"				},\r\n" + 
				"				attrs : {\r\n" + 
				"					text : {\r\n" + 
				"						text : 'my rectangle'\r\n" + 
				"					}\r\n" + 
				"				}\r\n" + 
				"			});\r\n" + 
				"			var rect2 = rect.clone();\r\n" + 
				"			var link = new joint.dia.Link({\r\n" + 
				"				source : {\r\n" + 
				"					id : rect.id\r\n" + 
				"				},\r\n" + 
				"				target : {\r\n" + 
				"					id : rect2.id\r\n" + 
				"				}\r\n" + 
				"			});\r\n" + 
				"			graph.addCell(rect).addCell(rect2).addCell(link);";

		return something;
	}
}
