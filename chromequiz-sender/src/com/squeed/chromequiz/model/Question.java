package com.squeed.chromequiz.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
	
	private String question;
	private String answerLetter;
	private List<Option> options = new ArrayList<Option>();
	
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswerLetter() {
		return answerLetter;
	}
	public void setAnswerLetter(String answerLetter) {
		this.answerLetter = answerLetter;
	}
	public List<Option> getOptions() {
		return options;
	}
	public void setOptions(List<Option> options) {
		this.options = options;
	}
	public Option getOption(String letter) {
		if("A".equals(letter)) {
			return options.get(0);
		}
		if("B".equals(letter)) {
			return options.get(1);		
				}
		if("C".equals(letter)) {
			return options.get(2);
		}
		throw new RuntimeException("Unknown answer letter: " + letter);
	}
	
	
}
