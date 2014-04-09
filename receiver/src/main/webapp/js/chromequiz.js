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
	    chromequiz.displayText('A new Player has connected!');
        game.addParticipant(event.data);
        chromequiz.sendNameRequest(event.data);
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
            case "EVENT_ANSWER":
                game.addAnswer(senderId, command.prm.answer);
                break;
            case "EVENT_START_GAME":
                game.startGame();
                break;
            case "EVENT_NAME_REQUEST_RESPONSE":
                game.addParticipantName(command.prm.castId, command.prm.name);
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

    this.sendQuestionTimedOut = function() {
        var evt = {
            "type":"event",
            "eventId":"EVENT_QUESTION_TIMEOUT"
        };
        chromequiz.sendEvent(JSON.stringify(evt));
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

    this.sendNameRequest = function(castId) {
        var cmd = {
            "type":"command",
            "commandId":"COMMAND_NAME_REQUEST",
            "castId" : castId
        };
        chromequiz.sendEvent(JSON.stringify(cmd));
    }

    this.sendIsMasterEvent = function(castId) {
        var cmd = {
            "type":"command",
            "commandId":"COMMAND_SET_MASTER",
            "castId" : castId
        };
        chromequiz.sendEvent(JSON.stringify(cmd));
    }
};