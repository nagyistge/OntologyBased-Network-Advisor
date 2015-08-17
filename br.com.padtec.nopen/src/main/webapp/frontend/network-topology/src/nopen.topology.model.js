nopen.topology.Model = Backbone.Model.extend({
	
	app : undefined,
	
	topologyId : undefined,
	equipments : undefined,
	
	initialize : function(){
		this.topologyId = joint.util.uuid();
		this.equipments = {};
	},
	
	setApp : function(app) {
		this.app = app;
	},
	
	getTopologyId : function() {
		return this.topologyId;
	},
	
	getEquipments : function() {
		return this.equipments;
	},
	
	addNewEquipment : function (nodeId, equipment) {
		this.equipments[nodeId] = equipment;
	},
	
	printEquipments : function() {
		console.log('Equipments: ' + JSON.stringify(this.equipments));
	},
	
	generetaMatchEquipmentToNodeDialog : function(node) {
		
		var $this = this;
		var file = this.app.file;
		
		var equipments = file.getAllEquipments();
		
		var content = '<form id="match">';
		for(var i = 0; i < equipments.length; i++){
			if(i == 0){
				content = content + '<input type="radio" name="equipment" value="' + equipments[i].equipment + '" checked>' 
						+ '<label>' + equipments[i].equipment + '</label> <br>';
			}
			else{
				content = content + '<input type="radio" name="equipment" value="' + equipments[i].equipment + '">' 
						+ '<label>' + equipments[i].equipment + '</label><br>';
			}

		}
		content = content +  '</form>';
		
		var dialog = new joint.ui.Dialog({
			width: 300,
			type: 'neutral',
			title: 'Match Equipment to Node',
			content: content,
			buttons: [
				{ action: 'cancel', content: 'Cancel', position: 'left' },
				{ action: 'match', content: 'Match', position: 'left' }
			]
		});

		dialog.on('action:match', matchEquipmentToNode);
		dialog.on('action:cancel', dialog.close);
		dialog.open();
		
		function matchEquipmentToNode() {
			
			var filename = $('input[name=equipment]:checked', '#match').val();
			
			//match equipment to node
			$this.matchEquipmentToNode(node, filename);
			
			dialog.close();
		}
		
	},
	
	matchEquipmentToNode : function(node, filename) {
		
		var $this = this;
		var file = this.app.file;
		var util = this.app.util;
		
		//open equipment
		var equipment = file.openEquipment(filename);
		
		//open cards
		$.each(equipment.cells, function(index, element) {
			
			if(element.subType === 'Card') {
				var cardId = element.id;
				var card = file.openEquipmentCard(filename, cardId);
				
				//add card in card data
				equipment.cells[index].attrs.data = card;
			}
			
		});
		
		//generate new ids
		equipment = util.generateNewEquipmentIDs(equipment);
		
		//add new equipment
		$this.addNewEquipment(node.id, equipment);
		
		node.attr('text/text', filename);
		
		node.attr('equipment/id', node.id);
		node.attr('equipment/name', filename);
		
		//print equipments
		$this.printEquipments();
		
	},
	
});