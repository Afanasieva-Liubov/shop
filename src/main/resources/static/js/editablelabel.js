$('.holder').on('click', "label.editable", function() {
    var $lbl = $(this), o = $lbl.text(),
        $txt = $('<input type="text" class="editable" align="right" value='+o+' />');
    $lbl.replaceWith($txt);
    $txt.focus();

    $txt.blur(function() {
        var newdescription = $(this).val();
        if (newdescription==null || newdescription ==""){
            $lbl.text("Введите описание");
        }
        else {
            $lbl.text(newdescription);
        }
        $txt.replaceWith($lbl);
        change_description($lbl.parent(), newdescription);
    })
        .keydown(function(evt){
            if(evt.keyCode === 13) {
                var newdescription = $(this).val();
                if (newdescription==null || newdescription ==""){
                    $lbl.text("Введите описание");
                }
                else {
                    $lbl.text(newdescription);
                }
                $txt.replaceWith($lbl);
                change_description($lbl.parent(), newdescription);
            }
        });
});

function change_description(parentNode, newdescription) {
    var photo = {}
    photo["identifier"] = parentNode.attr('id');
    photo["description"] = newdescription;

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/photo/changedescription/",
        data: JSON.stringify(photo),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            var json = "<h4>Ajax Response</h4>&lt;pre&gt;"
                + JSON.stringify(data, null, 4) + "&lt;/pre&gt;";
            console.log("SUCCESS : ", data);
        },
        error: function (e) {

            var json = "<h4>Ajax Response</h4>&lt;pre&gt;"
                + e.responseText + "&lt;/pre&gt;";
            console.log("ERROR : ", e);
        }
    });
}