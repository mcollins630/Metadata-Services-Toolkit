<!--
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  -->

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="/struts-tags" %>


<!--  document type -->
<c:import url="/inc/doctype-frag.jsp"/>

<html>
    <head>
        <title>My Account</title>
        <c:import url="/inc/meta-frag.jsp"/>
        
        <LINK href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/assets/skins/sam/skin.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/base-mst.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/menu/assets/skins/sam/menu.css"  rel="stylesheet" type="text/css" >
       
        <LINK href="page-resources/css/global.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/tables.css" rel="stylesheet" type="text/css" >
	<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">
	<LINK href="page-resources/css/main_menu.css" rel="stylesheet" type="text/css" >

        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/connection/connection-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container-min.js"></SCRIPT>    
    	<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/element/element-beta-min.js"></script>  
        
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/menu/menu-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/main_menu.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/button/button-min.js"></script> 
        
        <SCRIPT LANGUAGE="JavaScript" SRC="pages/js/base_path.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/edit_my_account.js"></SCRIPT>
    </head>
    
    <body class="yui-skin-sam">
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">  

		<!-- page header - this uses the yahoo page styling -->
		<div id="hd">
   
            <!--  this is the header of the page -->
            <c:import url="/inc/header.jsp"/>
            
            <c:import url="/inc/menu.jsp"/>
            
 		</div>
		<!--  end header -->
		
		<!-- body -->
		<div id="bd">
   			<h2> Edit My Account</h2>
   			
   			
   			<div class="errorMessage"> <s:fielderror /> </div>
   			
   			<form name="myAccountForm" method="post">
   			
				<table class="basicTable">
				<tr>
					<td class="label"> Full Name </td>
					<td>
						<input type="text"  id="user_full_name" name="fullName" value="${fullName}" size="35"/>
					</td>
				</tr>
				
				<tr>
					<td class="label"> Login Type</td>
					<td > ${user.server.name} </td>
				</tr>
				<tr>
					<td class="label"> User Name </td>
					<td>
						<input type="text" id="user_name" name="userName" value="${userName}" size="35"/>
					</td>
				</tr>
				<tr>
					<td class="label"> Email </td>
					<td>
						<input type="text" id="user_email" name="email" value="${email}" size="35"/>
					</td>
				</tr>
				<tr>
					<td colspan="2" align="center">
						<button class="xc_button" type="button" name="save" onClick="Javascript:YAHOO.xc.mst.account.save();">Save</button>
						<button class="xc_button" name="cancel" onClick="Javascript:YAHOO.xc.mst.account.cancel();">Cancel</button> 
						
					</td>
					<td>
						<button class="xc_button" type="button" name="changePassword" onClick="Javascript:YAHOO.xc.mst.account.changePassword(${user.id});">Change Password</button>
					</td>
				</tr>						
				</table> 
			</form>
 		</div>
		<!--  end body -->		
            
        </div>
        <!-- end doc -->
    </body>
</html>

    
