
package br.com.padtec.advisor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.padtec.advisor.application.dto.DtoResultAjax;
import br.com.padtec.advisor.application.types.ConceptEnum;
import br.com.padtec.advisor.application.types.RelationEnum;
import br.com.padtec.advisor.application.util.ApplicationQueryUtil;
import br.com.padtec.common.dto.DtoInstance;
import br.com.padtec.common.dto.DtoInstanceRelation;
import br.com.padtec.common.queries.DtoQueryUtil;
import br.com.padtec.common.queries.QueryUtil;
import br.com.padtec.okco.core.application.OKCoUploader;
import br.com.padtec.transformation.sindel.processor.BindsProcessor;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Statement;

@Controller
public class ProvisioningController {
	
	public static DtoResultAjax provisioningBinds(String outInt, String inInt, HttpServletRequest request, Boolean updateListsInTheEnd, ArrayList<String> listInstancesCreated) {

		DtoResultAjax dto = new DtoResultAjax();

		String outputNs = "";
		String inputNs = "";
		
		List<DtoInstanceRelation> outIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), OKCoUploader.getNamespace()+outInt);
		for (DtoInstanceRelation outIntRelation : outIntRelations) {
			if(outIntRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"maps_output")){
				outputNs = outIntRelation.Target;
				outputNs = outputNs.replace(OKCoUploader.getNamespace(), "");
				break;
			}
		}		

		List<DtoInstanceRelation> inIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), OKCoUploader.getNamespace()+inInt);
		for (DtoInstanceRelation inIntRelation : inIntRelations) {
			if(inIntRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"maps_input")){
				inputNs = inIntRelation.Target;
				inputNs = inputNs.replace(OKCoUploader.getNamespace(), "");
				break;
			}
		}
		
		Boolean inputIsPmInput = false;
		if(inputNs.equals("")){
			DtoInstance inputOrIntInput = getInstanceFromNameSpace(OKCoUploader.getNamespace()+inInt);
			for (String inputOrIntInputClass : inputOrIntInput.ListClasses) {
				if(inputOrIntInputClass.equals(OKCoUploader.getNamespace()+"Physical_Media_Input")){
					inputNs = inInt;
					inputIsPmInput = true;
					break;
				}
			}
		}
//		System.out.println();
		Boolean outputIsPmOutput = false;
		if(outputNs.equals("")){
			DtoInstance outputOrIntOutput = getInstanceFromNameSpace(OKCoUploader.getNamespace()+outputNs);
			for (String outputOrIntOutputClass : outputOrIntOutput.ListClasses) {
				if(outputOrIntOutputClass.equals(OKCoUploader.getNamespace()+"Physical_Media_Output")){
					outputNs = inInt;
					outputIsPmOutput = true;
					break;
				}
			}
		}
		
		//binds Interface out with in
		Individual a, b;
		ObjectProperty rel = null;
		if(inputIsPmInput && !outputIsPmOutput){
			a = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+outInt);
			b = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+inputNs);
			rel = OKCoUploader.getBaseModel().getObjectProperty(OKCoUploader.getNamespace()+"binds_PM_out_interface");
		}else if(!inputIsPmInput && outputIsPmOutput){
			a = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+inInt);
			b = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+outputNs);
			rel = OKCoUploader.getBaseModel().getObjectProperty(OKCoUploader.getNamespace()+"binds_PM_in_Interface");
		}else{
			a = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+outInt);
			b = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+inInt);
			rel = OKCoUploader.getBaseModel().getObjectProperty(OKCoUploader.getNamespace()+"interface_binds");
		}
		
		Statement stmt = OKCoUploader.getBaseModel().createStatement(a, rel, b);
		OKCoUploader.getBaseModel().add(stmt);

		if(listInstancesCreated == null){
			listInstancesCreated = new ArrayList<String>();
		}
		listInstancesCreated.add(a.getNameSpace()+a.getLocalName());
		listInstancesCreated.add(b.getNameSpace()+b.getLocalName());
		
		if(!outputNs.equals("") && !inputNs.equals("")){
			
			a = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+outputNs);
			b = OKCoUploader.getBaseModel().getIndividual(OKCoUploader.getNamespace()+inputNs);
			
