
var Stencil = {};

var Stencil = {
		
		createContainer: function(tech, upLayer) {
			var newContainer = new Container({
		        technology: tech,
		        upmostLayer: upLayer,
				attrs: {
					'.': { magnet: false },
					'.header': { fill: '#5799DA' }
				},
				lanes: { 
					label: tech
				}
			});
			
			return newContainer;
		},
		
		createSubnetwork: function() {
			var newSubnetwork = new joint.shapes.basic.Circle({
				position: {x: 150, y: 40},
				size: { width: 200, height: 120 },
			    attrs: {
					'.': { magnet: true },
			        text: { text: 'Subnetwork', fill: '#000000', 'font-size': 14, stroke: '#000000', 'stroke-width': 0 }
			    }
			});
			
			return newSubnetwork;
		},
		
		createNode: function() {
			var newNode = new joint.shapes.basic.Circle({
			    attrs: {
					'.': { magnet: true },
			        circle: { fill: '#ffffff', r: 10 }
			    }
			});
			
			return newNode;
		}
};

