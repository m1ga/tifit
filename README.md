Titanium Android module for Android Fit (work in progress!)

```
var tifit = require("ti.miga.tifit");
tifit.init();
tifit.start();
Ti.App.addEventListener("received",onReceived);

function onReceived(e){
	console.log("hier");
	var l = $.UI.create("Label",{
		text:  e.type + ": " + e.value
	});
	$.index.add(l);
}


$.index.open();
```

Converted one of the google samples to be used in Titanium.
