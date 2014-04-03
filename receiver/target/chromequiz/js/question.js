var question = new function() {
    this.q = '';
    this.a = '';
	this.options = [{}];

    this.parse = function(jsonStr) {
        return JSON.parse(jsonStr);
    }
};