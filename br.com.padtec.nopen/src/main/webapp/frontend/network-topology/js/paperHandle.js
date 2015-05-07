/**
 * Procedure to handle with paper.
 * @param paper
 */
function paperHandle(paper){
	
	// Procedure to open a Equipment Template
	paper.on('cell:pointerdblclick', function(cellView, evt, x, y) {    
	    var filename = cellView.model.attr('equipment/template');
//	    window.open('/nopen/equipment-studio.htm?template=' + template, '_blank');
	    
    	$(function () {
            $('#equipment-dialog').dialog({
                modal: true,
                //show: 'scale',
                height: 500,
    			width: 500,
                //position: 'middle',
                title: 'Dynamically Loaded Page',
                open: function ()
                {
                    $('#equipment-iframe').attr('src','/nopen/equipment-visualizer.htm?equipment=' + filename);
                },
                close: function() {
                }
            });
        });	
    	
    });

	
};

function closeIframe(){
	$('#equipment-dialog').dialog("close");
};
