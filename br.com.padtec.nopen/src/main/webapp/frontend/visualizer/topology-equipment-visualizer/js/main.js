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
		$('.inspector-container').hide();
		this.inspectorClosedGroups = {};

		this.initializePaper();
//		this.initializeStencil();
		this.initializeSelection();
		this.initializeHaloAndInspector();
//		this.initializeNavigator();
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
				if(cell.get('subType') === 'Card'){
					this.createInspector(cell);
				}				
				this.commandManager.stopListening();
				this.inspector.updateCell();
				this.commandManager.listen();
				this.inspector.$('[data-attribute]:first').focus();
			}
		}, this);

		this.paper = new joint.dia.Paper({
			//el: $('#paper-reparenting'),
			width: window.width,
			height: window.height,
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
			padding: 5,
			paper: this.paper
		});

		this.paperScroller.$el.appendTo('.paper-container');

		this.paperScroller.center();

//		this.graph.on('add', this.initializeLinkTooltips, this);

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
//			var linkView = this.paper.findViewByModel(cell);
//			new joint.ui.Tooltip({
//			className: 'tooltip small',
//			target: linkView.$('.tool-options'),
//			content: 'Click to open Inspector for this link',
//			left: linkView.$('.tool-options'),
//			direction: 'left'

//			});
		}
	},

//	Create and populate stencil.
	initializeStencil: function() {

		this.stencil = new joint.ui.Stencil({
			graph: this.graph,
			paper: this.paper,
			width: 220,
			groups: Stencil.groups,
//			search: {
//			'*': ['subType','attrs/text/text','attrs/.label/text'],
//			'org.Member': ['attrs/.rank/text','attrs/.name/text']
//			}
		});

		$('.stencil-container').append(this.stencil.render().el);	

		this.stencil.$el.on('contextmenu', function(evt) { evt.preventDefault(); });
		$('.stencil-paper-drag').on('contextmenu', function(evt) { evt.preventDefault(); });

		var layoutOptions = {
				columnWidth: this.stencil.options.width / 2 - 10,
				columns: 2,
				rowHeight: 100,
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


	initializeSelection: function() {

		this.selection = new Backbone.Collection;
		this.selectionView = new joint.ui.SelectionView({ paper: this.paper, graph: this.graph, model: this.selection });
		this.selectionView.removeHandle('rotate');
		this.selectionView.removeHandle('remove');

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

//			if (!$.contains(evt.target, this.paper.el)) {
//			// remove selected elements from the paper only if the target is the paper
//			return;
//			}

//			this.commandManager.initBatchCommand();
//			this.selection.invoke('remove');
//			this.commandManager.storeBatchCommand();
//			this.selectionView.cancelSelection();

//			// Prevent Backspace from navigating one page back (happens in FF).
//			if (_.contains(keys, 'backspace') && !$(evt.target).is("input, textarea")) {

//			evt.preventDefault();
//			}

		}, this));
	},

