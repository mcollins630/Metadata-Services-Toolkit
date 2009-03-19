<!--
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  -->
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<!--  document type -->
<c:import url="/inc/doctype-frag.jsp"/>

<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">


<html>
    <head>
        <title>Add LDAP User</title>
        <c:import url="/inc/meta-frag.jsp"/>

        <LINK href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/base-mst.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/menu/assets/skins/sam/menu.css"  rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/global.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/main_menu.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/tables.css" rel="stylesheet" type="text/css" >
		<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">
		<LINK href="page-resources/css/bodylayout.css" rel="stylesheet" type="text/css">
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/connection/connection-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container_core-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/menu/menu-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/main_menu.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/add_LDAPuser.js"></SCRIPT>

    </head>

    <body class="yui-skin-sam">
        <%@ taglib prefix="s" uri="/struts-tags" %>
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">

		<!-- page header - this uses the yahoo page styling -->
		<div id="hd">

            <!--  this is the header of the page -->
            <c:import url="/inc/header.jsp"/>

            <!--  this is the header of the page -->
            <c:import url="/inc/menu.jsp"/>
            <jsp:include page="/inc/breadcrumb.jsp">

                    <jsp:param name="bread" value="Users & Groups , Add LDAP User" />

            </jsp:include>
 		</div>
		<!--  end header -->

		<!-- body -->
		<div id="bd">
         
         <div class="errorMessage"> <s:fielderror /> </div> <br>
             <br>

                <form action="addLDAPUser.action" method="post" name="addLDAPUser">

                   <table>
                       <tr>
                           <td valign="top"><B>Username</B>  &nbsp;&nbsp;</td>
                           <td><input type="text" id="userName" name ="userName" style="width:200px;height:25px;" value="${temporaryUser.username}" maxlength="255"><br><br></td>
                       </tr>
                       
                       
                       <tr>
                           <td valign="top"><B>Full Name</B>  &nbsp;&nbsp;</td>
                           <td><input type="text" id="fullName" name ="fullName" style="width:200px;height:25px;" value="${temporaryUser.fullName}" maxlength="255"><br><br></td>
                       </tr>
                       
                       <tr>
                           <td valign="top"><B>Email</B>  &nbsp;&nbsp;</td>
                           <td><input type="text" id="email" name ="email" style="width:200px;height:25px;" value="${temporaryUser.email}" maxlength="255"><br><br></td>
                       </tr>
                       
                       
                       
                  
                   </table>
                    
                     

                       <br><br>

                        <table>
                           <tr>
                               <td><B>Groups</B>  &nbsp;&nbsp;

                                   <br><br>
                                   <select multiple size="8" id="groupsSelected" name ="groupsSelected" style="width:300px; height:125px;">
                                     <c:forEach var="m" varStatus="b" items="${groupList}">

                                         <c:set var="flag" value="${false}"/>
                                           <c:forEach var="n" varStatus="a" items="${selectedGroups}">

                                                <c:if test="${m.id==n}">
                                                   <c:set var="flag" value="${true}"/>
                                                </c:if>
                                           </c:forEach>
                                           <c:choose>
                                               <c:when test="${flag==true}">

                                                   <OPTION SELECTED value="${m.id}"><c:out value="${m.name}"/></OPTION>
                                               </c:when>
                                               <c:otherwise>

                                                   <OPTION value="${m.id}"><c:out value="${m.name}"/></OPTION>
                                               </c:otherwise>
                                           </c:choose>
                                     </c:forEach>

                                   </select>
                                   <BR><br><br>
                               </td>
                           </tr>
                        </table>




                        <div align="left">
                             <button style="vertical-align:bottom;" class="xc_button_small" type="button" name="cancel" onclick="javascript:YAHOO.xc.mst.user.cancel();">Cancel</button> &nbsp;&nbsp;&nbsp;
                            
                                 
                             <button class="xc_button" type="button" name="addlocaluser" onclick="javascript:YAHOO.xc.mst.user.addLDAPUser();">Add LDAP User</button>
                                
                                
                            
                             
                        </div>

                </form>
          </div>
      </div>
</body>
</html>