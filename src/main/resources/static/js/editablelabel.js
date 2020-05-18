$('.holder').on('click', "label.editable", function () {
    var $lbl = $(this),
        oldDescription = $lbl.text().trim(),
        $txt = $('<input type="text" class="editable" align="right" value=' + oldDescription + ' />');
    $lbl.replaceWith($txt);
    $txt.focus();

    $txt.blur(function () {
        setNewDescription(oldDescription, $(this).val().trim(), $lbl, $txt);
    })
        .keydown(function (evt) {
            if (evt.keyCode === 13) {
                setNewDescription(oldDescription, $(this).val().trim(), $lbl, $txt);
            }
        });
});

function setNewDescription(oldDescription, newDescription, labelElement, textElement) {
    if (newDescription == null || newDescription == "") {
        labelElement.text("Введите описание");
    } else {
        labelElement.text(newDescription);
    }
    textElement.replaceWith(labelElement);
    change_description(labelElement.parent(), oldDescription, newDescription);
}

function change_description(parentNode, oldDescription, newDescription) {

    if (newDescription != oldDescription && newDescription != "Введите описание") {
        var photo = {}
        photo["identifier"] = parentNode.attr('id');
        photo["description"] = newDescription;
        var errorNode = parentNode[0].querySelector('.operationStatusClass');

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "/photo/changedescription/",
            data: JSON.stringify(photo),
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (data) {
                console.log("SUCCESS : ", data);
                errorNode.innerHTML = "Description is changed!";
            },
            error: function (e) {
                console.log("ERROR : ", e);
                errorNode.innerHTML = "Error in changing description!";
            }
        });
    }
}

