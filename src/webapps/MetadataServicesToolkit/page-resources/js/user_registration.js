 /*
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */
  
YAHOO.namespace("xc.mst.registeration");



YAHOO.xc.mst.registeration = {

	register : function()
	{

		if (document.getElementById('user_full_name').value=='') {
			alert('User Full Name is required.');
			return false;
		}
		if (document.getElementById('login_server').value== '0') {
			alert('Please select login sever type.');
			return false;
		}						

		if (document.getElementById('user_name').value=='') {
			alert('User name is required.');
			return false;
		}
		
		

		if (!document.getElementById('user_password').disabled) {
			if (document.getElementById('user_password').value=='') {
				alert('Password is required.');
				return false;
			}
			if (document.getElementById('user_password').value!=document.getElementById('user_password_confirmation').value) {
				alert('Password does not match confirmation password.');
				return false;
			}
			
			if (document.getElementById('user_password').value.length < 6 || document.getElementById('user_password').value.length > 20) {
				alert('Password should be 6 to 10 characters long');
				return false;
			}
					
			var alphaExp = /^[0-9a-zA-Z]+$/;
			
			var validChar = '*,(,),_,#,@';
			var i = 0;
			for(i = 0; i < document.getElementById('user_password').value.length; i++) {
				if(document.getElementById('user_password').value[i].match(alphaExp)){

				}else{
					if  (validChar.indexOf(document.getElementById('user_password').value[i]) < 0 ) {
						alert('Invalid character in password. Only A-Z, a-z, 0-9 , *, @, (,), _,# are allowed.');
						return false;
					}
				}
			}

		}
		if (document.getElementById('user_email').value=='') {
			alert('Email is required.');
			return false;
		} else {
			if (!YAHOO.xc.mst.registeration.emailCheck(document.getElementById('user_email').value)) {
				alert("Invalid E-mail ID");
				return false;
			}

		}

		document.registerationForm.action= basePath + 'registerUser.action';
		document.registerationForm.submit();
	
	},

	cancel : function()
	{
		document.registerationForm.action= basePath + 'home.action';
		document.registerationForm.submit();
	
	},
	
	emailCheck : function(str) 
	{
			var at="@"
			var dot="."
			var lat=str.indexOf(at)
			var lstr=str.length
			var ldot=str.indexOf(dot)
			if (str.indexOf(at)==-1){
			   return false
			}
	
			if (str.indexOf(at)==-1 || str.indexOf(at)==0 || str.indexOf(at)==lstr){
			   return false
			}
	
			if (str.indexOf(dot)==-1 || str.indexOf(dot)==0 || str.indexOf(dot)==lstr){
			    return false
			}
	
			 if (str.indexOf(at,(lat+1))!=-1){
			    return false
			 }
	
			 if (str.substring(lat-1,lat)==dot || str.substring(lat+1,lat+2)==dot){
			    return false
			 }
	
			 if (str.indexOf(dot,(lat+2))==-1){
			    return false
			 }
			
			 if (str.indexOf(" ")!=-1){
			    return false
			 }
	
	 		 return true					
	},
	
	/*
	 * Show the form to enter the password
	 */
	determinePasswordBoxDisplay : function() 
	{

		if (document.getElementById("login_server").value == 'Local') {
			document.getElementById("user_password").disabled = false;
			document.getElementById("user_password_confirmation").disabled = false;
		} else {
			document.getElementById("user_password").disabled = 'true';
			document.getElementById("user_password_confirmation").disabled = 'true';
		} 
	},

	init : function()
    	{
		YAHOO.xc.mst.registeration.determinePasswordBoxDisplay();
	}
};



// initialize the code once the dom is ready
YAHOO.util.Event.onDOMReady(YAHOO.xc.mst.registeration.init);
