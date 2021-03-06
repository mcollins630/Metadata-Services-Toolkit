<!--
  * Copyright (c) 2009 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  -->
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="mst" uri="mst-tags"%>

<!--  document type -->
<c:import url="/st/inc/doctype-frag.jsp"/>

<html>
    <head>
        <title>Browse Records</title>
        <c:import url="/st/inc/meta-frag.jsp"/>

        <LINK href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/assets/skins/sam/skin.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/base-mst.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/yui/menu/assets/skins/sam/menu.css"  rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/global.css" rel="stylesheet" type="text/css" >
        <LINK href="page-resources/css/tables.css" rel="stylesheet" type="text/css" >
    <LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">
    <LINK href="page-resources/css/main_menu.css" rel="stylesheet" type="text/css" >

        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/utilities.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/connection/connection-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" src="page-resources/yui/container/container-min.js"></SCRIPT>
      <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/element/element-beta-min.js"></script>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/menu/menu-min.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/main_menu.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/yui/button/button-min.js"></script>
    </head>

    <body class="yui-skin-sam">
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">

    <!-- page header - this uses the yahoo page styling -->
    <div id="hd">

            <!--  this is the header of the page -->
            <c:import url="/st/inc/header.jsp"/>

            <c:import url="/st/inc/menu.jsp"/>

            <c:url var="viewResults" value="browseRecords.action">
          <c:param name="query" value="${query}"/>
          <c:param name="searchXML" value="${searchXML}"/>
          <c:param name="selectedFacetNames" value="${selectedFacetNames}"/>
            <c:param name="selectedFacetValues" value="${selectedFacetValues}"/>
          <c:param name="rowStart" value="${rowStart}"/>
          <c:param name="startPageNumber" value="${startPageNumber}"/>
        <c:param name="currentPageNumber" value="${currentPageNumber}"/>
        </c:url>
            <jsp:include page="/st/inc/breadcrumb.jsp">
            <jsp:param name="bread" value="Browse Records|<a href='${viewResults}'>Search Results</a>| Error Information" />
            </jsp:include>

     </div>
    <!--  end header -->

    <!-- body -->
       <!-- remove the error code and semi-colon prefix -->
       <c:set var="errorValue" value="${error}" />
       <c:set var="errCodePrefix" value="${fn:substringBefore(error, ':')}:" />
       <c:if test="${errCodePrefix != ':'}">
          <c:set var="errorValue" value="${fn:substringAfter(error, errCodePrefix)}" />
       </c:if>

    <div id="bd">
      <div class="record_box">
      <p>
        <br>
          <strong>${errorValue}</strong>
        <br><br>
          ${errorDescription}
        <br>
      </p>
      </div>

     </div>
    <!--  end body -->
            <!--  this is the footer of the page -->
            <c:import url="/st/inc/footer.jsp"/>
        </div>
        <!-- end doc -->
    </body>
</html>


