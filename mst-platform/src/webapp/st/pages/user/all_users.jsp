<!--
  * Copyright (c) 2009 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  -->

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="mst" uri="mst-tags"%>

<!--  document type -->
<c:import url="/st/inc/doctype-frag.jsp"/>

<LINK href="page-resources/css/header.css" rel="stylesheet" type="text/css">


<html>
    <head>
        <title>All Users</title>
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
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/all_users.js"></SCRIPT>

    </head>

    <body class="yui-skin-sam">
        <%@ taglib prefix="s" uri="/struts-tags" %>
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">

    <!-- page header - this uses the yahoo page styling -->
    <div id="hd">

            <!--  this is the header of the page -->
            <c:import url="/st/inc/header.jsp"/>

            <!--  this is the header of the page -->
            <c:import url="/st/inc/menu.jsp"/>
            <jsp:include page="/st/inc/breadcrumb.jsp">

                    <jsp:param name="bread" value="Users & Groups | All Users" />

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

                <div class="clear">&nbsp;</div>

             <div class="viewTable">
                    <table width="100%">
                        <c:if test="${not empty userList}">
                            <thead>
                                <tr>
                                    <td>
                                            <c:if test="${columnSorted!='UserName'}">
                                                 <c:url var="userSortUrl" value="allUsers.action">
                                                   <c:param name="isAscendingOrder" value="true"/>
                                                   <c:param name="columnSorted" value="UserName"/>
                                                 </c:url>
                                                  <a href="${userSortUrl}">User Name</a>
                                             </c:if>

                                             <c:if test="${columnSorted=='UserName'}">
                                               <c:url var="userSortUrl" value="allUsers.action">
                                                 <c:param name="isAscendingOrder" value="${!isAscendingOrder}"/>
                                                 <c:param name="columnSorted" value="UserName"/>
                                               </c:url>

                                               <a href="${userSortUrl}">User Name</a>

                                                <c:choose>
                                                    <c:when test="${isAscendingOrder==true}">
                                                        &nbsp;<img src="page-resources/img/triangle_sort.jpg">

                                                    </c:when>
                                                    <c:otherwise>
                                                        &nbsp;<img src="page-resources/img/triangle_sort_down.jpg">
                                                    </c:otherwise>
                                                </c:choose>
                                             </c:if>
                                    </td>
                                    <td>Account type</td>
                                    <td>
                                            <c:if test="${columnSorted!='LastLogin'}">
                                                 <c:url var="userSortUrl" value="allUsers.action">
                                                   <c:param name="isAscendingOrder" value="true"/>
                                                   <c:param name="columnSorted" value="LastLogin"/>
                                                 </c:url>
                                                  <a href="${userSortUrl}">Last Login</a>
                                             </c:if>

                                             <c:if test="${columnSorted=='LastLogin'}">
                                               <c:url var="userSortUrl" value="allUsers.action">
                                                 <c:param name="isAscendingOrder" value="${!isAscendingOrder}"/>
                                                 <c:param name="columnSorted" value="LastLogin"/>
                                               </c:url>

                                               <a href="${userSortUrl}">Last Login</a>

                                                <c:choose>
                                                    <c:when test="${isAscendingOrder==true}">
                                                        &nbsp;<img src="page-resources/img/triangle_sort.jpg">

                                                    </c:when>
                                                    <c:otherwise>
                                                        &nbsp;<img src="page-resources/img/triangle_sort_down.jpg">
                                                    </c:otherwise>
                                                </c:choose>
                                             </c:if>
                                    </td>
                                    <td>
                                            <c:if test="${columnSorted!='FirstName'}">
                                                 <c:url var="userSortUrl" value="allUsers.action">
                                                   <c:param name="isAscendingOrder" value="true"/>
                                                   <c:param name="columnSorted" value="FirstName"/>
                                                 </c:url>
                                                  <a href="${userSortUrl}">First Name</a>
                                             </c:if>

                                             <c:if test="${columnSorted=='FirstName'}">
                                               <c:url var="userSortUrl" value="allUsers.action">
                                                 <c:param name="isAscendingOrder" value="${!isAscendingOrder}"/>
                                                 <c:param name="columnSorted" value="FirstName"/>
                                               </c:url>

                                               <a href="${userSortUrl}">First Name</a>

                                                <c:choose>
                                                    <c:when test="${isAscendingOrder==true}">
                                                        &nbsp;<img src="page-resources/img/triangle_sort.jpg">

                                                    </c:when>
                                                    <c:otherwise>
                                                        &nbsp;<img src="page-resources/img/triangle_sort_down.jpg">
                                                    </c:otherwise>
                                                </c:choose>
                                             </c:if>
                                    </td>
                                    <td>
                                            <c:if test="${columnSorted!='LastName'}">
                                                 <c:url var="userSortUrl" value="allUsers.action">
                                                   <c:param name="isAscendingOrder" value="true"/>
                                                   <c:param name="columnSorted" value="LastName"/>
                                                 </c:url>
                                                  <a href="${userSortUrl}">Last Name</a>
                                             </c:if>

                                             <c:if test="${columnSorted=='LastName'}">
                                               <c:url var="userSortUrl" value="allUsers.action">
                                                 <c:param name="isAscendingOrder" value="${!isAscendingOrder}"/>
                                                 <c:param name="columnSorted" value="LastName"/>
                                               </c:url>

                                               <a href="${userSortUrl}">Last Name</a>

                                                <c:choose>
                                                    <c:when test="${isAscendingOrder==true}">
                                                        &nbsp;<img src="page-resources/img/triangle_sort.jpg">

                                                    </c:when>
                                                    <c:otherwise>
                                                        &nbsp;<img src="page-resources/img/triangle_sort_down.jpg">
                                                    </c:otherwise>
                                                </c:choose>
                                             </c:if>
                                    </td>
                                    <td>Delete</td>
                                </tr>
                           </thead>
                        </c:if>

                        <tbody>
                             <c:choose>
                                 <c:when test="${empty userList}">
                                     <div class="emptytablebar">
                                         <div class="emptytable_innerdiv"> Choose <b>List Users</b> <img class="emptytable_img" src="page-resources/img/bullet_go.gif"/> <b>Add Local User</b> to add a local user </div>
                                     </div>
                                </c:when>
                                <c:otherwise>
                                        <c:forEach var="user" items="${userList}" varStatus="userCount">

                                             <tr>
                                                 <c:choose>
                                                     <c:when test="${user.server.name=='Local'}">
                                                         <c:set var="url" value="viewEditLocalUser.action?userId=${user.id}"/>
                                                     </c:when>
                                                     <c:otherwise>
                                                          <c:set var="url" value="viewEditLDAPUser.action?userId=${user.id}"/>
                                                     </c:otherwise>
                                                </c:choose>

                                                            <c:set var="classColumn" value="plainColumn"/>
                                                            <c:if test="${columnSorted=='UserName'}">
                                                                <c:set var="classColumn" value="sortColumn"/>
                                                            </c:if>
                                                <td class="${classColumn}"><a href="${url}">${user.username}</a></td>
                                                <td>
                                                    ${user.server.type}
                                                </td>
                                                            <c:set var="classColumn" value="plainColumn"/>
                                                            <c:if test="${columnSorted=='LastLogin'}">
                                                                <c:set var="classColumn" value="sortColumn"/>
                                                            </c:if>
                                                <td class="${classColumn}">${user.lastLogin}</td>
                                                            <c:set var="classColumn" value="plainColumn"/>
                                                            <c:if test="${columnSorted=='FirstName'}">
                                                                <c:set var="classColumn" value="sortColumn"/>
                                                            </c:if>
                                                <td class="${classColumn}">${user.firstName}</td>
                                                            <c:set var="classColumn" value="plainColumn"/>
                                                            <c:if test="${columnSorted=='LastName'}">
                                                                <c:set var="classColumn" value="sortColumn"/>
                                                            </c:if>
                                                <td class="${classColumn}">${user.lastName}</td>
                                                <td>
                                                    <c:if test="${user.username == 'admin'}">
                                                        <button class="xc_button_disabled" disabled="true" type="submit" name="deleteUser" onclick="javascript:YAHOO.xc.mst.user.removeUser.deleteUser(${user.id});">Delete</button>
                                                    </c:if>
                                                    <c:if test="${user.username != 'admin'}">
                                                             <button class="xc_button" type="button" name="deleteUser" onclick="javascript:YAHOO.xc.mst.user.removeUser.deleteUser(${user.id});">Delete</button>
                                                    </c:if>

                                                </td>
                                             </tr>
                                        </c:forEach>
                                </c:otherwise>
                             </c:choose>

                        </tbody>
                    </table>
      </div>
             <form name="deleteUser" method="post" action= "deleteUser.action">
                <input type="hidden" id="userId" name="userId"/>
            </form>
        </div>
            <!--  this is the footer of the page -->
            <c:import url="/st/inc/footer.jsp"/>
   </div>
</body>
</html>
