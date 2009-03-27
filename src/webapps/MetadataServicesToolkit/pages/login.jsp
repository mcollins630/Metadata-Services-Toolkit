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
        <title>Welcome To MST</title>
        <c:import url="/inc/meta-frag.jsp"/>
        
        <LINK href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/base-mst.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/menu/assets/skins/sam/menu.css"  rel="stylesheet" type="text/css" >
        
        <LINK href="page-resources/css/global.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/main_menu.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/tables.css" rel="stylesheet" type="text/css" >
		<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">

        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container-min.js"></SCRIPT>    
    	<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/element/element-beta-min.js"></script>     
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/menu/menu-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/button/button-min.js"></script> 
          
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/main_menu.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/forgot_password.js"></SCRIPT>
    </head>
    
    <body class="yui-skin-sam">
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">  

		<!-- page header - this uses the yahoo page styling -->
		<div id="hd">
   
            <!--  this is the header of the page -->
            <c:import url="/inc/header.jsp"/>
              <div id="mainMenu" class="yuimenubar yuimenubarnav">
                <div class="bd">
                    <ul class="first-of-type">
                        <span class="wrenchImg">&nbsp;</span>
                    </ul>
                </div>
             </div>

            <!--  this is the header of the page -->
            <c:import url="/inc/menu.jsp"/>
            <div style="height:10px;">

            </div>
            <jsp:include page="/inc/breadcrumb.jsp">

                    <jsp:param name="bread" value="User Login" />

            </jsp:include>
 		</div>
		<!--  end header -->
		
		<!-- body -->
		<div id="bd">
   			
			 <!-- Display of error message -->
                <c:if test="${errorType != null}">
                    <div class="${errorType}">
                        <img  src="${pageContext.request.contextPath}/page-resources/img/${errorType}.jpg">
                        <s:fielderror cssClass="errorMessage"/>
                    </div>
                 </c:if>

            <div id="error_div"></div>

                <div class="clear">&nbsp;</div>
			
			<form name="loginForm" action="/MetadataServicesToolkit/login.action" method="post">
				<table class="basicTable" align="left">
					
					<tr>
						<td class="label"> Login server : </td>
						<td class="input"> 
							<select name="serverId">
								<c:forEach var="server" items="${servers}">
									<c:if test="${server.id == serverId}">
										<option value="${server.id}" selected>${server.name}</option>
									</c:if>
									<c:if test="${server.id != serverId}">
										<option value="${server.id}">${server.name}</option>
									</c:if>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td class="label"> User name : </td>
						<td class="input"> <input type="text" name="userName" value="${userName}"/> </td>
					</tr>
					<tr>
						<td class="label"> Password : </td>
						<td class="input"> <input type="password" name="password" value=""/> </td>
					</tr>
					<tr>
						<td colspan="2" align="center"> <button type="submit" class="xc_button" name="submit">&nbsp;&nbsp;Login&nbsp;&nbsp;</button> </td>
					</tr>
					<tr>
						<td colspan="2" align="center"> <a href="viewUserRegisteration.action">New User Registeration</a>&nbsp;&nbsp;&nbsp;&nbsp;     | 
						&nbsp;&nbsp;&nbsp;&nbsp;<a href="viewForgotPassword.action">Forgot Password</a>   </td>
					</tr>
				</table>
			</form>
			

 		</div>
		<!--  end body -->		
            
        </div>
        <!-- end doc -->
    </body>
</html>

    
