var Stencil = {};

Stencil.groups = {
		layers: { index: 1, label: 'Layers'},
		transportFunctions: { index: 2, label: 'Transport Functions' },
		interfaces: { index: 3, label: 'Interfaces'},
};

Stencil.shapes = {
	
	layers: [],

	transportFunctions: [
	
		new joint.shapes.basic.Path({
			subtype: 'AF',
			attrs: {
			        path: { d: 'M 50 0 L 0 0 L 25 50 L 75 50 L 100 0  z', fill: '#8e44ad' },
			    	text: { 'font-size': 12, display: '', 'ref-y': .2, fill: 'white'  }
			}
		}),

		new joint.shapes.basic.Path({
			subtype: 'TTF',
			attrs: {
			        path: { d: 'M 0 0 L 0.5 1 L 1 0 z', fill: '#8e44ad' },
			    	text: { 'font-size': 12, display: '', 'ref-y': .2, fill: 'white'  }
			}
		})
		
	],
	
	/* RF: Inserir portas de entrada e saída aos nós */
	interfaces: [
		//porta de entrada
		new joint.shapes.basic.Circle({
			subtype: 'in',
		    attrs: {
		        circle: { fill: '#f1c40f' },
		        text: { fill: '#000000', 'font-size': 10, stroke: '#000000', 'stroke-width': 0 }
		    }
		}),
		
		// porta de saída
		new joint.shapes.basic.Rect({
			subtype: 'out',
		    attrs: {
		        rect: {
		            rx: 2, ry: 2,
		            fill: '#e9967a'
		        },
		        text: { fill: '#000000', 'font-size': 10, stroke: '#000000', 'stroke-width': 0 }
		    }
		})
	]
};