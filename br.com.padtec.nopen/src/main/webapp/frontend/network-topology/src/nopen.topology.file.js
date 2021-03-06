nopen.topology.File = Backbone.Model.extend({
	
	app : undefined,
	
	initialize : function(){
		
	},
	
	setApp : function(app) {
		this.app = app;
	},
	
	getAllEquipments : function() {
		
		var equipments = undefined;
		
		$.ajax({
		   type: "POST",
		   async: false,
		   url: "getAllEquipmentsToMatch.htm",
		   dataType: 'json',
		   success: function(data){
			   equipments = data;
		   },
		   error : function(e) {
			   alert("error: " + e.status);
		   }
		});
		
		return equipments;
		
	},
	
	openEquipment : function(filename) {
		
		var equipment = undefined;
		
		$.ajax({
			type: "POST",
			async: false,
			url: "openEquipmentInTopology.htm",
			data: {
				'filename' : filename
			},
			dataType: 'json',
			success: function(data){
				equipment = data;
			},
			error : function(e) {
				alert("error: " + e.status);
			}
		});
		
		return equipment;
		
	},
	
	openEquipmentCard : function(filename, cardId) {
		
		var card = undefined;
		
		$.ajax({
			type: "POST",
			async: false,
			url: "openEquipmentCardInTopology.htm",
			data: {
				'filename' : filename,
				'cardName' : cardId
			},
			dataType: 'json',
			success: function(data){
				card = data;
			},
			error : function(e) {
				alert("error: " + e.status);
			}
		});
		
		return card;
		
	},
	
	getAllTopologies : function(graph) {
		
		var topologies = [];
		
		$.ajax({
		   type: "POST",
		   async: false,
		   url: "getAllTopologies.htm",
		   dataType: 'json',
		   success: function(data){ 	
			   topologies = data;
		   },
		   error : function(e) {
			   alert("error: " + e.status);
		   }
		});
		
		return topologies;
		
	},
	
	//Method to get paramentes from url
	getUrlParameter : function(sParam) {
	    var sPageURL = window.location.search.substring(1);
	    var sURLVariables = sPageURL.split('&');
	    for (var i = 0; i < sURLVariables.length; i++) 
	    {
	        var sParameterName = sURLVariables[i].split('=');
	        if (sParameterName[0] == sParam) 
	        {
	            return sParameterName[1];
	        }
	    }
	},   
	
	openTopologyFromURL : function(graph, filename){
		
		this.openTopology(graph, filename)
		
	},
	
	generateOpenTopologyDialog : function(graph){
		
		var $this = this;
		
		var topologies = $this.getAllTopologies(graph);
		
		var content = '<form id="open">';
		for(var i = 0; i < topologies.length; i++){
			if(i == 0){
				content = content + '<input type="radio" name="topology" value="' + topologies[i].topology + '" checked>' 
						+ '<label>' + topologies[i].topology + '</label> <br>';
			}
			else{
				content = content + '<input type="radio" name="topology" value="' + topologies[i].topology + '">' 
						+ '<label>' + topologies[i].topology + '</label><br>';
			}

		}
		content = content +  '</form>';
		
		var dialog = new joint.ui.Dialog({
			width: 300,
			type: 'neutral',
			title: 'Open Topology',
			content: content,
			buttons: [
				{ action: 'cancel', content: 'Cancel', position: 'left' },
				{ action: 'open', content: 'Open', position: 'left' }
			]
		});
		dialog.on('action:open', open);
		dialog.on('action:cancel', dialog.close);
		dialog.open();

		function open(){
			var filename = $('input[name=topology]:checked', '#open').val();
			$this.openTopology(graph, filename);
			dialog.close();
		}
		
	},
	
	openTopology : function(graph, filename){
		
		var $this = this;
		
		$.ajax({
		   type: "POST",
		   url: "openTopology.htm",
		   async: false,
		   data: {
			   'filename' : filename
		   },
		   dataType: 'json',
		   success: function(data){ 		   
			   graph.fromJSON(data);
			   
			   $.each(graph.getElements(), function(index, node) {

				 
				   $this.openTopologyEquipment(filename, node.id , node);
				   
			   });
		   },
		   error : function(e) {
			   alert("error: " + e.status);
		   }
		});
		
	},
	
	openTopologyEquipment : function(filename, nodeId , node) {
		
		var $this = this;
		var model = this.app.model;
		var owl = this.app.owl;
		
		$.ajax({
		   type: "POST",
		   url: "openTopologyEquipment.htm",
		   async: false,
		   data: {
			   'filename' : filename,
			   'nodeId' : nodeId,
		   },
		   dataType: 'json',
		   success: function(equipment){ 	
			   
				$.each(equipment.cells, function(index, element){
					if(element.subType === 'Card') {
						owl.parseCardToOWL(node , element);
					}	
				});
			 
				model.addNewEquipment(nodeId, equipment);
			   
		   },
		   error : function(e) {
			   alert("error: " + e.status);
		   }
		});
		
//		model.printEquipments();
		
	},
	
	isAllNodesMatched : function(app) {
		
		var graph = app.graph;
		var isAllNodesMatched = true;
		
		$.each(graph.getElements(), function(index, node) {
			
			if(node.attr('equipment/id') === '') {
				isAllNodesMatched = false;
			}
			
		});
		
		return isAllNodesMatched;
		
	},
	
	generateSaveTopologyDialog : function(app){
		
		var $this = this;
		
		//check if all nodes are matched
		if(!$this.isAllNodesMatched(app)) {
			alert('All nodes need to be matched!');
			return;
		}
		
		var content = '<div id="save-dialog" title="Save Topology">'
			+ 'Name: <input type="text" id="save-filename" value="' + $('#filename').val() + '"/>'
			+ '</div>'
			+ '<div id="name-error-message">' + 'Name cannot be empty!' + '</div>';
			
		var dialog = new joint.ui.Dialog({
			width: 300,
			type: 'neutral',
			title: 'Save Topology',
			content: content,
			buttons: [
				{ action: 'cancel', content: 'Cancel', position: 'left' },
				{ action: 'save', content: 'Save', position: 'left' }
			]
		});
		
		dialog.on('action:save', checkTopologyFile);
		dialog.on('action:cancel', cancel);

		dialog.open();
		
		function cancel(){
			dialog.close();
		}
		
		function checkTopologyFile(){
			
			var filename = $("#save-filename").val();
			
			if(filename == ""){
				$('#name-error-message').show();
			}
			else{
				$.ajax({
				   type: "POST",
				   url: "checkTopologyFile.htm",
				   data: {
					 'filename': filename,
				   },
				   success: function(data){ 		   
					   
					   if(data == "exist"){		   
						   if (confirm('The file already exist, do you want to replace it?')) {
							   $this.saveTopology(app, filename);
							   dialog.close();
						   } 
					   }
					   else{
						   $this.saveTopology(app, filename);
						   dialog.close();
					   }
				   },
				   error : function(e) {
					   alert("error: " + e.status);
					   dialog.close();
				   }
				});
			}
			
		};
	},
	
	saveTopology : function(app, filename){
		
		var $this = this;
		
		var graph = app.graph;
		var paper = app.paper;
		
		var topologySVG = "";
		paper.toSVG( function(svgString) {
			topologySVG = svgString;
		});
		
		$('#filename').val(filename);
		
		$.ajax({
		   type: "POST",
		   url: "saveTopology.htm",
		   async: false,
		   data: {
			 'filename': filename,
			 'graph': JSON.stringify(graph.toJSON()),
			 'svg' : topologySVG,
		   },
		   success: function(){ 		   
			   $this.saveTopologyEquipment(filename);
		   },
		   error : function(e) {
			   alert("error: " + e.status);
		   }
		});
		
	},
	
	saveTopologyEquipment : function(filename) {
		
		var model = this.app.model;

		var equipments = model.getEquipments();
		
		$.each(equipments, function(nodeId, equipment) {
			
			$.ajax({
			   type: "POST",
			   url: "saveTopologyEquipment.htm",
			   async: false,
			   data: {
				 'filename': filename,
				 'nodeId': nodeId,
				 'equipment' : JSON.stringify(equipment),
			   },
			   success: function(){ 		   
				   
			   },
			   error : function(e) {
				   alert("error: " + e.status);
			   }
			});
			
		});
		
		var saveDialog = new joint.ui.Dialog({
			type: 'neutral' ,
			width: 420,
			draggable: false,
			title: 'Topology Saved! ',
			content: 'The Topology ' + filename + ' was saved',
			open: function() {}
		});
		
		saveDialog.open();
		
	},
	
	
	
});