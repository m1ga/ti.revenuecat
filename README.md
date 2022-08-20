# Ti.revenuecat - RevenueCat module for Titanium

untested/unfinshed Android version.

```js
const win = Ti.UI.createWindow({layout:"vertical"});
const btn1 = Ti.UI.createButton({title:"offerings"});
const btn2 = Ti.UI.createButton({title:"products"});
const revenuecat = require("ti.revenuecat");
revenuecat.init({
	apiKey: "...",
	debug: true
})

btn1.addEventListener("click", function(e){
	revenuecat.getOfferings();
});
btn2.addEventListener("click", function(e){
});
revenuecat.addEventListener("offerings", function(e){
	console.log(e.items);
});
revenuecat.addEventListener("error", function(e){
	console.log(e.error);
});

win.add([btn1]);
win.open();
```
