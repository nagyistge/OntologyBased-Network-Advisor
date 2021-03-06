function getProvisioning(){
	
	var content
	
	$.ajax({
	   type: "GET",
	   async: false,
	   url: "getAllProvisioning.htm",
	   dataType: 'json',
	   success: function(data){ 		   
		   generateOptionsContent(data)
	   },
	   error : function(e) {
		   alert("error: " + e.status);
	   }
	});
	
	function generateOptionsContent(data){
		
		content = '';
		
		for(var i = 0; i < Object.keys(data).length; i++){
			
			if(i == 0){
				content = '<hr/>';
			}
			
			content = content + '<div class="btn-group">'
									+ '<a class="btn" title="Edit" href="provisioning.htm?provisioning=' + data[i].provisioning + '"><i class="icon-edit"></i></a>' 
									+ '<a class="btn" title="Delete" onclick="deleteProvisioning(\'' + data[i].provisioning + '\')"><i class="icon-trash"></i></a>'
									+ '<span class="name">' + data[i].provisioning + '</span>'
								+ '</div>'
								+ '<br/><hr/>'
		}
		
	}
	
	$('.btn-toolbar').append(content);

};

function deleteProvisioning(filename){
	
	if (confirm('Are you sure you want to delete this file?')) {
	
		$.ajax({
		   type: "POST",
		   async: false,
		   url: "deleteProvisioning.htm",
		   data: {
			   'filename' : filename
		   },
		   success: function(){ 		   
			   alert(filename + " deleted successfully!");
			   window.location.reload(true);
		   },
		   error : function(e) {
			   alert("error: " + e.status);
		   }
		});
	}
};

