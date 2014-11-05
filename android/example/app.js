var smsRev = require('ti.miga.sms.receive');
smsRev.init();

Ti.App.addEventListener("received",function(e){
    alert(e.number + " " + e.message);
});
