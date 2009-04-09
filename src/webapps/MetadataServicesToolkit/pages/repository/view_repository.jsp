<!--
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  -->

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<c:import url="/inc/doctype-frag.jsp"/>

<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">
<html>
<head>
 <title>View Repository</title>
<c:import url="/inc/meta-frag.jsp"/>

<LINK href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css" rel="stylesheet" type="text/css" >
<LINK href="page-resources/yui/assets/skins/sam/skin.css"  rel="stylesheet" type="text/css" >
<LINK href="page-resources/css/base-mst.css" rel="stylesheet" type="text/css" >
<LINK href="page-resources/yui/menu/assets/skins/sam/menu.css"  rel="stylesheet" type="text/css" >
<LINK HREF="page-resources/css/bodylayout.css" REL="stylesheet" TYPE="text/css">
<LINK href="page-resources/css/global.css" rel="stylesheet" type="text/css" >
<LINK href="page-resources/css/tables.css" rel="stylesheet" type="text/css" >
<LINK href="page-resources/css/main_menu.css" rel="stylesheet" type="text/css" >

<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/utilities.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="pages/js/base_path.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/connection/connection-min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container_core-min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/connection/connection-min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container-min.js"></SCRIPT>    
<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/element/element-beta-min.js"></script>     
<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/menu/menu-min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/button/button-min.js"></script> 
<SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/main_menu.js"></SCRIPT>
 <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/view_repository.js"></SCRIPT>

</head>