//			ArrayList<String> tiposA=HomeController.Search.GetClassesFrom(OKCoUploader.getNamespace()+a.getLocalName(),OKCoUploader.getBaseModel());
//			ArrayList<String> tiposB=HomeController.Search.GetClassesFrom(OKCoUploader.getNamespace()+b.getLocalName(),OKCoUploader.getBaseModel());
//			tiposA.remove(OKCoUploader.getNamespace()+"Geographical_Element");
//			tiposA.remove(OKCoUploader.getNamespace()+"Bound_Input-Output");
//			tiposB.remove(OKCoUploader.getNamespace()+"Geographical_Element");
//			tiposB.remove(OKCoUploader.getNamespace()+"Bound_Input-Output");
//			rel = OKCoUploader.getBaseModel().getObjectProperty(OKCoUploader.getNamespace()+"binds");
//			stmt = OKCoUploader.getBaseModel().createStatement(a, rel, b);
//			OKCoUploader.getBaseModel().add(stmt);	
//			HashMap<String, String> hash = new HashMap<String, String>();
//			hash.put("INPUT", tiposB.get(0));
//			hash.put("OUTPUT", tiposA.get(0));
//			HashMap<String, String>element= Provisioning.values.get(hash);
//			Provisioning.bindsSpecific(a,b,tiposA.get(0),tiposB.get(0));
			//BindsProcessor.bindPorts(outputNs, inputNs);
			
			BindsProcessor.bindPorts(null, a, b, null, OKCoUploader.getNamespace(), OKCoUploader.getBaseModel(), listInstancesCreated);

		}

		OKCoUploader.substituteInferredModelFromBaseModel(false);
		
		
//		if(updateListsInTheEnd){
//			
//			try {
//				
////				for (String instanceUri : listInstancesCreated) {
////					HomeController.UpdateAddIntanceInLists(instanceUri);	
////				}
//				
//			} catch (InconsistentOntologyException e) {
//				
//				e.printStackTrace();
//				
//			} catch (OKCoExceptionInstanceFormat e) {
//				
//				e.printStackTrace();
//			}			
//		}
		
		dto.ok = true;
		dto.result = "ok";

		return dto;

	}

	public static ArrayList<String> getCandidateInterfacesForConnection(String outIntNs){
		
		ArrayList<String> allowedInputInterfaces = new ArrayList<String>();
		//find the instance of the output interface
		DtoInstance outputInterface = getInstanceFromNameSpace(outIntNs);
//		Instance outputInterface = null;
//		for (Instance instance : HomeController.ListAllInstances) {
//			String instNs = instance.name;
//			instNs = instNs.replace(instance.ns, "");
//			outIntNs = outIntNs.replace(instance.ns, "");
//			if(instNs.equals(outIntNs)){
//				outputInterface = instance;
//				break;
//			}
//		}

		//get all relations of the output interface
		
		List<DtoInstanceRelation> outIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), outputInterface.ns+outputInterface.name);

		//get namespaces of individuals of some output interface relations
		String outputNs = "";
		String eqOutNs = "";
		String interfaceBindsNs = "";
		for (DtoInstanceRelation outRelation : outIntRelations) {
			if(outRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+RelationEnum.MAPS_OUTPUT)){
				outputNs = outRelation.Target;
			}else if(outRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+RelationEnum.INV_COMPONENTOF)){
				eqOutNs = outRelation.Target;
			}else if(outRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+RelationEnum.INTERFACE_BINDS)){
				interfaceBindsNs = outRelation.Target;
			}
		}

		ArrayList<DtoInstance> inputInterfaces = getInstancesFromClass(ConceptEnum.INPUT_INTERFACE);
		ArrayList<DtoInstance> physicalMediaInputs = getInstancesFromClass(ConceptEnum.PHYSICAL_MEDIA_INPUT);
		
		//if the output interface does not maps an output, it can not connects
		if(outputNs.equals("")){
			for (DtoInstance inputInterface : inputInterfaces) {
				List<DtoInstanceRelation> inIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), inputInterface.ns+inputInterface.name);
				String eqInNs = "";
				
				//get namespaces of individuals of some input interface relations
				for (DtoInstanceRelation inRelation : inIntRelations) {
					if(inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.componentOf")){
						eqInNs = inRelation.Target;		
						break;
					}
				}
				
				//if(!eqInNs.equals(eqOutNs)){
					String interfaceReturn = "";
					interfaceReturn += eqInNs; 
					interfaceReturn += "#";
					interfaceReturn += inputInterface.name;
					interfaceReturn += "#";
					interfaceReturn += "false";
					
					allowedInputInterfaces.add(interfaceReturn);
				//}
				
				
				
			}
			return allowedInputInterfaces;
		}
		
		//verify if the output interface is already binded
		Boolean outputInterfaceAlreadyBinded = false;
		if(!interfaceBindsNs.equals("")){
			outputInterfaceAlreadyBinded = true;
		}
		
		//get the instance of the output mapped by the output interface
		DtoInstance output = null;
		List<DtoInstance> listAllInstances = DtoQueryUtil.getIndividuals(OKCoUploader.getInferredModel(), false, false, false);
		for (DtoInstance instance : listAllInstances) {
			if(outputNs.equals(instance.ns+instance.name)){
				output = instance;
				break;
			}
		}

		//get all input interfaces
		
