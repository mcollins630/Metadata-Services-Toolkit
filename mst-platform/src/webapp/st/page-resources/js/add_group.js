 /*
  * Copyright (c) 2009 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

YAHOO.namespace("xc.mst.group.newGroup");

YAHOO.xc.mst.group.newGroup = {
    addGroup : function()
    {
        try
        {

            var groupName = document.getElementById("groupName").value;
            var groupDescription = document.getElementById("groupDescription").value;


            var permissionsSelected = document.getElementById("permissionsSelected");


            if((groupName=='')||(groupDescription==''))
                {
                    if(groupName=='')
                        {
                            createErrorDiv("error","Group Name is a required field");
                        }
                    else
                        {
                            createErrorDiv("error","Group Description is a required field");
                        }
                }

            else
             {
                 var flag = 0;
                 for(i=0;i<permissionsSelected.options.length;i++)
                     {

                         if(permissionsSelected.options[i].selected==true)
                             {

                                 flag=1;
                             }
                     }
                 if(flag==1)
                     {

                             document.addGroup.submit();

                     }
                  else
                      {
                          createErrorDiv("error",'Group must include at least one permission.  Select one or more permission that members of this group will receive.');
                      }
             }

        }
        catch(err)
        {
            alert(err);
        }
    },
    cancel : function()
    {
        document.addGroup.action = "allGroups.action";
        document.addGroup.submit();
    }
}
