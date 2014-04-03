var chromequiz = new function() {

    var senderId;

	cast.receiver.logger.setLevelValue(0);
	window.castReceiverManager = cast.receiver.CastReceiverManager.getInstance();
	console.log('Starting Receiver Manager');

	// handler for the 'ready' event
	castReceiverManager.onReady = function (event) {
		console.log('Received Ready event: ' + JSON.stringify(event.data));
		window.castReceiverManager.setApplicationState("Application status is ready...");
	};

	// handler for 'senderconnected' event
	castReceiverManager.onSenderConnected = function (event) {
		console.log('Received Sender Connected event: ' + event.data);
		console.log(window.castReceiverManager.getSender(event.data).userAgent);
	};

	// handler for 'senderdisconnected' event
	castReceiverManager.onSenderDisconnected = function (event) {
		console.log('Received Sender Disconnected event: ' + event.data);
		if (window.castReceiverManager.getSenders().length == 0) {
			window.close();
		}
	};

	// handler for 'systemvolumechanged' event
	castReceiverManager.onSystemVolumeChanged = function (event) {
		console.log('Received System Volume Changed event: ' + event.data['level'] + ' ' +
				event.data['muted']);
	};

	// create a CastMessageBus to handle messages for a custom namespace
	window.messageBus =
			window.castReceiverManager.getCastMessageBus(
					'urn:x-cast:com.squeed.chromequiz');

	// handler for the CastMessageBus message event
	window.messageBus.onMessage = function (event) {

        senderId = event.senderId;
		console.log('Message [' + event.senderId + ']: ' + event.data);

        var command = cmd.parse(event.data);
        switch(command.id) {
            case "ANSWER":
                game.addAnswer(senderId, command.prm.answer);
                break;
            case "START_GAME":
                game.startGame();
                break;
            
            default:
                chromequiz.displayText("Unknown or unparsable command: " + event.data);
                break;
        }

        // Echo everything back to all attached senders
		//window.messageBus.send(event.senderId, event.data);
	}

	// initialize the CastReceiverManager with an application status message
	window.castReceiverManager.start({statusText: "Application is starting"});
	console.log('Receiver Manager started');
   

    // utility function to display the text message in the input field
    this.displayText = function(text) {
        console.log(text);
        $('#message').clearQueue();
        $('#message').html(text);
		$('#message').css('opacity','1.0');
		$('#message').animate({'opacity':0.0}, 4000);
		
        window.castReceiverManager.setApplicationState(text);
    };

    this.sendEvent = function(evt) {
        window.messageBus.send(senderId, evt);
    };

    this.sendQuestion = function(question) {
        var rsp = {
            "type":"event",
            "eventId":"EVENT_QUESTION",
            "question": question
        };
        chromequiz.sendEvent(JSON.stringify(rsp));
    };
	
	this.sendGameStarting = function() {
        var rsp = {
            "type":"event",
            "eventId":"EVENT_GAME_START"
        };
        chromequiz.sendEvent(JSON.stringify(rsp));
    };
	
	this.sendGameEnded = function() {
        var rsp = {
            "type":"event",
            "eventId":"EVENT_GAME_END"
        };
        chromequiz.sendEvent(JSON.stringify(rsp));
    };
};