<body class="yui-skin-sam">


        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">

		<!-- page header - this uses the yahoo page styling -->
		<div id="hd">

            <!--  this is the header of the page -->
            <c:import url="/inc/header.jsp"/>

            <!--  this is the header of the page -->
            <c:import url="/inc/menu.jsp"/>
             <jsp:include page="/inc/breadcrumb.jsp">

                    <jsp:param name="bread" value="Repository , <a style='color:#292929;' href='allRepository.action'><U>All Repositories</U></a> , View Repository : ${provider.name}" />

            </jsp:include>

            <div id="error_div"></div>
            <!-- Display of error message -->
                <c:if test="${errorType != null}">
                    <div id="server_error_div">
                    <div id="server_message_div" class="${errorType}">
                        <img  src="${pageContext.request.contextPath}/page-resources/img/${errorType}.jpg">
                        <c:if test="${errorType == 'error'}">
                        	<s:fielderror cssClass="errorMessage"/>
                       	</c:if>
                        <c:if test="${errorType == 'info'}">
                        	<div class="jsErrorMessage"> ${message}</div>
                        </c:if>
                    </div>
                    </div>
                 </c:if> 
 		</div>
		<!--  end header -->

		<!-- body -->
		<div id="bd">

            <div class="clear">&nbsp;</div>
			<!-- start the grid -->
   	        <div class="yui-g">
   	        	<!-- First grid start -->
		        <div class="yui-u first">

			        <table class="formTable" style="color:#313131;">
			
			            
			            <tr>
			                <td class="label" style="font-size:13px;color:#313131;">
                                
                                    Repository name : ${provider.name} <br></td>
			            </tr>
			            <tr>
                            <td class="label" style="font-size:13px;color:#313131;">
                               
                                   URL : ${provider.oaiProviderUrl}
                            </td>
			            </tr>
                        <tr>
                            <td class="label" style="margin-top:20px;font-size:14px;color:#313131">
                                <br> Validation Results
                            </td>
                        </tr>
			            <tr>
                            <td style="margin-top:15px;color:#313131">
                               <ul style="list-style:none;">
                                    <li style="float:left;"><div style="width:150px;"><B>Formats</B> </div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;">
                                        <div style="margin-left:15px;">
                                            <c:forEach var="format" items="${provider.formats}">
                                                ${format.name} <br>
                                            </c:forEach>
                                        </div>
                                    </li>
                                </ul>
                            </td>
			               
			                
                           
			            </tr>
			            <tr>
                            <td>
                                <ul style="list-style:none;color:#313131;">
                                    <li style="float:left;"><div style="width:150px;"><B>Sets Supported</B> </div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;">
                                        <div style="margin-left:15px;">
                                            <c:forEach var="set" items="${provider.sets}">
                                                ${set.setSpec} <br>
                                            </c:forEach>
                                        </div>
                                    </li>
                                </ul>
                            </td>
			        
			                
			            </tr>
			            <tr>
                            <td>
                                <ul style="list-style:none;">
                                    <li style="float:left;"><div style="width:150px;"><B>Protocols Supported </B></div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;"><div style="margin-left:15px;">${provider.protocolVersion} </div></li>
                                </ul>
                            </td>
			                
			            </tr>
			            <tr>
                            <td>
                                <ul style="list-style:none;">
                                    <li style="float:left;"><div style="width:150px;"><B>Last Validation Date</B> </div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;"><div style="margin-left:15px;">${provider.lastValidationDate}</div></li>
                                </ul>
                            </td>
			                
			            </tr>
			            <tr>
                            <td>
                                <ul style="list-style:none;">
                                    <li style="float:left;"><div style="width:150px;"><B>Created By</B> </div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;"><div style="margin-left:15px;">${user.firstName} ${user.lastName}</div></li>
                                </ul>
                            </td>
			               
			            </tr>
			            <tr>
                            <td>
                                <ul style="list-style:none;">
                                    <li style="float:left;"><div style="width:150px;"><B>Creation Date</B> </div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;"><div style="margin-left:15px;">${provider.createdAt}</div></li>
                                </ul>
                            </td>
			               
			            </tr>
			            <tr>
                            <td>
                                <ul style="list-style:none;">
                                    <li style="float:left;"><div style="width:150px;"><B>Last Modified Date</B> </div> </li>
                                    <li style="float:left;"><div>:</div></li>
                                    <li style="float:left;"><div style="margin-left:15px;">${provider.updatedAt}</div></li>
                                </ul>
                            </td>
			                
			            </tr>
			       </table>
			           
	         
         	<!--  end the first column -->
                </div>
		
		<!-- start second column -->
       	        <div class="yui-u">
                   
                       <table width="220" border="1" style="color:#313131;">
                           <c:choose>
                               <c:when test="${provider.identify==true}">
                                    <tr>
                                        <td height="33" bgcolor="#edfaff">
                                            
                                            <ul style="list-style:none;">
                                                <li style="float:left;"><div style="width:90px;" align="right"><B>&nbsp;Identify</B> :</div></li>
                                                <li style="float:left;"><div><img src="page-resources/img/bluetick.jpg"></div></li>
                                                <li style="float:left;"><div>Success</div></li>
                                            </ul>
                                        </td>
                                    </tr>
                               </c:when>
                               <c:otherwise>
                                    <tr>
                                        <td height="33" bgcolor="#ffeded">
                                            <ul style="list-style:none;">
                                                <li style="float:left;"><div style="width:90px;" align="right"><B>&nbsp;Identify</B> : </div></li>
                                                <li style="float:left;"><div><img src="page-resources/img/error_triangle.jpg"></div></li>
                                                <li style="float:left;"><div>Error</div></li>
                                            </ul>
                                        </td>
                                    </tr>
                               </c:otherwise>
                           </c:choose>
                           <c:choose>
                               <c:when test="${provider.listFormats==true}">
                                    <tr>
                                        <td height="33" bgcolor="#edfaff">
                                            <ul style="list-style:none;">
                                                <li style="float:left;"><div style="width:90px;" align="right"><B>&nbsp;List Formats</B> :</div></li>
                                                <li style="float:left;"><div><img src="page-resources/img/bluetick.jpg"></div></li>
                                                <li style="float:left;"><div>Success</div></li>
                                            </ul>
                                        </td>
                                    </tr>
                               </c:when>
                               <c:otherwise>
                                    <tr>
                                        <td height="33" bgcolor="#ffeded">
                                            <ul style="list-style:none;">
                                                <li style="float:left;"><div style="width:90px;" align="right"><B>&nbsp;List Formats</B> :</div></li>
                                                <li style="float:left;"><div><img src="page-resources/img/error_triangle.jpg"></div></li>
                                                <li style="float:left;"><div>Error</div></li>
                                            </ul>
                                        </td>
                                    </tr>
                               </c:otherwise>
                           </c:choose>
                            <c:choose>
                               <c:when test="${provider.listSets==true}">
                                     <tr>
                                        <td height="33" bgcolor="#edfaff">
                                            <ul style="list-style:none;">
                                                <li style="float:left;"><div style="width:90px;" align="right"><B>&nbsp;List Sets</B> : </div></li>
                                                <li style="float:left;"><div><img src="page-resources/img/bluetick.jpg"></div></li>
                                                <li style="float:left;"><div>Success</div></li>
                                            </ul>
                                        </td>
                                     </tr>
                               </c:when>
                               <c:otherwise>
                                     <tr>
                                        <td height="33" bgcolor="#ffeded">
                                            <ul style="list-style:none;">
                                                <li style="float:left;"><div style="width:90px;" align="right"><B>&nbsp;List Sets</B> : </div></li>
                                                <li style="float:left;"><div><img src="page-resources/img/error_triangle.jpg"></div></li>
                                                <li style="float:left;"><div>Error</div></li>
                                            </ul>
                                        </td>
                                     </tr>
                               </c:otherwise>
                           </c:choose>
                           
                           
                       </table>
                        
                          <br><br><br>
                          
        	    </div>
                </td>
            	<!--  end the second column -->
               </tr>
                <tr>
                    <td colspan="2" align="center">
                     <Br><Br>
                    <div id="logRepository" style="display:none;">
                        <textarea cols="100" rows="20" readonly style="background-color:#edfaff;border-style:hidden;"></textarea>
                    </div>
                    </td>
                </tr>

               </table>
    	    </div>
        	<!--  end the grid -->
            <br><br>
            <hr size="1" style="color:#ced2d5;"><br>
              <ul style="list-style:none;">
                  <li style="float:left;">
                      <div align="left" style="vertical-align:bottom;">
                        <button class="xc_button" onclick="javascript:YAHOO.xc.mst.repository.editFunction(${provider.id});" type="button" name="edit">Edit</button>&nbsp;
                        <button class="xc_button" onclick="javascript:YAHOO.xc.mst.repository.downloadFile('HarvestIn','${provider.id}');" type="button" name="View Log">View Log</button>&nbsp;
                        <button class="xc_button" onclick="javascript:YAHOO.xc.mst.repository.reValidateFunction(${provider.id});" type="button" name="Revalidate">Revalidate</button>&nbsp;
                        <button class="xc_button" id="confirmDeleteRepository" type="button"  name="delete">Delete</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      </div>
                  </li>
                  <li style="float:right;">
                      <div align="right" style="vertical-align:bottom;">
                          <button class="xc_button" type="button" onclick="javascript:YAHOO.xc.mst.repository.doneFunction();" name="done">Done</button>
                      </div>
                  </li>
              
              </ul>
        </div>


	      <div id="deleteRepositoryDialog" class="hidden">
	          <div class="hd">Delete Repository</div>
		      <div class="bd">
		          <form id="deleteRepository" name="deleteRepository" method="POST" 
		              action="deleteRepository.action">
		              
		              <input type="hidden" name="repositoryId" value="${provider.id}"/>
		              
			          <p>Are you sure you wish to delete the repository?</p>
		          </form>
		      </div>
	      </div>

	      <div id="deleteRepositoryOkDialog" class="hidden">
	          <div class="hd">Delete Repository</div>
		      <div class="bd">
			          <div id="deleteRepositoryError" cssClass="errorMessage"></div>
		      </div>
	      </div>


       </div>
</body>
</html>
