function generateSaveEquipmentDialog(graph){
	
	var content = '<div id="save-dialog" title="Save Equipment">'
		+ 'Name: <input type="text" id="save-filename" value="' + $('#filename').val() + '"/>'
		+ '</div>'
		+ '<div id="name-error-message">' + 'Name cannot be empty!' + '</div>';
		
	var dialog = new joint.ui.Dialog({
		width: 300,
		type: 'neutral',
		title: 'Save Equipment',
		content: content,
		buttons: [
			{ action: 'cancel', content: 'Cancel', position: 'left' },
			{ action: 'save', content: 'Save', position: 'left' }
		]
	});
	
	dialog.on('action:save', checkEquipmentFile);
	dialog.on('action:cancel', cancel);

	dialog.open();
	
	function cancel(){
		dialog.close();
	}
	
	function checkEquipmentFile(){
		
		if($("#save-filename").val() == ""){
			$('#name-error-message').show();
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
						   
						   $.ajax({
							   type: "POST",
							   assync: false,
							   url: "deleteITUFiles.htm",
							   data: {
								 'filename': $("#save-filename").val(),
							   },
							   success: function(){},
							   error : function(e) {
								   alert("error: " + e.status);
								   dialog.close();
							   }
							});
						   
						    saveEquipment();
					   } 
				   }
				   else{
					   saveEquipment();
				   }
			   },
			   error : function(e) {
				   alert("error: " + e.status);
				   dialog.close();
			   }
			});
		}
		
	};
	
	function saveEquipment(){
		
		$('#filename').val($("#save-filename").val());
		
		$.ajax({
		   type: "POST",
		   url: "saveEquipment.htm",
		   data: {
			 'filename': $("#save-filename").val(),
			 'graph': JSON.stringify(graph.toJSON()),
		   },
		   success: function(){ 		   
			   saveITUFiles()
			   
			   alert($("#save-filename").val() + ' saved successfully!');
			   dialog.close();
		   },
		   error : function(e) {
			   alert("error: " + e.status);
			   dialog.close();
		   }
		});
		
		function saveITUFiles(){
			
			$.each(graph.getElements(), function( index, cell ) {
				
				if(!(cardArray[cell.id] === undefined)){
				
					if(cell.get('subType') === 'card'){
						$.ajax({
						   type: "POST",
						   url: "saveITUFiles.htm",
						   data: {
							 'path': $("#save-filename").val(),
							 'filename': cell.id,
							 'graph': JSON.stringify(cardArray[cell.id]),
						   },
						   success: function(){},
						   error : function(e) {
							   alert("error: " + e.status);
							   dialog.close();
						   }
						});	
					}
				}
				
			});
			
			
		}
		
	};
	
}