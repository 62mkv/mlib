function fixdesc( )
{
    var i = 0;
    var ii = document.all.tags("SPAN").length
                
    for (i = 0; i < ii; i++)
    {
        if (document.all.tags("SPAN").item(i).className == "my-description")
        {
            if (document.all.tags("SPAN").item(i).innerHTML == "")
            {  
                document.all.tags("SPAN").item(i).style.height = 1;
                document.all.tags("SPAN").item(i).style.overflow = "hidden";
            }

            if (document.all.tags("SPAN").item(i).childNodes(0).offsetHeight 
                <
                document.all.tags("SPAN").item(i).offsetHeight)
            {
                document.all.tags("SPAN").item(i).style.height = 
                document.all.tags("SPAN").item(i).childNodes(0).offsetHeight;
                document.all.tags("SPAN").item(i).style.overflow = "hidden";
            }
        }
    }
}
