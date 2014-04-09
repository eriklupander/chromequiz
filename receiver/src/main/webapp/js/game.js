var game = new function() {
	var participants = [];
	var currentAnswers = [];
	var startTimeMs = -1;

    var currentQuestionIndex = 0;
    var questions = [];
    var currentCount = -1;
    var countdownInterval = null;
    var questionTimeout = null;

    this.startGame = function() {
        chromequiz.sendGameStarting();
        countdownInterval = null;
        currentAnswers = [];
        startTimeMs = -1;
        currentQuestionIndex = 0;
        questions = seedQuestions();
        startCountdown();
    }

    var startCountdown = function() {
        currentCount = 5;
        if(countdownInterval != null) {
            window.clearTimeout(countdownInterval);
        }
        tickCountdown();
    }

    var tickCountdown = function() {
        countdownInterval = window.setTimeout(function() {
            // UPDATE GUI
            $('#gamearea').empty();
            $('#gamearea').append('Next question in ' + currentCount-- + ' seconds');
            if(currentCount > 0) {
                tickCountdown();
            } else {
                game.displayQuestion(questions[currentQuestionIndex++]);
            }
        }, 1000);
    }
	
	this.addAnswer = function(participantName, answerLetter) {
		var ms = new Date().getTime();
		currentAnswers.push({"name":participantName,"answer":answerLetter, "time": ms-startTimeMs});

        if(currentAnswers.length == participants.length) {
            if(questionTimeout != null) {
                window.clearTimeout(questionTimeout);
            }
            game.displayAnswer(questions[currentQuestionIndex-1]);
        }
	}

    /**
     * Registers a sender ID as a new participant
     * @param castId
     */
	this.addParticipant = function(castId) {
		var participant= {"castId":castId, "name":"Unknown","score":0};
		participants.push(participant);
		game.updateParticipantList();

        if(participants.length == 1) {
            participants[0].isMaster = true;
            chromequiz.sendIsMasterEvent(castId);
        }
	}

    /**
     * Associates a human-readable name to a castId.
     * @param castId
     * @param name
     */
    this.addParticipantName = function(castId, name) {
        var index = -1;
        for(var a=0;a < participants.length;a++) {
            var existingParticipant = participants[a];
            if(existingParticipant.castId == castId) {
                existingParticipant.name = name;
                break;
            }
        }
    }
	
	this.removeParticipant = function(name) {
		var index = -1;
		for(var a=0;a < participants.length;a++) {
			var existingParticipant = participants[a];
			if(existingParticipant.name == name) {
				index = a;
				break;
			}
		}
		if(index > -1) {
			participants.splice(index, 1);
		}
	}
	
	this.updateParticipantList = function() {
		$('#participants').empty().append('<ul>');
		for(var a=0;a < participants.length;a++) {
			$('#participants').append('<li>' + participants[a].name + ' ' + participants[a].score + '</li>');
		}
		$('#participants').append('</ul>');
	}
	
	this.displayQuestion = function(question) {
		currentAnswers = [];
		startTimeMs = new Date().getTime();
		$('#gamearea').empty();
		$('#gamearea').append('<center>');
		$('#gamearea').append('<br/><h1>' + question.q + '</h1><br/>');
		for(var a=0;a < question.options.length;a++) {
			$('#gamearea').append('<h2>' + question.options[a].letter + ': ' + question.options[a].text + '</h2>');
		}
		$('#gamearea').append('</center>');

        chromequiz.sendQuestion(question);

        // Start the timer
        questionTimeout = setTimeout(function() {
            game.displayAnswer(questions[currentQuestionIndex-1]);
        }, 10000);
	}
	
	this.displayAnswer = function(question) {
		$('#gamearea').empty();
		$('#gamearea').append('<center>');
		$('#gamearea').append('<br/><h1>The correct answer is: ' + question.a + '</h1><br/>');
		var correctOptionIndex = (question.a == 'A' ? 0 : (question.a == 'B' ? 1 : 2));
		$('#gamearea').append('<br/><h2>'+question.options[correctOptionIndex] + '</h2><br/>');
		for(var a=0;a < participants.length;a++) {
			$('#gamearea').append(participants[a].name);
			var didAnswer = false;
			for(var b=0;b < currentAnswers.length;b++) {
				var answ = currentAnswers[b];
				if(answ.name == participants[a].name) {
					didAnswer = true;
					$('#gamearea').append(' answered: ' + answ.answer);
				}
			}
			if(!didAnswer) {
				$('#gamearea').append(' failed to answer');
			}
		}
		$('#gamearea').append('</center>');

        game.updateScoring(question.a);

        if(currentQuestionIndex == questions-1) {
            game.finishGame();
        } else {
            setTimeout(function() {
                startCountdown();
            }, 10000);
        }
	}

    this.finishGame = function() {
        chromequiz.sendGameEnded();
    }
	
	this.updateScoring = function(correctAnswerLetter) {
		for(var a=0;a < participants.length;a++) {
			
			for(var b=0;b < currentAnswers.length;b++) {
				var answ = currentAnswers[b];
				if(answ.name == participants[a].name) {
					if(correctAnswerLetter == answ.answer) {
						participants[a].score = participants[a].score + 1;
					}
				}
			}
			if(!didAnswer) {
				$('#gamearea').append(' failed to answer');
			}
		}
	}

    var questions = [
        {"q":"What's the name of the world's longest river?","a":"C","options":
            [
                {"letter":"A","answer":"The Nile"},
                {"letter":"B","answer":"The Mississippi"},
                {"letter":"C","answer":"The Amazon"}
            ]
        },
        {"q":"What's the name of the capital of Mongolia?","a":"A","options":
            [
                {"letter":"A","answer":"Ulan Bator"},
                {"letter":"B","answer":"Tbilisi"},
                {"letter":"C","answer":"Astana"}
            ]
        },
        {"q":"Some question 1?","a":"B","options":
            [
                {"letter":"A","answer":"The A1"},
                {"letter":"B","answer":"The B1"},
                {"letter":"C","answer":"The C1"}
            ]
        },
        {"q":"Some question 2","a":"C","options":
            [
                {"letter":"A","answer":"The A2"},
                {"letter":"B","answer":"The B2"},
                {"letter":"C","answer":"The C2"}
            ]
        },
        {"q":"What's the name of that large iron thing in Paris","a":"C","options":
            [
                {"letter":"A","answer":"The Golden Gate bridge"},
                {"letter":"B","answer":"The Louvre"},
                {"letter":"C","answer":"The Eiffel Tower"}
            ]
        }
    ];

    var seedQuestions = function() {
        return shuffleArray(questions);
    }

    var shuffleArray = function(array) {
        for (var i = array.length - 1; i > 0; i--) {
            var j = Math.floor(Math.random() * (i + 1));
            var temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return array;
    }
}