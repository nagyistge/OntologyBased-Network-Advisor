var Rappid = Backbone.Router.extend({

	routes: {
		'*path': 'home'
	},

	initialize: function(options) {

		this.options = options || {};
	},

	home: function() {

		this.initializeEditor();
	},

	initializeEditor: function() {

		this.inspectorClosedGroups = {};

		this.initializePaper();
		this.initializeStencil();
		this.initializeSelection();
		this.initializeHaloAndInspector();
		this.initializeNavigator();
		this.initializeClipboard();
		this.initializeCommandManager();
		this.initializeToolbar();
		// Intentionally commented out. See the `initializeValidator()` method for reasons.
		// Uncomment for demo purposes.
		this.initializeValidator();
		// Commented out by default. You need to run `node channelHub.js` in order to make
		// channels working. See the documentation to the joint.com.Channel plugin for details.
		//this.initializeChannel('ws://jointjs.com:4141');
		if (this.options.channelUrl) {
			this.initializeChannel(this.options.channelUrl);
		}
	},

	// Create a graph, paper and wrap the paper in a PaperScroller.
	initializePaper: function() {

		this.graph = new joint.dia.Graph;

		this.graph.on('add', function(cell, collection, opt) {
			if (opt.stencil) {
				this.createInspector(cell);
				this.commandManager.stopListening();
				this.inspector.updateCell();
				this.commandManager.listen();
				this.inspector.$('[data-attribute]:first').focus();

				//addshelf(args); | args -> "rack0" | "rack0","slot1"
				console.log(cell.parent);
				//var gambiMethod = "add"+subType+"("+args+");";

				// Identificações temporarias
				//console.log(cell.attributes.type);
				//var subtype = cell.get('subType');
				//console.log(subtype);
				//var type = cell.get('type');
				//console.log(type);
				//if(type === 'bpmn.Pool' ){
				//	console.log(cell.attributes.lanes.label , " was just added. ");
				//}else{"A/An ", cell.a
				//	if(type === 'devs.Atomic'){
				//		console.log("A/An", cell.attributes.attrs, " was just added. ");
				//	}else{
				//		console.log("A/An", cell.attributes.attrs.text.text, " was just added. ");
				//	}
				//}
			}
		}, this);

		// some types of the elements need resizing after they are dropped
		this.graph.on('add', function(cell, collection, opt) {

			if (!opt.stencil) return;

			if(cell.get('type') === 'link') return;
			var type = cell.get('type');

			// configuration of resizing
			var sizeMultiplier = { 	'bpmn.Pool': 6,
					'basic.Circle': 1 ,
					'basic.Rect': 1.5,
					'AF':1,
					'TTF': 1
			}[type];

			if (sizeMultiplier) {
				var originalSize = cell.get('size');
				cell.set('size', {
					width: originalSize.width * sizeMultiplier,
					height: originalSize.height * sizeMultiplier
				});
			}

			sizeMultiplier = 0;
		}, this);

		// when a cell is added on another one, it should be embedded
		this.graph.on('add', function(cell) {

			//console.log(JSON.stringify(cell));
			if(cell.get('type') === 'link') return;
			var subtype = cell.get('subType');
			var position = cell.get('position');
			var size = cell.get('size');
			var area = g.rect(position.x, position.y, size.width, size.height);

			var parent;			
			_.each(this.graph.getElements(), function(e) {

				var position = e.get('position');
				var size = e.get('size');
				if (e.id !== cell.id && area.intersect(g.rect(position.x, position.y, size.width, size.height))) {
					parent = e;
				}
			});

			// See if the cell can connect with the parent : true or false


			if(parent) {
				//outros equipamentos conectados
				var filhos = parent.getEmbeddedCells().length;
				var soa = parent.getEmbeddedCells() ;
				console.log(filhos);
				//parent position and size
				var pposition = parent.get('position');
				var psize = parent.get('size');

				//child position and size
				var cposition = cell.get('position');
				var csize = cell.get('size');

				if(parent.get('subType') === 'rack') {

					//Aumento de tamanho e re posicionamento
					var newpositionx = pposition.x + 15 ;
					var nempositiony;
					if (parent.getEmbeddedCells().length === '0'){
						nempositiony = pposition.y + 20 ;
					}else {
						nempositiony = pposition.y + 20 + ((filhos) * (80));
					};
					// Prototype : check order
					if(cell.get('subType') === 'shelf'){
						parent.embed(cell);
						this.embedOrConnect(parent, cell);

						parent.set('size' , { 
							width: psize.width  ,
							height:	240 + ((parent.getEmbeddedCells().length - (1) ) * 70)
						});
						cell.set('position', {
							x: newpositionx  ,
							y: nempositiony 
						});
					}else{
						console.log('A ordem está errada');
					}

				}else{
					if(parent.get('subType') === 'shelf'){
						// selecionar o rack
						var grandparentId = parent.get('parent');
						if (!grandparentId) return;

						var grandparent = this.graph.getCell(grandparentId);

						var newpositiony = pposition.y + 7;
						var nempositionx;
						if (parent.getEmbeddedCells().length === '0'){
							nempositionx = pposition.x + 10;
						}else {
							nempositionx = pposition.x + 20 + ((filhos) * (75));
						};
						if(cell.get('subType') === 'slot'){
							parent.embed(cell);
							this.embedOrConnect(parent, cell);

							parent.set('size' , { 
								width: 100 + ((parent.getEmbeddedCells().length - (1) ) * 75) ,
								height:	parent.get('size').height});

							cell.set('position', {
								x: nempositionx,
								y: newpositiony
							});

							if (grandparent.get('size').width < parent.get('size').width){
								grandparent.set ('size' , {
									width: grandparent.get('size').width + 100 ,
									height: grandparent.get('size').height
								});
							};
						}else{
							console.log("A ordem está errada");
						}

					}else {
						if(parent.get('subType') === 'slot'){
							var increasew = (psize.width + 20 ) ;
							var increaseh = (psize.height) ;

							//reposicionamento da cell
							var newpositiony = pposition.y + 16 ;
							var newpositionx;
							if (parent.getEmbeddedCells().length === '0'){
								newpositionx = pposition.x + 15;
								console.log('nova posição da cell em x ' , newpositionx);
							}else {
								newpositionx = pposition.x + 15 + ((filhos) * (29));
								console.log('nova posição da cell em x sendo o segundo ' , newpositionx);
							};

							// Se já possuir um card cancela;
							console.log(parent.getEmbeddedCells().length);
							if (parent.getEmbeddedCells().length === 1){
								console.log("O slot já possui um card");
							}else{
								parent.embed(cell);
								this.embedOrConnect(parent, cell);

								parent.set('size' , { 
									width: increasew  ,
									height:	increaseh});
								cell.set('size' , {
									width: 12.5 ,
									height: 20							
								})
								cell.set('position' , {
									x : newpositionx ,
									y : newpositiony
								});
							}
						}else{
							if(parent.get('subType') === 'card' || 'supervisor' ){
								// configurar as portas de input e output quando sair do itu
							}
						}
					}
				}
			}
		}, this);

		this.graph.on('remove' , function (cell) {

			var parentId = cell.get('parent');
			if (!parentId) return;

			var parent = this.graph.getCell(parentId);


			if(parent.get('subType') === 'rack') {

				//console.log(parent.getEmbeddedCells().length);

				parent.set('size' , { 
					width: parent.get('size').width  ,
					height:	240 + ((parent.getEmbeddedCells().length - (2) ) * 70)
				});
				//console.log(parent.get('size'));

			}else{
				if(parent.get('subType') === 'shelf'){

					var grandparentId = parent.get('parent');
					if (!grandparentId) return;

					var grandparent = this.graph.getCell(grandparentId);

					parent.set('size' , { 
						width: 120 + ((parent.getEmbeddedCells().length - (2) ) * 70) ,
						height:	parent.get('size').height});

					grandparent.set('size', {
						width: grandparent.get('size').width ,
						height: grandparent.get('size').height
					});				


				}else{
					if(parent.get('subType') === 'slot'){
						var grandparentId = parent.get('parent');
						if (!grandparentId) return;

						var grandparent = this.graph.getCell(grandparentId);

						parent.set('size' , {
							width: 22.5 ,
							height: 52.5
						});

//						grandparent.set('size', {
//						width: 120 + ((parent.getEmbeddedCells().length - (1) ) * 70),
//						height: grandparent.get('size').height
//						})



					}if(parent.get('subType') === 'card' || 'supervisor' ) {

					}
				}
			}

		},this); 

//		this.graph.on('all' , function(a) {
//		console.log(a);
//		},this);

		this.graph.on('change:size', function(cell, newPosition, opt) {

			if (opt.skipParentHandler) return;

			if (cell.get('embeds') && cell.get('embeds').length) {
				// If we're manipulating a parent element, let's store
				// it's original size to a special property so that
				// we can shrink the parent element back while manipulating
				// its children.
				cell.set('originalSize', cell.get('size'));
			}
		});


		this.graph.on('change' , function (cell) {

			var parentId = cell.get('parent');
			if (!parentId) return;

			var parent = this.graph.getCell(parentId);
			var parentBbox = parent.getBBox();
			var cellBbox = cell.getBBox();

			if (parentBbox.containsPoint(cellBbox.origin()) && parentBbox.containsPoint(cellBbox.topRight()) &&
					parentBbox.containsPoint(cellBbox.corner()) &&
					parentBbox.containsPoint(cellBbox.bottomLeft())) {

				return;
			}
			cell.set('position', cell.previous('position'));

		},this);

		this.graph.on('change:position', function(cell, newPosition, opt) {

			if (opt.skipParentHandler) return;

			if (cell.get('embeds') && cell.get('embeds').length) {
				// If we're manipulating a parent element, let's store
				// it's original position to a special property so that
				// we can shrink the parent element back while manipulating
				// its children.
				cell.set('originalPosition', cell.get('position'));
			}

			var parentId = cell.get('parent');
			if (!parentId) return;

			var parent = this.graph.getCell(parentId);
			var parentBbox = parent.getBBox();

			if (!parent.get('originalPosition')) parent.set('originalPosition', parent.get('position'));
			if (!parent.get('originalSize')) parent.set('originalSize', parent.get('size'));

			var originalPosition = parent.get('originalPosition');
			var originalSize = parent.get('originalSize');

			var newX = originalPosition.x;
			var newY = originalPosition.y;
			var newCornerX = originalPosition.x + originalSize.width;
			var newCornerY = originalPosition.y + originalSize.height;

			_.each(parent.getEmbeddedCells(), function(child) {

				var childBbox = child.getBBox();

				if (childBbox.x < newX) { newX = childBbox.x; }
				if (childBbox.y < newY) { newY = childBbox.y; }
				if (childBbox.corner().x > newCornerX) { newCornerX = childBbox.corner().x; }
				if (childBbox.corner().y > newCornerY) { newCornerY = childBbox.corner().y; }
			});

			// Note that we also pass a flag so that we know we shouldn't adjust the
			// `originalPosition` and `originalSize` in our handlers as a reaction
			// on the following `set()` call.
			parent.set({
				position: { x: newX, y: newY },
				size: { width: newCornerX - newX, height: newCornerY - newY }
			}, { skipParentHandler: true });
		},this);


		this.paper = new joint.dia.Paper({
			width: 1000,
			height: 1000,
			gridSize: 10,
			perpendicularLinks: true,
			model: this.graph,
			// RF: Permitir que nós contenham outros nós
//			embeddingMode: true,
			// RF: Ao selecionar uma porta, destacar portas disponíveis para conexão com aquela
			markAvailable: true,
			// RF: Inserir 'snap link' às conexões
			snapLinks: { radius: 75 },
			defaultLink: new joint.dia.Link({
				attrs: {
					// @TODO: scale(0) fails in Firefox
					'.marker-source': { d: 'M 10 0 L 0 5 L 10 10 z', transform: 'scale(0.001)' },
					'.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' },
					'.connection': {
						stroke: 'black'
							// filter: { name: 'dropShadow', args: { dx: 1, dy: 1, blur: 2 } }
					}
				}
			})
		});

		this.paperScroller = new joint.ui.PaperScroller({
			autoResizePaper: true,
			padding: 50,
			paper: this.paper
		});

		this.paperScroller.$el.appendTo('.paper-container');

		this.paperScroller.center();

		this.paper.on('cell:pointerdblclick', function( cellView , evt, x, y) {
			var cellId = cellView.model.id;
			var equipament = this.graph.getCell(cellId);
			if((equipament.get('subType')) === 'card' || (equipament.get('subType')) === 'supervisor') {
				//will create an ITU view
				console.log('opening ITU studio');
				var a = this.graph.toJSON();
				console.log(a);
				console.log(JSON.stringify(a));

				generateSaveEquipmentDialog(app.graph);
				
				function generateSaveEquipmentDialog(graph){

					dialog = $("#save-dialog").dialog({
						autoOpen: false,
						height: 180,
						width: 350,
						modal: true,
						buttons: { 
							"Save": checkEquipmentFile,  
							Cancel: function() {
								dialog.dialog( "close" );
							}
						},
						close: function() { }
					});

					$("#save-dialog").dialog("open");

					function checkEquipmentFile(){

						if($("#save-filename").val() == ""){
							alert("File name cannot be empty!")
						}
						else{
							$.ajax({
								type: "POST",
								url: "checkEquipmentFile.htm",
								data: {
									'filename': $("#save-filename").val(),
								},
								success: function(data){ 		   

									if(data == "exist"){		   
										if (confirm('The file already exist, do you want to replace it?')) {
											saveEquipment();
										} 
									}
									else{
										saveEquipment();
									}
								},
								error : function(e) {
									alert("error: " + e.status);
									$("#save-dialog").dialog("close");
								}
							});
						}

					};


					function saveEquipment(){

						$.ajax({
							type: "POST",
							url: "saveEquipment.htm",
							data: {
								'filename': $("#save-filename").val(),
								'graph': JSON.stringify(graph.toJSON()),
							},
							success: function(){ 		   
								alert($("#save-filename").val() + ' saved successfully!');
								$("#save-dialog").dialog("close");
							},
							error : function(e) {
								alert("error: " + e.status);
								$("#save-dialog").dialog("close");
							}
						});


					};

				}

			}	

		},this);


		this.graph.on('add', this.initializeLinkTooltips, this);

		$('.paper-scroller').on('mousewheel DOMMouseScroll', _.bind(function(evt) {

			if (_.contains(KeyboardJS.activeKeys(), 'alt')) {
				evt.preventDefault();
				var delta = Math.max(-1, Math.min(1, (evt.originalEvent.wheelDelta || -evt.originalEvent.detail)));
				var offset = this.paperScroller.$el.offset();
				var o = this.paperScroller.toLocalPoint(evt.pageX - offset.left, evt.pageY - offset.top);
				this.paperScroller.zoom(delta / 10, { min: 0.2, max: 5, ox: o.x, oy: o.y });
			}

		}, this));

		this.snapLines = new joint.ui.Snaplines({ paper: this.paper });
	},

	initializeLinkTooltips: function(cell) {

		if (cell instanceof joint.dia.Link) {

			var linkView = this.paper.findViewByModel(cell);
			new joint.ui.Tooltip({
				className: 'tooltip small',
				target: linkView.$('.tool-options'),
				content: 'Click to open Inspector for this link',
				left: linkView.$('.tool-options'),
				direction: 'left'
			});
		}
	},

//	Create and populate stencil.
	initializeStencil: function() {

		this.stencil = new joint.ui.Stencil({
			graph: this.graph,
			paper: this.paper,
			width: 220,
			groups: Stencil.groups,
			search: {
				'*': ['type','attrs/text/text','attrs/.label/text'],
				'org.Member': ['attrs/.rank/text','attrs/.name/text']
			}
		});

		$('.stencil-container').append(this.stencil.render().el);

		this.stencil.$el.on('contextmenu', function(evt) { evt.preventDefault(); });
		$('.stencil-paper-drag').on('contextmenu', function(evt) { evt.preventDefault(); });

		var layoutOptions = {
				columnWidth: this.stencil.options.width / 2 - 10,
				columns: 2,
				rowHeight: 200,
				resizeToFit:false,
				dy: 10,
				dx: 10
		};

		_.each(Stencil.groups, function(group, name) {

			this.stencil.load(Stencil.shapes[name], name);
			joint.layout.GridLayout.layout(this.stencil.getGraph(name), layoutOptions);
			this.stencil.getPaper(name).fitToContent(5, 5, 10);

		}, this);

		this.stencil.on('filter', function(graph) {
			joint.layout.GridLayout.layout(graph, layoutOptions);
		});

		$('.stencil-container .btn-expand').on('click', _.bind(this.stencil.openGroups, this.stencil));
		$('.stencil-container .btn-collapse').on('click', _.bind(this.stencil.closeGroups, this.stencil));

		this.initializeStencilTooltips();
	},

	initializeStencilTooltips: function() {

		// Create tooltips for all the shapes in stencil.
		_.each(this.stencil.graphs, function(graph) {

			graph.get('cells').each(function(cell) {

				new joint.ui.Tooltip({
					target: '.stencil [model-id="' + cell.id + '"]',
					content: cell.get('subType').split('.').join(' '),
					left: '.stencil',
					direction: 'left'
				});
			});
		});
	},


//	openITUstudioView: function() {

//	this.paper.on('cell:pointerdblclick', function(evt, cellView, x, y) {
//	console.log('hi there!');
//	},this);

//	},

	initializeSelection: function() {

		this.selection = new Backbone.Collection;
		this.selectionView = new joint.ui.SelectionView({ paper: this.paper, graph: this.graph, model: this.selection });

		// Initiate selecting when the user grabs the blank area of the paper while the Shift key is pressed.
		// Otherwise, initiate paper pan.
		this.paper.on('blank:pointerdown', function(evt, x, y) {

			if (_.contains(KeyboardJS.activeKeys(), 'shift')) {
				this.selectionView.startSelecting(evt, x, y);
			} else {
				this.selectionView.cancelSelection();
				this.paperScroller.startPanning(evt, x, y);
			}
		}, this);

		this.paper.on('cell:pointerdown', function(cellView, evt) {
			// Select an element if CTRL/Meta key is pressed while the element is clicked.
			if ((evt.ctrlKey || evt.metaKey) && !(cellView.model instanceof joint.dia.Link)) {
				this.selection.add(cellView.model);
				this.selectionView.createSelectionBox(cellView);
			}
		}, this);

		this.selectionView.on('selection-box:pointerdown', function(evt) {
			// Unselect an element if the CTRL/Meta key is pressed while a selected element is clicked.
			if (evt.ctrlKey || evt.metaKey) {
				var cell = this.selection.get($(evt.target).data('model'));
				this.selection.reset(this.selection.without(cell));
				this.selectionView.destroySelectionBox(this.paper.findViewByModel(cell));
			}
		}, this);

		// Disable context menu inside the paper.
		// This prevents from context menu being shown when selecting individual elements with Ctrl in OS X.
		this.paper.el.oncontextmenu = function(evt) { evt.preventDefault(); };

		KeyboardJS.on('delete, backspace', _.bind(function(evt, keys) {

			if (!$.contains(evt.target, this.paper.el)) {
				// remove selected elements from the paper only if the target is the paper
				return;
			}

			this.commandManager.initBatchCommand();
			this.selection.invoke('remove');
			this.commandManager.storeBatchCommand();
			this.selectionView.cancelSelection();

			// Prevent Backspace from navigating one page back (happens in FF).
			if (_.contains(keys, 'backspace') && !$(evt.target).is("input, textarea")) {

				evt.preventDefault();
			}

		}, this));
	},

	createInspector: function(cellView) {

		var cell = cellView.model || cellView;

		// No need to re-render inspector if the cellView didn't change.
		if (!this.inspector || this.inspector.options.cell !== cell) {

			if (this.inspector) {

				this.inspectorClosedGroups[this.inspector.options.cell.id] = _.map(app.inspector.$('.group.closed'), function(g) {
					return $(g).attr('data-name');
				});

				// Clean up the old inspector if there was one.
				this.inspector.updateCell();
				this.inspector.remove();
			}

			var inspectorDefs = InspectorDefs[cell.get('type')];

			this.inspector = new joint.ui.Inspector({
				inputs: inspectorDefs ? inspectorDefs.inputs : CommonInspectorInputs,
						groups: inspectorDefs ? inspectorDefs.groups : CommonInspectorGroups,
								cell: cell
			});

			this.initializeInspectorTooltips();

			this.inspector.render();
			$('.inspector-container').html(this.inspector.el);

			if (this.inspectorClosedGroups[cell.id]) {

				_.each(this.inspectorClosedGroups[cell.id], this.inspector.closeGroup, this.inspector);

			} else {
				this.inspector.$('.group:not(:first-child)').addClass('closed');
			}
		}
	},

	initializeInspectorTooltips: function() {

		this.inspector.on('render', function() {

			this.inspector.$('[data-tooltip]').each(function() {

				var $label = $(this);
				new joint.ui.Tooltip({
					target: $label,
					content: $label.data('tooltip'),
					right: '.inspector',
					direction: 'right'
				});
			});

		}, this);
	},

	initializeHaloAndInspector: function() {

		this.paper.on('cell:pointerup', function(cellView, evt) {

			if (cellView.model instanceof joint.dia.Link || this.selection.contains(cellView.model)) return;

			// In order to display halo link magnets on top of the freetransform div we have to create the
			// freetransform first. This is necessary for IE9+ where pointer-events don't work and we wouldn't
			// be able to access magnets hidden behind the div.
			// descomentar para inserir a borda de redimensionamento
			//var freetransform = new joint.ui.FreeTransform({ graph: this.graph, paper: this.paper, cell: cellView.model });
			var halo = new joint.ui.Halo({ graph: this.graph, paper: this.paper, cellView: cellView });

			// As we're using the FreeTransform plugin, there is no need for an extra resize tool in Halo.
			// Therefore, remove the resize tool handle and reposition the clone tool handle to make the
			// handles nicely spread around the elements.
			// descomentar para remover a ferramenta de redimensionamento ao Halo
			//halo.removeHandle('resize');

			// descomentar para inserir a borda de redimensionamento
			//freetransform.render();
			halo.render();

			this.initializeHaloTooltips(halo);

			this.createInspector(cellView);

			this.selectionView.cancelSelection();
			this.selection.reset([cellView.model]);

		}, this);

		this.paper.on('link:options', function(evt, cellView, x, y) {

			this.createInspector(cellView);
		}, this);
	},

	initializeNavigator: function() {

		var navigator = this.navigator = new joint.ui.Navigator({
			width: 240,
			height: 115,
			paperScroller: this.paperScroller,
			zoomOptions: { max: 5, min: 0.2 }
		});

		navigator.$el.appendTo('.navigator-container');
		navigator.render();
	},

	initializeHaloTooltips: function(halo) {

		new joint.ui.Tooltip({
			className: 'tooltip small',
			target: halo.$('.remove'),
			content: 'Click to remove the object',
			direction: 'right',
			right: halo.$('.remove'),
			padding: 15
		});
		// new joint.ui.Tooltip({
		// className: 'tooltip small',
		// target: halo.$('.fork'),
		// content: 'Click and drag to clone and connect the object in one go',
		// direction: 'left',
		// left: halo.$('.fork'),
		// padding: 15
		// });
		// new joint.ui.Tooltip({
		// className: 'tooltip small',
		// target: halo.$('.clone'),
		// content: 'Click and drag to clone the object',
		// direction: 'left',
		// left: halo.$('.clone'),
		// padding: 15
		// });
		new joint.ui.Tooltip({
			className: 'tooltip small',
			target: halo.$('.unlink'),
			content: 'Click to break all connections to other objects',
			direction: 'right',
			right: halo.$('.unlink'),
			padding: 15
		});
		new joint.ui.Tooltip({
			className: 'tooltip small',
			target: halo.$('.link'),
			content: 'Click and drag to connect the object',
			direction: 'left',
			left: halo.$('.link'),
			padding: 15
		});
		// new joint.ui.Tooltip({
		// className: 'tooltip small',
		// target: halo.$('.rotate'),
		// content: 'Click and drag to rotate the object',
		// direction: 'right',
		// right: halo.$('.rotate'),
		// padding: 15
		// });
	},

	initializeClipboard: function() {

//		this.clipboard = new joint.ui.Clipboard;
//
////		KeyboardJS.on('ctrl + c', _.bind(function() {
////			// Copy all selected elements and their associated links.
////			this.clipboard.copyElements(this.selection, this.graph, { translate: { dx: 20, dy: 20 }, useLocalStorage: true });
////		}, this));
////
////		KeyboardJS.on('ctrl + v', _.bind(function() {
////
////			this.selectionView.cancelSelection();
////
////			this.clipboard.pasteCells(this.graph, { link: { z: -1 }, useLocalStorage: true });
////
////			// Make sure pasted elements get selected immediately. This makes the UX better as
////			// the user can immediately manipulate the pasted elements.
////			this.clipboard.each(function(cell) {
////
////				if (cell.get('type') === 'link') return;
////
////				// Push to the selection not to the model from the clipboard but put the model into the graph.
////				// Note that they are different models. There is no views associated with the models
////				// in clipboard.
////				this.selection.add(this.graph.getCell(cell.id));
////				this.selectionView.createSelectionBox(cell.findView(this.paper));
////
////			}, this);
////
////		}, this));
//
////		KeyboardJS.on('ctrl + x', _.bind(function() {
////
////			var originalCells = this.clipboard.copyElements(this.selection, this.graph, { useLocalStorage: true });
////			this.commandManager.initBatchCommand();
////			_.invoke(originalCells, 'remove');
////			this.commandManager.storeBatchCommand();
////			this.selectionView.cancelSelection();
////		}, this));
////	},
//
//	initializeCommandManager: function() {
//
//		this.commandManager = new joint.dia.CommandManager({ graph: this.graph });
//
////		KeyboardJS.on('ctrl + z', _.bind(function() {
////
////			this.commandManager.undo();
////			this.selectionView.cancelSelection();
////		}, this));
//
//		KeyboardJS.on('ctrl + y', _.bind(function() {
//
//			this.commandManager.redo();
//			this.selectionView.cancelSelection();
//		}, this));
	},

	initializeValidator: function() {

		// This is just for demo purposes. Every application has its own validation rules or no validation
		// rules at all.

		this.validator = new joint.dia.Validator({ commandManager: this.commandManager });

		// this.validator.validate('change:position change:size add', _.bind(function(err, command, next) {

		// if (command.action === 'add' && command.batch) return next();

		// var cell = command.data.attributes || this.graph.getCell(command.data.id).toJSON();
		// var area = g.rect(cell.position.x, cell.position.y, cell.size.width, cell.size.height);

		// if (_.find(this.graph.getElements(), function(e) {

		// var position = e.get('position');
		// var size = e.get('size');
		// return (e.id !== cell.id && area.intersect(g.rect(position.x, position.y, size.width, size.height)));

		// })) return next("Another cell in the way!");
		// }, this));

		this.validator.on('invalid',function(message) {

			$('.statusbar-container').text(message).addClass('error');

			_.delay(function() {

				$('.statusbar-container').text('').removeClass('error');

			}, 1500);
		});
	},

	initializeToolbar: function() {

		this.initializeToolbarTooltips();

		$('#btn-undo').on('click', _.bind(this.commandManager.undo, this.commandManager));
		$('#btn-redo').on('click', _.bind(this.commandManager.redo, this.commandManager));
		$('#btn-clear').on('click', _.bind(this.graph.clear, this.graph));
		$('#btn-svg').on('click', _.bind(this.paper.openAsSVG, this.paper));
		$('#btn-png').on('click', _.bind(this.paper.openAsPNG, this.paper));
		$('#btn-print-file').on('click', _.bind(this.testFunction, this));
		$('#btn-zoom-in').on('click', _.bind(function() { this.paperScroller.zoom(0.2, { max: 5, grid: 0.2 }); }, this));
		$('#btn-zoom-out').on('click', _.bind(function() { this.paperScroller.zoom(-0.2, { min: 0.2, grid: 0.2 }); }, this));
		$('#btn-zoom-to-fit').on('click', _.bind(function() {
			this.paperScroller.zoomToFit({
				padding: 20,
				scaleGrid: 0.2,
				minScale: 0.2,
				maxScale: 5
			});
		}, this));
		$('#btn-fullscreen').on('click', _.bind(this.toggleFullscreen, this));
		$('#btn-print').on('click', _.bind(this.paper.print, this.paper));

		// toFront/toBack must be registered on mousedown. SelectionView empties the selection
		// on document mouseup which happens before the click event. @TODO fix SelectionView?
		$('#btn-to-front').on('mousedown', _.bind(function(evt) { this.selection.invoke('toFront'); }, this));
		$('#btn-to-back').on('mousedown', _.bind(function(evt) { this.selection.invoke('toBack'); }, this));

		$('#btn-layout').on('click', _.bind(this.layoutDirectedGraph, this));

		$('#input-gridsize').on('change', _.bind(function(evt) {
			var gridSize = parseInt(evt.target.value, 10);
			$('#output-gridsize').text(gridSize);
			this.setGrid(gridSize);
		}, this));

		$('#snapline-switch').change(_.bind(function(evt) {
			if (evt.target.checked) {
				this.snapLines.startListening();
			} else {
				this.snapLines.stopListening();
			}
		}, this));

		var $zoomLevel = $('#zoom-level');
		this.paper.on('scale', function(scale) {
			$zoomLevel.text(Math.round(scale * 100));
		});
	},

	initializeToolbarTooltips: function() {

		$('.toolbar-container [data-tooltip]').each(function() {

			new joint.ui.Tooltip({
				target: $(this),
				content: $(this).data('tooltip'),
				top: '.toolbar-container',
				direction: 'top'
			});
		});
	},

	testFunction: function(){
		var modelo = paper-container.getGraph();
		console.log(modelo);
	},

	toggleFullscreen: function() {

		var el = document.body;

		function prefixedResult(el, prop) {

			var prefixes = ['webkit', 'moz', 'ms', 'o', ''];
			for (var i = 0; i < prefixes.length; i++) {
				var prefix = prefixes[i];
				var propName = prefix ? (prefix + prop) : (prop.substr(0, 1).toLowerCase() + prop.substr(1));
				if (!_.isUndefined(el[propName])) {
					return _.isFunction(el[propName]) ? el[propName]() : el[propName];
				}
			}
		}

		if (prefixedResult(document, 'FullScreen') || prefixedResult(document, 'IsFullScreen')) {
			prefixedResult(document, 'CancelFullScreen');
		} else {
			prefixedResult(el, 'RequestFullScreen');
		}
	},

	setGrid: function(gridSize) {

		this.paper.options.gridSize = gridSize;

		var backgroundImage = this.getGridBackgroundImage(gridSize);
		this.paper.$el.css('background-image', 'url("' + backgroundImage + '")');
	},

	getGridBackgroundImage: function(gridSize, color) {

		var canvas = $('<canvas/>', { width: gridSize, height: gridSize });

		canvas[0].width = gridSize;
		canvas[0].height = gridSize;

		var context = canvas[0].getContext('2d');
		context.beginPath();
		context.rect(1, 1, 1, 1);
		context.fillStyle = color || '#AAAAAA';
		context.fill();

		return canvas[0].toDataURL('image/png');
	},

	layoutDirectedGraph: function() {

		this.commandManager.initBatchCommand();

		_.each(this.graph.getLinks(), function(link) {

			// Reset vertices.
			link.set('vertices', []);

			// Remove all the non-connected links.
			if (!link.get('source').id || !link.get('target').id) {
				link.remove();
			}
		});

		var pad = 50; // padding for the very left and very top element.
		joint.layout.DirectedGraph.layout(this.graph, {
			setLinkVertices: false,
			rankDir: 'LR',
			rankDir: 'TB',
			setPosition: function(cell, box) {
				cell.position(box.x - box.width / 2 + pad, box.y - box.height / 2 + pad);
			}
		});

		// Scroll to the top-left corner as this is the initial position for the DirectedGraph layout.
		this.paperScroller.el.scrollLeft = 0;
		this.paperScroller.el.scrollTop = 0;

		this.commandManager.storeBatchCommand();
	},

	initializeChannel: function(url) {
		// Example usage of the Channel plugin. Note that this assumes the `node channelHub` is running.
		// See the channelHub.js file for furhter instructions.

		var room = (location.hash && location.hash.substr(1));
		if (!room) {
			room = joint.util.uuid();
			this.navigate('#' + room);
		}

		var channel = this.channel = new joint.com.Channel({ graph: this.graph, url: url || 'ws://localhost:4141', query: { room: room } });
		console.log('room', room, 'channel', channel.id);

		var roomUrl = location.href.replace(location.hash, '') + '#' + room;
		$('.statusbar-container .rt-colab').html('Send this link to a friend to <b>collaborate in real-time</b>: <a href="' + roomUrl + '" target="_blank">' + roomUrl + '</a>');
	},

	embedOrConnect: function(parent, child) {

		var parentsubType = parent.get('subType');
		var childsubType = child.get('subType');
		if(parentsubType === 'rack' && childsubType === 'shelf') {
			parent.embed(child);
			console.log('embedded!parent suType: ' +parentsubType+ '; child subType: ' +childsubType);
		}else{
			if((parentsubType === 'shelf') && (childsubType === 'slot')) {
				parent.embed(child);
				console.log('embedded!parent suType: ' +parentsubType+ '; child subType: ' +childsubType);
			}else{
				if((parentsubType === 'slot') && (childsubType === 'card')) {
					parent.embed(child);
					console.log('embedded! parent suType: ' +parentsubType+ '; child subType: ' +childsubType);
				}else {
					if((parentsubType === 'slot') &&  (childsubType === 'supervisor')) {
						parent.embed(child);
						console.log('embedded! parent suType: ' +parentsubType+ '; child subType: ' +childsubType);
					}else{
						if((parentsubType === 'card') && (childsubType === 'in')){
							parent.embed(child);
							console.log('embedded! parent suType: ' +parentsubType+ '; child subType: ' +childsubType);
						}else{
							parent.unembed(child);
							console.log("nao conectou!");	
							//(childsubType === 'in' || childsubType === 'out')
						}
					}
				}
			}
		}
	}});
