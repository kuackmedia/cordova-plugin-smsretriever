function SMSRetrieverPlugin() {};

SMSRetrieverPlugin.prototype.getMsisdn = function(success, error) {
    cordova.exec(success, error, 'SMSRetriever', 'getMsisdn', []);
};

SMSRetrieverPlugin.prototype.startListener = function(success, error) {
    cordova.exec(success, error, 'SMSRetriever', 'startListener', []);
};

SMSRetrieverPlugin.prototype.stopListener = function(success, error) {
    cordova.exec(success, error, 'SMSRetriever', 'stopListener', []);
};

module.exports = new SMSRetrieverPlugin();
