<%--
  * Copyright (c) 2009 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
--%>
/*  This creates a json object to return to the client
    This is for dealing with deleting a repository
*/
<jsp:directive.page contentType="application/javascript"/>

{
    "repositoryDeleted" : "${deleted}",
    "message" : "${message}"
}
