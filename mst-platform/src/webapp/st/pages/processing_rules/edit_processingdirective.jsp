<!--
  * Copyright (c) 2009 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  -->
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mst" uri="mst-tags"%>

<c:import url="/st/inc/doctype-frag.jsp"/>

<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">

<html>
    <head>
        <title>Edit Processing Directive</title>
        <c:import url="/st/inc/meta-frag.jsp"/>

        <LINK href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/base-mst.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/menu/assets/skins/sam/menu.css"  rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/global.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/main_menu.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/tables.css" rel="stylesheet" type="text/css" >
    <LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">
    <LINK href="page-resources/css/bodylayout.css" rel="stylesheet" type="text/css">

        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/utilities.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/connection/connection-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container_core-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/menu/menu-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/main_menu.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/edit_processingdirective.js"></SCRIPT>

    </head>


 <body class="yui-skin-sam">

        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">

            <!-- page header - this uses the yahoo page styling -->
            <div id="hd">

                <!--  this is the header of the page -->
                <c:import url="/st/inc/header.jsp"/>

                <!--  this is the header of the page -->
                <c:import url="/st/inc/menu.jsp"/>
                 <jsp:include page="/st/inc/breadcrumb.jsp">

                    <jsp:param name="bread" value="Processing Rules | <a href='listProcessingDirectives.action'><U>List Processing Rules</U></a> | Show Processing Rule (Step 1)" />

                </jsp:include>
            </div>
            <!--  end header -->

            <!-- body -->
            <div id="bd">

                 <!-- Display of error message -->
                 <c:if test="${errorType != null}">
                    <div id="server_error_div">
                    <div id="server_message_div" class="${errorType}">
                        <img  src="${pageContext.request.contextPath}/page-resources/img/${errorType}.jpg">
                        <span class="errorText">
                            <mst:fielderror error="${fieldErrors}">
                            </mst:fielderror>
                        </span>
                    </div>
                    </div>
                 </c:if>
                 <div id="error_div"></div>
                 <div id="clear">&nbsp;</div>

                <div class="stepsStructure">
                    <span style="position: relative; top: 13px;"><img src="page-resources/img/3.4_step1_highlight.gif"></span>
                    <span style="position: relative; top: 12px;"><img src="page-resources/img/3.4_step2_grey.gif"></span>

                </div>
                <div align="right" style="margin-bottom:10px;">
                    <button style="vertical-align:bottom;" class="xc_button_small" type="button" onclick="javascript:YAHOO.xc.mst.processingDirective.editDirective.cancel();" name="cancel">Cancel</button> &nbsp;&nbsp;&nbsp;
                    <button style="width:170px;" class="xc_button" type="button" onclick="javascript:YAHOO.xc.mst.processingDirective.editDirective.editProcessingDirective();" name="next"><span style="position:relative;top:-3px;">Continue to Step 2 </span> <img src="page-resources/img/bullet_go.gif"></button>
                </div>

                    <form action="editProcessingDirectives.action?ProcessingDirectiveId=${processingDirectiveId}" method="post" name="editProcessingDirective">
                    <div class="greybody">
                    <table cellpadding="0" cellspacing="0" border="0" width="75%">

                        <tr>
                            <td>
                                <div align="right" style="margin-right:25px;">
                                    <c:choose>
                                        <c:when test="${temporaryProcessingDirective.sourceProvider!=null}">
                                            <c:set var="source" value="${temporaryProcessingDirective.sourceProvider.name}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="source" value="${temporaryProcessingDirective.sourceService.name}"/>
                                        </c:otherwise>
                                    </c:choose>
                                        <B>Select Source for records to be Processed</B> <br><br>
                                        <B>External Repositories</B> <br><br>
                                            <c:forEach var="provider" items="${providerList}" varStatus="providerCount">
                                                <c:choose>
                                                    <c:when test="${source==provider.name}">
                                                        <input checked type="radio" name="source" value="${provider.name}"  style="display:none;"><br><br>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <input type="radio" name="source" value="${provider.name}"  style="display:none;"><br><br>
                                                    </c:otherwise>
                                                </c:choose>

                                            </c:forEach>
                                            <c:forEach var="provider" items="${providerList}" varStatus="providerCount">
                                                <c:choose>
                                                    <c:when test="${source==provider.name}">
                                                        <c:out value="${provider.name}"/>&nbsp;&nbsp;<input checked type="radio" name="source2" value="${provider.name}" disabled><br><br>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${provider.name}"/>&nbsp;&nbsp;<input type="radio" name="source2" value="${provider.name}" disabled><br><br>
                                                    </c:otherwise>
                                                </c:choose>

                                            </c:forEach>
                                            <br><br><br>
                                        <B> Input Records to Services </B><br><br>
                                            <c:forEach var="service" items="${serviceList}" varStatus="serviceCount">
                                                <c:choose>
                                                    <c:when test="${source==service.name}">
                                                        <c:out value="${service.name}"/>&nbsp;&nbsp;<input checked type="radio" name="source" value="${service.name}" disabled><br><br>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${service.name}"/>&nbsp;&nbsp;<input type="radio" name="source" value="${service.name}" disabled><br><br>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                </div>

                            </td>
                            <td valign="top">
                                <div align="center">
                                    <img src="page-resources/img/greenarrow_greybgrd.jpg">
                                </div>
                            </td>
                            <td valign="top">
                                <div style="margin-left:25px;">
                                    <B> Output Records from Services </B><br><br>
                                    <c:forEach var="service" items="${serviceList}" varStatus="serviceCount">
                                        <c:choose>
                                            <c:when test="${service.id==temporaryProcessingDirective.service.id}">
                                                <input checked type="radio" name="service" value="${service.id}" disabled>&nbsp;&nbsp;<c:out value="${service.name}"/>&nbsp;&nbsp;<br><br>
                                            </c:when>
                                            <c:otherwise>
                                                <input type="radio" name="service" value="${service.id}" disabled>&nbsp;&nbsp;<c:out value="${service.name}"/>&nbsp;&nbsp;<br><br>
                                            </c:otherwise>
                                        </c:choose>

                                    </c:forEach>
                                </div>
                            </td>
                        </tr>


                    </table>
                    </div>

                    <div align="right" style="margin-top:10px;">
                        <button style="vertical-align:bottom;" class="xc_button_small" type="button" onclick="javascript:YAHOO.xc.mst.processingDirective.editDirective.cancel();" name="cancel">Cancel</button> &nbsp;&nbsp;&nbsp;
                        <button style="width:170px;" class="xc_button" type="button" onclick="javascript:YAHOO.xc.mst.processingDirective.editDirective.editProcessingDirective();" name="next"><span style="position:relative;top:-3px;">Continue to Step 2 </span> <img src="page-resources/img/bullet_go.gif"></button>
                    </div>
                    </form>
             </div>
                         <!--  this is the footer of the page -->
            <c:import url="/st/inc/footer.jsp"/>
           </div>
       </body>
     </html>
