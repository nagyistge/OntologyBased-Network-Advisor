function graphHandler(graph, app) {

    graph.on('add', function(cell, collection, opt) {
        if (opt.stencil) {
            this.createInspector(cell);
            this.commandManager.stopListening();
            this.inspector.updateCell();
            this.commandManager.listen();
            this.inspector.$('[data-attribute]:first').focus();
        }
    }, app);
    
	// some types of the elements need resizing after they are dropped
	graph.on('add', function(cell, collection, opt) {
		if(isLink(cell)) return;
		if (!opt.stencil) return;

		var type = cell.attributes.type;
		var subtype = cell.attributes.subtype;
		
		// configuration of resizing
		var sizeMultiplierTypeWidth = { 	'bpmn.Pool': 5, // layers
								'basic.Path': 1.3, // transport functions
								}[type];			
		var sizeMultiplierTypeHeight = { 	'bpmn.Pool': 3.5, // layers
									'basic.Path': 1.3, // transport functions
								}[type];

		var sizeMultiplierSubtypeWidth = {  'in': 0.5, // in port
										'out': 0.5, // out port
									}[subtype];
		var sizeMultiplierSubTypeHeight = {  'in': 0.5, // in port
										'out': 0.5, // out port
										'AF': 0.7, // AF transport function
									}[subtype];
		
		var sizeMultiplierWidth = sizeMultiplierSubtypeWidth ? sizeMultiplierSubtypeWidth : sizeMultiplierTypeWidth;
		var sizeMultiplierHeight = sizeMultiplierSubTypeHeight ? sizeMultiplierSubTypeHeight : sizeMultiplierTypeHeight;
					
		var originalSize = cell.attributes.size;
		if (sizeMultiplierWidth && sizeMultiplierHeight) {
			cell.set('size', {
				width: originalSize.width * sizeMultiplierWidth,
				height: originalSize.height * sizeMultiplierHeight
			});
		}
		
		sizeMultiplierWidth = 0;
		sizeMultiplierHeight = 0;
	}, app);
	
	// validar inserção de transport functions no grafo
	graph.on('add', function(cell, collection, opt) {
		if(isLink(cell)) return;
		if(isNotTransportFunction(cell)) return;
		
		var tFunctionID = cell.id;

		var tFunctionType = cell.attributes.subtype;
		var cardID = this.cardID;
		var tFunctionName = getName(tFunctionType);

		var containerName = this.cardName;
		var containerType = 'card';

		var position = cell.attributes.position;
		var size = cell.attributes.size;
		var area = g.rect(position.x, position.y, size.width, size.height);

		var parent;
		// get all elements below the added one
		_.each(graph.getElements(), function(e) {

			var position = e.attributes.position;
			var size = e.attributes.size;
			if (e.id !== cell.id && area.intersect(g.rect(position.x, position.y, size.width, size.height))) {
				parent = e;
			}
		});

		if(parent) { // existe algum elemento abaixo

			if(isLayer(parent)){ // elemento abaixo é uma camada
				containerName = parent.attributes.subtype;
				containerType = 'layer';
				// consultar ontologia para inserção de transport function no layer
				insertTransportFunction();

			} else { // elemento abaixo não é um container
				this.generateAlertDialog('Please, add the transport function on the paper or a layer.');
				this.skipOntologyRemoveHandler = true;
				cell.remove();
			}
		} else { // não existe elemento abaixo
			// consultar ontologia para inserção de transport function diretamente no card
			insertTransportFunction();
		}
		
		function insertTransportFunction() {
			console.log('try to insert ' +tFunctionID+ ' name: ' +tFunctionName+ ';type: ' +tFunctionType+ ';layer: ' +containerName+ ';card: ' +cardID);
			
			var result = canCreateTransportFunction(tFunctionID, tFunctionName, tFunctionType, containerName, containerType, cardID);
			if(result === "true") {
				result = createTransportFunction(tFunctionID, tFunctionName, tFunctionType, containerName, containerType, cardID);
				
				if(result === "success") {	
					if(parent) {
						Util.isAddingTransportFunction = true;
						parent.embed(cell);
					}
					cell.attr({
						text: {text: tFunctionName}
					});
					nextName(tFunctionType);
				} else {
					app.generateAlertDialog(result);
					app.skipOntologyRemoveHandler = true;
					cell.remove();
				}
			} else {
				app.generateAlertDialog(result);
				app.skipOntologyRemoveHandler = true;
				cell.remove();
			}
		};
	}, app);
	
	// validar inserção de interfaces no grafo
    graph.on('add', function(cell) {

		if(isLink(cell)) return;
		if(isNotInterface(cell)) return;
		
    	var cellSubType = cell.attributes.subtype;

    	var position = cell.attributes.position;
		var size = cell.attributes.size;
		var area = g.rect(position.x, position.y, size.width, size.height);

		var portID = cell.id;
		var portType = cellSubType;
		var portName = getName(portType);
		
		var parent;
		// get all elements below the added one
		_.each(graph.getElements(), function(e) {
		
			var position = e.attributes.position;
			var size = e.attributes.size;
			if (e.id !== cell.id && area.intersect(g.rect(position.x, position.y, size.width, size.height))) {
				parent = e;
			}
		});
		
		if(parent) { // existe algum elemento abaixo
			var parentType = parent.attributes.type;
			
			if(parentType === TypeEnum.TRANSPORT_FUNCTION){ // elemento abaixo é um transport function
				
					var transportFunctionID = parent.id;
					var tFunctionName = parent.attributes.attrs.text.text;
					var tFunctionType = parent.attributes.subtype;
					console.log('try to create port ' +portID+ ';name: ' +portName+ ';TF: ' +transportFunctionID);
					var result = createPort(portID, portName, portType, transportFunctionID, tFunctionName, tFunctionType)
					
					if(result === "success") {
					
						var newLink = new joint.dia.Link({	source: {id: transportFunctionID}, target: {id: portID}, attrs: { '.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' }}});
		    			graph.addCell(newLink);
		    			
		    			// Move the port to the superior (in port) or inferior (out port) bar
		    			if(portType === 'in') {
		    				cell.transition('position/y', 15, {});
		    				this.barIn.embed(cell);
		    			}
		    			else {
		    				cell.transition('position/y', 955, {});
		    				this.barOut.embed(cell);
		    			}
						cell.attr({
							text: {text: portName}
						});
		    			nextName(portType);
					} else {
						this.generateAlertDialog(result);
						this.skipOntologyRemoveHandler = true;
						cell.remove();
					}
			} else { // elemento abaixo não é um transport function uma camada 
				this.generateAlertDialog('Please, add the port over a transport function.');
				this.skipOntologyRemoveHandler = true;
				cell.remove();
			}
			
		} else { // nenhum elemento abaixo
			this.generateAlertDialog('Please, add the port over a transport function.');
			this.skipOntologyRemoveHandler = true;
			cell.remove();
		}
		
    }, app);
	
    /* ------ AUXILIAR FUNCTIONS ------- */
	// Check if cell is not a link
	function isNotLink(cell) {
	    if (cell.attributes.type !== 'link') return true;
	};

	// Check if cell is a link
	function isLink(cell) {
	    if (cell.attributes.type === 'link') return true;
	};

	// Check if cell is a transport function
	function isTransportFunction(cell) {
		if (cell.attributes.type === TypeEnum.TRANSPORT_FUNCTION) return true;
	};
	
	// Check if cell is not a transport function
	function isNotTransportFunction(cell) {
		if (cell.attributes.type !== TypeEnum.TRANSPORT_FUNCTION) return true;
	};

	//Check if cell is an interface
	function isInterface(cell) {
		var cellSubType = cell.attributes.subtype;
		if(cellSubType === 'out' || cellSubType === 'in') return true;
	};

	//Check if cell is not an interface
	function isNotInterface(cell) {
		var cellSubType = cell.attributes.subtype;
		if(cellSubType !== 'out' && cellSubType !== 'in') return true;
	};

	//Check if cell is a layer
	function isLayer(cell) {
		if (cell.attributes.type === TypeEnum.LAYER) return true;
	};
	
	// Get name for properly element being added
	function getName(elementSubtype) {
		if(elementSubtype === 'in') return 'in_' +app.inPortCounter;
		if(elementSubtype === 'out') return 'out_' +app.outPortCounter;
		if(elementSubtype === 'AF') return 'AF_' +app.AFCounter;
		if(elementSubtype === 'TTF') return 'TTF_' +app.TTFCounter;
	};
	
	// Increment the counter of the properly element
	function nextName(elementSubtype) {
		if(elementSubtype === 'in') app.inPortCounter++;
		if(elementSubtype === 'out') app.outPortCounter++;
		if(elementSubtype === 'AF') app.AFCounter++;
		if(elementSubtype === 'TTF') app.TTFCounter++;
	};
};