/**
 * Phonegap Web Intent plugin
 * Copyright (c) Boris Smus 2010
 *
 */
var WebIntent = function() { 

};

WebIntent.ACTION_SEND = "android.intent.action.SEND";
WebIntent.ACTION_VIEW= "android.intent.action.VIEW";
WebIntent.EXTRA_TEXT = "android.intent.extra.TEXT";
WebIntent.EXTRA_SUBJECT = "android.intent.extra.SUBJECT";

WebIntent.prototype.startActivity = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'startActivity', [params]);
};

WebIntent.prototype.hasExtra = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'hasExtra', [params]);
};

WebIntent.prototype.getIntentData = function(success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'getIntentData', []);
};

WebIntent.prototype.getExtra = function(params, success, fail) {
	return PhoneGap.exec(function(args) {
        success(args);
    }, function(args) {
        fail(args);
    }, 'WebIntent', 'getExtra', [params]);
};


WebIntent.prototype.onNewIntent = function(callback) {
	return PhoneGap.exec(function(args) {
		callback(args);
    }, function(args) {
    }, 'WebIntent', 'onNewIntent', []);
};

PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin('webintent', new WebIntent());
});
