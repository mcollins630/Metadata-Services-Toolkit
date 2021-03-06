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
        <title>Email Server Configuration</title>
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
        <SCRIPT LANGUAGE="JavaScript" SRC="page-resources/js/email_configuration.js"></SCRIPT>

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

                    <jsp:param name="bread" value="Configuration | Email Server" />

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
                        <c:if test="${errorType == 'error'}">
                          <span class="errorText">
                                <mst:fielderror error="${fieldErrors}">
                                </mst:fielderror>
                            </span>
                         </c:if>
                        <c:if test="${errorType == 'info'}">
                          <div class="jsErrorMessage"> ${message}</div>
                        </c:if>
                     </div>
                     </div>
                 </c:if>
                 <div id="error_div"></div>

                <form action="changeEmailConfig.action" method="post" name="emailConfig">


                    <table style="margin-left:10px">

                        <tr>
                            <td><B>Outgoing Mail Server(SMTP):  </B>&nbsp;&nbsp;<br>
                            <input type="text" id="emailServerAddress" name ="emailServerAddress" class="textfield" value="${emailConfig.emailServerAddress}" maxlength="255" size="45"><br><br></td>
                        </tr>

                       <tr>
                           <td><B>From Address </B>&nbsp;&nbsp;<br>
                           <input type="text" id="fromAddress" name ="fromAddress" class="textfield" value="${emailConfig.fromAddress}" maxlength="255" size="45"><br><br></td>
                       </tr>

                       <tr>
                           <td><B>Password </B>&nbsp;&nbsp;<br>
                           <input type="password" id="password" name ="password" class="textfield" value="${emailConfig.password}" maxlength="100" size="45"><br><br></td>
                       </tr>

                       <tr>
                           <td><B> Port Number </B>&nbsp;&nbsp;<br>
                           <input type="text" id="port" name ="port" class="textfield" value="${emailConfig.portNumber}" maxlength="11" size="45"><br><br></td>
                       </tr>

                       <tr>
                           <td> <B>Encrypted Connection </B>&nbsp;&nbsp;

                               <br>
                               <SELECT style="width:290px; height:20px;" ID="encryptedConnection" name="encryptedConnection" >
                                  <OPTION  value="none">None</OPTION>

                                  <OPTION value="ssl"
                                  <c:if test="${emailConfig.encryptedConnection == 'ssl'}">
                                    SELECTED
                                  </c:if>
                                  >SSL</OPTION>

                                  <OPTION value="tls"
                                  <c:if test="${emailConfig.encryptedConnection == 'tls'}">
                                    selected
                                  </c:if>
                                  >TLS</OPTION>

                                  <OPTION value="auto"
                                  <c:if test="${emailConfig.encryptedConnection == 'auto'}">
                                    selected
                                  </c:if>
                                  >AUTO</OPTION>
                                </SELECT>
                                <br><br>
                           </td>
                       </tr>

                        <tr>
                            <td> <B>Timeout </B>&nbsp;&nbsp; <br>
                            <input type="text" id="timeout" name ="timeout" class="textfield" value="${emailConfig.timeout}" maxlength="11" size="45"><br><br></td>
                        </tr>



                    <br><br><br>
    <tr><td>
                    <div align="left">
                        <!-- <button class="xc_button" type="button" onclick="javascript:YAHOO.xc.mst.configuration.cancel();" name="cancel">Cancel</button> &nbsp;&nbsp;&nbsp; -->
                        <button style="width:200px;" class="xc_button" type="button" onclick="javascript:YAHOO.xc.mst.configuration.email.changeEmailConfig();" name="changeEmailConfig">Update Email Configuration</button>
                    </div>
                 </td></tr>
                    </table>

                </form>
            </div>
            <!--  this is the footer of the page -->
            <c:import url="/st/inc/footer.jsp"/>
        </div>

</body>
</html>