//		ArrayList<Instance> inputInterfaces = new ArrayList<Instance>(); 
//		for (Instance instance : HomeController.ListAllInstances) {
//			for (String className : instance.ListClasses) {
//				className = className.replace(OKCoUploader.getNamespace(), "");
//				if(className.equalsIgnoreCase("Input_Interface")){
//					inputInterfaces.add(instance);
//					break;
//				}
//			}
//		}

		
		//now, the idea is compare all types of the output with all types of all inputs
		for (String outputClassName : output.ListClasses) {
			outputClassName = outputClassName.replace(OKCoUploader.getNamespace(), "");
			
			//here, I look for possible connections with input interfaces
			for (DtoInstance inputInterface : inputInterfaces) {
				List<DtoInstanceRelation> inIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), inputInterface.ns+inputInterface.name);
				String inputNs = "";
				String eqInNs = "";
				Boolean inputInterfaceAlreadyConnected = false;
				
				//get namespaces of individuals of some input interface relations
				for (DtoInstanceRelation inRelation : inIntRelations) {
					if(inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"maps_input")){
						inputNs = inRelation.Target;
					}else if(inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.componentOf")){
						eqInNs = inRelation.Target;						
					}else if(inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.interface_binds")){
						inputInterfaceAlreadyConnected = true;
					}
				}

				List<DtoInstance> allInstances = DtoQueryUtil.getIndividuals(OKCoUploader.getInferredModel(), false, false, false);
				
				//since I verify the inverse relation of interface_binds above, 
				//it's necessary to verify if some output interface has the interface_binds relation
				//with the actual input interface
				//the block below it's for this purpose
				if(!inputInterfaceAlreadyConnected){
					for(DtoInstance otherOutput : allInstances){
						for (String otherOutputClassName : otherOutput.ListClasses) {
							otherOutputClassName = otherOutputClassName.replace(OKCoUploader.getNamespace(), "");
							if(otherOutputClassName.equalsIgnoreCase("Output_Interface")){
								List<DtoInstanceRelation> otherOutputRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), otherOutput.ns+otherOutput.name);
								for (DtoInstanceRelation otherOutputRelation : otherOutputRelations) {
									if(otherOutputRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"interface_binds")){
										if((inputInterface.ns+inputInterface.name).equals(otherOutputRelation.Target)){
											inputInterfaceAlreadyConnected = true;
											break;
										}
									}
								}
								if(inputInterfaceAlreadyConnected){
									break;
								}
							}
						}
						if(inputInterfaceAlreadyConnected){
							break;
						}
					}
				}
				
				Boolean hasAllowedRelation = false;
				if(inputNs != ""){
					//get the input mapped by the input interface 
					DtoInstance input = null;
					for (DtoInstance instance : allInstances) {
						if(inputNs.equals(instance.ns+instance.name)){
							input = instance;
							break;
						}
					}
					
					//for each input and output class names, I verify if exist a possible relation of binds
					for(String inputClassName : input.ListClasses){
						inputClassName = inputClassName.replace(OKCoUploader.getNamespace(), ""); 
						HashMap<String, String> tf1 = new HashMap<String, String>();
						tf1.put("INPUT", inputClassName);
						tf1.put("OUTPUT", outputClassName);

						HashMap<String, String> allowedRelation = BindsProcessor.values.get(tf1);

						if(allowedRelation != null){
							hasAllowedRelation = true;
							break;
						}
					}
				}
				
				String interfaceReturn = "";
				eqInNs = eqInNs.replace(inputInterface.ns, "");
				inputNs = inputNs.replace(inputInterface.ns, "");
				eqOutNs = eqOutNs.replace(outputInterface.ns, "");
				interfaceReturn += eqInNs; 
				interfaceReturn += "#";
				interfaceReturn += inputInterface.name;
				interfaceReturn += "#";

				//the return only can be true if:
				// - has an allowed relation
				// - it is a different equipment
				// - the output interface it is not binded
				// - the input interface it is not binded
				Boolean hasCyclicalRel = false;
				
				if(hasAllowedRelation && !eqInNs.equals(eqOutNs) && !outputInterfaceAlreadyBinded && !inputInterfaceAlreadyConnected){
					hasCyclicalRel = hasCyclicalRelationship(outputInterface.ns, eqOutNs, inputInterface.ns, eqInNs);
				}
				
				if(hasAllowedRelation && !eqInNs.equals(eqOutNs) && !outputInterfaceAlreadyBinded && !inputInterfaceAlreadyConnected && !hasCyclicalRel){
					if(allowedInputInterfaces.contains(interfaceReturn+"false;")){
						allowedInputInterfaces.remove(interfaceReturn+"false;");
					}
					interfaceReturn += "true;";
				}else{
					if(!allowedInputInterfaces.contains(interfaceReturn.replace("true;", "false;"))){
						interfaceReturn += "false;";
					}					
				}

				if(!allowedInputInterfaces.contains(interfaceReturn) && !allowedInputInterfaces.contains(interfaceReturn.replace("false;", "true;"))){
					allowedInputInterfaces.add(interfaceReturn);
				}				
			}
			
			//here, I look for possible connections with physical media inputs
			for (DtoInstance pmInput : physicalMediaInputs) {
				List<DtoInstanceRelation> inPMRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), pmInput.ns+pmInput.name);
				String pmNs = "";
				Boolean inputPMAlreadyConnected = false;
				
				//get namespaces of individuals of some input interface relations
				for (DtoInstanceRelation inRelation : inPMRelations) {
					if(inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.componentOf.Single_Physical_Media.Physical_Media_Input") || inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.componentOf")){
						pmNs = inRelation.Target;
					}else if(inRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.binds_PM_out_interface")){
						inputPMAlreadyConnected = true;
					}
				}
				
				//since I verify the inverse relation of binds_PM_out_interface above, 
				//it's necessary to verify if some output port has the binds_PM_out_interface relation
				//with the actual PM input
				//the block below it's for this purpose
				if(!inputPMAlreadyConnected){
					List<DtoInstance> allInstances = DtoQueryUtil.getIndividuals(OKCoUploader.getInferredModel(), false, false, false);
					for(DtoInstance otherOutput : allInstances){
						for (String otherOutputClassName : otherOutput.ListClasses) {
							otherOutputClassName = otherOutputClassName.replace(OKCoUploader.getNamespace(), "");
							if(otherOutputClassName.equalsIgnoreCase("Output")){
								if(otherOutput.name.equals("out_sotf3")){
									System.out.println();
								}
								List<DtoInstanceRelation> otherOutputRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), otherOutput.ns+otherOutput.name);
								for (DtoInstanceRelation otherOutputRelation : otherOutputRelations) {
									if(otherOutputRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"interface_binds") || otherOutputRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"binds")){
										if((pmInput.ns+pmInput.name).equals(otherOutputRelation.Target)){
											inputPMAlreadyConnected = true;
											break;
										}
									}
								}
								if(inputPMAlreadyConnected){
									break;
								}
							}
						}
						if(inputPMAlreadyConnected){
							break;
						}
					}
				}
				
				Boolean hasAllowedRelation = false;
				
				//for each input and output class names, I verify if exist a possible relation of binds
				for(String pmInputClassName : pmInput.ListClasses){
					pmInputClassName = pmInputClassName.replace(OKCoUploader.getNamespace(), ""); 
					HashMap<String, String> tf1 = new HashMap<String, String>();
					tf1.put("INPUT", pmInputClassName);
					tf1.put("OUTPUT", outputClassName);

					HashMap<String, String> allowedRelation = BindsProcessor.values.get(tf1);

					if(allowedRelation != null){
						hasAllowedRelation = true;
						break;
					}
				}
				
				String interfaceReturn = "";
				pmNs = pmNs.replace(pmInput.ns, "");
				//inputNs = inputNs.replace(pmInput.ns, "");
				eqOutNs = eqOutNs.replace(outputInterface.ns, "");
				interfaceReturn += pmNs; 
				interfaceReturn += "#";
				interfaceReturn += pmInput.name;
				interfaceReturn += "#";
				
				if(hasAllowedRelation && !pmNs.equals(eqOutNs) && !outputInterfaceAlreadyBinded && !inputPMAlreadyConnected){
					if(allowedInputInterfaces.contains(interfaceReturn+"false;")){
						allowedInputInterfaces.remove(interfaceReturn+"false;");
					}
					interfaceReturn += "true;";
				}else{
					if(!allowedInputInterfaces.contains(interfaceReturn.replace("true;", "false;"))){
						interfaceReturn += "false;";
					}					
				}

				if(!allowedInputInterfaces.contains(interfaceReturn) && !allowedInputInterfaces.contains(interfaceReturn.replace("false;", "true;"))){
					allowedInputInterfaces.add(interfaceReturn);
				}	
			}
		}

		return allowedInputInterfaces;
	}
	
	public static DtoInstance getInstanceFromNameSpace(String nameSpace){
		DtoInstance instance = null;
		List<DtoInstance> allInstances = DtoQueryUtil.getIndividuals(OKCoUploader.getInferredModel(), false, false, false);
		for (DtoInstance inst : allInstances) {
			String instNs = inst.name;
			instNs = instNs.replace(inst.ns, "");
			nameSpace = nameSpace.replace(inst.ns, "");
			if(instNs.equals(nameSpace)){
				instance = inst;
				break;
			}
		}
		return instance;
	}
	
	public static ArrayList<DtoInstance> getInstancesFromClass(ConceptEnum inputInterface){
		ArrayList<String> classNamesWithoutNameSpace = new ArrayList<String>();
		classNamesWithoutNameSpace.add(String.valueOf(inputInterface));
		
		ArrayList<DtoInstance> instances = getInstancesFromClasses(classNamesWithoutNameSpace);
		
		return instances;
	}
	
	public static ArrayList<DtoInstance> getInstancesFromClasses(ArrayList<String> classNamesWithoutNameSpace){
		ArrayList<DtoInstance> instances = new ArrayList<DtoInstance>();
		List<DtoInstance> allInstances = DtoQueryUtil.getIndividuals(OKCoUploader.getInferredModel(), false, false, false);
		for (DtoInstance instance : allInstances) {
			for (String classNameWithoutNameSpace : classNamesWithoutNameSpace) {
				Boolean foundInstance = false;
				if(instance.ListClasses.contains(OKCoUploader.getNamespace()+classNameWithoutNameSpace)){
					if(!instances.contains(instance)){
						instances.add(instance);
					}
					foundInstance  = true;
					break;
				}
				if(foundInstance){
					break;
				}
			}			
//			for (String className : instance.ListClasses) {
//				className = className.replace(OKCoUploader.getNamespace(), "");
//				if(className.equalsIgnoreCase(classNameWithoutNameSpace)){
//					
//				}
//			}
		}
		return instances;
	}
	
	public static Boolean hasCyclicalRelationship(String searchForEquipNS, String searchForEquipName, String actualEquipNS, String actualEquipName){
		if(searchForEquipNS.equals(actualEquipNS) && searchForEquipName.equals(actualEquipName)){
			return true;
		}
		
		List<DtoInstanceRelation> equipRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), actualEquipNS+actualEquipName);
		
		ArrayList<String> outIntNss = new ArrayList<String>();
		//get namespaces of individuals of some input interface relations
		for (DtoInstanceRelation eqRelation : equipRelations) {
			if(eqRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"componentOf.Equipment.Output_Interface") || eqRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"componentOf")){
				DtoInstance outIntInstance = getInstanceFromNameSpace(eqRelation.Target);
				for (String classOutIntName : outIntInstance.ListClasses) {
					if(classOutIntName.equals(OKCoUploader.getNamespace()+"Output_Interface")){
						outIntNss.add(eqRelation.Target);
					}
				}
			}
		}
		
		for (String oiNs : outIntNss) {
			DtoInstance outputInterface = getInstanceFromNameSpace(oiNs);
			
			List<DtoInstanceRelation> outIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), outputInterface.ns+outputInterface.name);
			String inIntNs = "";
			//get namespaces of individuals of some input interface relations
			for (DtoInstanceRelation outIntRelation : outIntRelations) {
				if(outIntRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"interface_binds")){
					inIntNs = outIntRelation.Target;
					break;
				}
			}
			//se inIntNs != null
			if(!inIntNs.equals("")){
				List<DtoInstanceRelation> inIntRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), inIntNs);
				String inIntEquipNs = "";
				//get namespaces of individuals of some input interface relations
				for (DtoInstanceRelation inIntRelation : inIntRelations) {
					if(inIntRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"INV.componentOf")){
						inIntEquipNs = inIntRelation.Target;
						break;
					}
				}
				
				String newActualEquipNs = inIntEquipNs.split("#")[0]+"#";
				String newActualEquipName = inIntEquipNs.split("#")[1];
				
				if(hasCyclicalRelationship(searchForEquipNS, searchForEquipName, newActualEquipNs, newActualEquipName)){
					return true;
				}
			}
			
		}
		
		
		return false;
	}
	
	@RequestMapping(value = "/autoBindsAndConnects", method = RequestMethod.POST)
	public String autoBindsAndConnects(HttpServletRequest request){
		autoBinds(request);
		return autoConnects(request);
	}
	
	@RequestMapping(value = "/autoConnects", method = RequestMethod.POST)
	public String autoConnects(HttpServletRequest request){
		return "";
		
	}
	
	@RequestMapping(value = "/autoBinds", method = RequestMethod.POST)
	public String autoBinds(HttpServletRequest request){
		//pego todas as instancias de interface de output nao conectadas
		ArrayList<DtoInstance> outputInterfaces = new ArrayList<DtoInstance>(); 
		List<DtoInstance> allInstances = DtoQueryUtil.getIndividuals(OKCoUploader.getInferredModel(), false, false, false);
		for (DtoInstance instance : allInstances) {
			for (String className : instance.ListClasses) {
				if(className.equalsIgnoreCase(OKCoUploader.getNamespace()+"Output_Interface")){
					List<DtoInstanceRelation> outputInterfaceRelations = ApplicationQueryUtil.GetInstanceRelations(OKCoUploader.getInferredModel(), instance.ns+instance.name);
					boolean alreadyConnected = false;
					for (DtoInstanceRelation outputInterfaceRelation : outputInterfaceRelations) {
						if(outputInterfaceRelation.Property.equalsIgnoreCase(OKCoUploader.getNamespace()+"interface_binds")){
							if((instance.ns+instance.name).equals(outputInterfaceRelation.Target)){
								alreadyConnected  = true;
								break;
							}
						}
					}
					
					if(!alreadyConnected){
						outputInterfaces.add(instance);
						break;
					}					
				}
			}
		}
		
		HashMap<String, ArrayList<String>> uniqueCandidatesForBinds = new HashMap<String, ArrayList<String>>();
		
		for (DtoInstance outputInterface : outputInterfaces) {
			ArrayList<String> candidatesForConnection = getCandidateInterfacesForConnection(outputInterface.ns+outputInterface.name);
			int noCandidates = 0;
			String inputCandidateName = "";
			for (String candidate : candidatesForConnection) {
				if(candidate.contains("true")){
					noCandidates++;
					inputCandidateName = candidate.split("#")[1];
				}
				
				if(noCandidates > 1){
					break;
				}
			}	
			
			if(uniqueCandidatesForBinds.containsKey(inputCandidateName)){
				uniqueCandidatesForBinds.get(inputCandidateName).add(outputInterface.name);
			}else if(noCandidates == 1){
				ArrayList<String> outs = new ArrayList<String>();
				outs.add(outputInterface.name);
				uniqueCandidatesForBinds.put(inputCandidateName, outs);
			}
		}
		
		ArrayList<String> listInstancesCreated = new ArrayList<String>();
		int bindsMade = 0;
		String returnMessage = "Interfaces binded:<br>";
		for(Entry<String, ArrayList<String>> candidates : uniqueCandidatesForBinds.entrySet()) {
			String inputInterface = candidates.getKey();
			ArrayList<String> outs = candidates.getValue();
			
			if(outs.size() == 1){
				provisioningBinds(outs.get(0), inputInterface, request, false, listInstancesCreated);
				bindsMade++;
				returnMessage += outs.get(0);
				returnMessage += " -> ";
				returnMessage += inputInterface;
				returnMessage += "<br>";
			}
			
		}
		
		if(bindsMade>0){
//			try {
////				for (String instanceUri : listInstancesCreated) {
////					HomeController.UpdateAddIntanceInLists(instanceUri);	
////				}
//				//HomeController.UpdateLists();
//			} catch (InconsistentOntologyException e) {
//				e.printStackTrace();
//			} catch (OKCoExceptionInstanceFormat e) {
//				e.printStackTrace();
//			}
		}else{
			returnMessage = "No interfaces binded.";
		}
		
		request.getSession().setAttribute("loadOk", returnMessage);
		
		return VisualizationController.bindsV(request);
	}
	
	public static void getEquipmentsWithRPs(InfModel infModel, String NS, ArrayList<String> equipsWithRps, ArrayList<String> connectsBetweenEqsAndRps, ArrayList<String> connectsBetweenRps){
		if(equipsWithRps == null){
			equipsWithRps = new ArrayList<String>();
		}
		if(connectsBetweenEqsAndRps == null){
			connectsBetweenEqsAndRps = new ArrayList<String>();
		}
		if(connectsBetweenRps == null){
			connectsBetweenRps = new ArrayList<String>();
		}
		ArrayList<DtoInstance> rpInstances = getInstancesFromClass(ConceptEnum.REFERENCE_POINT);
		
		for (DtoInstance rp : rpInstances) {
			List<DtoInstanceRelation> rpRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, rp.ns+rp.name);
			String bindingNs = "";
			String hasFW = "";
			for (DtoInstanceRelation rel : rpRelations) {
				String propertyName = rel.Property.replace(NS, "");
				if(propertyName.equals("INV.binding_is_represented_by")){
					bindingNs = rel.Target;			
				}else if(propertyName.equals("has_forwarding")){
					String cnct = "";
					cnct += rp.name;
					cnct += "#";
					cnct += rel.Target.replace(NS, "");
					connectsBetweenRps.add(cnct);
				}
			}
			
			ArrayList<String> equips = new ArrayList<String>();
			if(!bindingNs.equals("")){
				equips = getEquipmentFromBinding(infModel, NS, bindingNs);
			}
			
			String equipmentWithRp = "";
			if(equips.size() == 1){
				equipmentWithRp += equips.get(0).replace(NS, "");
			}else if(equips.size() >= 2){
				for (String eq : equips) {
					String cnct = "";
					cnct += eq.replace(NS, "");
					cnct += "#";
					cnct += rp.name;
					
					String cnctInv = "";
					cnctInv += rp.name;
					cnctInv += "#";
					cnctInv += eq.replace(NS, "");
					
					if(!connectsBetweenEqsAndRps.contains(cnct) && !connectsBetweenEqsAndRps.contains(cnctInv)){
						connectsBetweenEqsAndRps.add(cnct);
					}
				}								
			}
			equipmentWithRp += "#";
			equipmentWithRp += rp.name;
			
			if(!equipsWithRps.contains(equipmentWithRp)){
				equipsWithRps.add(equipmentWithRp);
			}
			
		}
		
	}
	
	public static ArrayList<String> getEquipmentFromBinding(InfModel infModel, String NS, String bindingName){
		bindingName = bindingName.replace(NS, "");
		List<DtoInstanceRelation> bindingRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, NS+bindingName);
		
		String bindedPort1Ns="";
		String bindedPort2Ns="";
		for (DtoInstanceRelation rel : bindingRelations) {
			String propertyName = rel.Property.replace(NS, "");
			if(propertyName.equals("is_binding")){
				if(bindedPort1Ns.equals("")){
					bindedPort1Ns = rel.Target;
				}else{
					bindedPort2Ns = rel.Target;
					break;
				}
			}
		}
		ArrayList<String> equips = new ArrayList<String>();
		if(!bindedPort1Ns.equals("")){
			//String equipmentNs = getEquipmentFromPort(infModel, NS, bindedPort1Ns, searchEquipmentFromPortToTop(infModel, NS, bindedPort1Ns));
			//if(!equipmentNs.equals("")){
				//equips.add(equipmentNs);
			//}			
			ArrayList<String> equipsNs = getEquipmentFromPort(infModel, NS, bindedPort1Ns, searchEquipmentFromPortToTop(infModel, NS, bindedPort1Ns));
			equips.addAll(equipsNs);
		}
		if(!bindedPort2Ns.equals("")){
			//String equipmentNs = getEquipmentFromPort(infModel, NS, bindedPort2Ns, searchEquipmentFromPortToTop(infModel, NS, bindedPort2Ns));
			//if(!equips.contains(equipmentNs)){
			//if(!equipmentNs.equals("") && !equips.contains(equipmentNs)){
			//	equips.add(equipmentNs);
			//}
			ArrayList<String> equipsNs = getEquipmentFromPort(infModel, NS, bindedPort2Ns, searchEquipmentFromPortToTop(infModel, NS, bindedPort2Ns));
			for (String eqNs : equipsNs) {
				if(!equips.contains(eqNs)){
					equips.add(eqNs);
				}
			}
		}
		return equips;
	}
	
	public static ArrayList<String> getEquipmentFromPort(InfModel infModel, String NS, String bindedPortNs, Boolean searchToTop){
		ArrayList<String> ret = new ArrayList<String>();
		bindedPortNs = bindedPortNs.replace(NS, "");
		
		List<DtoInstanceRelation> portRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, NS+bindedPortNs);
		String outIntNs = "";
		String inIntNs = "";
		String tfNs = "";
		for (DtoInstanceRelation portRel : portRelations) {
			String portRelName = portRel.Property.replace(NS, "");
			if(portRelName.equals("INV.maps_output")){
				outIntNs = portRel.Target;
			}else if(portRelName.equals("INV.maps_input")){
				inIntNs = portRel.Target;
			}else if(portRelName.equals("INV.componentOf")){
				tfNs = portRel.Target;
			}
		}
		
		if(!tfNs.equals("") && outIntNs.equals("") && inIntNs.equals("")){
			tfNs = tfNs.replace(NS, "");
			List<String> tiposPm=QueryUtil.getClassesURI(infModel,NS+tfNs);
			if(tiposPm.contains(NS+"Physical_Media")){
				ret.add(tfNs);
				return ret;
				//return tfNs;
			}
			
			ArrayList<String> nextPorts = new ArrayList<String>(); 
			List<DtoInstanceRelation> tfRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, NS+tfNs);
			String eqNs = "";
			for (DtoInstanceRelation tfRel : tfRelations) {
				String tfRelRelName = tfRel.Property.replace(NS, "");
				if(tfRelRelName.equals("INV.componentOf")){
					eqNs = tfRel.Target;
					eqNs = eqNs.replace(NS, "");
					List<String> tiposEq=QueryUtil.getClassesURI(infModel,NS+eqNs);
					if(tiposEq.contains(NS+"Equipment")){
						ret.add(eqNs);
						return ret;
						//return eqNs;
					}
				}else if(tfRelRelName.equals("componentOf")){
					if(!tfRel.Target.equals(NS+bindedPortNs)){
						List<String> tiposTf=QueryUtil.getClassesURI(infModel,tfRel.Target);
						if(tiposTf.contains(NS+"Input") || tiposTf.contains(NS+"Output")){
							nextPorts.add(tfRel.Target);
						}
					}
				}
				
			}
			
			ArrayList<String> nextRps = getNextRpsFromTf(infModel, NS, bindedPortNs, nextPorts, searchToTop);
			ret.addAll(nextRps);
			System.out.println();
			
		}else if(!outIntNs.equals("")){
			ret.add(getEquipmentFromInterface(infModel, NS, outIntNs));
			return ret;
			//return getEquipmentFromInterface(infModel, NS, outIntNs);
		}else if(!inIntNs.equals("")){
			ret.add(getEquipmentFromInterface(infModel, NS, inIntNs));
			return ret;
			//return getEquipmentFromInterface(infModel, NS, inIntNs);
		}
		
		return ret;
	}
	
	public static Boolean searchEquipmentFromPortToTop(InfModel infModel, String NS, String portNs){
		portNs = portNs.replace(NS, "");
		List<String> tiposPort=QueryUtil.getClassesURI(infModel,NS+portNs);
		if(tiposPort.contains(NS+"Output")){
			return true;
		}
		return false;
	}
	
	public static String getEquipmentFromInterface(InfModel infModel, String NS, String interfaceNs){
		interfaceNs = interfaceNs.replace(NS, "");
		List<DtoInstanceRelation> portRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, NS+interfaceNs);
		
		for (DtoInstanceRelation intRel : portRelations) {
			String intRelName = intRel.Property.replace(NS, "");
			if(intRelName.equals("INV.componentOf")){
				return intRel.Target;
			}
		}
		
		return "";
	}
	
	public static String getRPFromBinding(InfModel infModel, String NS, String bindingNs){
		bindingNs = bindingNs.replace(NS, "");
		List<DtoInstanceRelation> bindingRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, NS+bindingNs);
		
		for (DtoInstanceRelation bindingRel : bindingRelations) {
			String intRelName = bindingRel.Property.replace(NS, "");
			if(intRelName.equals("binding_is_represented_by")){
				return bindingRel.Target;
			}
		}
		
		return "";
	}
	
	public static ArrayList<String> getNextRpsFromTf(InfModel infModel, String NS, String actualPort, ArrayList<String> nextPorts, Boolean searchToTop){
		ArrayList<String> nextRps = new ArrayList<String>();
		for (String portNs : nextPorts) {
			portNs = portNs.replace(NS, "");
			
			List<String> nextPortClasses = QueryUtil.getClassesURI(infModel,NS+portNs);
			List<String> actualPortClasses = QueryUtil.getClassesURI(infModel,NS+actualPort);
			
			if((nextPortClasses.contains(NS+"Output") && actualPortClasses.contains(NS+"Input")) || (nextPortClasses.contains(NS+"Input") && actualPortClasses.contains(NS+"Output"))){
				List<DtoInstanceRelation> portRelations = ApplicationQueryUtil.GetInstanceAllRelations(infModel, NS+portNs);
				
				for (DtoInstanceRelation portRel : portRelations) {
					String portRelName = portRel.Property.replace(NS, "");
					if(portRelName.equals("INV.is_binding")){
						List<DtoInstanceRelation> bindingRelations =ApplicationQueryUtil.GetInstanceAllRelations(infModel, portRel.Target);
						for (DtoInstanceRelation bindingRel : bindingRelations) {
							String bindingRelName = bindingRel.Property.replace(NS, "");
							if(bindingRelName.equals("binding_is_represented_by")){
								nextRps.add(bindingRel.Target);
							}
						}					
					}
				}	
			}
			
					
		}
		
		
		return nextRps;
	}
}
