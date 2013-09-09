$(function() {
    // add a click handler to the button
    $("#getMessageButton").click(function(event) {
        // make an ajax get request to get the message
        jsRoutes.controllers.MessageController.getMessage().ajax({
            success: function(data) {
                console.log(data)
                $(".well").append($("<h1>").text(data.value))
            }
        })
    });

    $("#validateButton").click(function(event) {
            console.log($("#input_text"))
            // make an ajax get request to get the message
            mjsRoutes.controllers.MainController.process().ajax({
                type: "POST",
                data: { text: $("#input_text").val() },
                success: function(data) {
                    console.log(data)
                    $(".well").append($("<h1>").text(data.text))
                }
            })
    });
})