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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!--  document type -->
<c:import url="/inc/doctype-frag.jsp"/>

<html>
    <head>
        <title>Browse Records</title>
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
    </head>
    
    <body class="yui-skin-sam">
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">  

		<!-- page header - this uses the yahoo page styling -->
		<div id="hd">
   
            <!--  this is the header of the page -->
            <c:import url="/inc/header.jsp"/>
            
            <c:import url="/inc/menu.jsp"/>
            <jsp:include page="/inc/breadcrumb.jsp"> 

                    <jsp:param name="bread" value="Browse Records" />

            </jsp:include>
            
 		</div>
		<!--  end header -->
		
		<!-- body -->
		<div id="bd">
   			
   			
   			
				<div class="facet_search_results">
					
					 	<div class="facetContainer">
					 		<c:forEach var="facet" items="${result.facets}">
						       <div class="facetTitle">
							   	<p><strong>${facet.name}</strong>
							   		<c:forEach var="filter" items="${result.facetFilters}">
								   		<c:if test="${facet.name == filter.name}">
										  	 <c:url var="removeFacet" value="browseRecords.action">
												  <c:param name="query" value="${query}"/>
												  <c:param name="removeFacetName" value="${filter.name}"/>
												  <c:param name="removeFacetValue" value="${filter.value}"/>
												  <c:param name="selectedFacetNames" value="${selectedFacetNames}"/>
												  <c:param name="selectedFacetValues" value="${selectedFacetValues}"/>
											  </c:url>
										  	: ${filter.value} (<a href="${removeFacet}">Remove</a>)
								  		</c:if>
								  	</c:forEach>
							   	</p>
						       </div>

						       <div class="facetContent">
								<p>
								<c:forEach var="fcount" items="${facet.values}">
						      			<c:url var="facetFilter" value="browseRecords.action">
										  <c:param name="query" value="${query}"/>
										  <c:param name="addFacetName" value="${facet.name}"/>
										  <c:param name="addFacetValue" value="${fcount.name}"/>
										  <c:param name="selectedFacetNames" value="${selectedFacetNames}"/>
										  <c:param name="selectedFacetValues" value="${selectedFacetValues}"/>
									 </c:url>

									<a href="${facetFilter}">${fcount.name} (${fcount.count})</a><br/>
								</c:forEach> 
								</p>

						       </div>
						        </c:forEach>
					   	</div>	
					
<!--				
				<div class="facet_container">
				    <div class="facet_search_selection">
					  <h2>Selected Facets</h2>
					  <c:forEach var="filter" items="${result.facetFilters}">
					  	 <c:url var="removeFacet" value="browseRecords.action">
							  <c:param name="query" value="${query}"/>
							  <c:param name="removeFacetName" value="${filter.name}"/>
							  <c:param name="removeFacetValue" value="${filter.value}"/>
							  <c:param name="selectedFacetNames" value="${selectedFacetNames}"/>
							  <c:param name="selectedFacetValues" value="${selectedFacetValues}"/>
						  </c:url>
					  	${filter.name}: ${filter.value} <a href="${removeFacet}">Remove</a>
					  
					  </c:forEach>
				     </div>

				     <div class="facets"> 

					    <c:forEach var="facet" items="${result.facets}">
						<c:if test="${facet.name == 'format_name'}">
							<div class="facet_name">Format Name<br/> </div>
						</c:if>
						<c:if test="${facet.name == 'provider_name'}">
							<div class="facet_name">Provider Name<br/> </div>
						</c:if>
						<c:forEach var="fcount" items="${facet.values}">
						      <c:url var="facetFilter" value="browseRecords.action">
								  <c:param name="query" value="${query}"/>
								  <c:param name="addFacetName" value="${facet.name}"/>
								  <c:param name="addFacetValue" value="${fcount.name}"/>
								  <c:param name="selectedFacetNames" value="${selectedFacetNames}"/>
								  <c:param name="selectedFacetValues" value="${selectedFacetValues}"/>
							  </c:url>

						<a href="${facetFilter}">${fcount.name} (${fcount.count})</a><br/>
						</c:forEach>
						<br/>
					 </c:forEach>
				    </div>
				</div>
-->
				
					<div class="search_box_div">
						<form name="browseRecordsForm" method="post" action="browseRecords.action">

							<input type="text" id="search_text" name="searchText" value="${searchText}" size="40"/>
							<button class="xc_button" type="submit" name="save" >Search</button>
						</form>	
					</div>
					<div class="search_results_div">
						<c:if test="${result.totalNumberOfResults > 0}">
						
								<c:if test="${result.totalNumberOfResults % numberOfResultsToShow == 0}">
									<c:set var="totalNumOfPages" value="${result.totalNumberOfResults / numberOfResultsToShow}"/>
								</c:if>
								<c:if test="${result.totalNumberOfResults % numberOfResultsToShow != 0}">
									<c:set var="totalNumOfPages" value="${result.totalNumberOfResults / numberOfResultsToShow + 1}"/>
								</c:if>
							Page <strong>${currentPageNumber}</strong> of <strong> ${fn:substringBefore(totalNumOfPages,".")} </strong> &nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; <strong>${result.totalNumberOfResults}</strong> results
									
							<div class="search_div_pager">
								<c:import url="browse_records_pager.jsp"/>
							</div>
						</c:if>	
						

						<c:forEach var="record" items="${result.records}" varStatus="rowCounter">
						<c:if test="${rowCounter.count % 2 != 0}">
							<div class="record_result_odd_div">
						</c:if>
						<c:if test="${rowCounter.count % 2 == 0}">
							<div class="record_result_even_div">
						</c:if>
							<div class="record_text">
								<c:url var="viewRecord" value="viewRecord.action">
									  <c:param name="recordId" value="${record.id}"/>
									  <c:param name="recordXML" value="${record.oaiXml}"/>
								  </c:url>
								<a href="${viewRecord}">${record.provider.name} ${record.id}</a>
								<br>
								Schema: ${record.format.name}
								<br>
								Provider: ${record.provider.name}
								<br>
								Harvest: 
								<c:if test="${record.harvest != null}">
									${record.harvest.harvestSchedule.scheduleName}
									<br>
								</c:if>
								
								
							</div>
						</div>

						</c:forEach>						   			



				    </div>
						    
						    
			</div>
			
 		</div>
		<!--  end body -->		
            
        </div>
        <!-- end doc -->
    </body>
</html>

    