//	eval("createInspector: function(cellView) {" + dateFn + "},"),

	createInspector: function(cellView,opt ) {


		var cell = cellView.model || cellView;
		if (!this.inspector || this.inspector.options.cell !== cell) {
//			if (this.inspector) {this.inspectorClosedGroups[this.inspector.options.cell.id] = _.map(app.inspector.$('.group.closed'), function(g) {return $(g).attr('data-name');				});						this.inspector.updateCell();this.inspector.remove();			}
//			if(cell.get('subType') === "Card"){var inspectorDefs = InspectorDefs[cell.get('subType')];}else{return;}
			if (this.inspector) {
				// Set unsaved changes to the model and clean up the old inspector if there was one.
				this.inspector.updateCell();
				this.inspector.remove();
			}
			var a = "";
			var b = "";
			var i = 1;
//			$each(opt, function(index, element)
			$.each(opt, function(index, element){
//				console.log(opt[i]);
                 console.log(index);
                 console.log(element);
				if(element === "och-ctp-grouping"){	
					var elementname = index;
					var input = "directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'], index: 1 , group: 'ochctp_"+elementname+"', label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality of <br>the termination point. Valid values are<br> sink, source,and bidirectional.<br> This attribute is read-only.'}}},";
					var group = "ochctp_"+elementname+": {label: 'OCH CTP _"+elementname+"', index: "+i+" },";
                    var attr1 = 'directionality_'+elementname;
					//cell.prop(elementname,i);
					cell.prop(elementname, {'name':elementname,'grouping':'och-ctp-grouping',});

					eval("cell.prop(elementname, {'name':elementname,'grouping':'och-client-ctp-grouping','directionality_"+elementname+"':''});");
					
					a = a+input;
					b = b+group;
					i++;
					console.log(a);
					console.log(b);
				
					console.log(cell);
				};

				if(element === "och-nim-grouping"){
					var elementname = index;
					var input = "operationalstate_"+elementname+": {type: 'select' , options:['enabled','disabled'],index: 1 ,group:'ochnim_"+elementname+"', label: 'Operational State', attrs: { 'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br> Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br> operational state is unknown, then the resource will<br> be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number', min: '0', max: '2147483647' ,index: 2 ,group:'ochnim_"+elementname+"', label: 'Current Problem List', attrs: {'input' : {'data-tooltip': 'This attribute indicates the actual payload type<br> signal received. This attribute is read-only.'}}},";
					var group = "ochnim_"+elementname+": { label: 'OCH NIM _"+elementname+"', index: "+i+"},";
					//cell.prop(elementname,i);
					var attr1 = 'operationalstate_'+elementname;
					var attr2 = 'currentproblemlist_'+elementname;
					
					cell.prop(elementname, {'name':elementname,'grouping':'och-nim-grouping',});
                    console.log(cell);
					
                    a = a+input;
					b = b+group;
					i++;
					console.log(a);
					console.log(b);

				};
				if(element === "gcc0-tp-grouping"){
					var elementname = index; 
					var input = "directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'],group: 'gcc0tp_"+elementname+"',index: 1 , label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality of <br>the termination point. Valid values are<br> sink, source,and bidirectional.<br> This attribute is read-only.'}}},application_"+elementname+": {type: 'text' ,group: 'gcc0tp_"+elementname+"',index: 2 , label: 'Application', attrs: {'input' : {'data-tooltip': 'This attribute indicates the <br>applications transported by the <br>GCC channel. Example applications <br>are ECC(user data channel).<br> Valid values are string.<br>This attribute is read-only.'}}},";
					var group = "gcc0tp_"+elementname+": { label: 'GCC0 tp _"+elementname+"', index: "+i+" },";
					
					//cell.prop(elementname,i);
					var attr1= 'directionality_'+elementname ;
					var attr2= 'application_'+elementname ;
					cell.prop(elementname, {'name':elementname,'grouping':'gcc0-tp-grouping', });
					console.log(cell);
					
					a = a+input;
					b = b+group;
					i++;
					console.log(a);
					console.log(b);

				};

				if(element === "gcc12-tp-grouping"){
					var elementname = index; 
					var input = "directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'],group: 'gcc12tp_"+elementname+"',index: 1 , label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality<br> of the termination point. Valid <br>values are sink, source,and bidirectional.<br> This attribute is read-only.'}}},codirectional: {type: 'toggle',group: 'gcc12tp_"+elementname+"',index: 2 , label: 'Codirectional', attrs: { 'label': {'data-tooltip': 'This attribute specifies<br> the directionality of <br>the GCC12_TP with respect to <br>the associated ODUk_CTP.<br>The value of TRUE means that<br> the sink part <br>of the GCC12_TP terminates the same signal<br> direction as the sink part of the ODUk_CTP.<br>The Source part behaves similarly.<br>This attribute is meaningful only on objects instantiated<br> under ODUk_CTP,<br> and at least	one among ODUk_CTP and the subordinate <br>object has directionality equal to Bidirectional.<br> This attribute is read-only.'}}},gccaccess_"+elementname+": {type: 'select' , options:['gg1','gcc2','gcc1-plus-gcc2'],group: 'gcc12tp_"+elementname+"',index: 3 , label: 'GCC Access', attrs: { 'label': {'data-tooltip': 'This attribute indicates the GCC access <br>represented	by this entity.<br> Valid values are: <br>1) GCC1 <br>2) GCC2 <br>3) GCC1 + GCC2.<br>This attribute is read-only.'}}},gccpassthrough_"+elementname+" : {type: 'toggle' ,group: 'gcc12tp_"+elementname+"',index: 4 , label: 'GCC Pass Through', attrs: { 'label': {'data-tooltip': 'This attribute controls the<br> selected GCC overhead<br> whether it is passed through or modified. Valid<br> 	values are TRUE and FALSE.<br>The value of TRUE means<br> that the GCC overhead shall pass through unmodified<br> from the ODUk CTP input to the ODUk CTP output.<br> Otherwise shall be set to all 0s at the ODUk CTP<br> output after the extraction of the COMMS data. This<br> attribute is not meaningful on objects instantiated<br> under ODUk_TTP, and on objects with directionality<br> equals to Source.'}}},application_"+elementname+": {type: 'text',group: 'gcc12tp_"+elementname+"',index: 5 , label: 'Application', attrs: {'input' : {'data-tooltip': 'This attribute indicates the applications <br>transported by the GCC channel.<br> Example applications	are ECC,<br> (user data channel).<br> Valid values are string.<br>This attribute is read-only.'}}},";
					var group = "gcc12tp_"+elementname+": { label: 'GCC12 TP _"+elementname+"', index: "+i+"}";
					
					var attr1 = 'directionality_'+elementname ;
					var attr2 = 'codirectional_'+elementname;
					var attr3 = 'gccaccess_'+elementname;
					var attr4 = 'gccpassthrough_'+elementname;
					var attr5 = 'application_'+elementname;
					cell.prop(elementname, {'name':elementname,'grouping':'gcc12-tp-grouping',});
					//cell.prop(elementname,i);

					console.log(cell);
					
					a = a+input;
					b = b+group;
					i++;
					console.log(a);
					console.log(b);

				};

				if(element === "och-client-ctp-grouping"){
					var elementname = index;
					var input = "adaptativetype_"+elementname+": {type: 'number' ,min: '5', max: '15' ,index: 1 ,group: 'ochclientctp_"+elementname+"', label: 'Adaptative Type', attrs: {'input' : {'data-tooltip': 'This attribute indicates the type of client signal<br> currently supported by the OCh adaptation function.<br> Valid values are integers between 1 and 15, representing:<br>1) CBR_2G5;<br>2) CBR_10G;<br>3) CBR_40G;<br>4) RSn.<br>This attribute is read-only.'}}},sinkadaptactive_"+elementname+": {type: 'toggle',index: 2 ,group: 'ochclientctp_"+elementname+"', label: 'Sink Adapt Active', attrs: {'input' : {'data-tooltip': 'This attribute allows for activation or<br> deactivation the sink adaptation function. The value<br> of TRUE means active. This attribute is read-write.'}}},sourceadaptactive_"+elementname+":{type: 'toggle',index: 3 ,group: 'ochclientctp_"+elementname+"', label: 'Source Adapt Active', attrs: {'input' : {'data-tooltip': 'This attribute allows for activation or deactivation<br> the source adaptation function. The value of TRUE<br> means activate. This attribute is read-write.'}}}, payloadtypeac_"+elementname+": {type: 'number', min: '1', max: '2147483647' , index: 4 ,group: 'ochclientctp_"+elementname+"', label: 'Payload Type AC', attrs: {'input' : {'data-tooltip': 'This attribute indicates the actual payload type<br> signal received. This attribute is read-only.'}}},directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'],index: 5 ,group: 'ochclientctp_"+elementname+"', label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality<br> of the termination point. Valid <br>values are sink, source,and bidirectional.<br> This attribute is read-only.'}}},operationalstate_"+elementname+": {type: 'select' , options:['enabled','disabled'],index: 6 ,group: 'ochclientctp_"+elementname+"', label: 'Operational State', attrs: { 'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br> Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br> operational state is unknown, then the resource will<br> be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: '0', max: '2147483647' ,index: 7 ,group: 'ochclientctp_"+elementname+"', label: 'Current Problem List', attrs: {'input' : {'data-tooltip': 'This attribute indicates the actual payload type<br> signal received. This attribute is read-only.'}}},";
					var group = "ochclientctp_"+elementname+": { label: 'OCH Client CTP _"+elementname+"', index: "+i+"},";

					cell.prop(elementname, {'name':elementname,'grouping':'och-client-ctp-grouping',});
					
					a = a+input;
					b = b+group;
					i++;
					console.log(a);
					console.log(b);
				};

				if(element === "och-ttp-grouping"){
					var elementname = index;
					var input = "directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'],index: 1 ,group:'ochttp_"+elementname+"' , label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality of the<br>termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},operationalstate_"+elementname+": {type: 'select' , options:['enabled','disabled'],index: 2,group:'ochttp_"+elementname+"' , label: 'Operational State', attrs: { 'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br>X.731 and the behaviour description for<br>operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br>Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br>the object is created. If there is a period of time<br>during the initialization process where the<br>operational state is unknown, then the resource will<br>be considered disabled until initialization has<br>completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},adiministrativestate_"+elementname+": {type: 'select', options:['unlocked','locked','shutting-down'],index:3,group:'ochttp_"+elementname+"',label:'Administrative State', attrs: {'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br>X.731 and the behaviour description for<br>administrativeState in ITU-T Rec. M.3100.<br>Possible Values − Unlocked, Locked, and Shutting<br>Down. See ITU-T Recs. X.731 and M.3100.<br>Default Value − Unlocked (If there is a need that<br>can be identified for locking this resource, this<br>decision will need to be re-evaluated).<br>Constraints to Provisioning − Constrained to<br>Unlocked (If there is a need that can be identified<br>for locking this resource, this decision will need<br>to be re-evaluated).<br>Effect of Change in Value − N/A (If there is a need<br>that can be identified for locking this resource,<br>this decision will need to be re-evaluated).<br>This attribute is read-write.'}}},currentproblemlist_"+elementname+":{type: 'number', min: '1', max: '63' ,index: 4,group:'ochttp_"+elementname+"' , label: 'Current Problem List', attrs: {'input' : {'data-tooltip': 'This attribute indicates the failure conditions of<br>the entity. Possible values of this attribute include decimal numbers between 1 and 63, representing:<br>1) no defect;<br>2) LOS-P (Loss of Signal − Payload);<br>3) OCI (Open Connection Indicator);<br>4) SSF-P (Server Signal failure − Payload);<br>5) SSF-O (Server Signal failure − Overhead);<br>6) SSF (Server Signal failure).<br>This attribute is read-only.' }}},";
					var group = "ochttp_"+elementname+": { label: 'OCH TTP _"+elementname+"', index: "+i+" },";

					a = a+input;
					b = b+group;
					i++;
					console.log(a);
					console.log(b);
					
					var attr1 = 'operationalstate_'+elementname;
					var attr2 = 'directionality_'+elementname;
					var attr3 = 'adiministrativestate_'+elementname;
					
					cell.prop(elementname, {'name':elementname,'grouping':'och-ttp-grouping',});
					//cell.prop(elementname,i);
					console.log(cell);
				};

				if(element === "ochr-ctp-grouping"){
				var elementname = index;
				var input = "directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'],index: 1 ,group:'ochrctp_"+elementname+"', label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality of the termination point. Valid values are sink, source,and bidirectional. This attribute is read-only.'}}},";
				var group = "ochrctp_"+elementname+": { label: 'OCHr CTP _"+elementname+"', index: "+i+" },";

				
				cell.prop(elementname, {'name':elementname,'grouping':'ochr-ctp-grouping',});
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "ochr-ttp-grouping"){
				var elementname = index;
				var input = "directionality_"+elementname+": {type: 'select' , options:['sink','source','bidirectional'],index: 1,group:'ochrttp_"+elementname+"' , label: 'directionality ', attrs: { 'label': {'data-tooltip': 'This attribute indicates the directionality of the<br>termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},operationalstate_"+elementname+": {type: 'select' , options:['enabled','disabled'],index: 2 ,group:'ochrttp_"+elementname+"', label: 'Operational State', attrs: { 'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br>X.731 and the behaviour description for<br>operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br>Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br>the object is created. If there is a period of time<br>during the initialization process where the<br>operational state is unknown, then the resource will<br>be considered disabled until initialization has<br>completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},adiministrativestate_"+elementname+": {type: 'select', options:['unlocked','locked','shutting-down'],index:3,group:'ochrttp_"+elementname+"',label:'Administrative State', attrs: {'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br>X.731 and the behaviour description for<br>administrativeState in ITU-T Rec. M.3100.<br>Possible Values − Unlocked, Locked, and Shutting<br>Down. See ITU-T Recs. X.731 and M.3100.<br>Default Value − Unlocked (If there is a need that<br>can be identified for locking this resource, this<br>decision will need to be re-evaluated).<br>Constraints to Provisioning − Constrained to<br>Unlocked (If there is a need that can be identified<br>for locking this resource, this decision will need<br>to be re-evaluated).<br>Effect of Change in Value − N/A (If there is a need<br>that can be identified for locking this resource,<br>this decision will need to be re-evaluated).<br>This attribute is read-write.'}}},currentproblemlist_"+elementname+":{type: 'number', min: '1', max: '63' ,index: 4 ,group:'ochrttp_"+elementname+"' , label: 'Current Problem List', attrs: {'input' : {'data-tooltip': 'This attribute indicates the failure conditions of<br>the entity. Possible values of this attribute include decimal numbers between 1 and 63, representing:<br>1) no defect;<br>2) LOS-P (Loss of Signal − Payload);<br>3) OCI (Open Connection Indicator);<br>4) SSF-P (Server Signal failure − Payload);<br>5) SSF-O (Server Signal failure − Overhead);<br>6) SSF (Server Signal failure).<br>This attribute is read-only.' }}},";
				var group = "ochrttp_"+elementname+": { label: 'OCHr TTP _"+elementname+"', index: "+i+" },";
			
				cell.prop(elementname, {'name':elementname,'grouping':'ochr-ttp-grouping',});
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "octk-nim-grouping"){
				var elementname = index;
				var input = "k_"+elementname+":{type:'range',min:'1',max:'3',step:'1',index:1,group:'octknim_"+elementname+"',label:'k',attrs:{'label': {'data-tooltip' : 'This attribute specifies the index k that is used<br> to represent a supported bit rate and the different<br> versions of OPUk, ODUk and OTUk. Valid values for<br> this attribute are integers 1, 2 and 3.<br>k = 1 represents an approximate bit rate of 2.5 Gbit/s;<br>k = 2 represents an approximate bit rate of 10 Gbit/s; and<br>k = 3 represents an approximate bit rate of 40 Gbit/s.<br>This attribute is read-only.'}}},operationalstate_"+elementname+": {type: 'select' , options:['enabled','disabled'],index: 2 ,group:'octknim_"+elementname+"', label: 'Operational State', attrs: { 'label': {'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br> Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br> operational state is unknown, then the resource will<br> be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},exdapi_"+elementname+":{type:'text',index:3,group:'octknim_"+elementname+"',label:'EX DAPI',attrs:{'input':{'data-tooltip':'The Expected Source Access Point Identifier<br> (ExSAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br> read-write.'}}},acti_"+elementname+":{type:'text',index:4,group:'octknim_"+elementname+"',label:'AC TI',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information<br> recovered (Accepted) from the TTI overhead position<br> at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type:'select',options:['off','dapi','sapi','both'],index:5,group:'octknim_"+elementname+"',label:'Operational State',attrs:{'label':{'data-tooltip' : 'This attribute indicates the mode of the Trace<br> Identifier Mismatch (TIM) Detection function. Valid<br> values are: off, dapi, sapi, both. This attribute is<br> read-write.'}}},timactdisabled_"+elementname+":{type:'togle',index:6,group:'octknim_"+elementname+"',label:'TIM ACT Disabled',attrs:{'input':{'data-tooltip':'This attribute provides the control capability for the<br> managing system to enable or disable the Consequent<br> Action function when detecting Trace Identifier Mismatch<br> (TIM) at the trail termination sink. The value of TRUE<br> means disabled. This attribute is read-write.'}}},degthr_"+elementname+":{type: 'number',min: 0,max: 2147483647,index: 7,group:'octknim_"+elementname+"',label: 'DEG THR',	attrs:{'input':{'data-tooltip': 'This attribute indicates the threshold level for<br> declaring a performance monitoring (PM) Second to be<br> bad. A PM Second is declared bad if the percentage<br> of detected errored blocks in that second is greater<br> than or equal to the specified threshold level.<br> Valid values are integers in units of percentages.<br> This attribute is read-write.'}}},degm_"+elementname+":{type: 'number',min: 0,max: 2147483647,index:8,group:'octknim_"+elementname+"',label: 'DEG M',attrs:{'input':{'data-tooltip': 'This attribute indicates the threshold level for<br> declaring a Degraded Signal defect (dDEG). A dDEG<br> shall be declared if DegM consecutive bad PM Seconds<br> are detected. This attribute is read-write.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 0,max: 2147483647,index:9,group:'octknim_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip': 'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},";
				var group = "octknim_"+elementname+": { label: 'OCTk NIM _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'octk-nim-grouping',});
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "oduk-client-ctp-grouping"){
				var elementname = index;
				var input = "k_"+elementname+":{type:'range',min:'1',max:'3',step:'1',index:1,group:'odukclientctp_"+elementname+"',label:'k',attrs:{'label': {'data-tooltip' : 'This attribute specifies the index k that is used<br> to represent a supported bit rate and the different<br> versions of OPUk, ODUk and OTUk. Valid values for<br> this attribute are integers 1, 2 and 3.<br>k = 1 represents an approximate bit rate of 2.5 Gbit/s;<br>k = 2 represents an approximate bit rate of 10 Gbit/s; and<br>k = 3 represents an approximate bit rate of 40 Gbit/s.<br>This attribute is read-only.'}}},	adaptationtype_"+elementname+":{type: 'number',	min: 0,max: 2147483647,index:2,group:'odukclientctp_"+elementname+"',label: 'Adaptation Type',attrs:{'input':{'data-tooltip':'This attribute indicates the type of the supported<br> adaptation function at the interface port. Valid<br> values of this attribute include integers between 1 and 63, representing:<br>1) CBR;<br>2) ATMvp;<br>3) GFP;<br>4) NULL;<br>5) PRBS;<br>6) RSn.<br>This attribute is read-only.'}}},sinadaptactive_"+elementname+":{type: 'toggle',index:3,group:'odukclientctp_"+elementname+"',label: 'Sink Adapt Active',	attrs:{'input':{'data-tooltip':'This attribute allows for activation or<br> deactivation the sink adaptation function. The value<br> of TRUE means active. This attribute is read-write.'}}},sourceadaptactive_"+elementname+":{type: 'toggle',index:4,group:'odukclientctp_"+elementname+"',label: 'Source Adapt Active',attrs:{'input':{'data-tooltip':'This attribute allows for activation or deactivation<br> the source adaptation function. The value of TRUE<br> means activate. This attribute is read-write.'}}},payloadtypeac_"+elementname+":{type: 'number',min: 0,max: 2147483647,index:5,group:'odukclientctp_"+elementname+"',label: 'Payload Type AC',attrs:{'input':{'data-tooltip':'This attribute indicates the actual payload type<br> signal received. This attribute is read-only.'}}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],index:6,group:'odukclientctp_"+elementname+"',label:'Operational State',attrs:{'label':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate_"+elementname+"_"+elementname+" in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br>Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br>the object is created. If there is a period of time<br>during the initialization process where the<br>operational state is unknown, then the resource will<br> be considered disabled until initialization has<br>completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],index:7,group:'odukclientctp_"+elementname+"',label: 'directionality ',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br> termination point. Valid values are sink, source,<br> and bidirectional. This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 15,index:8,group:'odukclientctp_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},";
				var group = "odukclientctp_"+elementname+": { label: 'ODUk client CTP _"+elementname+"', index: "+i+" },";

//				cell.prop(elementname, {'name':elementname,'grouping':'oduk-client-ctp-grouping','k_"+elementname+"':'','adaptationtype_"+elementname+"':'','sinadaptactive_"+elementname+"':'',});
//				
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "oduk-ctp-grouping"){
				var elementname = index;
				var input = "operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],index:1,group:'odukctp_"+elementname+"',label: 'Operational State',attrs:{'input':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br> Recs. X.731 and M.3100 for details.<br> Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br> operational state is unknown, then the resource will<br>  be considered disabled until initialization has<br>  completed and the state updated accordingly.<br> Constraints to Provisioning – N/A.<br> Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br> This attribute is read-only.'}}},directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],index:2,group:'odukctp_"+elementname+"',label: 'directionality ',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br> termination point. Valid values are sink, source,<br> and bidirectional. This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 15,index:3,group:'odukctp_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},";
				var group = "odukctp_"+elementname+": { label: 'ODUk CTP _"+elementname+"', index: "+i+"  },";
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
//				eval("cell.prop(elementname, {'name':elementname,'grouping':'oduk-client-ctp-grouping','operationalstate_"+elementname+"':'','directionality_"+elementname+"':'','currentproblemlist_"+elementname+"':'',});");
//				cell.prop(elementname, {'name':elementname,'grouping':'oduk-client-ctp-grouping','operationalstate_"+elementname+"':'','directionality_"+elementname+"':'','currentproblemlist_"+elementname+"':'',});
				
//				var attr1 = 'operationalstate_'+elementname;
//				var attr2 = 'directionality_'+elementname; 
//				var attr3 = 'currentproblemlist_'+elementname;
				
				//cell.prop(elementname,i);
				cell.prop(elementname, {'name':elementname,'grouping':'oduk-ctp-grouping',});
				console.log(cell);
				};
				
				if(element === "oduk-nim-grouping"){
				var elementname = index;
				var input = "k_"+elementname+":{type: 'range',min: 1,max: 3,step: 1,index: 1,group:'oduknim_"+elementname+"',label: 'k',attrs:{'input':{'data-tooltip':'This attribute specifies the index k that is used<br> to represent a supported bit rate and the different<br> versions of OPUk, ODUk and OTUk. Valid values for<br> this attribute are integers 1, 2 and 3.<br>k = 1 represents an approximate bit rate of 2.5 Gbit/s;<br>k = 2 represents an approximate bit rate of 10 Gbit/s; and<br>k = 3 represents an approximate bit rate of 40 Gbit/s.<br>This attribute is read-only.' }}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],label: 'Operational State',attrs:{'label':{'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate_"+elementname+"_"+elementname+" in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br> Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br> operational state is unknown, then the resource will<br>be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.' }}},exdapi_"+elementname+":{type: 'text',index: 2,group:'oduknim_"+elementname+"',label: 'EX DAPI',attrs:{'input':{'data-tooltip':'The Expected Destination Access Point Identifier<br> (ExDAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br> read-write.' }}},exsapi_"+elementname+":{type: 'text',index: 3,group:'oduknim_"+elementname+"',label: 'EX SAPI',attrs:{'input':{'data-tooltip':'The Expected Source Access Point Identifier<br>(ExSAPI), provisioned by the managing system, to be<br>compared with the TTI accepted at the overhead<br>position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br>read-write.'}}},acti_"+elementname+":{type: 'text',index:4 ,group:'oduknim_"+elementname+"',label: 'AC TI',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information<br>recovered (Accepted) from the TTI overhead position<br>at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type: 'select',options: ['off','dapi','sapi','both'],index: 5,group:'oduknim_"+elementname+"',label: 'TIM DET Mode',attrs:{'label':{'data-tooltip':'This attribute indicates the mode of the Trace<br>Identifier Mismatch (TIM) Detection function. Valid<br>values are: off, dapi, sapi, both. This attribute is<br>read-write.' }}},timactdisabled_"+elementname+":{type: 'toggle',index:6 ,group:'oduknim_"+elementname+"',label: 'TIM ACT Disabled',attrs:{'input':{'data-tooltip':'This attribute provides the control capability for the<br>managing system to enable or disable the Consequent<br>Action function when detecting Trace Identifier Mismatch<br>(TIM) at the trail termination sink. The value of TRUE<br>means disabled. This attribute is read-write.'}}},degthr_"+elementname+":{type: 'number',min: 0,max: 2147483647,index:7 ,group:'oduknim_"+elementname+"',label: 'DEG THR',attrs:{'input':{'data-tooltip':'This attribute indicates the threshold level for<br>declaring a performance monitoring (PM) Second to be<br>bad. A PM Second is declared bad if the percentage<br>of detected errored blocks in that second is greater<br>than or equal to the specified threshold level.<brValid values are integers in units of percentages.<br>This attribute is read-write.'}}},degm_"+elementname+":{type: 'number',min: 0,max: 2147483647,index: 8,group:'oduknim_"+elementname+"',label: 'DEG M',attrs:{'input':{'data-tooltip':'This attribute indicates the threshold level for<br> declaring a Degraded Signal defect (dDEG). A dDEG<br>shall be declared if DegM consecutive bad PM Seconds<br>are detected. This attribute is read-write.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,	max: 127,index:9 ,group:'oduknim_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},nimdirectionality_"+elementname+":{type: 'select',options: ['sink','source'],index:10 ,group:'oduknim_"+elementname+"',label: 'NIM directionality',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the ODUk<br> Path non-intrusive monitoring function. Valid values are<br> sink and source. This attribute is significant for ODUk<br> Path unidirectional non-intrusive monitoring when the<br> associated ODUk_CTP is bidirectional. This attribute is<br> read-only.'}}},";
				var group = "oduknim_"+elementname+": { label: 'ODUk NIM _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'oduk-nim-grouping',});
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				
				if(element === "oduk-ttp-grouping"){
				var elementname = index;
				var input = "k_"+elementname+":{type: 'range',min: 1,max: 3,step: 1,group: 'odukttp_"+elementname+"',label: 'k',attrs:{'input':{'data-tooltip':'This attribute specifies the index k that is used<br>to represent a supported bit rate and the different<br>versions of OPUk, ODUk and OTUk. Valid values for<br>this attribute are integers 1, 2 and 3.<br>k = 1 represents an approximate bit rate of 2.5 Gbit/s;<br>k = 2 represents an approximate bit rate of 10 Gbit/s; and<br>k = 3 represents an approximate bit rate of 40 Gbit/s.<br>This attribute is read-only.' }}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],group: 'odukttp_"+elementname+"',label: 'Operational State',attrs:{'label':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br>X.731 and the behaviour description for<br>operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br>Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br>the object is created. If there is a period of time<br>during the initialization process where the<br>operational state is unknown, then the resource will<br>be considered disabled until initialization has<br>completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],group: 'odukttp_"+elementname+"',label: 'directionality ',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br>termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},txti_"+elementname+":{type: 'text',group: 'odukttp_"+elementname+"',label: 'TX TI',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information,<br>provisioned by the managing system at the<br>termination source, to be placed in the TTI overhead<br>position of the source of a trail for transmission.<br>This attribute is read-write.'}}},exdapi_"+elementname+":{type: 'text',group: 'odukttp_"+elementname+"',label: 'EX DAPI',attrs:{'input':{'data-tooltip':'The Expected Destination Access Point Identifier<br>(ExDAPI), provisioned by the managing system, to be<br>compared with the TTI accepted at the overhead<br>position of the sink for the purpose of checking the<br>integrity of connectivity. This attribute is<br>read-write.'}}},exsapi_"+elementname+":{type: 'text',group: 'odukttp_"+elementname+"',label: 'EX SAPI',attrs:{'input':{'data-tooltip':'The Expected Source Access Point Identifier<br>(ExSAPI), provisioned by the managing system, to be<br>compared with the TTI accepted at the overhead<br>position of the sink for the purpose of checking the<br>integrity of connectivity. This attribute is<br>read-write.'}}},acti_"+elementname+":{type: 'text',group: 'odukttp_"+elementname+"',label: 'AC TI',attrs:{'input':{'data-tooltip': 'The Trail Trace Identifier (TTI) information<br>recovered (Accepted) from the TTI overhead position<br>at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type: 'select',options: ['off','dapi','sapi','both'],group: 'odukttp_"+elementname+"',label: 'TIM DET Mode',attrs:{'label':{'data-tooltip':'This attribute indicates the mode of the Trace<br>Identifier Mismatch (TIM) Detection function. Valid<br>values are: off, dapi, sapi, both. This attribute is<br>read-write.'}}},timactdisabled_"+elementname+":{type: 'toggle',group: 'odukttp_"+elementname+"',label: 'TIM ACT Disabled',attrs:{'input':{'data-tooltip':'This attribute provides the control capability for the<br>managing system to enable or disable the Consequent<br>Action function when detecting Trace Identifier Mismatch<br>(TIM) at the trail termination sink. The value of TRUE<br>means disabled. This attribute is read-write.'}}},degthr_"+elementname+":{type: 'number',min: 0,max: 2147483647,group: 'odukttp_"+elementname+"',label: 'DEG THR',attrs:{'input':{'data-tooltip':'This attribute indicates the threshold level for<br>declaring a performance monitoring (PM) Second to be<br>bad. A PM Second is declared bad if the percentage<br>of detected errored blocks in that second is greater<br>than or equal to the specified threshold level.<br>Valid values are integers in units of percentages.<br>This attribute is read-write.'}}},degm_"+elementname+":{type: 'number',min: 0,max: 2147483647,group: 'odukttp_"+elementname+"',label: 'DEG M',attrs:{'input':{'data-tooltip':'This attribute indicates the threshold level for<br>declaring a Degraded Signal defect (dDEG). A dDEG<br>shall be declared if DegM consecutive bad PM Seconds<br>are detected. This attribute is read-write.'}}},positionseg_"+elementname+":{type: 'number',min: 0,max: 2147483647,group: 'odukttp_"+elementname+"',label: 'Position Seg',attrs:{'input':{'data-tooltip':'This attribute indicates the positions of the TCM<br>and GCC processing functions within the ODUk TP.<br>The order of the position in the positionSeq<br>attribute together with the signal flow determine<br>the processing sequence of the TCM and GCC functions<br>within the ODUk TP. Once the positions are<br>determined, the signal processing sequence will<br>follow the signal flow for each direction of the<br> signal.<br>Within the ODUk_CTP, the position order is going<br>from adaptation to connection function. Within the<br>ODUk_TTP, the order is going from connection to<br>adaptation function.<br>The syntax of the PositionSeq attribute will be a<br>SEQUENCE OF pointers, which point to the contained<br>TCM and GCC function.<br>The order of TCM and GCC access function in the<br>positionSeq attribute is significant only when there<br>are more than one TCM functions within the ODUk TP<br>and also at least one of them have the<br>TimActDisabled attribute set to FALSE (i.e. AIS is<br>inserted upon TIM).<br>If a GCC12_TP is contained in an ODUk_TTP and the<br>GCC12_TP is not listed in the PositionSeq attribute<br>of the ODUk_TTP, then the GCC access is at the AP<br>side of the ODUk TT function.<br>This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 127,group: 'odukttp_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.'}}},tcmfieldsinuse_"+elementname+":{type: 'number',min: 0,max: 2147483647,group: 'odukttp_"+elementname+"',label: 'TCM Fields In Use',attrs:{'input':{'data-tooltip':'This attribute indicates the used TCM fields of the<br>ODUk OH. Valid values of this attribute are integers between 1 and 63 This attribute is read-only.'}}},";
				var group = "odukttp_"+elementname+": { label: 'ODUk TTP _"+elementname+"', index: "+i+" },";
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				
				var attr1 = 'k_'+elementname;
				var attr2 = 'operationalstate_'+elementname;
				var attr3 = 'txti_'+elementname;
				var attr4 = 'exdapi_'+elementname;
				var attr5 = 'timactdisabled_'+elementname;
				var attr6 = 'degthr_'+elementname;
				var attr7 = 'acti_'+elementname;
				var attr8 = 'exsapi_'+elementname;
				var attr9 = 'degm_'+elementname;
				var attr10 = 'timdetmode_'+elementname;
				var attr11 = 'currentproblemlist_'+elementname;
				var attr12 = 'positionseg_'+elementname;
				var attr13 = 'tcmfieldinuse_'+elementname;
				
				
				cell.prop(elementname, {'name':elementname,'grouping':'oduk-ttp-grouping',});
				//cell.prop(elementname,i);
				console.log(cell);
				};
				
				
				if(element === "odukt-nim-grouping"){
				var elementname = index;
				var input = "tcmfield:{type: 'range',min: 1,max: 6,step: 1,group:'oduktnim_"+elementname+"',label: 'TCM Field',attrs:{'input':{'data-tooltip':'This attribute indicates the tandem connection<br> monitoring field of the ODUk OH. Valid values are<br> integers from 1 to 6. This attribute is read-only.'}}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],group:'oduktnim_"+elementname+"',label: 'Operational State',attrs:{'label':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate_"+elementname+"_"+elementname+" in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br> Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br>operational state is unknown, then the resource will<br> be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},exdapi_"+elementname+":{type: 'text',group:'oduktnim_"+elementname+"',label: 'EX DAPI',attrs:{'input':{'data-tooltip':'The Expected Destination Access Point Identifier<br> (ExDAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br> read-write.'}}},exsapi_"+elementname+":{type: 'text',group:'oduktnim_"+elementname+"',label: 'EX SAPI',attrs:{'input':{'data-tooltip':'The Expected Source Access Point Identifier<br> (ExSAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br> read-write.'}}},acti_"+elementname+":{type: 'text',group:'oduktnim_"+elementname+"',label: 'AC TI',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information<br> recovered (Accepted) from the TTI overhead position<br> at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type: 'select',options: ['off','dapi','sapi','both'],group:'oduktnim_"+elementname+"',label: 'TIM DET Mode',attrs:{'label':{'data-tooltip':'This attribute indicates the mode of the Trace<br> Identifier Mismatch (TIM) Detection function. Valid<br> values are: off, dapi, sapi, both. This attribute is<br> read-write.'}}},timactdisabled_"+elementname+":{type: 'toggle',group:'oduktnim_"+elementname+"',label: 'TIM ACT Disabled',attrs:{'input':{'data-tooltip': 'This attribute provides the control capability for the<br> managing system to enable or disable the Consequent<br> Action function when detecting Trace Identifier Mismatch<br> (TIM) at the trail termination sink. The value of TRUE<br>means disabled. This attribute is read-write.'}}},degthr_"+elementname+":{type: 'number',min: 0,max: 2147483647,group:'oduktnim_"+elementname+"',label: 'DEG THR',attrs:{'input':{'data-tooltip':'This attribute indicates the threshold level for<br>declaring a performance monitoring (PM) Second to be<br>bad. A PM Second is declared bad if the percentage<br>of detected errored blocks in that second is greater<br>than or equal to the specified threshold level.<br>Valid values are integers in units of percentages.<br>This attribute is read-write.'}}},degm_"+elementname+":{type: 'number',min: 0,max: 2147483647,group:'oduktnim_"+elementname+"',label: 'DEG M',attrs:{'input':{'data-tooltip':'This attribute indicates the threshold level for<br>declaring a Degraded Signal defect (dDEG). A dDEG<br>shall be declared if DegM consecutive bad PM Seconds<br>are detected. This attribute is read-write.'}}},nimdirectionality_"+elementname+":{type: 'select',options: ['sink','source'],group:'oduktnim_"+elementname+"',label: 'NIM directionality',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the ODUk<br>Path non-intrusive monitoring function. Valid values are<br>sink and source. This attribute is significant for ODUk<br>Path unidirectional non-intrusive monitoring when the<br>associated ODUk_CTP is bidirectional. This attribute is<br>read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 127,group:'oduktnim_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},";
				var group = "oduktnim_"+elementname+": { label: 'ODUkt NIM _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'odukt-nim-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "odukt-ttp-grouping"){
				var elementname = index;
				var input = "operationalstate_"+elementname+":{type: 'select','options': ['enabled','disabled'],group:'oduktttp_"+elementname+"',label: 'Operational State',attrs:{'input':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br>X.731 and the behaviour description for<br>operationalstate_"+elementname+"_"+elementname+" in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br>Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br>the object is created. If there is a period of time<br>during the initialization process where the<br>operational state is unknown, then the resource will<br>be considered disabled until initialization has<br>completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}}, directionality_"+elementname+":{type: 'select','options': ['sink','source','bidirectional'],group:oduktttp_"+elementname+",'label': 'directionality','attrs':{'input':{'data-tooltip':'This attribute indicates the directionality of the<br>termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},tcmfield_"+elementname+":{type: 'range','min': 1,'max': 6,'step': 1,group:oduktttp_"+elementname+",'label': 'TCM Field','attrs':{'input':{'data-tooltip':'This attribute indicates the tandem connection<br>monitoring field of the ODUk OH. Valid values are<br>integers from 1 to 6. This attribute is read-only.'}}},txti_"+elementname+":{type: 'text',group:oduktttp_"+elementname+",'label': 'TX TI','attrs':{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information,<br>provisioned by the managing system at the<br>termination source, to be placed in the TTI overhead<br>position of the source of a trail for transmission.<br>This attribute is read-write.'}}},exdapi_"+elementname+":{type: 'text',group:oduktttp_"+elementname+",'label': 'EX DAPI','attrs':{'input':{'data-tooltip':'The Expected Destination Access Point Identifier<br> (ExDAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br>integrity of connectivity. This attribute is<br>read-write.'}}},exsapi_"+elementname+":{type: 'text',group:oduktttp_"+elementname+",'label': 'EX SAPI','attrs':{'input':{'data-tooltip': 'The Expected Source Access Point Identifier<br> (ExSAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br>integrity of connectivity. This attribute is<br>read-write.'}}},acti_"+elementname+":{type: 'text',group:oduktttp_"+elementname+",'label': 'AC TI','attrs':{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information<br> recovered (Accepted) from the TTI overhead position<br>at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type: 'select','option': ['off','dapi','sapi','both'],group:oduktttp_"+elementname+",'label': 'TIM DET Mode','attrs':{'input':{'data-tooltip':'This attribute indicates the mode of the Trace<br> Identifier Mismatch (TIM) Detection function. Valid<br> values are: off, dapi, sapi, both. This attribute is<br> read-write.'}}},timactdisabled_"+elementname+":{type: 'toggle',group:oduktttp_"+elementname+",'label': 'TIM ACT Disabled','attrs':{'input':{'data-tooltip':'This attribute provides the control capability for the<br>managing system to enable or disable the Consequent<br> Action function when detecting Trace Identifier Mismatch<br>(TIM) at the trail termination sink. The value of TRUE<br>means disabled. This attribute is read-write.'}}},degthr_"+elementname+":{type: 'number','min': 0,'max': 2147483647,group:oduktttp_"+elementname+",'label': 'DEG THR','attrs':{'input':{'data-tooltip': 'This attribute indicates the threshold level for<br> declaring a performance monitoring (PM) Second to be<br> bad. A PM Second is declared bad if the percentage<br> of detected errored blocks in that second is greater<br> than or equal to the specified threshold level.<br> Valid values are integers in units of percentages.<br>This attribute is read-write.'}}},degm_"+elementname+":{type: 'number','min': 0,'max': 2147483647,group:oduktttp_"+elementname+",'label': 'DEG M','attrs':{'input':{'data-tooltip':'This attribute indicates the threshold level for<br> declaring a Degraded Signal defect (dDEG). A dDEG<br> shall be declared if DegM consecutive bad PM Seconds<br> are detected. This attribute is read-write.'}}},adminstatesource_"+elementname+":{type: 'select','option': ['locked','normal'],group:oduktttp_"+elementname+",'label': 'Admin State Source','attrs':{'input':{'data-tooltip':'This attribute provides the capability to provision<br>the LOCK signal at the sink, which is one of the<br>ODUk maintenance signals. Valid values for this<br>attribute are Locked and Normal. When a Tandem <br>Connection endpoint is set to admin state locked, it<br> will insert the ODUk-LCK signal in the downstream<br>direction.'}}},modesink_"+elementname+":{type: 'select','options': ['operational','monitor','transparent'],group:oduktttp_"+elementname+",'label': 'Mode Sink','attrs':{'input':{'data-tooltip': 'This attribute specifies the TCM mode at the entity.<br>Valid values are: Operational, Monitor, and Transparent.'}}},modesource_"+elementname+":{type: 'select','options': ['operational','monitor','transparent'],group:oduktttp_"+elementname+",'label': 'Mode Source','attrs':{'input':{'data-tooltip':'This attribute specifies the TCM mode at the entity.<br>Valid values are: Operational, Monitor, and Transparent.'}}},acstatussink_"+elementname+":{group:oduktttp_"+elementname+",type: 'select','options': ['no-source-tc','in-use-without-iae','in-use-with-ia', 'reserved-for-future-internacional-standardizartion-one','reserved-for-future-internacional-standardizartion-two','aintenance-signal-oduk-lck','maintenance-signal-oduk-oci','maintenance-signal-oduk-ais'],'label': 'AC Status Sink','attrs':{'input':{'data-tooltip':'This attribute indicates the status of the accepted<br>TCM. This attribute is read-only'}}},acstatussource_"+elementname+":{group:oduktttp_"+elementname+",type: 'select','option': ['no-source-tc','in-use-without-iae','in-use-with-ia', 'reserved-for-future-internacional-standardizartion-one','reserved-for-future-internacional-standardizartion-two','aintenance-signal-oduk-lck','maintenance-signal-oduk-oci','maintenance-signal-oduk-ais'],'label': 'AC Status Source','attrs':{'input':{'data-tooltip':'This attribute indicates the status of the accepted<br>TCM. This attribute is read-only'}}},codirectional_"+elementname+":{group:oduktttp_"+elementname+",type: 'toggle','label': 'Codirectional','attrs':{'input':{'data-tooltip': 'This attribute specifies the directionality of the<br>ODUkT TP with respect to the associated ODUk_CTP.<br>The value of TRUE means that the sink part of the<br>ODUkT TP terminates the same signal direction as the<br>sink part of the ODUk_CTP. The Source part behaves<br>similarly. This attribute is meaningful only on<br> objects instantiated under ODUk_CTP, and at least<br>one among ODUk_CTP and the subordinate object has<br>directionality equal to Bidirectional. This<br>attribute is read-only. '}}},currentproblemlist_"+elementname+":{group:oduktttp_"+elementname+",type: 'number','min': 1,'max': 127,'label': 'Current Problem List','attrs':{'input':{'data-tooltip':'This attribute indicates the failure conditions of<br>the entity. Possible values of this attribute include decimal numbers between 1 and 127, representing:<br>1) no defect;<br>2) OCI (Open Connection Indication);<br>3) LCK (Locked);<br>4) TIM (Trail Trace Identifier Mismatch);<br>5) DEG (Signal Degraded);<br>6) BDI (Backward Defect Indication);<br>7) SSF (Server Signal Fail).<br>This attribute is read-only.'}}},";
				var group = "oduktttp_"+elementname+": { label: 'ODUkt TTP _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'odukt-ttp-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "omsn-ctp-grouping"){
				var elementname = index;
				var input = "directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],group:'omsnctp_"+elementname+"',label: 'directionality ',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br>termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},";
				var group = "omsnctp_"+elementname+": { label: 'OMSn CTP _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'omsn-ctp-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "omsn-ttp-grouping"){
				var elementname = index;
				var input = "directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],label: 'directionality ',group : 'omsnttp_"+elementname+"',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br>	termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],label: 'Operational State',group : 'omsnttp_"+elementname+"',attrs:{'label':{'data-tooltip':'This attribute is generally defined in ITU-T Rec. <br>X.731 and the behaviour description for<br>operationalstate_"+elementname+"_"+elementname+" in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T<br>Recs. X.731 and M.3100 for details.<br>Default Value – Actual state of resource at the time<br>the object is created. If there is a period of time<br>during the initialization process where the<br> operational state is unknown, then the resource will <br>be considered disabled until initialization has <br>completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 255,label: 'Current Problem List',group : 'omsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.'}}},";
				var group = "omsnttp_"+elementname+": { label: 'OMSn ttp _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'omsn-ttp-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "omsnp-grouping"){
				var elementname = index;
				var input = "ordertype_"+elementname+": {type: 'select',defaultValue: '1-plus-1',options: ['1-plus-1'],group:omnsnp_"+elementname+",label: 'Oper Type',attrs:{'label':{'data-tooltip':'This attribute indicates the trail protection<br>schemes supported by the entity. Valid value for this<br>attribute is:<br>1 + 1 unidirectional.<br>This attribute is read-write.'}}},waittorestoretime_"+elementname+":{type: 'number',min: 0,max: 2147483647,group:omnsnp_"+elementname+",label: 'Wait to Restore Time',attrs:{'input':{'data-tooltip':'If the protection systems is revertive, this<br>attribute specifies the amount of time, in seconds,<br>to wait after a fault clears before restoring<br>traffic to the protected protectionUnit that<br>initiated the switching. Valid values for this<br>attribute are integers. This attribute is optional.<br>This attribute is read-write. '}}},holdofftime_"+elementname+":{type: 'number',min: 0,max: 2147483647,group:omnsnp_"+elementname+",label: 'Hold Off Time',attrs:{'input':{'data-tooltip':'This attribute indicates the time, in seconds,<br>between declaration of signal degrade or signal<br>fail, and the initialization of the protection<br>switching algorithm. Valid values are integers in<br>units of seconds. This attribute is read-write.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 15,group:omnsnp_"+elementname+",label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.'}}},";
				var group = "{omnsnp_"+elementname+": { label: 'OMSnp _"+elementname+"', index: "+i+"},";
				
				cell.prop(elementname, {'name':elementname,'grouping':'omsnp-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "opsn-ttp-grouping"){
				var elementname = index;
				var input = "directionality_"+elementname+": {type: 'select',options: ['sink','source','bidirectional'],label: 'directionality ',group:'opsnttp_"+elementname+"',attrs:{'label':{'data-tooltip': 'This attribute indicates the directionality of the<br> termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],label: 'Operational State',group:'opsnttp_"+elementname+"',attrs:{'label':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for <br>operationalstate_"+elementname+"_"+elementname+" in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T <br>Recs. X.731 and M.3100 for details.<br>	Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the<br> operational state is unknown, then the resource will <br>be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 3,label: 'Current Problem List',group:'opsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.'}}},";
				var group = "{opsnttp_"+elementname+": { label: 'OPSn ttp _"+elementname+"', index: "+i+" },";
				
				cell.prop(elementname, {'name':elementname,'grouping':'opsn-ttp-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "otmn-entity-grouping"){
				var elementname = index;
				var input = "order: {type: 'number',min: 0,max: 2147483647,group:'otmnentity_"+elementname+"',label: 'Order',attrs:{'input':{'data-tooltip':'This attribute indicates the order of the OTM,<br> which represents the maximum number of wavelengths <br>	that can be supported at the bit rate(s) supported<br> on the interface. See ITU-T Rec. G.709/Y.1331 for<br> details. This attribute is read-only.'}}},reduced_"+elementname+": { type: 'toggle',label: 'Reduced',attrs:{'input':{'data-tooltip':'This attribute indicates whether a reduced or full<br> functionality is supported at the interface. A value<br> of TRUE means reduced. A value of FALSE means full.<br> See ITU-T Rec.G.709/Y.1331 for details. This<br> attribute is read-only.'}}},bitrate_"+elementname+":{type: 'select',options: ['1', '2', '3', '12','123','23'],group:'otmnentity_"+elementname+"',label: 'Bit Rate',attrs:{'label':{'data-tooltip':'This attribute is an index used to represent the<br> bit rate or set of bit rates supported on the<br> interface. Valid values are 1, 2, 3, 12, 123 and 23.<br> In the index, each digit k represents an approximate<br>bit rate supported by the interface. k = 1 means 2.5<br> Gbit/s, k = 2 means 10 Gbit/s, and k = 3 means 40<br>Gbit/s. Default value of this attribute is system <br>	specific. This attribute is read-only.'}}},interfacetype_"+elementname+":{type: 'text',group:'otmnentity_"+elementname+"',label: 'Interface Type',attrs:{'input':{'data-tooltip':'This attribute identifies the type of interface.<br> The value of this attribute will affect the<br> behaviour of the OTM with respect to<br> presence/absence of OOS processing and TCM<br> activation. For an IrDI interface, there is no OOS<br> processing and TCM activation is limited to n levels<br> as specified by a TCM level threshold.<br> Possible Values:<br> field 1: enumeration of IrDI or IaDI;<br>field 2: 10 character string for additional information.<br> Default Value:<br>field 1: IaDI;<br> field 2: vendor and/or provider specific.<br> Constraints to Provisioning – none identified.<br> Effect of Change in Value – change in behaviour in<br> accordance with value.<br> This attribute is read-only.'}}},tcmmax_"+elementname+":{type: 'range',min: 1,max: 6,step: 1,group:'otmnentity_"+elementname+"',label: 'TCM Max',attrs:{'input':{'data-tooltip':'This attribute identifies the maximum number of TCM<br> levels allowed for any Optical Channel contained in<br> this OTM. A new TCM activation will be rejected if<br> the requested level is greater than the threshold.<br> If InterfaceType for the OTM is IaDI, then this<br> attribute is irrelevant.<br>Possible Values – integer from 0 to 6. n (IrDI),<br> where 0 < n < 7.<br> Default Value – Value will default to 3.<br>Constraints to Provisioning – cannot be modified to<br> new value if new value does not support the number <br>	of currently activated TCM levels for any contained<br> Optical Channel.<br>Effect of Change in Value – change in behaviour in <br>	accordance with value.<br>This attribute is read-write.'}}},opticalreach_"+elementname+":{type: 'select',options: ['intra-office', 'short-haul', 'long-haul'],group:'otmnentity_"+elementname+"',label: 'Bit Rate',attrs:{'label':{'data-tooltip':'This attribute indicates the length the optical<br>signal may travel before requiring termination or <br>regeneration. Valid values are:<br>	1) intraOffice;<br>	2) shortHaul;<br>3) longHaul.<br>This attribute is read-only.'}}},";
				var group = "otmnentity_"+elementname+": { label: 'OTMn entity _"+elementname+"', index: "+i+" },";
			
				cell.prop(elementname, {'name':elementname,'grouping':'otmn-entity-grouping', });
				
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				};
				
				if(element === "otsn-ttp-grouping"){
				var elementname = index;
				var input = "directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],label: 'directionality ',group:'otsnttp_"+elementname+"',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br> termination point. Valid values are sink, source,<br> and bidirectional. This attribute is read-only.'}}},operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],label: 'Operational State',group:'otsnttp_"+elementname+"',attrs:{'label':{'data-tooltip': 'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for<br> operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T <br>	Recs. X.731 and M.3100 for details.<br>	Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br> during the initialization process where the <br>operational state is unknown, then the resource will<br> be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>	Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},aprstatus_"+elementname+":{type: 'text',label: 'APR Status',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute indicates the status of the<br> Automatic Power Reduction (APR) function of the <br>entity. Valid values are on and off. This <br>	attribute is read-only.'}}},aprcntrl_"+elementname+":{type: 'text',label: 'APR CNTRL',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute provides for the control of the<br> Automatic Power Reduction (APR) function of the<br> entity. The specific APR procedures and trigger<br> criteria of APR is outside the scope of this <br> Recommendation. This attribute is optional. This <br> attribute is read-write.'}}},		txti_"+elementname+":{type: 'text',label: 'TX TI',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information,<br> provisioned by the managing system at the<br> termination source, to be placed in the TTI overhead<br> position of the source of a trail for transmission.<br> This attribute is read-write.'}}},exdapi_"+elementname+":{type: 'text',label: 'EX DAPI',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'The Expected Destination Access Point Identifier<br>(ExDAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br> position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br> read-write.'}}},exsapi_"+elementname+":{type: 'text',label: 'EX SAPI',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'The Expected Source Access Point Identifier<br> (ExSAPI), provisioned by the managing system, to be<br> compared with the TTI accepted at the overhead<br>	position of the sink for the purpose of checking the<br> integrity of connectivity. This attribute is<br> read-write.'}}},acti_"+elementname+":{type: 'text',label: 'AC TI',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information<br> recovered (Accepted) from the TTI overhead position<br> at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type: 'select',options: ['off', 'dapi', 'sapi','both'],label: 'TIM DET Mode',group:'otsnttp_"+elementname+"',attrs:{'label':{'data-tooltip':'This attribute indicates the mode of the Trace<br> Identifier Mismatch (TIM) Detection function. Valid<br> values are: off, dapi, sapi, both. This attribute is<br> read-write.'}}},timactdisabled_"+elementname+":{type: 'toggle',label: 'TIM ACT Disabled',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute provides the control capability for the<br> managing system to enable or disable the Consequent <br>Action function when detecting Trace Identifier Mismatch<br> (TIM) at the trail termination sink. The value of TRUE<br> means disabled. This attribute is read-write.'}}},currentproblemlist_"+elementname+":{type: 'range',min: 1,max: 255,step: 1,label: 'Current Problem List',group:'otsnttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},";
				var group = "otsnttp_"+elementname+": { label: 'OTSn TTP _"+elementname+"', index: "+i+" },";
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				
				var attr1 = 'directionality_'+elementname;
				var attr2 = 'operationalstate_'+elementname;
				var attr3 = 'aprstatus_'+elementname;
				var attr4 = 'txti_'+elementname;
				var attr5 = 'aprcntrl_'+elementname;
				var attr6 = 'exsapi_'+elementname;
				var attr7 = 'acti_'+elementname;
				var attr8 = 'timdetmode_'+elementname;
				var attr9 = 'exdapi_'+elementname;
				var attr10 = 'timactdisabled_'+elementname;
				var attr11 = 'currentproblemlist_'+elementname;
				
				cell.prop(elementname, {'name':elementname,'grouping':'otsn-ttp-grouping',});
				//cell.prop(elementname,i);
				console.log(cell);
				
				};
				
				if(element === "otuk-ctp-grouping"){
				var elementname = index;
				var input = "k_"+elementname+":{type: 'range',min: -1,max: 4,step: 1,group:'otukctp_"+elementname+"',label: 'k',attrs:{'input':{'data-tooltip':'This attribute specifies the index k that is used<br> to represent a supported bit rate and the different<br> versions of OPUk, ODUk and OTUk. Valid values for<br> this attribute are integers 1, 2 and 3.<br>	k = 1 represents an approximate bit rate of 2.5 Gbit/s;<br>	k = 2 represents an approximate bit rate of 10 Gbit/s; and<br>	k = 3 represents an approximate bit rate of 40 Gbit/s.<br>This attribute is read-only.'}}},sinkadaptactive_"+elementname+":{type: 'toggle',group:'otukctp_"+elementname+"',label: 'Sink Adapt Active',attrs:{'input':{'data-tooltip':'This attribute allows for activation or<br> deactivation the sink adaptation function. The value<br> of TRUE means active. This attribute is read-write.' }}},sourceadaptactive_"+elementname+":{type: 'toggle',group:'otukctp_"+elementname+"',label: 'Source Adapt Active',attrs:{'input':{'data-tooltip':'This attribute allows for activion or deactivation<br> the source adaptation function. The value of TRUE<br> means activate. This attribute is read-write.'}}},fecenabled_"+elementname+":{type: 'toggle',group:'otukctp_"+elementname+"',label:'FEC Enabled',attrs:{'input':{'data-tooltip':'If Forward Error Correction (FEC) is supported,<br> this object indicates whether FEC at the OTUk sink <br> adaptation function is enabled or not. This <br> attribute is optional. Valid values are TRUE and<br> FALSE. TRUE means FEC is enabled. This attribute is<br> read-write.'}}},directionality_"+elementname+":{type: 'select',options: ['sink','source','bidirectional'],group:'otukctp_"+elementname+"',label: 'directionality ',attrs:{'label':{'data-tooltip':'This attribute indicates the directionality of the<br> termination point. Valid values are sink, source,<br> and bidirectional. This attribute is read-only.'}}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 15,group:'otukctp_"+elementname+"',label: 'Current Problem List',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.' }}},";
				var group = "otukctp_"+elementname+": { label: 'OTUk CTP _"+elementname+"', index: "+i+" },";
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				var attr1 = 'k_'+elementname ;
				var attr2 = 'sinkadaptactive_'+elementname;
				var attr3 = 'sourceadaptactive_'+elementname;
				var attr4 = 'directionality_'+elementname;
				var attr5 = 'currentproblemlist_'+elementname;
				
				cell.prop(elementname, {'name':elementname,'grouping':'otuk-ctp-grouping',});
				//cell.prop(elementname,i);
				console.log(cell);
				
				};
				
				if(element === "otuk-ttp-grouping"){
				var elementname = index;
				var input = "operationalstate_"+elementname+":{type: 'select',options: ['enabled','disabled'],label: 'Operational State',group:'otukttp_"+elementname+"',attrs:{'label':{'data-tooltip':'This attribute is generally defined in ITU-T Rec.<br> X.731 and the behaviour description for <br>operationalstate in ITU-T Rec. M.3100.<br>Possible Values – Enabled and Disabled. See ITU-T <br>Recs. X.731 and M.3100 for details.<br>	Default Value – Actual state of resource at the time<br> the object is created. If there is a period of time<br>during the initialization process where the<br> operational state is unknown, then the resource will <br>be considered disabled until initialization has<br> completed and the state updated accordingly.<br>Constraints to Provisioning – N/A.<br>Effect of Change in Value – See ITU-T Recs. X.731 and M.3100.<br>This attribute is read-only.'}}},directionality_"+elementname+": {type: 'select',options: ['sink','source','bidirectional'],label: 'directionality ',group:'otukttp_"+elementname+"',attrs:{'label':{'data-tooltip': 'This attribute indicates the directionality of the<br> termination point. Valid values are sink, source,<br>and bidirectional. This attribute is read-only.'}}},txti_"+elementname+":{type: 'text',group: 'otukttp_"+elementname+"',label: 'TX TI',attrs:{'input':{'data-tooltip':'The Trail Trace Identifier (TTI) information,<br>provisioned by the managing system at the<br>termination source, to be placed in the TTI overhead<br>position of the source of a trail for transmission.<br>This attribute is read-write.'}}},exdapi_"+elementname+":{type: 'text',group: 'otukttp_"+elementname+"',label: 'EX DAPI',attrs:{'input':{'data-tooltip':'The Expected Destination Access Point Identifier<br>(ExDAPI), provisioned by the managing system, to be<br>compared with the TTI accepted at the overhead<br>position of the sink for the purpose of checking the<br>integrity of connectivity. This attribute is<br>read-write.'}}},exsapi_"+elementname+":{type: 'text',group: 'otukttp_"+elementname+"',label: 'EX SAPI',attrs:{'input':{'data-tooltip':'The Expected Source Access Point Identifier<br>(ExSAPI), provisioned by the managing system, to be<br>compared with the TTI accepted at the overhead<br>position of the sink for the purpose of checking the<br>integrity of connectivity. This attribute is<br>read-write.'}}},acti_"+elementname+":{type: 'text',group: 'otukttp_"+elementname+"',label: 'AC TI',attrs:{'input':{'data-tooltip': 'The Trail Trace Identifier (TTI) information<br>recovered (Accepted) from the TTI overhead position<br>at the sink of a trail. This attribute is read-only.'}}},timdetmode_"+elementname+":{type: 'select',options: ['off','dapi','sapi','both'],group: 'otukttp_"+elementname+"',label: 'TIM DET Mode',attrs:{'label':{'data-tooltip':'This attribute indicates the mode of the Trace<br>Identifier Mismatch (TIM) Detection function. Valid<br>values are: off, dapi, sapi, both. This attribute is<br>read-write.'}}},timactdisabled_"+elementname+":{type: 'toggle',group: 'otukttp_"+elementname+"',label: 'TIM ACT Disabled',attrs:{'input':{'data-tooltip':'This attribute provides the control capability for the<br>managing system to enable or disable the Consequent<br>Action function when detecting Trace Identifier Mismatch<br>(TIM) at the trail termination sink. The value of TRUE<br>means disabled. This attribute is read-write.'}}},degthr_"+elementname+":{type: 'number',min: 0,max: 2147483647,index: 7,group:'otukttp_"+elementname+"',label: 'DEG THR',	attrs:{'input':{'data-tooltip': 'This attribute indicates the threshold level for<br> declaring a performance monitoring (PM) Second to be<br> bad. A PM Second is declared bad if the percentage<br> of detected errored blocks in that second is greater<br> than or equal to the specified threshold level.<br> Valid values are integers in units of percentages.<br> This attribute is read-write.'}}},	degm_"+elementname+":{type: 'number',min: 0,max: 2147483647,index:8,group:'otukttp_"+elementname+"',label: 'DEG M',attrs:{'input':{'data-tooltip': 'This attribute indicates the threshold level for<br> declaring a Degraded Signal defect (dDEG). A dDEG<br> shall be declared if DegM consecutive bad PM Seconds<br> are detected. This attribute is read-write.'}}},k_"+elementname+":{type: 'range',min: 1,max: 3,step: 1,group: 'otukttp_"+elementname+"',label: 'k',attrs:{'input':{'data-tooltip':'This attribute specifies the index k that is used<br>to represent a supported bit rate and the different<br>versions of OPUk, ODUk and OTUk. Valid values for<br>this attribute are integers 1, 2 and 3.<br>k = 1 represents an approximate bit rate of 2.5 Gbit/s;<br>k = 2 represents an approximate bit rate of 10 Gbit/s; and<br>k = 3 represents an approximate bit rate of 40 Gbit/s.<br>This attribute is read-only.' }}},currentproblemlist_"+elementname+":{type: 'number',min: 1,max: 31,label: 'Current Problem List',group:'otukttp_"+elementname+"',attrs:{'input':{'data-tooltip':'This attribute indicates the failure conditions of the entity.<br>Possible values of this attribute include decimal <br> numbers between 1 and 31, representing:<br>1)no defect;<br>2)TIM (Trail Trace Identifier Mismatch);<br>3) DEG (Signal Degraded);<br>4) BDI (Backward Defect Indication);<br>5) SSF (Server Signal Fail).<br>This attribute is read-only.'}}},";
				var group = "otukttp_"+elementname+": { label: 'OTUk ttp _"+elementname+"', index: "+i+" },";
				a = a+input;
				b = b+group;
				i++;
				console.log(a);
				console.log(b);
				
				
				var attr1 = 'k_'+elementname;
				var attr2 = 'operationalstate_'+elementname ;
				var attr3 = 'txti_'+elementname;
				var attr4 = 'exdapi_'+elementname;
				var attr5 = 'directionality_'+elementname;
				var attr6 = 'exsapi_'+elementname;
				var attr7 = 'acti_'+elementname;
				var attr8 = 'timdetmode_'+elementname;
				var attr9 = 'timactdisabled_'+elementname;
				var attr10 = 'degthr_'+elementname;
				var attr11 = 'degm_'+elementname;
				var attr12 = 'currentproblemlist_'+elementname;
				
				cell.prop(elementname, {'name':elementname,'grouping':'otuk-ttp-grouping', });
				//cell.prop(elementname,i);
				console.log(cell);
				};
				
				
			}),

//			eval("this.inspector = new joint.ui.Inspector({inputs: {"+a+"},groups:{ "+b+" },cellView: cellView})");


//			eval("this.inspector = new joint.ui.Inspector({inputs: {myproperty: { type: 'range', min: 0, max: 30, defaultValue: 1, group: 'mydata', index: 1 }, attrs: {text: {text: { type: 'textarea', group: 'text', index: 1 },'font-size': { type: 'number', group: 'text', index: 2 }}}},groups: {mydata: { label: 'My Data', index: 1 },text: { label: 'Text', index: 2 } },cellView: cellView})");
			eval("this.inspector = new joint.ui.Inspector({inputs: inspectorDefs ? inspectorDefs.inputs : CommonInspectorInputs,groups: inspectorDefs ? inspectorDefs.groups : CommonInspectorGroups,cell: cell})");
//			this.inspector = new joint.ui.Inspector({    				inputs: inspectorDefs ? inspectorDefs.inputs : CommonInspectorInputs,    						groups: inspectorDefs ? inspectorDefs.groups : CommonInspectorGroups,    								cell: cell    			});    				

			this.initializeInspectorTooltips();this.inspector.render();$('.inspector-container').html(this.inspector.el); 	if (this.inspectorClosedGroups[cell.id]) {_.each(this.inspectorClosedGroups[cell.id], this.inspector.closeGroup, this.inspector);} else {this.inspector.$('.group:not(:first-child)').addClass('closed');}		    
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
			halo.removeHandle('resize');

			// descomentar para inserir a borda de redimensionamento
			//freetransform.render();
			halo.removeHandle('fork');
			halo.removeHandle('clone');
			halo.removeHandle('rotate');
			halo.removeHandle('link');
			halo.removeHandle('unlink');
			halo.removeHandle('remove');
			halo.render();

//			this.initializeHaloTooltips(halo);
			// Verificar atributos da cell nessa parte! e usar o eval


//			this.createInspector(cellView);
//			console.log(cellView.model.attributes.subType);
			if(cellView.model.attributes.subType === 'Card'){

//				var ITUelements = [], ITUlinks = [];

				$('.inspector-container').hide();

//				$('.inspector-container').show();

//				var cellId = cellView.model.id;
//				var card = app.graph.getCell(cellId);
//				console.log(card);
//				console.log(card.attributes.subType);
//				console.log(card.attributes.attrs.data.cells);

//				card.prop('directionality','sink');
//				console.log(card);
//				console.log(cellView);
//				var opt;
//				$.ajax({
//					type: "POST",
//					async: false,
//					url: "getCardAttributes.htm",
//					data: {
//						'card' : cellId,
//						'supervisor' : 'test',
//					},
////					dataType: 'json',
//					success: function(data){
//						console.log(data);
//						opt = data;
////						atributte.fromJSON(data);
//					},
//					error : function(e) {
//						alert("error: " + e.status);
//					}
//				});

//				console.log(opt.length);
//				console.log(opt);
				
//				this.createInspector(cellView
//				this.createInspector(cellView,opt);
			}else{
				$('.inspector-container').hide();
			}

			this.selectionView.cancelSelection();
			this.selection.reset([cellView.model]);

		}, this);

		this.paper.on('link:options', function(evt, cellView, x, y) {

			this.createInspector(cellView);
		}, this);
	},

	initializeNavigator: function() {

//		var navigator = this.navigator = new joint.ui.Navigator({
//			width: 240,
//			height: 115,
//			paperScroller: this.paperScroller,
//			zoomOptions: { max: 5, min: 0.2 }
//		});
//
//		navigator.$el.appendTo('.navigator-container');
//		navigator.render();
	},

	initializeHaloTooltips: function(halo) {

//		new joint.ui.Tooltip({
//			className: 'tooltip small',
//			target: halo.$('.remove'),
//			content: 'Click to remove the object',
//			direction: 'right',
//			right: halo.$('.remove'),
//			padding: 15
//		});
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
//		new joint.ui.Tooltip({
//		className: 'tooltip small',
//		target: halo.$('.unlink'),
//		content: 'Click to break all connections to other objects',
//		direction: 'right',
//		right: halo.$('.unlink'),
//		padding: 15
//		});
//		new joint.ui.Tooltip({
//		className: 'tooltip small',
//		target: halo.$('.link'),
//		content: 'Click and drag to connect the object',
//		direction: 'left',
//		left: halo.$('.link'),
//		padding: 15
//		});
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

		this.clipboard = new joint.ui.Clipboard;

//		KeyboardJS.on('ctrl + c', _.bind(function() {
//		// Copy all selected elements and their associated links.
//		this.clipboard.copyElements(this.selection, this.graph, { translate: { dx: 20, dy: 20 }, useLocalStorage: true });
//		}, this));

//		KeyboardJS.on('ctrl + v', _.bind(function() {

//		this.selectionView.cancelSelection();

//		this.clipboard.pasteCells(this.graph, { link: { z: -1 }, useLocalStorage: true });

//		// Make sure pasted elements get selected immediately. This makes the UX better as
//		// the user can immediately manipulate the pasted elements.
//		this.clipboard.each(function(cell) {

//		if (cell.get('type') === 'link') return;

//		// Push to the selection not to the model from the clipboard but put the model into the graph.
//		// Note that they are different models. There is no views associated with the models
//		// in clipboard.
//		this.selection.add(this.graph.getCell(cell.id));
//		this.selectionView.createSelectionBox(cell.findView(this.paper));

//		}, this);

//		}, this));

//		KeyboardJS.on('ctrl + x', _.bind(function() {

//		var originalCells = this.clipboard.copyElements(this.selection, this.graph, { useLocalStorage: true });
//		this.commandManager.initBatchCommand();
//		_.invoke(originalCells, 'remove');
//		this.commandManager.storeBatchCommand();
//		this.selectionView.cancelSelection();
//		}, this));
	},

	initializeCommandManager: function() {

		this.commandManager = new joint.dia.CommandManager({ graph: this.graph });

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
				padding: 40,
				scaleGrid: 0.2,
				minScale: 0.1,
				maxScale: 10
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
	}

});
